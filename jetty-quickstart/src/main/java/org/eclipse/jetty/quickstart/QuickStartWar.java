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

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 * QuickStartWar
 *
 */
public class QuickStartWar
{
    public static final long __start=System.nanoTime();
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

        if (args.length<1)
            error("No WAR file or directory given");
        
        //war file or dir to start
        String war = args[0];
        
        //optional jetty context xml file to configure the webapp
        Resource contextXml = null;
        if (args.length > 1)
            contextXml = Resource.newResource(args[1]);
        
        Server server = new Server(8080);
        
        WebAppContext webapp = new WebAppContext();
        webapp.setConfigurationClasses(__configurationClasses);
        webapp.setContextPath("/");
        webapp.setWar(war);

        //apply context xml file
        if (contextXml != null)
        {
            System.err.println("Applying "+contextXml);
            XmlConfiguration xmlConfiguration = new XmlConfiguration(contextXml.getURL());  
            xmlConfiguration.configure(webapp);   
        }

        
        server.setHandler(webapp);

        long serverStart = System.nanoTime();
        server.start();

        long end = System.nanoTime();
        
        System.err.println("Server start in "+ TimeUnit.NANOSECONDS.toMillis(end-serverStart));
        System.err.println("Started in "+TimeUnit.NANOSECONDS.toMillis(end-__start));
      
        server.join();
    }
  
    

    private static void error(String message)
    {
        System.err.println("ERROR: "+message);
        System.err.println("Usage: java -jar QuickStartWar.jar <war-directory> <context-xml>");
        System.err.println("       java -jar QuickStartWar.jar <war-file> <context-xml>");
        System.exit(1);
    }
}
