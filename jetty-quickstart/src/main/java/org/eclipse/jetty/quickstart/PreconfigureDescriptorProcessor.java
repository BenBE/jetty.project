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

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.IterativeDescriptorProcessor;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlParser;

/**
 * Preconfigure DescriptorProcessor
 *
 * Saves literal XML snippets
 *
 */

public class PreconfigureDescriptorProcessor extends IterativeDescriptorProcessor
{
    private static final Logger LOG = Log.getLogger(PreconfigureDescriptorProcessor.class);
    
    private final StringBuilder _buffer = new StringBuilder();

    public PreconfigureDescriptorProcessor ()
    {
        try
        {
            registerVisitor("env-entry", getClass().getDeclaredMethod("saveSnippet", __signature));
            registerVisitor("resource-ref", getClass().getDeclaredMethod("saveSnippet", __signature));
            registerVisitor("resource-env-ref", getClass().getDeclaredMethod("saveSnippet", __signature));
            registerVisitor("message-destination-ref", getClass().getDeclaredMethod("saveSnippet", __signature));
            registerVisitor("data-source", getClass().getDeclaredMethod("saveSnippet", __signature));
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void start(WebAppContext context, Descriptor descriptor)
    {
        LOG.debug("process {}",descriptor);
        _buffer.append("\n  <!-- ").append(descriptor).append(" -->\n");
    }


    @Override
    public void end(WebAppContext context,Descriptor descriptor)
    {
    }


    public void saveSnippet (WebAppContext context, Descriptor descriptor, XmlParser.Node node)
    throws Exception
    {
        LOG.debug("save {}",node.getTag());
        _buffer.append("  ").append(node.toString()).append("\n");
    }
    
    public String getXML()
    {
        return _buffer.toString();
    }

}
