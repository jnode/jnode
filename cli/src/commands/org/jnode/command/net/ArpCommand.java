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
 
package org.jnode.command.net;

import java.io.PrintWriter;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.arp.ARPCacheEntry;
import org.jnode.net.arp.ARPNetworkLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * @author epr
 */
public class ArpCommand extends AbstractCommand {

    @SuppressWarnings("unused")
    private static final String help_clear = "if set, clear the ARP cache";
    private static final String help_super = "print or clear the ARP cache";
    private static final String str_cleared = "Cleared the ARP cache";
    
    private final FlagArgument argClear;

    public ArpCommand() {
        super(help_super);
        argClear = new FlagArgument("clear", Argument.OPTIONAL, help_super);
        registerArguments(argClear);
    }

    /**
     * Execute this command
     */
    public static void main(String[] args) throws Exception {
        new ArpCommand().execute(args);
    }

    public void execute() throws NoSuchProtocolException, NetworkException {
        ARPNetworkLayer arp = (ARPNetworkLayer) 
                NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_ARP);
        PrintWriter out = getOutput().getPrintWriter();
        if (argClear.isSet()) {
            arp.getCache().clear();
            out.println(str_cleared);
        } else {
            for (ARPCacheEntry entry : arp.getCache().entries()) {
                out.println(entry);
            }
        }
    }
}
