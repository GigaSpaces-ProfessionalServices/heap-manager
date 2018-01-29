package com.gigaspaces.feeder;

import com.gigaspaces.common.Data;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;

import java.util.logging.Logger;

public class Feeder {

    private static Logger logger = Logger.getLogger(Feeder.class.getName());

    public static final String XAP_LOOKUP_GROUPS = "XAP_LOOKUP_GROUPS";
    public static final String XAP_LOOKUP_LOCATORS = "XAP_LOOKUP_LOCATORS";

    private static final String spaceName = "mySpace";

    private GigaSpace gigaSpace;

    static {
        // read from environment variable
        System.setProperty("com.gs.jini_lus.locators", System.getenv(XAP_LOOKUP_LOCATORS));
        System.setProperty("com.gs.jini_lus.groups", System.getenv(XAP_LOOKUP_GROUPS));
    }


    public void feeder() {

        for(int i=0; i < 1; i++) {
            Data data = new Data();
            gigaSpace.write(data);
        }
    }

    private void initialize() {
        SpaceProxyConfigurer spaceProxyConfigurer = new SpaceProxyConfigurer(spaceName);
        gigaSpace = new GigaSpaceConfigurer(spaceProxyConfigurer).gigaSpace();
    }

    public static void main(String[] args) {
        try {

            Feeder feeder = new Feeder();
            feeder.initialize();
            feeder.feeder();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

