package org.eclipse.jetty.quickstart;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

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
        org.eclipse.jetty.webapp.JettyWebXmlConfiguration.class.getCanonicalName()
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
                
                dumpStdErr();
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
}
