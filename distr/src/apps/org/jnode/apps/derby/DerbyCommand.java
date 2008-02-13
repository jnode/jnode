/*
 * $Id$
 */
package org.jnode.apps.derby;


import org.apache.derby.drda.NetworkServerControl;
import org.apache.derby.impl.drda.NetworkServerControlImpl;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.argument.FileArgument;
import org.jnode.shell.help.argument.IntegerArgument;
import org.jnode.shell.help.argument.StringArgument;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Command for handling Derby server.
 *
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class DerbyCommand extends AbstractCommand {
  static final FileArgument ARG_HOME = new FileArgument("derbyhome", "home directory for derby");
  static final Argument ARG_COMMAND = new StringArgument("command","start/stop command for derby");
  static final IntegerArgument ARG_PORT = new IntegerArgument("port", "jdbc port");

  private static Parameter PARAM_HOME = new Parameter(ARG_HOME, Parameter.OPTIONAL);
  private static Parameter PARAM_PORT = new Parameter(ARG_PORT, Parameter.OPTIONAL);

  public static Help.Info HELP_INFO = new Help.Info("derby", "start or stop the derby db server on a given port (default 1527)",
      new Parameter(ARG_COMMAND, Parameter.MANDATORY),
      PARAM_HOME,
      PARAM_PORT);

  public static void main(String[] args) throws Exception {
    new DerbyCommand().execute(args);
  }

  final void find(long fixedDate)
  {
    long d0;
    int  d1, d2, d3, d4;
    int  n400, n100, n4, n1;
    int  year;

    if (fixedDate > 0) {
    d0 = fixedDate - 1;

        n400 = (int)(d0 / 146097);

        d1 = (int)(d0 % 146097);
        n100 = d1 / 36524;
        d2 = d1 % 36524;
        n4 = d2 / 1461;
        d3 = d2 % 1461;
        n1 = d3 / 365;
        d4 = (d3 % 365) + 1;

      System.out.println(n400);
    }
  }

  public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
    ParsedArguments arguments = HELP_INFO.parse(commandLine);
    File home_dir = ARG_HOME.getFile(arguments);
    String command = ARG_COMMAND.getValue(arguments);

    int port;
    if (PARAM_PORT.isSet(arguments)) {
      port = ARG_PORT.getInteger(arguments);
    }

    NetworkServerControlImpl server = new NetworkServerControlImpl();

    try {
      int server_command = server.parseArgs( new String[]{command} );
      PrintWriter printWriter = new PrintWriter(out);
      server.setLogWriter(printWriter);
      server.start(printWriter);
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

//    server.executeWork(server_command);

//    NetworkServerControl.main(new String[]{command});
  }
}