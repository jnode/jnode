/*
 * $Id$
 */
package org.jnode.apps.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.IntegerArgument;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;

/**
 * Starts a Jetty server instance with the specified web application and HTTP port.
 *
 * @author Levente S\u00e1ntha
 */
public class JettyCommand extends AbstractCommand {
    static final FileArgument ARG_WEBAPP = new FileArgument("webapp", "web application directory");
    static final IntegerArgument ARG_PORT = new IntegerArgument("port", "http port");

    private static Parameter PARAM_PORT = new Parameter(ARG_PORT, Parameter.OPTIONAL);
    public static Help.Info HELP_INFO = new Help.Info("jetty", "start the Jetty web server with a web application on the given port (default 8080)",
            new Parameter(ARG_WEBAPP, Parameter.MANDATORY),
            PARAM_PORT);

    public static void main(String[] args) throws Exception {
        new JettyCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
        ParsedArguments arguments = HELP_INFO.parse(commandLine);
        File webapp_dir = ARG_WEBAPP.getFile(arguments);
        int port = 8080;
        if(PARAM_PORT.isSet(arguments)){
            port = ARG_PORT.getInteger(arguments);
        }

        Server server = new Server(port);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(webapp_dir.getAbsolutePath());
        server.setHandler(webapp);

        server.start();
        server.join();        
    }
}
