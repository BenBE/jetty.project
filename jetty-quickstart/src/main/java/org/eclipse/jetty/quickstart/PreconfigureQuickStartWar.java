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
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.security.ConstraintAware;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.Holder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.MetaData.OriginInfo;
import org.eclipse.jetty.webapp.Origin;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlAppendable;
import org.eclipse.jetty.xml.XmlConfiguration;

public class PreconfigureQuickStartWar
{
    static final boolean ORIGIN=true;
    
    public static final String[] __configurationClasses = new String[]
    { 
        org.eclipse.jetty.webapp.WebInfConfiguration.class.getCanonicalName(), org.eclipse.jetty.webapp.WebXmlConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.webapp.MetaInfConfiguration.class.getCanonicalName(), org.eclipse.jetty.webapp.FragmentConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.plus.webapp.EnvConfiguration.class.getCanonicalName(), org.eclipse.jetty.plus.webapp.PlusConfiguration.class.getCanonicalName(),
        org.eclipse.jetty.annotations.AnnotationConfiguration.class.getCanonicalName(),
    };

    public static void main(String... args) throws Exception
    {
        Resource war = null;
        Resource dir = null;
        Resource xml = null;

        switch (args.length)
        {
            case 0:
                error("No WAR file or directory given");
                break;

            case 1:
                dir = Resource.newResource(args[0]);

            case 2:
                war = Resource.newResource(args[0]);
                if (war.isDirectory())
                {
                    dir = war;
                    war = null;
                    xml = Resource.newResource(args[1]);
                }
                else
                {
                    dir = Resource.newResource(args[1]);
                }

                break;

            case 3:
                war = Resource.newResource(args[0]);
                dir = Resource.newResource(args[1]);
                xml = Resource.newResource(args[2]);
                break;

            default:
                error("Too many args");
                break;
        }

        // Do we need to unpack a war?
        if (war != null)
        {
            if (war.isDirectory())
                error("war file is directory");

            if (!dir.exists())
                dir.getFile().mkdirs();
            JarResource.newJarResource(war).copyTo(dir.getFile());
        }

        final Server server = new Server();

        WebAppContext webapp = new WebAppContext()
        {
            @Override
            protected void startContext() throws Exception
            {
                configure();
                getMetaData().resolve(this);

                quickstartWebXml(this);
            }
        };
        webapp.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*/[^/]*servlet-api-[^/]*\\.jar$");
        webapp.setConfigurationClasses(PreconfigureQuickStartWar.__configurationClasses);
        webapp.setContextPath("/");
        webapp.setWar(dir.getFile().getAbsolutePath());

        if (xml != null)
        {
            if (xml.isDirectory() || !xml.toString().toLowerCase().endsWith(".xml"))
                error("Bad context.xml: "+xml);
            XmlConfiguration xmlConfiguration = new XmlConfiguration(xml.getURL());
            xmlConfiguration.configure(webapp);
        }
        server.setHandler(webapp);

        server.start();
        server.stop();
    }

    private static void error(String message)
    {
        System.err.println("ERROR: " + message);
        System.err.println("Usage: java -jar PreconfigureQuickStartWar.jar <war-directory>");
        System.err.println("       java -jar PreconfigureQuickStartWar.jar <war-directory> <context-xml-file>");
        System.err.println("       java -jar PreconfigureQuickStartWar.jar <war-file> <target-war-directory>");
        System.err.println("       java -jar PreconfigureQuickStartWar.jar <war-file> <target-war-directory> <context-xml-file>");
        System.exit(1);
    }

    private static void quickstartWebXml(WebAppContext webapp) throws IOException
    {
        webapp.getMetaData().getOrigins();
        // webapp.dumpStdErr();

        File webxml = new File(webapp.getWebInf().getFile(),"quickstart-web.xml");

        XmlAppendable out = new XmlAppendable(new PrintStream(webxml));
        MetaData md = webapp.getMetaData();

        Map<String, String> webappAttr = new HashMap<>();
        webappAttr.put("xmlns","http://xmlns.jcp.org/xml/ns/javaee");
        webappAttr.put("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        webappAttr.put("xsi:schemaLocation","http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd");
        webappAttr.put("metadata-complete","true");
        webappAttr.put("version","3.1");

        out.open("web-app",webappAttr);

        if (webapp.getDisplayName() != null)
            out.tag("display-name",webapp.getDisplayName());

        // Set some special context parameters

        // The location of the war file on disk
        out.open("context-param")
        .tag("param-name","org.eclipse.jetty.quickstart.baseResource")
        .tag("param-value",webapp.getBaseResource().getFile().getCanonicalFile().getAbsoluteFile().toURI().toString())
        .close();

        // The library order
        if (webapp.getAttribute(ServletContext.ORDERED_LIBS)!=null)
            out.open("context-param")
            .tag("param-name",ServletContext.ORDERED_LIBS)
            .tag("param-value",webapp.getAttribute(ServletContext.ORDERED_LIBS).toString())
            .close();

        List<ContainerInitializer> initializers = (List<ContainerInitializer>)webapp.getAttribute(AnnotationConfiguration.CONTAINER_INITIALIZERS);
        if (initializers != null && !initializers.isEmpty())
        {
            int i = 0;
            for (ContainerInitializer ci : initializers)
            {
                out.open("context-param")
                .tag("param-name",AnnotationConfiguration.CONTAINER_INITIALIZERS + "." + i++)
                .tag("param-value",ci.toString())
                .close();
            }
        }

        for (String p : webapp.getInitParams().keySet())
            out.open("context-param",origin(md,"context-param." + p))
            .tag("param-name",p)
            .tag("param-value",webapp.getInitParameter(p))
            .close();

        if (webapp.getEventListeners() != null)
            for (EventListener e : webapp.getEventListeners())
                out.open("listener",origin(md,e.getClass().getCanonicalName() + ".listener"))
                .tag("listener-class",e.getClass().getCanonicalName())
                .close();

        ServletHandler servlets = webapp.getServletHandler();

        if (servlets.getFilters() != null)
        {
            for (FilterHolder holder : servlets.getFilters())
                outholder(out,md,"filter",holder);
        }

        if (servlets.getFilterMappings() != null)
        {
            for (FilterMapping mapping : servlets.getFilterMappings())
            {
                out.open("filter-mapping");
                out.tag("filter-name",mapping.getFilterName());
                if (mapping.getPathSpecs() != null)
                    for (String s : mapping.getPathSpecs())
                        out.tag("url-pattern",s);
                if (mapping.getServletNames() != null)
                    for (String n : mapping.getServletNames())
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
        }

        if (servlets.getServlets() != null)
        {
            for (ServletHolder holder : servlets.getServlets())
                outholder(out,md,"servlet",holder);
        }

        if (servlets.getServletMappings() != null)
        {
            for (ServletMapping mapping : servlets.getServletMappings())
            {
                out.open("servlet-mapping",origin(md,mapping.getServletName() + ".servlet.mappings"));
                out.tag("servlet-name",mapping.getServletName());
                if (mapping.getPathSpecs() != null)
                    for (String s : mapping.getPathSpecs())
                        out.tag("url-pattern",s);
                out.close();
            }
        }

        // Security elements
        SecurityHandler security = webapp.getSecurityHandler();
        
        if (security!=null && (security.getRealmName()!=null || security.getAuthMethod()!=null))
        {
            out.open("login-config");
            if (security.getAuthMethod()!=null)
                out.tag("auth-method",origin(md,"auth-method"),security.getAuthMethod());
            if (security.getRealmName()!=null)
                out.tag("realm-name",origin(md,"realm-name"),security.getRealmName());
            
            
            if (Constraint.__FORM_AUTH.equalsIgnoreCase(security.getAuthMethod()))
            {
                out.open("form-login-config");
                out.tag("form-login-page",origin(md,"form-login-page"),security.getInitParameter(FormAuthenticator.__FORM_LOGIN_PAGE));
                out.tag("form-error-page",origin(md,"form-error-page"),security.getInitParameter(FormAuthenticator.__FORM_ERROR_PAGE));
                out.close();
            }
            
            out.close();
        }
        
        if (security instanceof ConstraintAware)
        {
            ConstraintAware ca = (ConstraintAware)security;
            for (String r:ca.getRoles())
                out.open("security-role")
                .tag("role-name",r)
                .close();
            
            for (ConstraintMapping m : ca.getConstraintMappings())
            {
                out.open("security-constraint");
                
                if (m.getConstraint().getAuthenticate())
                {
                    out.open("auth-constraint");
                    if (m.getConstraint().getRoles()!=null)
                        for (String r : m.getConstraint().getRoles())
                            out.tag("role-name",r);

                    out.close();
                }
                
                switch (m.getConstraint().getDataConstraint())
                {
                    case Constraint.DC_NONE:
                        out.open("user-data-constraint").tag("transport-guarantee","NONE").close();
                        break;
                        
                    case Constraint.DC_INTEGRAL:
                        out.open("user-data-constraint").tag("transport-guarantee","INTEGRAL").close();
                        break;
                        
                    case Constraint.DC_CONFIDENTIAL:
                        out.open("user-data-constraint").tag("transport-guarantee","CONFIDENTIAL").close();
                        break;
                        
                    default:
                            break;
                        
                }

                out.open("web-resource-collection");
                {
                    if (m.getConstraint().getName()!=null)
                        out.tag("web-resource-name",m.getConstraint().getName());
                    if (m.getPathSpec()!=null)
                        out.tag("url-pattern",origin(md,"constraint.url."+m.getPathSpec()),m.getPathSpec());
                    if (m.getMethod()!=null)
                        out.tag("http-method",m.getMethod());

                    if (m.getMethodOmissions()!=null)
                        for (String o:m.getMethodOmissions())
                            out.tag("http-method-omission",o);

                    out.close();
                }
                
                out.close();
                
            }
        }
        
        
        out.close();
    }

    private static void outholder(XmlAppendable out, MetaData md, String tag, Holder<?> holder) throws IOException
    {
        out.open(tag,Collections.singletonMap("source",holder.getSource().toString()));
        String n = holder.getName();
        out.tag(tag + "-name",n);

        String ot = n + "." + tag + ".";

        out.tag(tag + "-class",origin(md,ot + tag + "-class"),holder.getClassName());

        for (String p : holder.getInitParameters().keySet())
            out.open("init-param",origin(md,ot + "init-param." + p))
            .tag("param-name",p)
            .tag("param-value",holder.getInitParameter(p))
            .close();

        if (holder instanceof ServletHolder)
        {
            ServletHolder s = (ServletHolder)holder;
            if (s.getForcedPath() != null)
                out.tag("jsp-file",s.getForcedPath());

            if (s.getInitOrder() != 0)
                out.tag("load-on-startup",Integer.toString(s.getInitOrder()));

            if (s.getRunAsRole() != null)
                out.open("run-as",origin(md,ot + "run-as"))
                .tag("role-name",s.getRunAsRole())
                .close();

            Map<String,String> roles = s.getRoleRefMap();
            if (roles!=null)
            {
                for (Map.Entry<String, String> e : roles.entrySet())
                {
                    out.open("security-rol-ref",origin(md,ot+"role-name."+e.getKey()))
                    .tag("role-name",e.getKey())
                    .tag("role-link",e.getValue())
                    .close();
                }
            }
            
            if (!s.isEnabled())
                out.tag("enabled",origin(md,ot + "enabled"),"false");

            // TODO multipart-config
        }

        out.tag("async-supported",origin(md,ot + "async-supported"),holder.isAsyncSupported()?"true":"false");
        out.close();
    }

    public static Map<String, String> origin(MetaData md, String name)
    {
        if (!ORIGIN)
            return Collections.emptyMap();
        if (name == null)
            return Collections.emptyMap();
        OriginInfo origin = md.getOriginInfo(name);
        // System.err.println("origin of "+name+" is "+origin);
        if (origin == null)
            return Collections.emptyMap();
        return Collections.singletonMap("origin",origin.toString());

    }
}
