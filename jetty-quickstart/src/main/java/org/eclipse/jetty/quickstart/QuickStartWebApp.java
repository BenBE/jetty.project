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
public class QuickStartWebApp extends WebAppContext
{
    private static final Logger LOG = Log.getLogger(QuickStartWebApp.class);
    
    public static final String[] __configurationClasses = new String[] 
            {
                org.eclipse.jetty.quickstart.QuickStartConfiguration.class.getCanonicalName(),
                org.eclipse.jetty.plus.webapp.EnvConfiguration.class.getCanonicalName(),
                org.eclipse.jetty.plus.webapp.PlusConfiguration.class.getCanonicalName(),
                org.eclipse.jetty.webapp.JettyWebXmlConfiguration.class.getCanonicalName()
            };
    
    
    private boolean _autoPreconfigure=false;
    private String _xml=null;
    
    
    public QuickStartWebApp()
    {
        super();
        setConfigurationClasses(__configurationClasses);
    }

    public boolean isAutoPreconfigure()
    {
        return _autoPreconfigure;
    }
    
    public void setAutoPreconfigure(boolean autoPrecompile)
    {
        _autoPreconfigure = autoPrecompile;
    }

    @Override
    protected void doStart() throws Exception
    {
        Resource war = null;
        Resource dir = null;
        Resource xml = null;
        
        Resource base = getBaseResource();
        if (base==null)
            base=Resource.newResource(getWar());
        
        if (base.isDirectory())
            dir=base;
        else if (base.toString().toLowerCase().endsWith(".war"))
        {
            war=base;
            String w=war.toString();
            dir=Resource.newResource(w.substring(0,w.length()-4));
        }
        else 
            throw new IllegalArgumentException();

        Resource qswebxml=dir.addPath("/WEB-INF/quickstart-web.xml");
        if (_autoPreconfigure && (!dir.isDirectory() || !qswebxml.exists()))
        {   
            if (_xml!=null)
                xml=Resource.newResource(_xml);
            else
            {
                Resource x=Resource.newResource(dir.toString()+".xml");
                if (x.exists())
                    xml=x;
                else 
                {
                    x=Resource.newResource(dir.toString()+".XML"); 
                    if (x.exists())
                        xml=x;
                }
            }
            
            LOG.info("Preconfigure for quickstart: {}(war={},dir={},xml={})",this,war,dir,xml);
            PreconfigureQuickStartWar.preconfigure(war,dir,xml);
        }

        setWar(null);
        setBaseResource(dir);

        super.doStart();
    }



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
        
        QuickStartWebApp webapp = new QuickStartWebApp();
        webapp.setAutoPreconfigure(true);
        webapp.setWar(war);
        webapp.setContextPath("/");

        //apply context xml file
        if (contextXml != null)
        {
            System.err.println("Applying "+contextXml);
            XmlConfiguration xmlConfiguration = new XmlConfiguration(contextXml.getURL());  
            xmlConfiguration.configure(webapp);   
        }
        
        server.setHandler(webapp);

        server.start();

        
      
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
