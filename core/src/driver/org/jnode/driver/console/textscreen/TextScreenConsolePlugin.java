/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.driver.console.textscreen;

import java.io.PrintStream;
import javax.naming.NamingException;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.util.WriterOutputStream;
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
            final TextConsole first = (TextConsole) mgr.createConsole(
                null,
                (ConsoleManager.CreateOptions.TEXT |
                    ConsoleManager.CreateOptions.SCROLLABLE));
            mgr.focus(first);
            System.setOut(new PrintStream(new WriterOutputStream(first.getOut()), true));
            System.setErr(new PrintStream(new WriterOutputStream(first.getErr()), true));
            System.out.println(VmSystem.getBootLog());
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
