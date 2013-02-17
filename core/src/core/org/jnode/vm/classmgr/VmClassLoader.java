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
 
package org.jnode.vm.classmgr;

import java.io.Writer;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;

import org.jnode.vm.compiler.CompiledIMT;
import org.jnode.vm.facade.VmArchitecture;
import org.jnode.vm.objects.VmSystemObject;

/**
 * Interface for the delegation of loading classes with a given name
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmClassLoader extends VmSystemObject {

    /**
     * Load a class into memory, but do not resolve or compile its method
     *
     * @param className
     * @param resolve
     * @return The loaded class
     * @throws ClassNotFoundException
     */
    public abstract VmType<?> loadClass(String className, boolean resolve) throws ClassNotFoundException;

    /**
     * Find a loaded class with the given name.
     *
     * @param className
     * @return The loaded class, or null if not found.
     */
    public abstract VmType<?> findLoadedClass(String className);

    /**
     * Define a byte-array of class data into a loaded class.
     *
     * @param name
     * @param data
     * @param offset
     * @param length
     * @param protDomain
     * @return VmClass
     */
    public abstract VmType<?> defineClass(String name, byte[] data, int offset, int length,
                                          ProtectionDomain protDomain);

    /**
     * Define a bytebuffer of class data into a loaded class.
     *
     * @param name
     * @param data
     * @param protDomain
     * @return VmClass
     */
    public abstract VmType<?> defineClass(String name, ByteBuffer data, ProtectionDomain protDomain);

    /**
     * Define a class that is created in memory.
     *
     * @param createdType
     * @return VmClass
     */
    public abstract VmType<?> defineClass(VmType<?> createdType);

    /**
     * Gets the ClassLoader belonging to this loader.
     *
     * @return ClassLoader
     */
    public abstract ClassLoader asClassLoader();

    public abstract void disassemble(VmMethod vmMethod, int optLevel, boolean enableTestCompilers, Writer writer);

    /**
     * Compile the given IMT.
     */
    public abstract CompiledIMT compileIMT(IMTBuilder builder);

    /**
     * Gets the architecture used by this loader.
     *
     * @return The architecture
     */
    public abstract VmArchitecture getArchitecture();

    /**
     * Should prepared classes be compiled.
     *
     * @return boolean
     */
    public abstract boolean isCompileRequired();

    /**
     * Is this the system classloader.
     *
     * @return boolean
     */
    public abstract boolean isSystemClassLoader();

    /**
     * Does a resource with a given name exist in this loader.
     *
     * @param resName
     * @return boolean
     */
    public abstract boolean resourceExists(String resName);

    /**
     * Gets the shared statics table.
     *
     * @return The statics table
     */
    public abstract VmSharedStatics getSharedStatics();

    /**
     * Gets the isolated statics table (of the current isolate)
     *
     * @return The statics table
     */
    public abstract VmIsolatedStatics getIsolatedStatics();

    /**
     * Gets the selector map used to create unique method selectors.
     *
     * @return the selector map.
     */
    protected abstract SelectorMap getSelectorMap();
}
