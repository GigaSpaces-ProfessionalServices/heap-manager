package com.gigaspaces.heapmanager;


import com.gigaspaces.common.*;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.core.space.mode.AfterSpaceModeChangeEvent;
import org.openspaces.core.space.mode.PostBackup;
import org.openspaces.core.space.mode.PostPrimary;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class EvictionManager  {

    private static final Logger logger = Logger.getLogger(EvictionManager.class.getName());

    @GigaSpaceContext(name = "gigaSpace")
    private GigaSpace gigaSpace;

    public GigaSpace getGigaSpace() { return this.gigaSpace; }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /*
        See: https://docs.gigaspaces.com/xap/12.2/dev-java/the-space-notifications.html
     */
    @PostPrimary
    public void postPrimary(AfterSpaceModeChangeEvent afterSpaceModeChangeEvent) {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        ScheduledFuture scheduledFuture = scheduledExecutorService.scheduleAtFixedRate (new MyRunnable(), 10,10, TimeUnit.SECONDS);
    }

    @PostBackup
    public void postBackup(AfterSpaceModeChangeEvent afterSpaceModeChangeEvent) {

    }
    /**
     * Returns a count of the objects in the provided space.
     *
     * @param space
     *            Space proxy
     * @param objectName
     *            Class name to count instances
     * @return Count of the instances
     * @throws java.rmi.RemoteException
     *             Errors connecting to space
     */
    public static int getEntryCount(GigaSpace space, String objectName) {

        IRemoteJSpaceAdmin spaceAdmin;
        try {
            spaceAdmin = (IRemoteJSpaceAdmin) space.getSpace().getAdmin();
            SpaceRuntimeInfo rtInfo = spaceAdmin.getRuntimeInfo();

            logger.info("Object types found in Space = " + rtInfo.m_ClassNames.size());

            for (int i = 0; (i < rtInfo.m_ClassNames.size()); i++) {

                if (logger.isLoggable(Level.FINE))
                    logger.fine(rtInfo.m_ClassNames.get(i));

                if (rtInfo.m_ClassNames.get(i).equals(objectName)) {
                    if (logger.isLoggable(Level.FINEST))
                        logger.finest(rtInfo.m_NumOFEntries.get(i).toString());
                    return rtInfo.m_NumOFEntries.get(i);
                }
            }

        } catch (RemoteException e) {
            logger.severe(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    class MyRunnable implements Runnable {

        @Override
        public void run() {
            try {
                logger.info("MyCallable.call() started");

                int objectCount = getEntryCount(gigaSpace, "com.gigaspaces.common.Data");
                logger.info("objectCount for com.gigaspaces.common.Data" + objectCount);
                if (objectCount > 100) {
                    logger.info("about to take");
                    Data template = new Data();
                    Data retValue = gigaSpace.take(template);
                    logger.info("sequenceNumber taken: " + retValue.getSequenceNumber());
                }
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
    }
}
