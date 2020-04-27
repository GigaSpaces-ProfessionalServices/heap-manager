package com.gigaspaces.demo.purgemanager;

import com.gigaspaces.demo.purgemanager.strategy.PurgeStrategy;

/*
   Stores an association of a purge strategy and the thresholds for purging.
 */
public class ClassPurgeConfig {
    private final int highThreshold;
    private final int lowThreshold;
    private final int purgeBatchSize;
    private final PurgeStrategy purgeStrategy;

    public ClassPurgeConfig(int purgeThreshold, int lowThreshold, int purgeBatchSize, PurgeStrategy purgeStrategy) {
        this.highThreshold = purgeThreshold;
        this.lowThreshold = lowThreshold;
        this.purgeBatchSize = purgeBatchSize;
        this.purgeStrategy = purgeStrategy;
    }

    public int getHighThreshold() {
        return highThreshold;
    }

    public int getLowThreshold() {
        return lowThreshold;
    }

    public int getPurgeBatchSize() { return purgeBatchSize; }

    public PurgeStrategy getPurgeStrategy() {
        return purgeStrategy;
    }
}
