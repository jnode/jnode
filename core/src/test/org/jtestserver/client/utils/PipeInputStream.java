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

public class PipeInputStream {
    private final InputStream input;
    private final Logger logger;
    private final Level level;     
    private final PipeThread pipeThread;

    public PipeInputStream(final InputStream input, final Logger logger, Level level) {
        this(input, logger, level, null);
    }
    
    public PipeInputStream(final InputStream input, final Logger logger, Level level, Listener listener) {
        this.level = level;
        this.input = input;
        this.logger = logger;
        this.pipeThread = new PipeThread(listener);
    }
    
    public void start() {
        pipeThread.start();
    }
    
    public static interface Listener {
        void lineReceived(String line);
    }
    
    private class PipeThread extends Thread {
        private Listener listener;
        
        private PipeThread(Listener listener) {
            this.listener = listener;
        }
        
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
