package com.gigaspaces.demo.purgemanager;

import com.gigaspaces.demo.purgemanager.strategy.FifoPurgeStrategy;
import com.gigaspaces.demo.purgemanager.strategy.LowestPropertyPurgeStrategy;
import com.gigaspaces.demo.purgemanager.strategy.RandomPurgeStrategy;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.PostPrimary;

import javax.annotation.PreDestroy;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PurgeManager {

    private static final Logger logger = Logger.getLogger(PurgeManager.class.getName());

    private long pollingInterval;
    private int purgeBatchSize;
    private final Map<String, ClassPurgeConfig> purgeConfig = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService;

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    @PostPrimary
    public void postPrimary(AfterSpaceModeChangeEvent afterSpaceModeChangeEvent) {
        initialize();
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleWithFixedDelay(this::monitorEntries, pollingInterval, pollingInterval, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        if (scheduledExecutorService != null)
            scheduledExecutorService.shutdownNow();
    }

    private void initialize() {
        // For demo purposes initialization is hard-coded, but it can
        // read config from external resource (e.g. properties file, pu.xml)
        pollingInterval = 10;
        purgeBatchSize = 5;
        purgeConfig.put("com.gigaspaces.demo.Data", new ClassPurgeConfig(1000, 500,
                new RandomPurgeStrategy("com.gigaspaces.demo.Data")));
        purgeConfig.put("com.gigaspaces.demo.DataWithFifo", new ClassPurgeConfig(1000, 500,
                new FifoPurgeStrategy("com.gigaspaces.demo.DataWithFifo")));
        purgeConfig.put("com.gigaspaces.demo.DataWithSequence", new ClassPurgeConfig(1000, 500,
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
