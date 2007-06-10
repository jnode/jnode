/*
 * Copyright 1997-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 *   Main for the 'javap' program
 *   @version @(#)JavaP.java	1.29 07/05/05
 *   @author Robert Field
 */

package sun.tools.javap.oldjavap;

import sun.tools.java.BinaryClass;
import sun.tools.java.ClassNotFound;
import sun.tools.java.Identifier;

import sun.tools.util.ModifierFilter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.Vector;

/**
 *  JavaP - program to print information about class files
 */
public 
class JavaP {

    private JavaPEnvironment env;
    private Vector classList = new Vector();
    private PrintWriter output;
    private static boolean errorOccurred = false;
    
    private static final String progname = "javap";

    /**
     * Constructor
     */
    public
    JavaP(PrintWriter output) {
    	this.output = output;
	this.env = new JavaPEnvironment(output);
    }
    
	
    /**
    * Output an error message.
    */
    private void error(String msg) {
        errorOccurred = true;
        System.err.println(msg);
        System.err.flush();
    }

    /**
     * Print usage information
     */
    private void usage() {
        java.io.PrintStream out = System.out;
        out.println("Usage: " + progname + " <options> <classes>...");
        out.println();
        out.println("where options include:");
        out.println("   -b                        Backward compatibility with " + progname + " in JDK 1.1");
        out.println("   -c                        Disassemble the code");
        out.println("   -classpath <pathlist>     Specify where to find user class files");
        out.println("   -extdirs <dirs>           Override location of installed extensions");
        out.println("   -help                     Print this usage message");
        out.println("   -J<flag>                  Pass <flag> directly to the runtime system");
        out.println("   -l                        Print line number and local variable tables");
	out.println("   -public                   Show only public classes and members");
	out.println("   -protected                Show protected/public classes and members");
	out.println("   -package                  Show package/protected/public classes");
	out.println("                             and members (default)");
	out.println("   -private                  Show all classes and members");
        out.println("   -s                        Print internal type signatures");
        out.println("   -bootclasspath <pathlist> Override location of class files loaded");
        out.println("                             by the bootstrap class loader");
        out.println("   -verbose                  Print stack size, number of locals and args for methods");
        out.println("                             If verifying, print reasons for failure");
        out.println();
    }
    
    /**
     * Parse the command line arguments. 
     * Set flags, construct the class list and create environment.
     */
    private boolean parseArguments(String argv[]) {
	String classPathString = null;
	String sysClassPathString = null;
	String extDirsString = null;
	boolean nothingToDo = true;
	    
	for (int i = 0 ; i < argv.length ; i++) {
	    String arg = argv[i];
	    if (arg.startsWith("-")) {
		if (arg.equals("-l")) {
		    env.showLineAndLocal = true;
		} else if (arg.equals("-private") || arg.equals("-p")) {
		    env.showAccess = new ModifierFilter(ModifierFilter.ALL_ACCESS);
		} else if (arg.equals("-package")) {
		    env.showAccess = new ModifierFilter(
				    ModifierFilter.PUBLIC |
				    ModifierFilter.PROTECTED |
				    ModifierFilter.PACKAGE );
		} else if (arg.equals("-protected")) {
		    env.showAccess = new ModifierFilter(
				    ModifierFilter.PUBLIC |
				    ModifierFilter.PROTECTED );
		} else if (arg.equals("-public")) {
		    env.showAccess = new ModifierFilter(ModifierFilter.PUBLIC);
		} else if (arg.equals("-b")) {
		    env.showBackwardCompatible = true;
		} else if (arg.equals("-c")) {
		    env.showDisassembled = true;
		} else if (arg.equals("-s")) {
		    env.showInternalSigs = true;
		} else if (arg.equals("-verbose"))  {
		    env.showVerbose = true;
		} else if (arg.equals("-v")) {
		    env.showVerbose = true;
		} else if (arg.equals("-h")) {
                    error("-h is no longer available - use the 'javah' program");
                    return false;
		} else if (arg.equals("-verify")) {
		    error("-verify is no longer available - use 'java -verify'");
                    return false;
		} else if (arg.equals("-verify-verbose")) {
		    error("-verify is no longer available - use 'java -verify'");
                    return false;
		} else if (arg.equals("-help")) {
                    usage();
                    return false;
		} else if (arg.equals("-classpath")) {
		    if ((i + 1) < argv.length) {
			classPathString = argv[++i];
		    } else {
			error("-classpath requires argument");
			usage();
			return false;
		    }
		} else if (arg.equals("-bootclasspath")) {
		    if ((i + 1) < argv.length) {
			sysClassPathString = argv[++i];
		    } else {
			error("-bootclasspath requires argument");
			usage();
			return false;
		    }
		} else if (arg.equals("-extdirs")) {
		    if ((i + 1) < argv.length) {
			extDirsString = argv[++i];
		    } else {
			error("-extdirs requires argument");
			usage();
			return false;
		    }
		} else {
		    error("invalid flag: " + arg);
		    usage();
		    return false;
		}
	    } else {
		classList.addElement(arg);
		nothingToDo = false;
	    }
	}
	if (nothingToDo) {
	    usage();
	    return false;
	};
	env.setPath(classPathString, sysClassPathString, extDirsString);
	return true;
    }

    /**
     * Display results
     */
    private void displayResults() {
	for (int i = 0; i < classList.size() ; i++ ) {
		String className = (String)classList.elementAt(i);
		Identifier id = Identifier.lookup(className);
		id = env.resolvePackageQualifiedName(id);
		if (!env.classExists(id)) {
		    error("Class '" + className + "' not found");
		    continue;
		}
		try {
		    BinaryClass cdef = (BinaryClass)env.getClassDefinition(id);
		    cdef.loadNested(env);	// load inner classes
		    JavaPClassPrinter printer = new JavaPClassPrinter(cdef, env);
		    printer.print();		// actual do display
		} catch (ClassNotFound exc) {
		    error("Class '" + className + "' not found");
		} catch (IOException exc) {
                    error("I/O Exception - " + exc.getMessage());
		} catch (IllegalArgumentException exc) {
                    error(exc.getMessage());
		}
	}
    }
    
    /**
     * Process the arguments and perform the desired action
     */
    private void perform(String argv[]) {
	if (parseArguments(argv)) {
	    displayResults();
	}
    }
    
    /**
     * Main of JavaP
     */
    public static void main(String argv[]) {
        entry(argv);
        if (errorOccurred) {
            System.exit(1);
        }
    }

    /**
     * Entry point for tool if you don't want System.exit() called.
     */
    public static void entry(String argv[]) {
	PrintWriter output = new PrintWriter( new OutputStreamWriter(System.out));
	try {
	    JavaP jp = new JavaP(output);
	    jp.perform(argv);
	} finally {
	    output.close();
	}
    }
    
} // end JavaP
