package com.gigaspaces.demo.purgemanager.strategy;

import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;

/**
 * Demos purging by lowest value of a property in class.
 * That property must be indexed with extended index.
 */
public class LowestPropertyPurgeStrategy extends PurgeStrategy {

    private final String className;
    private final String propertyName;
    private Object template;

    public LowestPropertyPurgeStrategy(String className, String propertyName) {
        this.className = className;
        this.propertyName = propertyName;
    }

    @Override
    public int purge(GigaSpace gigaSpace, int maxEntries) {
        if (template == null)
            template = gigaSpace.prepareTemplate(new SQLQuery<SpaceDocument>(className, propertyName + " > 0"));
        Object[] purged = gigaSpace.takeMultiple(template, maxEntries);
        return purged.length;
    }
}
