/*
 * $Id$
 */
package org.jnode.net.command;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.NetworkLayer;
import org.jnode.net.NetworkLayerManager;
import org.jnode.net.TransportLayer;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.CommandLine;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;
import org.jnode.shell.help.*;

/**
 * @author epr
 */
public class NetstatCommand {

	public static Help.Info HELP_INFO = new Help.Info(
		"netstat",
		"Print the statistics of all network devices"
	);

	public static void main(String[] args)
	throws Exception {
		new NetstatCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(
		CommandLine cmdLine,
		InputStream in,
		PrintStream out,
		PrintStream err)
	throws Exception {
		showStats(out);
	}

	private void showStats(PrintStream out)
	throws NetworkException {
		final NetworkLayerManager nlm = NetUtils.getNLM();

		for (Iterator i = nlm.getNetworkLayers().iterator(); i.hasNext(); ) {
			final NetworkLayer nl = (NetworkLayer)i.next();
			showStats(out, nl, 80);
		}
	}

	private void showStats(PrintStream out, NetworkLayer nl, int maxWidth)
	throws NetworkException {
		out.println(nl.getName() + ": ID " + nl.getProtocolID());
		final String prefix = "    ";
		out.print(prefix);
		showStats(out, nl.getStatistics(), maxWidth - prefix.length(), prefix);
		for (Iterator i = nl.getTransportLayers().iterator(); i.hasNext(); ) {
			final TransportLayer tl = (TransportLayer)i.next();
			out.println(prefix + tl.getName() + ": ID " + tl.getProtocolID());
			final String prefix2 = prefix + prefix;
			out.print(prefix2);
			showStats(out, tl.getStatistics(), maxWidth - prefix2.length(), prefix2);
			//out.println();
		}
		out.println();
	}

	private void showStats(PrintStream out, Statistics stat, int maxWidth, String prefix)
	throws NetworkException {
		final Statistic[] list = stat.getStatistics();
		if (list.length == 0) {
			out.print("none");
		} else {
			int width = 0;
			for (int i = 0; i < list.length; i++) {
				final Statistic st = list[i];
				String msg = st.getName() + " " + st.getValue();
				if (i+1 < list.length) {
					msg = msg + ", ";
				}
				if (width + msg.length() > maxWidth) {
					out.println();
					out.print(prefix);
					width = 0;
				}
				out.print(msg);
				width += msg.length();
			}
		}
		out.println();
	}

}
