package org.jnode.emu;

import java.io.File;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.shell.CommandShell;

/**
 * @author Levente S\u00e1ntha
 */
public class ShellEmu extends Emu {

    public static void main(String[] argv) throws Exception {
        if (argv.length > 0 && argv[0].startsWith("-")) {
            System.err.println("Usage: shellemu [<jnode-home>]");
            return;
        }
        initEnv(argv.length > 0 ? new File(argv[0]) : null);
        TextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        new Thread(new CommandShell(cm.createConsole(
            "Console 1",
            (ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.SCROLLABLE)))).start();
    }
}
