/**
 * $Id$
 */
package org.jnode.driver.input;

import java.io.IOException;
import java.io.InputStream;

import org.jnode.util.Queue;

/**
 * @author epr
 */
public class KeyboardInputStream extends InputStream implements KeyboardListener {

	private final KeyboardAPI api;
	/** The queue of keyboard events */
	private final Queue queue = new Queue();
	/** Should we echo keys on the System.out? */
	private boolean echo = false;

	/**
	 * Create a new instance
	 * @param api
	 */
	public KeyboardInputStream(KeyboardAPI api) {
		this.api = api;
		api.addKeyboardListener(this);
	}

	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return queue.size();
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		while (true) {
			KeyboardEvent event = (KeyboardEvent) queue.get();
			if (!event.isConsumed()) {
				event.consume();
				char ch = event.getKeyChar();
				if (ch != 0) {
					if (echo) {
						System.out.print(ch);
					}
					return ch;
				}
			}
		}
	}

	/**
	 * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyPressed(KeyboardEvent event) {
		//log.debug("got event(" + event + ")");
		queue.add(event);
	}

	/**
	 * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyReleased(KeyboardEvent event) {
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		api.removeKeyboardListener(this);
		super.close();
	}

}
