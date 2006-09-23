package org.jnode.driver.console.swing;

import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.textscreen.swing.SwingTextScreenManager;
import org.jnode.driver.DeviceManager;

/**
 * @author Levente S\u00e1ntha
 */
public class SwingTextScreenConsoleManager extends TextScreenConsoleManager {
    private SwingTextScreenManager textScreenManager;

    public SwingTextScreenConsoleManager() throws ConsoleException {

    }

    protected void openInput(DeviceManager devMan) {
        initializeKeyboard(getTextScreenManager().getSystemScreen().getKeyboardDevice());
    }

    protected SwingTextScreenManager getTextScreenManager() {
        if(textScreenManager == null){
            this.textScreenManager = new SwingTextScreenManager();
        }
        return textScreenManager;
    }
}
