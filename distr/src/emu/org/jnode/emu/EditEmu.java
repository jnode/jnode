package org.jnode.emu;

import java.io.File;
import org.jnode.apps.editor.TextEditor;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.textscreen.TextScreenConsole;

/**
 * @author Levente S\u00e1ntha
 */
public class EditEmu extends Emu {
    
    public EditEmu(File root) throws EmuException {
        super(root);
    }
    
    public static void main(String[] argv) throws Exception {
        if (argv.length == 0 || argv[0].startsWith("-")) {
            System.err.println("Usage: editEmu <file> [<jnode-home>]");
            return;
        }

        new EditEmu(argv.length > 1 ? new File(argv[1]) : null).run(new File(argv[0]));
    }
    
    private void run(File file) throws Exception {
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        final TextScreenConsole console = cm.createConsole(
            null,
            (ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.NO_SYSTEM_OUT_ERR |
                ConsoleManager.CreateOptions.NO_LINE_EDITTING));

        TextEditor te = new TextEditor(console);
        te.loadFile(file);
    }
}
