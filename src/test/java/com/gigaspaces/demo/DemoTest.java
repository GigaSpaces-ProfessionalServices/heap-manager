package com.gigaspaces.demo;

import com.gigaspaces.client.TakeModifiers;
import com.j_spaces.core.client.SQLQuery;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.EmbeddedSpaceConfigurer;

public class DemoTest {
    @Test
    public void demo() {
        GigaSpace gigaSpace = new GigaSpaceConfigurer(new EmbeddedSpaceConfigurer("demoSpace")).create();
        int count = gigaSpace.count(null);
        System.out.println("Objects in space: " + count);

        for (int i=0 ; i < 1000 ; i++) {
            String payload = "payload #" + i;
            gigaSpace.write(new Data().setPayload(payload));
            gigaSpace.write(new DataWithFifo().setPayload(payload));
            gigaSpace.write(new DataWithSequence().setPayload(payload));
        }

        System.out.println("Testing data take:");
        for (Object o : gigaSpace.takeMultiple(new SQLQuery(Data.class, ""), 10)) {
            System.out.println(o.toString());
        }

        System.out.println("Testing DataWithFifo take with Fifo:");
        for (Object o : gigaSpace.takeMultiple(new SQLQuery(DataWithFifo.class, ""), 10, TakeModifiers.FIFO)) {
            System.out.println(o.toString());
        }

        System.out.println("Testing DataWithSequence take lowest:");
        for (Object o : gigaSpace.takeMultiple(new SQLQuery(DataWithSequence.class, "seqId > 0"), 10)) {
            System.out.println(o.toString());
        }
    }
}
