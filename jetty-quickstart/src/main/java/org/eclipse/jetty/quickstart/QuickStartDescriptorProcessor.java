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

import org.eclipse.jetty.webapp.Descriptor;
import org.eclipse.jetty.webapp.IterativeDescriptorProcessor;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * QuickStartDescriptorProcessor
 * 
 * Handle  extended elements for quickstart-web.xml
 */
public class QuickStartDescriptorProcessor extends IterativeDescriptorProcessor
{

    /**
     * 
     */
    public QuickStartDescriptorProcessor()
    {
        //TODO register methods to handle additional elements only in quickstart-web.xml
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

}
