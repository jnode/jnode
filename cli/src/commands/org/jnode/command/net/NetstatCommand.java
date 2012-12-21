/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
import org.jnode.net.NetworkLayer;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.TransportLayer;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.vm.objects.Statistic;
import org.jnode.vm.objects.Statistics;

/**
 * @author epr
 */
public class NetstatCommand extends AbstractCommand {

    private static final String help_super = "Print statistics for all network devices";
    private static final String fmt_stat = "%s: ID %s\n";
    private static final String str_none = "none";
    
    public NetstatCommand() {
        super(help_super);
    }

    public static void main(String[] args) throws Exception {
        new NetstatCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        final NetworkLayerManager nlm = NetUtils.getNLM();

        for (NetworkLayer nl : nlm.getNetworkLayers()) {
            showStats(getOutput().getPrintWriter(), nl, 80);
        }
    }
    
    private void showStats(PrintWriter out, NetworkLayer nl, int maxWidth) throws NetworkException {
        out.format(fmt_stat, nl.getName(), nl.getProtocolID());
        showStats(out, nl.getStatistics(), 4);
        for (TransportLayer tl : nl.getTransportLayers()) {
            padOutput(out, 4);
            out.format(fmt_stat, tl.getName(), tl.getProtocolID());
            showStats(out, tl.getStatistics(),8);
        }
        out.println();
    }
    
    private void showStats(PrintWriter out, Statistics stat, int padSize)
        throws NetworkException {
        final Statistic[] statistics = stat.getStatistics();
        if (statistics.length == 0) {
        	padOutput(out, padSize);
            out.print(str_none);
        } else {
        	StringBuffer buffer = new StringBuffer();
        	for(Statistic statistic : statistics){
        		buffer.append(paddedString(padSize)).append(statistic.getName()).append(' ').append(statistic.getValue()).append("\n");
        	}
        	out.print(buffer.toString());
        }
        out.println();
    }
    
    private String paddedString(int padSize) {
    	String result = "";
    	for(int i = 0; i < padSize; i++){
			result += " ";	
		}
    	return result;
    }
    
	private void padOutput(PrintWriter out, int size) {
		for(int i = 0; i < size; i++){
			out.print(" ");	
		}
	}

}
