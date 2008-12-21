/*
JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.client.utils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that manages the redirection of an {@link InputStream} to a {@link java.util.logging.Logger} at a given level.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class PipeInputStream {
    /**
     * Input stream that will be redirected to a logger.
     */
    private final InputStream input;
    
    /**
     * Logger to use for the redirection of the input stream.
     */
    private final Logger logger;
    
    /**
     * Logging level to use.
     */
    private final Level level;
    
    /**
     * Thread that will get messages from the input stream and redirect them to the logger.
     */
    private final PipeThread pipeThread;

    /**
     * 
     * @param input stream to redirect
     * @param logger to which stream is redirected. 
     * @param level of logging.
     */
    public PipeInputStream(final InputStream input, final Logger logger, Level level) {
        this(input, logger, level, null);
    }

    /**
     * 
     * @param input stream to redirect
     * @param logger to which stream is redirected. 
     * @param level of logging.
     * @param listener optional listener used to do additional processing on lines received from the input stream.
     */
    public PipeInputStream(final InputStream input, final Logger logger, Level level, Listener listener) {
        this.level = level;
        this.input = input;
        this.logger = logger;
        this.pipeThread = new PipeThread(listener);
    }
    
    /**
     * Starts the redirection process.
     */
    public void start() {
        pipeThread.start();
    }
    
    /**
     * Interface to implement for doing additional processing on the received lines. 
     * @author Fabien DUMINY (fduminy@jnode.org)
     *
     */
    public static interface Listener {
        void lineReceived(String line);
    }
    
    /**
     * Thread class that is doing the actual redirection of the input stream lines.
     * @author Fabien DUMINY (fduminy@jnode.org)
     *
     */
    private class PipeThread extends Thread {
        /**
         * 
         */
        private Listener listener;
        
        /**
         * 
         * @param listener
         */
        private PipeThread(Listener listener) {
            this.listener = listener;
        }
        
        /**
         * 
         */
        public void run() {
            final InputStreamReader isr = new InputStreamReader(input);
            final BufferedReader br = new BufferedReader(isr, 100);
            String line;
            try {
                try {
                    while ((line = br.readLine()) != null) {
                        logger.log(level, line);
                        
                        if (listener != null) {
                            listener.lineReceived(line);
                        }
                    }
                } catch (EOFException e) {
                    // ignore
                }
                br.close();
            } catch (IOException e) {
                // ignore
                
            }
        }
    }
}
