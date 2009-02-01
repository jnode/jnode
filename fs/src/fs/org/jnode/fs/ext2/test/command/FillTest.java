/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.fs.ext2.test.command;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Sorry, this is not a proper JNode command...
 * @author Andras Nagy
 *
 */
public class FillTest {
    public FillTest(String fname, int kilos) throws FileNotFoundException, IOException {
        byte[] bbuf = new byte[1024];
        for (int i = 0; i < 1024; i++)
            bbuf[i] = (byte) (i % 8 + 65);

        FileOutputStream fos = new FileOutputStream(fname, false);
        int written = 0;
        while (written < kilos) {
            if (written % 10 == 0)
                System.out.print(".");
            fos.write(bbuf, 0, 1024);
            written++;
            if ((written % 100) == 0)
                System.out.println(written + " KB");
        }

        fos.close();
    }

    public static void main(String args[]) {
        String fname = null;
        int kilos = 0;
        if (args.length >= 2) {
            fname = args[0];
            kilos = Integer.valueOf(args[1]).intValue();
        } else {
            System.out.println("2 args: [FILENAME] [MEGABYTES TO WRITE]");
        }

        try {
            new FillTest(fname, kilos * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
