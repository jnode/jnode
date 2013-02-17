/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm;

import java.io.Writer;
import java.util.HashMap;

import org.jnode.annotation.PrivilegedActionPragma;
import org.jnode.vm.classmgr.IMTBuilder;
import org.jnode.vm.classmgr.SelectorMap;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.CompiledIMT;
import org.jnode.vm.scheduler.VmProcessor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmJavaClassLoader extends VmAbstractClassLoader {

    /**
     * The java classloader
     */
    private final ClassLoader loader;

    /**
     * The system classloader
     */
    private final VmSystemClassLoader systemLoader;

    /**
     * A className to VmType map for all loaded classes
     */
    private final HashMap<String, VmType> loadedClasses = new HashMap<String, VmType>();

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
    public final BaseVmArchitecture getArchitecture() {
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
    @PrivilegedActionPragma
    public final VmType<?> loadClass(String className, boolean resolve)
        throws ClassNotFoundException {
        final VmType<?> cls;
        if (className.charAt(0) == '[') {
            cls = loadArrayClass(className, resolve);
            addLoadedClass(className, cls);
        } else {
            final Class<?> javaType = loader.loadClass(className);
            cls = VmType.fromClass((Class<?>) javaType);
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
     * @see org.jnode.vm.VmAbstractClassLoader#getSharedStatics()
     */
    public final VmSharedStatics getSharedStatics() {
        return systemLoader.getSharedStatics();
    }

    /**
     * Gets the isolated statics table (of the current isolate)
     *
     * @return The statics table
     */
    public final VmIsolatedStatics getIsolatedStatics() {
        return VmProcessor.current().getIsolatedStatics();
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#resourceExists(java.lang.String)
     */
    public final boolean resourceExists(String resName) {
        return false;
    }

}
