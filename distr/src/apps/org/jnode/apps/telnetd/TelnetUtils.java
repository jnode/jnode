package org.jnode.apps.telnetd;

import java.io.IOException;

import net.wimpi.telnetd.io.TerminalIO;

public class TelnetUtils {

	public static void write(TerminalIO terminalIO, byte[] b, int off, int len) throws IOException {
		int offset = off;
		for(int i = 0 ; i < len ; i++)
		{
			terminalIO.getTelnetIO().write(b[offset++]);
		}
		if(terminalIO.isAutoflushing())
		{
			terminalIO.flush();
		}
	}

	public static void write(TerminalIO terminalIO, byte[] b) throws IOException {
		terminalIO.getTelnetIO().write(b);
		if(terminalIO.isAutoflushing())
		{
			terminalIO.flush();
		}
	}

	private TelnetUtils()
	{
	}
}
