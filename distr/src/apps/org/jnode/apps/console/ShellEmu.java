package org.jnode.apps.console;

import org.jnode.test.gui.Emu;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.shell.CommandShell;

/**
 * @author Levente S\u00e1ntha
 */
public class ShellEmu extends Emu {

    public static void main(String[] argv) throws Exception {
        initEnv();
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        new Thread(new CommandShell((TextConsole) cm.createConsole(null,
                ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.SCROLLABLE))).
                start();
    }
}
