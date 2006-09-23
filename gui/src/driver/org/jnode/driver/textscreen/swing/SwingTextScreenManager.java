package org.jnode.driver.textscreen.swing;

import org.jnode.driver.textscreen.TextScreenManager;

/**
 * @author Levente S\u00e1ntha
 */
public final class SwingTextScreenManager implements TextScreenManager {
    private SwingPcTextScreen systemScreen;

    /**
     * @see org.jnode.driver.textscreen.TextScreenManager#getSystemScreen()
     */
    public SwingPcTextScreen getSystemScreen() {
        if(systemScreen == null){
            systemScreen = new SwingPcTextScreen();
        }
        return systemScreen;
    }

}
