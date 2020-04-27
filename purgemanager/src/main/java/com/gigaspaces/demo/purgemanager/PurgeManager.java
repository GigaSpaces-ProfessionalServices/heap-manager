package com.gigaspaces.demo.purgemanager;

import com.gigaspaces.demo.purgemanager.strategy.FifoPurgeStrategy;
import com.gigaspaces.demo.purgemanager.strategy.LowestPropertyPurgeStrategy;
import com.gigaspaces.demo.purgemanager.strategy.PurgeStrategy;
import com.gigaspaces.demo.purgemanager.strategy.RandomPurgeStrategy;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.PostPrimary;

import javax.annotation.PreDestroy;
import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
  This class will poll repeatedly to find objects available to be purged.
  An internal map is used to associate class names with a ClassPurgeConfig, which contains the purge strategy.
 */
public class PurgeManager {

    private static final Logger logger = Logger.getLogger(PurgeManager.class.getName());

    private long initialDelay;
    private long pollingInterval;

    // key: class name, value: classPurgeConfig instance
    private final Map<String, ClassPurgeConfig> purgeConfig = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService;

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public void setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    @PostPrimary
    public void postPrimary(AfterSpaceModeChangeEvent afterSpaceModeChangeEvent) {
        //initialize();

        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(this::monitorEntries, initialDelay, pollingInterval, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        if (scheduledExecutorService != null)
            scheduledExecutorService.shutdownNow();
    }

    public PurgeManager(List<String> initValues) {
        try {
            for (String s : initValues) {
                String[] values = s.split(",");
                String className = values[0];
                int purgeThreshold = Integer.valueOf(values[1]);
                int lowThreshold = Integer.valueOf(values[2]);
                int purgeBatchSize = Integer.valueOf(values[3]);
                String strategyClassName = values[4];

                Constructor c;
                PurgeStrategy purgeStrategy;

                if (!"com.gigaspaces.demo.purgemanager.strategy.LowestPropertyPurgeStrategy".equals(strategyClassName)) {
                    c = Class.forName(strategyClassName).getConstructor(String.class);
                    purgeStrategy = (PurgeStrategy) c.newInstance(className);
                } else {
                    c = Class.forName(strategyClassName).getConstructor(String.class, String.class);
                    purgeStrategy = (PurgeStrategy) c.newInstance(className, values[5]);
                }

                purgeConfig.put(className, new ClassPurgeConfig(purgeThreshold, lowThreshold, purgeBatchSize, purgeStrategy));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error initializing purgeManager", e);
        }
    }
    private void initialize() {
        // For demo purposes initialization is hard-coded, but it can be coded to
        // read config from external resource (e.g. properties file, pu.xml)
        pollingInterval = 10;
        purgeConfig.put("com.gigaspaces.demo.Data", new ClassPurgeConfig(1000, 500, 5,
                new RandomPurgeStrategy("com.gigaspaces.demo.Data")));
        purgeConfig.put("com.gigaspaces.demo.DataWithFifo", new ClassPurgeConfig(1000, 500, 5,
                new FifoPurgeStrategy("com.gigaspaces.demo.DataWithFifo")));
        purgeConfig.put("com.gigaspaces.demo.DataWithSequence", new ClassPurgeConfig(1000, 500, 5,
                new LowestPropertyPurgeStrategy("com.gigaspaces.demo.DataWithSequence", "seqId")));
    }

    private void monitorEntries() {
        logger.info("monitorEntries() started");

        try {
            SpaceRuntimeInfo runtimeInfo = getRuntimeInfo();
            for (int i = 0; (i < runtimeInfo.m_ClassNames.size()); i++)
                purge(runtimeInfo.m_ClassNames.get(i), runtimeInfo.m_NumOFEntries.get(i));
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Unexpected exception during monitorEntries", e);
        }
    }

    private SpaceRuntimeInfo getRuntimeInfo() {
        try {
            return ((IRemoteJSpaceAdmin) gigaSpace.getSpace().getAdmin()).getRuntimeInfo();
        } catch (RemoteException e) {
            throw new IllegalStateException("RemoteException within collocated space", e);
        }
    }

    private void purge(String className, int numOfEntries) {
        if (numOfEntries == 0)
            return;
        ClassPurgeConfig config = purgeConfig.get(className);
        if (config == null) {
            logger.info("There are " + numOfEntries + " entries of type '" + className + "' but no purge is configured for it");
            return;
        }

        if (numOfEntries < config.getHighThreshold()) {
            logger.info("Skipping purge for class '" + className + "': " + numOfEntries + " below " + config.getHighThreshold());
            return;
        }

        int remaining = numOfEntries - config.getLowThreshold();
        int purgeBatchSize = config.getPurgeBatchSize();
        int totalPurged = 0;
        logger.info("Starting to purge " + remaining + " entries of type '" + className + "'");
        while (remaining > 0) {

            int batch = Math.min(purgeBatchSize, remaining);
            int purged = config.getPurgeStrategy().purge(gigaSpace, batch);
            remaining -= purged;
            totalPurged += purged;
            if (purged != batch)
                break;
        }
        logger.info("Purged entries: "+ totalPurged);
    }
}
