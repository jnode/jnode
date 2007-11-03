package org.jnode.shell;

/**
 * Interface for objects that listen for CommandThread exit. 
 * 
 * @author crawley@jnode.org
 */
public interface ThreadExitListener {

	void notifyThreadExitted(CommandThread thread);

}
