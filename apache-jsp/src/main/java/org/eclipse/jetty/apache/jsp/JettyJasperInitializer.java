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

package org.eclipse.jetty.apache.jsp;

import java.net.URL;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.jasper.servlet.JasperInitializer;
import org.apache.jasper.servlet.TldScanner;

/**
 * JettyJasperInitializer
 *
 */
public class JettyJasperInitializer extends JasperInitializer
{

    /**
     * Make a TldScanner, and prefeed it the tlds that have already been discovered in jar files
     * by the MetaInfConfiguration.
     * 
     * @see org.apache.jasper.servlet.JasperInitializer#prepareScanner(javax.servlet.ServletContext, boolean, boolean, boolean)
     */
    @Override
    public TldScanner prepareScanner(ServletContext context, boolean namespaceAware, boolean validate, boolean blockExternal)
    {
        TldScanner scanner = super.prepareScanner(context, namespaceAware, validate, blockExternal);
        Collection<URL> tldUrls = (Collection<URL>)context.getAttribute("org.eclipse.jetty.tlds");
        if (tldUrls != null && !tldUrls.isEmpty())
        {
            scanner.setJarTldURLs(tldUrls);
        }
        return scanner;
    }
    

}
