package com.gigaspaces.demo.purgemanager;

import com.gigaspaces.demo.purgemanager.strategy.PurgeStrategy;

public class ClassPurgeConfig {
    private final int highThreshold;
    private final int lowThreshold;
    private final PurgeStrategy purgeStrategy;

    public ClassPurgeConfig(int purgeThreshold, int lowThreshold, PurgeStrategy purgeStrategy) {
        this.highThreshold = purgeThreshold;
        this.lowThreshold = lowThreshold;
        this.purgeStrategy = purgeStrategy;
    }

    public int getHighThreshold() {
        return highThreshold;
    }

    public int getLowThreshold() {
        return lowThreshold;
    }

    public PurgeStrategy getPurgeStrategy() {
        return purgeStrategy;
    }
}
