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

package org.eclipse.jetty.webapp;


import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.resource.Resource;

/**
 * MetaInfConfiguration
 *
 * Scan META-INF of all jars in WEB-INF/lib to find:
 * <ul>
 * <li>tlds
 * <li>web-fragment.xml
 * <li>resources
 * </ul>
 */
public class MetaInfConfiguration extends AbstractConfiguration
{
    private static final Logger LOG = Log.getLogger(MetaInfConfiguration.class);

    public static final String METAINF_TLDS = "org.eclipse.jetty.tlds";
    public static final String METAINF_FRAGMENTS = FragmentConfiguration.FRAGMENT_RESOURCES;
    public static final String METAINF_RESOURCES = WebInfConfiguration.RESOURCE_DIRS;
  
    @Override
    public void preConfigure(final WebAppContext context) throws Exception
    {
       //Merge all container and webinf lib jars to look for META-INF resources     
        ArrayList<Resource> jars = new ArrayList<Resource>();
        jars.addAll(context.getMetaData().getContainerResources());
        jars.addAll(context.getMetaData().getWebInfJars());
        
        //Scan jars for META-INF information
        if (jars != null)
        {
            for (Resource r : jars)
            {
                URI uri = r.getURI();
                Resource metaInfDir = Resource.newResource("jar:"+uri+"!/META-INF/");
                            
                if (metaInfDir.exists() && metaInfDir.isDirectory())
                {
                    //check if META-INF/resources exists
                    Resource resourcesDir = metaInfDir.addPath("/resources");
                    if (resourcesDir.exists() && resourcesDir.isDirectory())
                    {  
                        Set<Resource> dirs = (Set<Resource>)context.getAttribute(METAINF_RESOURCES);
                        if (dirs == null)
                        {
                            dirs = new HashSet<Resource>();
                            context.setAttribute(METAINF_RESOURCES, dirs);
                        }
                        dirs.add(resourcesDir);
                    }
                    
                    //check if META-INF/web-fragment.xml exists
                    Resource webFrag = metaInfDir.addPath("web-fragment.xml");
                    if (webFrag.exists() && !webFrag.isDirectory())
                    {
                        Map<Resource, Resource> fragments = (Map<Resource,Resource>)context.getAttribute(METAINF_FRAGMENTS);
                        if (fragments == null)
                        {
                            fragments = new HashMap<Resource, Resource>();
                            context.setAttribute(METAINF_FRAGMENTS, fragments);
                        }
                        fragments.put(r, webFrag);    
                    }
                    
                    //find any *.tld files inside META-INF or subdirs
                    Collection<Resource> resources = metaInfDir.getAllResources();
                    for (Resource t:resources)
                    {
                        String name = t.toString();
                        if (name.endsWith(".tld"))
                        {
                            Collection<URL> tld_resources=(Collection<URL>)context.getAttribute(METAINF_TLDS);
                            if (tld_resources == null)
                            {
                                tld_resources = new HashSet<URL>();
                                context.setAttribute(METAINF_TLDS, tld_resources);
                            }
                            tld_resources.add(t.getURL());
                        }
                    }
                }
            }
        }
    }
   
    @Override
    public void postConfigure(WebAppContext context) throws Exception
    {
        context.setAttribute(METAINF_FRAGMENTS, null); 
        context.setAttribute(METAINF_RESOURCES, null);
        context.setAttribute(METAINF_TLDS, null);
    }
}
