/*
 * $Id$
 */
package org.jnode.shell.command.plugin;

import org.jnode.shell.help.Help;
import org.jnode.vm.VmSystem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RebootCommand {

    public static Help.Info HELP_INFO = new Help.Info("reboot",
            "Stop all services and devices and reset the computer");

    public static void main(String[] args) throws Exception {
        VmSystem.halt(true);
    }
}