package org.jnode.apps.telnetd;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.driver.console.TextConsole;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;

public class JNodeCommandShell extends CommandShell {
	private final JNodeShell shell;

	public JNodeCommandShell(JNodeShell shell, TextConsole console, InputStream in, PrintStream out, PrintStream err)
			throws ShellException {
		super(console, in, out, err);
		this.shell = shell;
	}

	@Override
	public void exit(){
		shell.close();
		super.exit();
	}
}
