/*
 * $Id$
 */
package org.jnode.driver.console.textscreen;

import javax.naming.NamingException;

import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.vm.VmSystem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TextScreenConsolePlugin extends Plugin {

    private TextScreenConsoleManager mgr;

    /**
     * @param descriptor
     */
    public TextScreenConsolePlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        try {
            mgr = new TextScreenConsoleManager();
            InitialNaming.bind(ConsoleManager.NAME, mgr);
            
            // Create the first console
            final TextConsole first = (TextConsole)mgr.createConsole(null, ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.SCROLLABLE);
            mgr.focus(first);
            System.setOut(first.getOut());
            System.setErr(first.getErr());
            first.getOut().println(VmSystem.getBootLog());
        } catch (ConsoleException ex) {
            throw new PluginException(ex);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        if (mgr != null) {
            mgr.closeAll();
            InitialNaming.unbind(ConsoleManager.NAME);
            mgr = null;
        }
    }
}