/*
 * $Id$
 */
package org.jnode.driver.console;

import java.io.IOException;
import java.io.InputStream;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.util.Queue;

/**
 * @author epr
 */
public class ConsoleInputStream extends InputStream implements KeyboardListener {
	
	private final Queue queue = new Queue();
	private boolean echo = false;

	public ConsoleInputStream(Console console) {
		console.addKeyboardListener(this);
	}

	/**
	 * @see java.io.InputStream#available()
	 * @return Available bytes
	 * @throws IOException
	 */
	public int available() throws IOException {
		return queue.size();
	}

	/**
	 * @see java.io.InputStream#read()
	 * @return int
	 * @throws IOException
	 */
	public int read() throws IOException {
		while (true) {
			KeyboardEvent event = (KeyboardEvent)queue.get();
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
	 * @param event
	 * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyPressed(KeyboardEvent event) {
		//log.debug("got event(" + event + ")");
		queue.add(event);
	}

	/**
	 * @param event
	 * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyReleased(KeyboardEvent event) {
	}
}
