package com.gigaspaces.demo.purgemanager.strategy;

import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;

/**
 * Demonstrates random purging - purges entries at random
 */
public class RandomPurgeStrategy extends PurgeStrategy {

    private final String className;
    private Object template;

    public RandomPurgeStrategy(String className) {
        this.className = className;
    }

    @Override
    public int purge(GigaSpace gigaSpace, int maxEntries) {
        if (template == null)
            template = gigaSpace.prepareTemplate(new SQLQuery<SpaceDocument>(className, ""));
        Object[] purged = gigaSpace.takeMultiple(template, maxEntries);
        return purged.length;
    }
}
