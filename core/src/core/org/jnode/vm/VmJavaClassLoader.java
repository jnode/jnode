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
 
package org.jnode.vm;

import java.util.HashMap;
import java.io.Writer;

import org.jnode.vm.classmgr.IMTBuilder;
import org.jnode.vm.classmgr.SelectorMap;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.CompiledIMT;
import org.vmmagic.pragma.PrivilegedActionPragma;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmJavaClassLoader extends VmAbstractClassLoader {

    /** The java classloader */
    private final ClassLoader loader;

    /** The system classloader */
    private final VmSystemClassLoader systemLoader;

    /** The loaded classes (nbame, VmType) */
    private final HashMap loadedClasses = new HashMap();

    /**
     * Initialize this class.
     * 
     * @param loader
     */
    public VmJavaClassLoader(ClassLoader loader) {
        this.loader = loader;
        this.systemLoader = VmSystem.getSystemClassLoader();
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#asClassLoader()
     */
    public final ClassLoader asClassLoader() {
        return loader;
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#compileRuntime(org.jnode.vm.classmgr.VmMethod,
     *      int, boolean)
     */
    public final void compileRuntime(VmMethod vmMethod, int optLevel, boolean enableTestCompilers) {
        systemLoader.compileRuntime(vmMethod, optLevel, enableTestCompilers);
    }

    public void disassemble(VmMethod vmMethod, int optLevel, boolean enableTestCompilers, Writer writer) {
        systemLoader.disassemble(vmMethod, optLevel, enableTestCompilers, writer);
    }

    /**
     * Compile the given IMT.
     */
    public CompiledIMT compileIMT(IMTBuilder builder) {
    	return systemLoader.compileIMT(builder);    	
    }
    
    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#findLoadedClass(java.lang.String)
     */
    public final VmType findLoadedClass(String className) {
        return (VmType) loadedClasses.get(className);
    }

    /**
     * Add a class that has been loaded.
     * 
     * @param name
     * @param cls
     */
    public final void addLoadedClass(String name, VmType cls) {
        loadedClasses.put(name, cls);
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#getArchitecture()
     */
    public final VmArchitecture getArchitecture() {
        return systemLoader.getArchitecture();
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#isCompileRequired()
     */
    public final boolean isCompileRequired() {
        return systemLoader.isCompileRequired();
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#loadClass(java.lang.String,
     *      boolean)
     */
    public final VmType loadClass(String className, boolean resolve)
            throws ClassNotFoundException, PrivilegedActionPragma {
        final VmType cls;
        if (className.charAt(0) == '[') {
            cls = loadArrayClass(className, resolve);
            addLoadedClass(className, cls);
        } else {
            cls = loader.loadClass(className).getVmClass();
        }
        if (resolve) {
            cls.link();
        }
        return cls;
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#isSystemClassLoader()
     */
    public final boolean isSystemClassLoader() {
        return false;
    }

    /**
     * @see org.jnode.vm.VmAbstractClassLoader#getSelectorMap()
     */
    protected final SelectorMap getSelectorMap() {
        return systemLoader.getSelectorMap();
    }

    /**
     * @see org.jnode.vm.VmAbstractClassLoader#getStatics()
     */
    public final VmStatics getStatics() {
        return systemLoader.getStatics();
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#resourceExists(java.lang.String)
     */
    public final boolean resourceExists(String resName) {
        return false;
    }

}
