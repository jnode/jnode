/*
 * $Id$
 */
package org.jnode.shell.command.plugin;

import org.jnode.shell.help.Help;
import org.jnode.vm.VmSystem;

/**
 * Halts the system
 * 
 * @author epr
 */
public class HaltCommand {

    public static Help.Info HELP_INFO = new Help.Info("halt",
            "Stop all services and devices, so the computer can be turned off");

    public static void main(String[] args) throws Exception {
        VmSystem.halt(false);
    }
}