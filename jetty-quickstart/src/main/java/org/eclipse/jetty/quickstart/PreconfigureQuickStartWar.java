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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.Origin;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlAppendable;

public class PreconfigureQuickStartWar
{
    public static final String[] __configurationClasses = new String[] 
    {
        org.eclipse.jetty.webapp.WebInfConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.WebXmlConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.MetaInfConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.FragmentConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.plus.webapp.EnvConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.plus.webapp.PlusConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.annotations.AnnotationConfiguration.class.getCanonicalName(),
        // org.eclipse.jetty.webapp.JettyWebXmlConfiguration.class.getCanonicalName()
    };

    public static void main(String... args) throws Exception
    {
        if (args.length<1)
            error("No WAR file or directory given");
        Resource war = Resource.newResource(args[0]);
        if (!war.isDirectory() || args.length==2)
        {
            if (args.length<2)
                error("No target WAR directory given");
            Resource dir = Resource.newResource(args[1]);
            if (!dir.exists())
                dir.getFile().mkdirs();
            JarResource.newJarResource(war).copyTo(dir.getFile());
            war=dir;
        }
        
        final Server server = new Server();

        WebAppContext webapp = new WebAppContext()
        {
            @Override
            protected void startContext()
                throws Exception
            {
                configure();
                getMetaData().resolve(this);
                
                quickstartWebXml(this,new File(getWebInf().getFile(),"quickstart-web.xml"));
            }
        };
        
        webapp.setConfigurationClasses(PreconfigureQuickStartWar.__configurationClasses);
        webapp.setContextPath("/");
        webapp.setWar(war.getFile().getAbsolutePath());

        server.setHandler(webapp);

        server.start();
        server.stop();
    }

    private static void error(String message)
    {
        System.err.println("ERROR: "+message);
        System.err.println("Usage: java -jar PreconfigureQuickStartWar.jar <war-directory>");
        System.err.println("       java -jar PreconfigureQuickStartWar.jar <war-file> <target-war-directory>");
        System.exit(1);
    }
    
    private static void quickstartWebXml(WebAppContext webapp,File webxml) throws IOException
    {
        XmlAppendable out = new XmlAppendable(new PrintStream(webxml));
        MetaData md = webapp.getMetaData();
        
        Map<String,String> webappAttr = new HashMap<>();
        webappAttr.put("xmlns","http://xmlns.jcp.org/xml/ns/javaee"); 
        webappAttr.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        webappAttr.put("xsi:schemaLocation","http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd");
        webappAttr.put("metadata-complete","true");
        webappAttr.put("version","3.1");
        
        out.open("web-app",webappAttr);
        
        if (webapp.getDisplayName()!=null)
            out.tag("display-name",webapp.getDisplayName());
        
        for (String p:webapp.getInitParams().keySet())
            out
            .open("context-param",origin(md,"context-param."+p))
            .tag("param-name",p)
            .tag("param-value",webapp.getInitParameter(p))
            .close();
        
        if (webapp.getEventListeners()!=null)
            for (EventListener e : webapp.getEventListeners())
                out
                .open("listener",origin(md,e.getClass().getCanonicalName()+".listener"))
                .tag("listener-class",e.getClass().getCanonicalName())
                .close();
        
        ServletHandler servlets = webapp.getServletHandler();
        
        
        for (FilterHolder holder : servlets.getFilters())
            outholder(out,md,"filter",holder);
        
        for (FilterMapping mapping : servlets.getFilterMappings())
        {
            out.open("filter-mapping");
            out.tag("filter-name",mapping.getFilterName());
            if (mapping.getPathSpecs()!=null)
                for (String s:mapping.getPathSpecs())
                    out.tag("url-pattern",s);
            if (mapping.getServletNames()!=null)
                for (String n:mapping.getServletNames())
                    out.tag("servlet-name",n);
            
            if (!mapping.isDefaultDispatches())
            {
                if (mapping.appliesTo(DispatcherType.REQUEST))
                    out.tag("dispatcher","REQUEST");
                if (mapping.appliesTo(DispatcherType.ASYNC))
                    out.tag("dispatcher","ASYNC");
                if (mapping.appliesTo(DispatcherType.ERROR))
                    out.tag("dispatcher","ERROR");
                if (mapping.appliesTo(DispatcherType.FORWARD))
                    out.tag("dispatcher","FORWARD");
                if (mapping.appliesTo(DispatcherType.INCLUDE))
                    out.tag("dispatcher","INCLUDE");
            }
            out.close();
        }

        for (ServletHolder holder : servlets.getServlets())
            outholder(out,md,"servlet",holder);

        for (ServletMapping mapping : servlets.getServletMappings())
        {
            out.open("servlet-mapping",origin(md,mapping.getServletName()+".servlet.mappings"));
            out.tag("servlet-name",mapping.getServletName());
            if (mapping.getPathSpecs()!=null)
                for (String s:mapping.getPathSpecs())
                    out.tag("url-pattern",s);
            out.close();
        }
        
        out.close();
    }

    private static void outholder(XmlAppendable out, MetaData md, String tag, Holder<?> holder)
    throws IOException
    {
        out.open(tag, Collections.singletonMap("source",holder.getSource().toString()));
        String n=holder.getName();
        out.tag(tag+"-name",n);
        
        String ot=n+"."+tag+".";
        
        out.tag(tag+"-class",origin(md,ot+tag+"-class"),holder.getClassName());
        

        for (String p:holder.getInitParameters().keySet())
            out
            .open("init-param",origin(md,ot+"init-param."+p))
            .tag("param-name",p)
            .tag("param-value",holder.getInitParameter(p))
            .close();
        
        if (holder instanceof ServletHolder)
        {
            ServletHolder s = (ServletHolder)holder;
            if (s.getForcedPath()!=null)
                out.tag("jsp-file",s.getForcedPath());
                
            if (s.getInitOrder()!=0)
                out.tag("load-on-startup",Integer.toString(s.getInitOrder()));
            
            if (s.getRunAsRole()!=null)
                out.open("run-as",origin(md,ot+"run-as")).tag("role-name",s.getRunAsRole()).close();
            
            if (!s.isEnabled())
                out.tag("enabled",origin(md,ot+"enabled"),"false");
            
            // TODO security-role-ref
            // TODO multipart-config
        }
     
        out.tag("async-supported",origin(md,ot+"async-supported"),holder.isAsyncSupported()?"true":"false");
        out.close();
    }
    
    public static Map<String,String> origin(MetaData md,String name)
    {
        Origin origin=md.getOrigin(name);
        // System.err.println("origin of "+name+" is "+origin);
        if (name==null || origin==Origin.NotSet)
            return Collections.emptyMap();
        return Collections.singletonMap("origin",origin.toString());
        
    }
}
