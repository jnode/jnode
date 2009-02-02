/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.debugger;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.SystemTriggerListener;
import org.jnode.system.event.SystemEvent;
import org.jnode.vm.VmSystem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Debugger implements SystemTriggerListener, KeyboardListener,
    PrivilegedAction<Void> {

    private boolean enabled;

    private KeyboardEvent event;

    private DebugState state;

    /**
     * @see org.jnode.driver.input.SystemTriggerListener#systemTrigger(org.jnode.system.event.SystemEvent)
     */
    public void systemTrigger(SystemEvent event) {
        event.consume();
        if (!enabled) {
            // Entering debugger
            reset();
            setPreferredListener();
            VmSystem.getOut().println("[Debugger ('h' for help)]");
        } else {
            // Exiting debugger
            System.out.println(); // Trigger a screen update
        }
        enabled = !enabled;
    }

    /**
     * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyPressed(KeyboardEvent event) {
        if (enabled) {
            this.event = event;
            AccessController.doPrivileged(this);
            event.consume();
        }
    }

    /**
     * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyReleased(KeyboardEvent event) {
        if (enabled) {
            // Do nothing
            event.consume();
        }
    }

    /**
     * Perform the actual debugger actions.
     *
     * @see java.security.PrivilegedAction#run()
     */
    public Void run() {
        final PrintStream out = VmSystem.getOut();
        DebugState st = this.state;

        while ((st != null) && (!event.isConsumed())) {
            final DebugState newState = st.process(event);
            if (event.isConsumed()) {
                newState.print(out);
                this.state = newState;
                out.println();
            } else {
                st = st.getParent();
            }
        }

        if (!event.isConsumed()) {
            switch (event.getKeyChar()) {
                case 'p':
                    state.print(out);
                    out.println();
                    break;
                case '.':
                    out.println("State trace:");
                    out.println(state.getStateTrace());
                    out.println();
                    break;
                case 'h':
                case '?':
                case 'H':
                    help(out);
            }
        }
        return null;
    }

    private void help(PrintStream out) {
        out.println("Usage:");
        final TreeMap<String, String> map = new TreeMap<String, String>();
        map.put(".", "Print state trace");
        map.put("p", "Print current state");
        map.put("h", "Print usage information");
        state.fillHelp(map);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.println(entry.getKey() + " - " + entry.getValue());
        }
        out.println();
    }

    private void setPreferredListener() {
        final KeyboardListener l = this;
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                try {
                    final Collection<Device> devs = DeviceUtils
                        .getDevicesByAPI(KeyboardAPI.class);
                    for (Device dev : devs) {
                        final KeyboardAPI api = dev.getAPI(KeyboardAPI.class);
                        api.setPreferredListener(l);
                    }
                } catch (ApiNotFoundException ex) {
                    // Ignore
                }
                return null;
            }
        });
    }

    private void reset() {
        state = new RootState();
    }
}
