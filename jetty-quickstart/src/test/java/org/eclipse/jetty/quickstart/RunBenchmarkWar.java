package org.eclipse.jetty.quickstart;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

public class RunBenchmarkWar 
{
    private static final long __start=System.nanoTime();
    private static final Logger LOG = Log.getLogger(Server.class);
    
    
    public static void main(String... args) throws Exception
    {   
        Server server = new Server(8080);
        
        // Setup JMX
        MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);

        WebAppContext webapp = new WebAppContext();
        webapp.setConfigurationClasses(PreconfigureQuickStartWar.__configurationClasses);
        webapp.setContextPath("/");
        webapp.setWar("src/test/resources/benchmark-java-webapp-1.0.war");

        server.setHandler(webapp);

        server.start();

        LOG.info("Started in {}ms",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-__start));
        server.join();
    }
}
