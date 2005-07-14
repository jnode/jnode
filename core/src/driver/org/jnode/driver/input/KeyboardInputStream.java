/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
	private final Queue<KeyboardEvent> queue = new Queue<KeyboardEvent>();
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
	    return 0;      
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
