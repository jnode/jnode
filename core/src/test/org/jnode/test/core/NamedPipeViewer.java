/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

package org.jnode.test.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class NamedPipeViewer {

    public static void main(String[] args) throws IOException {
        File pipeFile = new File("\\\\.\\pipe\\jnode-com1");
        final RandomAccessFile raf = new RandomAccessFile(pipeFile, "rw");

        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int ch;
                try {
                    while ((ch = raf.read()) >= 0) {
                        System.out.print((char) ch);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readThread.start();

        // Write to kdb
        int ch;
        System.out.println("Started. Press q to exit");
        while ((ch = System.in.read()) >= 0) {
            System.out.println("read " + (char) ch);
            if (ch == 'q') {
                break;
            }
            raf.write(ch);
        }

        System.out.println("Closing...");
        readThread.interrupt();
        raf.close();
    }

}
