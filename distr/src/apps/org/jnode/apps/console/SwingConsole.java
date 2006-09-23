package org.jnode.apps.console;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellManager;
import org.jnode.naming.InitialNaming;

/**
 * @author Levente S\u00e1ntha
 */
public class SwingConsole {

    public static void main(String[] argv) throws Exception {
        ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        TextScreenConsoleManager manager = (TextScreenConsoleManager) sm.getCurrentShell().getConsole().getManager();
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        cm.setParent(manager);
        new Thread(new CommandShell((TextConsole) cm.createConsole(null,
                ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.SCROLLABLE))).
                start();
    }
}
