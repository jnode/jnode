package org.jnode.emu;

import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.shell.CommandShell;

/**
 * @author Levente S\u00e1ntha
 */
public class ShellEmu extends Emu {

    public static void main(String[] argv) throws Exception {
        initEnv();
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        new Thread(new CommandShell(cm.createConsole(
        		"Console 1",
                (ConsoleManager.CreateOptions.TEXT | 
                		ConsoleManager.CreateOptions.SCROLLABLE)))).start();
    }
}
