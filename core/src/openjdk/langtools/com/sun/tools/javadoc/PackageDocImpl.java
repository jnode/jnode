/*
 * Copyright 1997-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.javadoc;

import com.sun.javadoc.*;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Position;

import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;


/**
 * Represents a java package.  Provides access to information
 * about the package, the package's comment and tags, and the
 * classes in the package.
 *
 * @since 1.2
 * @author Kaiyang Liu (original)
 * @author Robert Field (rewrite)
 * @author Neal Gafter (rewrite)
 * @author Scott Seligman (package-info.java)
 */

public class PackageDocImpl extends DocImpl implements PackageDoc {
    
    private static final String PACKAGE_HTML_FILE_NAME = "package.html";

    protected PackageSymbol sym;
    private JCCompilationUnit tree = null;    // for source position

    public String docPath = null;
    public String zipDocPath = null;
    public String zipDocEntry = null;
    private boolean foundDoc;	// found a doc comment in either
				// package.html or package-info.java
    
    boolean isIncluded = false;  // Set in RootDocImpl.
    public boolean setDocPath = false;  //Flag to avoid setting doc path multiple times.
    
    /**
     * Constructor
     */
    public PackageDocImpl(DocEnv env, PackageSymbol sym) {
        this(env, sym, null, null);
    }
    
    /**
     * Constructor
     */
    public PackageDocImpl(DocEnv env, PackageSymbol sym,
			  String documentation, JCTree tree) {
        super(env, documentation);
        this.sym = sym;
	this.tree = (JCCompilationUnit) tree;
	foundDoc = (documentation != null);
    }

    void setTree(JCTree tree) {
	this.tree = (JCCompilationUnit) tree;
    }

    public void setRawCommentText(String rawDocumentation) {
	super.setRawCommentText(rawDocumentation);
	checkDoc();
    }
    
    /**
     * Do lazy initialization of "documentation" string.
     */
    String documentation() {
        if (documentation != null) return documentation;
        if (zipDocPath != null) {
            try {
                ZipFile f = new ZipFile(zipDocPath);
                ZipEntry entry = f.getEntry(zipDocEntry);
                if (entry != null) {
                    InputStream s = f.getInputStream(entry);
                    return (documentation = readHTMLDocumentation(s, 
                        zipDocPath + File.separatorChar + zipDocEntry));
                }
            } catch (IOException exc) {
                documentation = "";
                env.error(null, "javadoc.File_Read_Error",
                          zipDocPath + File.separatorChar + zipDocEntry);
            }
        }
        if (docPath != null) {
            // read from file
            try {
                InputStream s = new FileInputStream(docPath);
                documentation = readHTMLDocumentation(s, docPath);
            } catch (IOException exc) {
                documentation = "";
                env.error(null, "javadoc.File_Read_Error", docPath);
            }
        } else {
            // no doc file to be had
            documentation = "";
        }
        return documentation;
    }
    
    /**
     * Cache of all classes contained in this package, including
     * member classes of those classes, and their member classes, etc.
     * Includes only those classes at the specified protection level
     * and weaker.
     */
    private List<ClassDocImpl> allClassesFiltered = null;
    
    /**
     * Cache of all classes contained in this package, including
     * member classes of those classes, and their member classes, etc.
     */
    private List<ClassDocImpl> allClasses = null;
    
    /**
     * Return a list of all classes contained in this package, including
     * member classes of those classes, and their member classes, etc.
     */
    private List<ClassDocImpl> getClasses(boolean filtered) {
        if (allClasses != null && !filtered) {
            return allClasses;
        }
        if (allClassesFiltered != null && filtered) {
            return allClassesFiltered;
        }
        ListBuffer<ClassDocImpl> classes = new ListBuffer<ClassDocImpl>();
        for (Scope.Entry e = sym.members().elems; e != null; e = e.sibling) {
            if (e.sym != null) {
                ClassSymbol s = (ClassSymbol)e.sym;
                ClassDocImpl c = env.getClassDoc(s);
                if (c != null && !c.isSynthetic())
                    c.addAllClasses(classes, filtered);
            }
        }
        if (filtered)
            return allClassesFiltered = classes.toList();
        else
            return allClasses = classes.toList();
    }
    
    /**
     * Add all included classes (including Exceptions and Errors)
     * and interfaces.
     */
    public void addAllClassesTo(ListBuffer<ClassDocImpl> list) {
        list.appendList(getClasses(true));
    }
    
    /**
     * Get all classes (including Exceptions and Errors)
     * and interfaces.
     * @since J2SE1.4.
     *
     * @return all classes and interfaces in this package, filtered to include
     * only the included classes if filter==true.
     */
    public ClassDoc[] allClasses(boolean filter) {
        List<ClassDocImpl> classes = getClasses(filter);
        return classes.toArray(new ClassDocImpl[classes.length()]);
    }
    
    /**
     * Get all included classes (including Exceptions and Errors)
     * and interfaces.  Same as allClasses(true).
     *
     * @return all included classes and interfaces in this package.
     */
    public ClassDoc[] allClasses() {
        return allClasses(true);
    }
    
    /**
     * Get ordinary classes (that is, exclude exceptions, errors,
     * enums, interfaces, and annotation types) in this package.
     *
     * @return included ordinary classes in this package.
     */
    public ClassDoc[] ordinaryClasses() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
	for (ClassDocImpl c : getClasses(true)) {
            if (c.isOrdinaryClass()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    
    /**
     * Get Exception classes in this package.
     *
     * @return included Exceptions in this package.
     */
    public ClassDoc[] exceptions() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
	for (ClassDocImpl c : getClasses(true)) {
            if (c.isException()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    
    /**
     * Get Error classes in this package.
     *
     * @return included Errors in this package.
     */
    public ClassDoc[] errors() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
	for (ClassDocImpl c : getClasses(true)) {
            if (c.isError()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }

    /**
     * Get included enum types in this package.
     *
     * @return included enum types in this package.
     */
    public ClassDoc[] enums() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
	for (ClassDocImpl c : getClasses(true)) {
            if (c.isEnum()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }
    
    /**
     * Get included interfaces in this package, omitting annotation types.
     *
     * @return included interfaces in this package.
     */
    public ClassDoc[] interfaces() {
        ListBuffer<ClassDocImpl> ret = new ListBuffer<ClassDocImpl>();
	for (ClassDocImpl c : getClasses(true)) {
            if (c.isInterface()) {
                ret.append(c);
            }
        }
        return ret.toArray(new ClassDocImpl[ret.length()]);
    }

    /**
     * Get included annotation types in this package.
     *
     * @return included annotation types in this package.
     */
    public AnnotationTypeDoc[] annotationTypes() {
        ListBuffer<AnnotationTypeDocImpl> ret =
	    new ListBuffer<AnnotationTypeDocImpl>();
	for (ClassDocImpl c : getClasses(true)) {
            if (c.isAnnotationType()) {
                ret.append((AnnotationTypeDocImpl)c);
            }
        }
        return ret.toArray(new AnnotationTypeDocImpl[ret.length()]);
    }

    /**
     * Get the annotations of this package.
     * Return an empty array if there are none.
     */
    public AnnotationDesc[] annotations() {
	AnnotationDesc res[] = new AnnotationDesc[sym.getAnnotationMirrors().length()];
	int i = 0;
	for (Attribute.Compound a : sym.getAnnotationMirrors()) {
	    res[i++] = new AnnotationDescImpl(env, a);
	}
	return res;
    }
    
    
    /**
     * Lookup for a class within this package.
     *
     * @return ClassDocImpl of found class, or null if not found.
     */
    public ClassDoc findClass(String className) {
        final boolean filtered = true;
	for (ClassDocImpl c : getClasses(filtered)) {
            if (c.name().equals(className)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Return true if this package is included in the active set.
     */
    public boolean isIncluded() {
        return isIncluded;
    }
    
    /**
     * Get package name.
     *
     * Note that we do not provide a means of obtaining the simple
     * name of a package -- package names are always returned in their
     * uniquely qualified form.
     */
    public String name() {
        return qualifiedName();
    }
    
    /**
     * Get package name.
     */
    public String qualifiedName() {
	Name fullname = sym.getQualifiedName();
	// Some bogus tests depend on the interned "" being returned.
	// See 6457276.
	return fullname.isEmpty() ? "" : fullname.toString();
    }
    
    /**
     * set doc path for an unzipped directory
     */
    public void setDocPath(String path) {        
	setDocPath = true;
	if (path == null)
	    return;
	String newDocPath = path + File.separatorChar + PACKAGE_HTML_FILE_NAME;
	if (!newDocPath.equals(docPath)) {
	    docPath = newDocPath;
	    checkDoc();
	}
    }
    
    /**
     * set the doc path for zipped directory
     */
    public void setDocPath(String path, String entry) {
	if (!path.equals(zipDocPath)) {
	    zipDocPath = path;
	    zipDocEntry = entry + PACKAGE_HTML_FILE_NAME;
	    checkDoc();
	}
    }

    // Has checkDoc() sounded off yet?
    private boolean checkDocWarningEmitted = false;

    /**
     * Invoked when a source of package doc comments is located.
     * Emits a diagnostic if this is the second one.
     */
    private void checkDoc() {
	if (foundDoc) {
	    if (!checkDocWarningEmitted) {
		env.warning(null, "javadoc.Multiple_package_comments", name());
		checkDocWarningEmitted = true;
	    }
	} else {
	    foundDoc = true;
	}
    }
    
    /**
     * Return the source position of the entity, or null if
     * no position is available.
     */
    public SourcePosition position() {
	return (tree != null)
		? SourcePositionImpl.make(tree.sourcefile + "", tree.pos, tree.lineMap)
		: SourcePositionImpl.make(docPath, Position.NOPOS, null);
    }
}
