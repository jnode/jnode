/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
	private boolean echo = true;

	/**
	 * Create a new instance
	 * @param api the keyboard API
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

    /*
    Doesn't block after the first blocking read.
    public int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || b.length - off < len)
            throw new IndexOutOfBoundsException();

        long time = System.currentTimeMillis();

        int i = 0;
        int ch = read(time);

        for(;;) {
            b[off + i ++] = (byte) ch;

            if(i < len && queue.size() > 0)
                ch = read(time);
            else
                break;
        }

        return i;
    }
    */

    public int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || len < 0 || b.length - off < len)
            throw new IndexOutOfBoundsException();

        long time = System.currentTimeMillis();

        int i = 0;
        int ch = read(time);

        for(;;) {
            b[off + i ++] = (byte) ch;

            if(i < len && ch != '\n')
                ch = read(time);
            else
                break;
        }

        return i;
    }

    /**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
        long time = System.currentTimeMillis();
        return read(time);
	}

    private int read(long time){
       while (true) {
			KeyboardEvent event = queue.get();
            int c = event2char(event, time);
            if(c > 0)
                return c;
        }
    }

    private int event2char(KeyboardEvent event, long time) {
        if (!event.isConsumed() && event.getTime() - time > -10000) {
            event.consume();
            char ch = event.getKeyChar();
            if (ch != 0) {
                if (echo) {
                    System.out.print(ch);
                }
                return ch;
            }
        }
        return -1;
    }

    /**
	 * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
	 */
	public void keyPressed(KeyboardEvent event) {
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
