package com.gigaspaces.demo.purgemanager.strategy;

import org.openspaces.core.GigaSpace;

public abstract class PurgeStrategy {
    public abstract int purge(GigaSpace gigaSpace, int maxEntries);
}
