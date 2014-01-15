//
//  ========================================================================
//  Copyright (c) 1995-2014 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.quickstart;

import java.lang.management.ManagementFactory;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

public class QuickStartTestJNDIWar 
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
        webapp.setWar("src/test/resources/test-jndi");
        
        
        org.eclipse.jetty.plus.jndi.Transaction txmgr = new org.eclipse.jetty.plus.jndi.Transaction(new com.acme.MockUserTransaction());
        org.eclipse.jetty.plus.jndi.EnvEntry woggle = new org.eclipse.jetty.plus.jndi.EnvEntry(server, "woggle", new Integer(4000), false);
        org.eclipse.jetty.plus.jndi.EnvEntry wiggle = new org.eclipse.jetty.plus.jndi.EnvEntry(webapp, "wiggle", new Double(100), true);
        org.eclipse.jetty.jndi.factories.MailSessionReference mailref = new org.eclipse.jetty.jndi.factories.MailSessionReference();
        mailref.setUser("CHANGE-ME");
        mailref.setPassword("CHANGE-ME");
        Properties props = new Properties();
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.host","CHANGE-ME");
        props.put("mail.from", "CHANGE-ME");
        props.put("mail.debug", "false");
        mailref.setProperties(props);
        org.eclipse.jetty.plus.jndi.Resource xxxmail = new org.eclipse.jetty.plus.jndi.Resource(webapp, "mail/Session", mailref);
        org.eclipse.jetty.plus.jndi.Resource mydatasource = new org.eclipse.jetty.plus.jndi.Resource(webapp, "jdbc/mydatasource",new com.acme.MockDataSource());                                                                  
          

        server.setHandler(webapp);

        server.start();

        LOG.info("Started in {}ms",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-__start));
        server.join();
    }
}
