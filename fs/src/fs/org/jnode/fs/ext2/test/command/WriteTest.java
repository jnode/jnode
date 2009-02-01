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
import java.io.FileWriter;
import java.io.IOException;

/**
 * Sorry, this is not a proper JNode command...
 * @author Andras Nagy
 */
public class WriteTest {
    public WriteTest(String fname) throws FileNotFoundException, IOException {
        byte[] bbuf = new byte[20];
        for (byte i = 0; i < 20; i++)
            bbuf[i] = (byte) (i + 65);
        FileOutputStream fos = new FileOutputStream(fname, false);
        fos.write(bbuf);
        fos.close();
    }

    public WriteTest(String fname, String text) throws FileNotFoundException, IOException {
        FileWriter writer = new FileWriter(fname);
        writer.write(text.toCharArray());
        writer.close();
    }

    public static void main(String args[]) {
        String fname;
        if (args.length > 0)
            fname = args[0];
        else {
            System.out.println("writeTest filename [some_text]");
            return;
        }

        try {
            if (args.length > 1)
                new WriteTest(fname, args[1]);
            else
                new WriteTest(fname);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
