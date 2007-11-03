/**
 * 
 */
package org.jnode.shell;

public abstract class CommandRunner implements Runnable {
	private final CommandShell shell;

	/**
	 * @param invoker
	 */
	CommandRunner(CommandShell shell) {
		this.shell = shell;
	}

	private int rc;
	
	public int getRC() {
		return rc;
	}

	void setRC(int rc) {
		this.rc = rc;
	}

	boolean isDebugEnabled() {
		return this.shell.isDebugEnabled();
	}
	
	void stackTrace(Throwable ex) {
		if (ex != null && isDebugEnabled()) {
			ex.printStackTrace(this.shell.getConsole().getErr());
		}
	}

}