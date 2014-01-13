package org.eclipse.jetty.quickstart;

import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

public class QuickStartBenchmarkWar 
{
    private static final long __start=System.nanoTime();
    private static final Logger LOG = Log.getLogger(Server.class);
    
    public static final String[] __configurationClasses = new String[] 
            {
                org.eclipse.jetty.quickstart.QuickStartConfiguration.class.getCanonicalName(),
                org.eclipse.jetty.plus.webapp.EnvConfiguration.class.getCanonicalName(),
                org.eclipse.jetty.plus.webapp.PlusConfiguration.class.getCanonicalName(),
                org.eclipse.jetty.webapp.JettyWebXmlConfiguration.class.getCanonicalName()
            };
    
    public static void main(String... args) throws Exception
    {   
        Server server = new Server(8080);
        
        // Setup JMX
        MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);

        WebAppContext webapp = new WebAppContext();
        webapp.setConfigurationClasses(__configurationClasses);
        webapp.setContextPath("/");
        webapp.setWar("target/preconfigured");
        server.setHandler(webapp);

        server.start();

        LOG.info("Started in {}ms",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-__start));
        server.join();
    }
}
