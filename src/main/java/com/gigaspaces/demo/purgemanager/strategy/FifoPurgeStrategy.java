package com.gigaspaces.demo.purgemanager.strategy;

import com.gigaspaces.client.TakeModifiers;
import com.gigaspaces.document.SpaceDocument;
import com.j_spaces.core.client.SQLQuery;
import org.openspaces.core.GigaSpace;

/**
 * Demos purging by Space FIFO - class must be annotated with @SpaceClass with Fifo enabled
 */
public class FifoPurgeStrategy extends PurgeStrategy {

    private final String className;
    private Object template;

    public FifoPurgeStrategy(String className) {
        this.className = className;
    }

    @Override
    public int purge(GigaSpace gigaSpace, int maxEntries) {
        if (template == null)
            template = gigaSpace.prepareTemplate(new SQLQuery<SpaceDocument>(className, ""));
        Object[] purged = gigaSpace.takeMultiple(template, maxEntries, TakeModifiers.FIFO);
        return purged.length;
    }
}
