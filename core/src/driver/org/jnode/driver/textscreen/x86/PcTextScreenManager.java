/*
 * $Id$
 */
package org.jnode.driver.textscreen.x86;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.TextScreenManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PcTextScreenManager implements TextScreenManager {

    /**
     * @see org.jnode.driver.textscreen.TextScreenManager#getSystemScreen()
     */
    public TextScreen getSystemScreen() {
        return PcTextScreen.getInstance();
    }
}
