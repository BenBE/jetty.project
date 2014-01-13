package org.eclipse.jetty.quickstart;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class PreconfigureBenchmarkWar
{
    private static final long __start=System.nanoTime();
    private static final Logger LOG = Log.getLogger(Server.class);
    
    public static void main(String[] args) throws Exception
    {
        String target="target/preconfigured";
        File file = new File(target);
        if (file.exists())
            IO.delete(file);
        
        PreconfigureQuickStartWar.main("src/test/resources/benchmark-java-webapp-1.0.war",target);

        LOG.info("Preconfigured in {}ms",TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-__start));
    }

}
