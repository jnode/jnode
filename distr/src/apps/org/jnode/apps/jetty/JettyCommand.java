/*
 * $Id$
 */
package org.jnode.apps.jetty;

import java.io.File;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.PortNumberArgument;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Starts a Jetty server instance with the specified web application and HTTP port.
 *
 * @author Levente S\u00e1ntha
 */
public class JettyCommand extends AbstractCommand {
    private final FileArgument ARG_WEBAPP =
        new FileArgument("webapp", Argument.MANDATORY, "web application directory");

    private final PortNumberArgument ARG_PORT =
        new PortNumberArgument("port", Argument.OPTIONAL, "http port (default 8080)");


    public JettyCommand() {
        super("start the Jetty web server running a web application");
        registerArguments(ARG_PORT, ARG_WEBAPP);
    }

    public static void main(String[] args) throws Exception {
        new JettyCommand().execute(args);
    }

    public void execute()
        throws Exception {
        File webapp_dir = ARG_WEBAPP.getValue();
        int port = ARG_PORT.isSet() ? ARG_PORT.getValue() : 8080;

        Server server = new Server(port);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar(webapp_dir.getAbsolutePath());
        server.setHandler(webapp);

        server.start();
        server.join();
    }
}
