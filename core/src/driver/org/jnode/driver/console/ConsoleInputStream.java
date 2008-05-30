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

    private final Queue<KeyboardEvent> queue = new Queue<KeyboardEvent>();
    private boolean echo = false;

    public ConsoleInputStream(Console console) {
        console.addKeyboardListener(this);
    }

    /**
     * @return Available bytes
     * @throws IOException
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        return queue.size();
    }

    /**
     * @return int
     * @throws IOException
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
