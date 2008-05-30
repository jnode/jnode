/* SingleTestHarness.java -- Runs one test given on the command line
 Copyright (C) 2005 Mark J. Wielaard
 This file is part of Mauve.

 Mauve is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 Mauve is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Mauve; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.
 */

package org.jnode.test.mauve;

import gnu.testlet.ResourceNotFoundException;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class SingleTestHarness extends TestHarness {
    private int count;

    private String className;
    private boolean verbose = false;

    private String last_check;

    public SingleTestHarness(Testlet t) {
        className = t.getClass().getName();
    }

    public void check(boolean result) {
        String message = (result ? "PASS" : "FAIL") + ": " + className
            + ((last_check == null) ? "" : (": " + last_check))
            + " (number " + count++ + ")";
        System.out.println(message);
    }

    public Reader getResourceReader(String name)
        throws ResourceNotFoundException {
        return new BufferedReader(
            new InputStreamReader(getResourceStream(name)));
    }

    public InputStream getResourceStream(String name)
        throws ResourceNotFoundException {
        // The following code assumes File.separator is a single character.
        if (File.separator.length() > 1)
            throw new Error("File.separator length is greater than 1");
        String realName = name.replace('#', File.separator.charAt(0));
        try {
            return new FileInputStream(getSourceDirectory() + File.separator
                + realName);
        } catch (FileNotFoundException ex) {
            throw new ResourceNotFoundException(ex.getLocalizedMessage() + ": "
                + getSourceDirectory() + File.separator + realName);
        }
    }

    public String getSourceDirectory() {
        return ".";
    }

    /**
     * Provide a directory name for writing temporary files.
     *
     * @return The temporary directory name.
     */

    public String getTempDirectory() {
        return ".";
    }

    public File getResourceFile(String name) throws ResourceNotFoundException {
        // The following code assumes File.separator is a single character.
        if (File.separator.length() > 1)
            throw new Error("File.separator length is greater than 1");
        String realName = name.replace('#', File.separator.charAt(0));
        File f = new File(getSourceDirectory() + File.separator + realName);
        if (!f.exists()) {
            throw new ResourceNotFoundException(
                "cannot find mauve resource file" + ": "
                    + getSourceDirectory() + File.separator + realName);
        }
        return f;
    }

    public void checkPoint(String name) {
        last_check = name;
        count = 0;
    }

    public void verbose(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    public void debug(String message) {
        debug(message, true);
    }

    public void debug(String message, boolean newline) {
        if (newline)
            System.out.println(message);
        else
            System.out.print(message);
    }

    public void debug(Throwable ex) {
        ex.printStackTrace(System.out);
    }

    public void debug(Object[] o, String desc) {
        debug("Dumping Object Array: " + desc);
        if (o == null) {
            debug("null");
            return;
        }

        for (int i = 0; i < o.length; i++) {
            if (o[i] instanceof Object[])
                debug((Object[]) o[i], desc + " element " + i);
            else
                debug("  Element " + i + ": " + o[i]);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String name = args[0];
            Class k = Thread.currentThread().getContextClassLoader().loadClass(
                name);
            Testlet t = (Testlet) k.newInstance();
            TestHarness h = new SingleTestHarness(t);
            t.test(h);
        } else {
            System.out.println("Usage: mauve-simple <test-class>");
        }
    }
}
