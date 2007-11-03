package org.jnode.shell;

import org.jnode.vm.VmExit;

public abstract class AbstractCommand implements Command {

	@SuppressWarnings("deprecation")
	public final void execute(String[] args) throws Exception {
		execute(new CommandLine(args), System.in, System.out, System.err);
	}


	/**
	 * Exit this command with the given return code.
	 * @param rc
	 */
	protected void exit(int rc) {
		throw new VmExit(rc);
	}
}
