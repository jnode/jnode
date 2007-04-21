package org.jnode.emu;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.textscreen.TextScreenConsole;
import org.jnode.apps.editor.TextEditor;

import java.io.File;

/**
 * @author Levente S\u00e1ntha
 */
public class EditEmu extends Emu {
    public static void main(String[] argv) throws Exception{
        initEnv();


        if(argv.length == 0){
            System.out.println("No file specified");
            return;
        }

        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        final TextScreenConsole console = cm.createConsole(null,
                ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR_IN);

        TextEditor te = new TextEditor(console);
        File f = new File(argv[0]);
        te.loadFile(f);
    }
}
