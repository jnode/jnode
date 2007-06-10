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

package sun.tools.javap.oldjavap;

import java.util.Hashtable;
import java.util.StringTokenizer;

import sun.tools.util.ModifierFilter;

import sun.tools.java.BinaryClass;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.ClassFile;
import sun.tools.java.ClassPath;
import sun.tools.java.Constants;
import sun.tools.java.Environment;
import sun.tools.java.Identifier;
import sun.tools.java.Package;
import sun.tools.java.Type;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;

/**
 *   Main environment of JavaP.
 *   Repository for flag settings.
 *   Inherits loading capability. 
 *   @version 1.30, 07/05/05
 *   @author Robert Field
 */
class JavaPEnvironment extends Environment implements Constants {

    /**
     * Flags for Compiler classes
     */
    int flags = 0;
    
    /**
     * The path we use for finding source files (always empty).
     */
    ClassPath sourcePath = new ClassPath("");
    
    /**
     * The path we use for finding class (binary) files.
     */
    ClassPath binaryPath;
    
    /**
     * A hashtable of resource contexts.
     */
    Hashtable packages = new Hashtable(31);

    /**
     * The classes, this is only a cache, not a
     * complete list.
     */
    Hashtable classes = new Hashtable(351);

    // JavaP flag settings
    boolean showLineAndLocal = false;
    ModifierFilter showAccess = new ModifierFilter(
	ModifierFilter.PUBLIC | ModifierFilter.PROTECTED | ModifierFilter.PACKAGE);
    boolean showDisassembled = false;
    boolean showBackwardCompatible = false;
    boolean showVerbose = false;
    boolean showInternalSigs = false;

    /**
     * The destination for output
     */
    PrintWriter output;

	
    /**
     * Constructor
     */
    JavaPEnvironment(PrintWriter printWriter) {
	super();
        this.output = printWriter;
    }
    
    /**
     * Default flags for loadFile.  Load byte codes.
     */
    protected int loadFileFlags() { 
	return ATT_CODE|ATT_ALLCLASSES; 
    }

    /**
     * Return flags
     */
    public int getFlags() {
	return flags;
    }


    /**
     * Check if a class exists
     * Applies only to package members (non-nested classes).
     */
    public boolean classExists(Identifier nm) {
	if (nm.isInner()) {
	    nm = nm.getTopName();	// just in case
	}
	Type t = Type.tClass(nm);
	try {
	    ClassDeclaration c = (ClassDeclaration)classes.get(t);
            if (c == null) {
                Package pkg = getPackage(nm.getQualifier());
                Identifier cn = nm.getName();
                ClassFile cf = pkg.getBinaryFile(cn);
                return cf != null;
            } else {
                return c.getName().equals(nm);
            }
	} catch (IOException e) {
	    return false;
	}
    }

    /**
     * Get a class, given the fully qualified class name
     */
    public ClassDeclaration getClassDeclaration(Identifier nm) {
	return getClassDeclaration(Type.tClass(nm));
    }

    /**
     * Get a class, given a type
     */
    public ClassDeclaration getClassDeclaration(Type t) {
	ClassDeclaration c = (ClassDeclaration)classes.get(t);
	if (c == null) {
	    classes.put(t, c = new ClassDeclaration(t.getClassName()));
	}
	return c;
    }

    /**
     * Get the package path for a package
     */
    public Package getPackage(Identifier pkg) throws IOException {
	Package p = (Package)packages.get(pkg);
	if (p == null) {
	    packages.put(pkg, p = new Package(sourcePath, binaryPath, pkg));
	}
	return p;
    }
    
    /**
     * Load a binary file
     */
    BinaryClass loadFile(ClassFile file) throws IOException {
	long tm = System.currentTimeMillis();
	InputStream input = file.getInputStream();
	BinaryClass c = null;

	try {
	    DataInputStream is = 
		new DataInputStream(new BufferedInputStream(input));
	    c = BinaryClass.load(new Environment(this, file), is, 
				 loadFileFlags());
	} catch (ClassFormatError e) {
	    throw new IllegalArgumentException("Error: bad class format" + file.getPath() + e.getMessage());
	} catch (Exception e) {
	    e.printStackTrace();
	}

	input.close();

	return c;
    }
    
    /**
     * Load the definition of a class
     */
    public void loadDefinition(ClassDeclaration c) {
	switch (c.getStatus()) {
	  case CS_UNDEFINED: {
	    Identifier nm = c.getName();
	    Package pkg;
	    try {
		pkg = getPackage(nm.getQualifier());
	    } catch (IOException e) {
	    	throw new IllegalArgumentException(
                    "Error: I/O Exception - " + e.getMessage());
	    }
	    ClassFile binfile = pkg.getBinaryFile(nm.getName());
	    if (binfile == null) {
		throw new IllegalArgumentException(
                    "Error: No binary file '" + nm.getName() + "'");
	    }

	    BinaryClass bc = null;
	    try {
		bc = loadFile(binfile);
	    } catch (IOException e) {
	    	throw new IllegalArgumentException(
                    "Error: I/O Exception - " + e.getMessage() + 
                    " in " + binfile);
	    }
	    if (bc == null) {
		throw new IllegalArgumentException(
                    "Error: Class not found in '" + binfile + "'");
	    }
	    if (!bc.getName().equals(nm)) {
		throw new IllegalArgumentException(
                    "Error: Binary file '" + nm.getName() + 
                    "'  contains " + bc.getName());
	    }

	    c.setDefinition(bc, CS_BINARY);
	    bc.loadNested(this, loadFileFlags());
	    return;
	  }
	    
	  case CS_UNDECIDED: 
	  case CS_SOURCE: 
	  default: {
	    throw new IllegalArgumentException("Error: No binary file");
	  }
	}
    }

    /**
     * Constructor the binary path from the class path strings
     */
    void setPath(String classPathString, 
                 String sysClassPathString, 
                 String extDirsString) {
        StringBuffer binaryPathBuffer = new StringBuffer();

        if (classPathString == null) {
            classPathString = System.getProperty("env.class.path");
            if (classPathString == null) {
                classPathString = ".";
            }
        }
        if (sysClassPathString == null) {
            sysClassPathString = System.getProperty("sun.boot.class.path");
            if (sysClassPathString == null) {
                sysClassPathString = "";
            }
        }
        appendPath(binaryPathBuffer, sysClassPathString);

        if (extDirsString == null) {
            extDirsString = System.getProperty("java.ext.dirs");
        }
        if (extDirsString != null) {
            StringTokenizer st = new StringTokenizer(extDirsString, 
                                                     File.pathSeparator);
            while (st.hasMoreTokens()) {
                String dirName = st.nextToken();
                File dir = new File(dirName);
                if (!dirName.endsWith(File.separator)) {
                    dirName += File.separator;
                }
                if (dir.isDirectory()) {
                    String[] files = dir.list();
                    for (int i = 0; i < files.length; ++i) {
                        String name = files[i];
                        if (name.endsWith(".jar")) {
                            appendPath(binaryPathBuffer, dirName + name);
                        }
                    }
                }
            }
        }
        
        appendPath(binaryPathBuffer, classPathString);

        binaryPath = new ClassPath(binaryPathBuffer.toString());
    }

    private static void appendPath(StringBuffer buf, String str) {
        if (str.length() > 0) {
            if (buf.length() > 0) {
                buf.append(File.pathSeparator);
            }
            buf.append(str);
        }
    }
}
