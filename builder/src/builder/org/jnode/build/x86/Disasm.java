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
 
package org.jnode.build.x86;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Disasm {

	private static final String NDISASM = (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) ? "ndisasmw" : "ndisasm";

    public static void main(String[] args) throws IllegalArgumentException,
            SecurityException, IOException, InterruptedException {
        final File f = File.createTempFile("disasm", "jnode");
        FileOutputStream os = new FileOutputStream(f);
        try {
            for (int i = 0; i < args.length; i++) {
                os.write(Integer.parseInt(args[i], 16));
            }
        } finally {
            os.close();
        }

        final String cmdLine = NDISASM + " -u " + " "
                + f.getAbsolutePath();
        exec(cmdLine);

    }
    private static void exec(String cmdLine) throws IOException,
            InterruptedException {
        Process proc = Runtime.getRuntime().exec(cmdLine);
        CopyThread stderr = new CopyThread(proc.getErrorStream(), System.out);
        CopyThread stdout = new CopyThread(proc.getInputStream(), System.out);
        stderr.start();
        stdout.start();
        proc.waitFor();
    }

    static class CopyThread extends Thread {

        private final InputStream is;

        private final OutputStream out;

        public CopyThread(InputStream is, OutputStream out) {
            this.is = is;
            this.out = out;
        }

        public void run() {
            int ch;
            try {
                while ((ch = is.read()) >= 0) {
                    out.write(ch);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
