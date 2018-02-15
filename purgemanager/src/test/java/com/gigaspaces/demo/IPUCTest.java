package com.gigaspaces.demo;

import com.gigaspaces.demo.common.DataWithSequence;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

public class IPUCTest {

    @BeforeClass
    public static void intializeAppContext() throws Exception {

        String locatorPort = getAvailablePort();

        // environment variable should be set to computer's ip address
        String lookupLocator = System.getenv("XAP_LOOKUP_LOCATORS");
        if (lookupLocator == null) lookupLocator = "127.0.0.1";

        // these lus configurations are done so
        // 1. each test has its own lus
        // 2. lets you run multiple tests concurrently. Teardown of the test doesn't affect other tests
        System.setProperty("com.gs.jini_lus.locators", lookupLocator +  ":" + locatorPort);
        System.setProperty("com.sun.jini.reggie.initialUnicastDiscoveryPort", locatorPort);
        System.setProperty("com.gs.multicast.enabled", "false");
        System.setProperty("com.gs.jini_lus.groups", IPUCTest.class.getName() + "-" + locatorPort);
        System.setProperty("com.gs.start-embedded-lus", "false");


        createRegistrar();
    }

    // lus creation
    public static com.sun.jini.reggie.Registrar createRegistrar() throws Exception {
        URL config = com.j_spaces.kernel.ResourceLoader.getServicesConfigUrl();

        System.out.println("config is: " + config);
        return new com.sun.jini.reggie.GigaRegistrar(new String[] {config.toExternalForm()}, null);
    }

    public static String getAvailablePort() {
        String locatorPort = null;

        try {
            ServerSocket server = new ServerSocket(0);
            locatorPort = String.valueOf(server.getLocalPort());
            server.close();

            Thread.sleep(5000L);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error determining available port to run lus");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return locatorPort;
    }

    @Test
    public void runTest() {
        try {
            IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();


            // provide cluster information for the specific PU instance
            // 1 partition 0 backups simplifies configuration
            ClusterInfo clusterInfo = new ClusterInfo();
            clusterInfo.setSchema("partitioned");
            clusterInfo.setNumberOfInstances(1);
            clusterInfo.setNumberOfBackups(0);
            clusterInfo.setInstanceId(1);
            provider.setClusterInfo(clusterInfo);

            // set the config location (override the default one - classpath:/META-INF/spring/pu.xml)
            //provider.addConfigLocation("classpath:com/gigaspaces/demo/my-pu.xml");

            // build the Spring application context and "start" it
            ProcessingUnitContainer container = provider.createContainer();

            // create a remote proxy
            SpaceProxyConfigurer spaceProxyConfigurer = new SpaceProxyConfigurer("mySpace");
            GigaSpace spaceProxy = new GigaSpaceConfigurer(spaceProxyConfigurer).gigaSpace();

            System.out.println("Count of objects: " + spaceProxy.count(new Object()));

            for(int i = 0; i < 1002; i++) {
                DataWithSequence data = new DataWithSequence();
                spaceProxy.write(data);
            }

            Thread.sleep(10000);
            System.out.println("Count of DataWithSequence: " + spaceProxy.count(new DataWithSequence()));

            //container.close();

        } catch( Exception e ) {
            e.printStackTrace();
        }
    }
}
