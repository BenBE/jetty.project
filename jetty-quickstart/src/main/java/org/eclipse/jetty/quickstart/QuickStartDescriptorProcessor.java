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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.IterativeDescriptorProcessor;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlParser;

/**
 * QuickStartDescriptorProcessor
 * 
 * Handle  extended elements for quickstart-web.xml
 */
public class QuickStartDescriptorProcessor extends IterativeDescriptorProcessor
{
    public static final String CONTAINER_INITIALIZER_ID = "org.eclipse.jetty.containerInitializers";

    /**
     * 
     */
    public QuickStartDescriptorProcessor()
    {
        try
        {
            registerVisitor("context-param", this.getClass().getDeclaredMethod("visitContextParam", __signature));
        }    
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @see org.eclipse.jetty.webapp.IterativeDescriptorProcessor#start(org.eclipse.jetty.webapp.WebAppContext, org.eclipse.jetty.webapp.Descriptor)
     */
    @Override
    public void start(WebAppContext context, Descriptor descriptor)
    {
    }

    /**
     * @see org.eclipse.jetty.webapp.IterativeDescriptorProcessor#end(org.eclipse.jetty.webapp.WebAppContext, org.eclipse.jetty.webapp.Descriptor)
     */
    @Override
    public void end(WebAppContext context, Descriptor descriptor)
    { 
    }
    

    /**
     * @param context
     * @param descriptor
     * @param node
     */
    public void visitContextParam (WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    {
        String name = node.getString("param-name", false, true);
        String value = node.getString("param-value", false, true);
        if (name.startsWith(CONTAINER_INITIALIZER_ID))
        {
            visitContainerInitializer(context, new ContainerInitializer(Thread.currentThread().getContextClassLoader(), value));
            context.removeAttribute(name);
        }
    }
    

    public void visitContainerInitializer (WebAppContext context, ContainerInitializer containerInitializer)
    {
        if (containerInitializer == null)
            return;
        
        //add the ContainerInitializer to the list of container initializers
        List<ContainerInitializer> containerInitializers = (List<ContainerInitializer>)context.getAttribute(AnnotationConfiguration.CONTAINER_INITIALIZERS);
        if (containerInitializers == null)
        {
            containerInitializers = new ArrayList<ContainerInitializer>();
            context.setAttribute(AnnotationConfiguration.CONTAINER_INITIALIZERS, containerInitializers);
        }
        
        containerInitializers.add(containerInitializer);

        //Ensure a bean is set up on the context that will invoke the ContainerInitializers as the context starts
        ServletContainerInitializersStarter starter = (ServletContainerInitializersStarter)context.getAttribute(AnnotationConfiguration.CONTAINER_INITIALIZER_STARTER);
        if (starter == null)
        {
            starter = new ServletContainerInitializersStarter(context);
            context.setAttribute(AnnotationConfiguration.CONTAINER_INITIALIZER_STARTER, starter);
            context.addBean(starter, true);
        }
    }
}
