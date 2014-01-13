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
import org.eclipse.jetty.webapp.WebAppContext;

public class StartBenchmarkWar 
{
    private static final long __start=System.nanoTime();
    private static final Logger LOG = Log.getLogger(Server.class);
    
    
    public static void main(String... args) throws Exception
    {   
        Server server = new Server(8080);

        WebAppContext webapp = new WebAppContext();
        webapp.setConfigurationClasses(PreconfigureQuickStartWar.__configurationClasses);
        webapp.setContextPath("/");
        webapp.setWar("src/test/resources/benchmark-java-webapp-1.0.war");

        server.setHandler(webapp);

        server.start();

        LOG.info("Started in {}ms",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-__start));
        server.join();
    }
}
