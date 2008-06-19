package org.jnode.driver.console.textscreen;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.naming.InitialNaming;
import org.jnode.system.BootLog;
import org.jnode.system.event.FocusEvent;
import org.jnode.system.event.FocusListener;
import org.jnode.util.Queue;


/**
 * KeyInputStream maps keyboard events into a stream of characters.  Current functionality includes:
 * <ul>
 * <li>line buffering and line editing, using a text console,
 * <li>integrated input history and completion,
 * <li>CTRL-D is interpretted as a 'soft' EOF mark,KeyboardInputStream
 * <li>listens to keyboard focus events.
 * </ul>
 * 
 * Future enhancements include:
 * <ul>
 * <li>a "raw" mode in which characters and other keyboard events are delivered without line editing,
 * <li>a "no echo" mode in which line editting occurs without echoing of input characters,
 * <li>making the active characters and keycodes "soft",
 * <li>making completion and history context sensitive; e.g. when switching between a shell and 
 * an application, and
 * <li>code refactoring to support classical terminal devices and remote consoles.
 * </ul>
 * 
 * Bugs:
 * <ul>
 * <li>The current method of echoing the input is suboptimal, and is broken in the case where an 
 * application outputs a prompt string to stdout or stderr.
 * </ul>
 * 
 * @author crawley@jnode.org
 *
 */
abstract public class KeyboardHandler {
	
	/** The queue of keyboard events */
	private final Queue<KeyboardEvent> queue = new Queue<KeyboardEvent>();

	protected void postEvent(KeyboardEvent event)
	{
		queue.add(event);
	}

	/**
	 * Get the next KeyboardEvent from the internal queue (and wait if none is available).
	 *  
	 * @return
	 */
	public final KeyboardEvent getEvent()
	{
		return queue.get();		
	}
	
	abstract public void close() throws IOException;	
}
