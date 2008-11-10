/* SingleTestHarness.java -- Runs one test given on the command line
 Copyright (C) 2005 Mark J. Wielaard
 This file is part of Mauve.
 
 Modified by Levente S\u00e1ntha (lsantha@jnode.org)
 Modified by Ewout Prangsma (epr@jnode.org)

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
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginClassLoader;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;

public class PluginTestHarness extends TestHarness {
    private int count;

    private final String className;

    private final boolean verbose;

    private String last_check;

    private int passed = 0;

    private int failed = 0;

    public PluginTestHarness(Testlet t, boolean verbose) {
        className = t.getClass().getName();
        this.verbose = verbose;
    }

    public void check(boolean result) {
        if (!result || verbose) {
            String message = (result ? "PASS" : "FAIL") + ": " + className
                + ((last_check == null) ? "" : (": " + last_check))
                + " (number " + count + ")";
            System.out.println(message);
        }
        if (result) {
            passed++;
        } else {
            failed++;
        }
        count++;
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
        // System.out.println("# " + name);
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

    private static void usage() {
        System.out.println("Usage: mauve-plugin <test-plugin> [filter]");
    }

    public static void main(String[] args) throws Exception {

        // Parse arguments
        String name = null;
        String filter = null;
        boolean stopOnFail = true;
        boolean verbose = false;
        boolean quiet = false;

        int argIndex = 0;
        for (; argIndex < args.length; argIndex++) {
            String arg = args[argIndex];
            if ((arg.charAt(0) == '/') || (arg.charAt(0) == '-')) {
                arg = arg.substring(1);
                if (arg.equals("c") || arg.equals("continue")) {
                    stopOnFail = false;
                } else if (arg.equals("v") || arg.equals("verbose")) {
                    verbose = true;
                } else if (arg.equals("q") || arg.equals("quiet")) {
                    quiet = true;
                } else {
                    System.out.println("Unknown argument " + args[argIndex]);
                }
            } else {
                break;
            }
        }
        name = (argIndex < args.length) ? args[argIndex++] : null;
        filter = (argIndex < args.length) ? args[argIndex++] : "";

        if (name == null) {
            usage();
        } else {
            PluginManager pm = InitialNaming.lookup(PluginManager.NAME);
            PluginDescriptor descr = pm.getRegistry().getPluginDescriptor(name);
            if (descr == null) {
                System.out.println("Plugin " + name + " not found");
            } else {
                int passed = 0;
                int failed = 0;
                ClassLoader cl = descr.getPluginClassLoader();
                if (cl instanceof PluginClassLoader) {
                    PluginClassLoader pcl = (PluginClassLoader) cl;
                    for (String className : pcl.getClassNames()) {
                        if (className.startsWith(filter)) {
                            try {
                                Class k = pcl.loadClass(className);
                                if (Testlet.class.isAssignableFrom(k)) {
                                    if (!quiet) {
                                        System.out.println("Running "
                                            + className);
                                    }
                                    Testlet t = (Testlet) k.newInstance();
                                    PluginTestHarness h = new PluginTestHarness(
                                        t, verbose);
                                    t.test(h);
                                    passed += h.passed;
                                    failed += h.failed;
                                    if ((h.failed > 0) && stopOnFail) {
                                        break;
                                    }
                                }
                            } catch (Throwable ex) {
                                System.out.println("Exception in " + className);
                                ex.printStackTrace();
                            }
                        }
                    }
                    System.out.println("Tests passed: " + passed + ", failed: "
                        + failed);
                } else {
                    System.out.println("Plugin has no PluginClassLoader");
                }
            }
        }
    }
}
