/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import org.jnode.annotation.Internal;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Word;

/**
 * Class containing "magic" methods that are interpreted by the VM itself,
 * instead of being executed as normal java methods.  The actual method
 * bodies are not used.
 * <p/>
 * Methods in this class can also be called from inside JNode.
 * <p/>
 * See {@link org.jnode.vm.classmgr.VmType VmType} to get the list of "magic" classes and
 * {@link org.jnode.vm.compiler.BaseMagicHelper.MagicMethod MagicMethod}
 * to get the list of "magic" methods.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmMagic {

    /**
     * Gets the VmType of the given object.
     *
     * @param object
     * @return the VmType of the given object
     */
    public static VmClassType<?> getObjectType(Object object) {
        return null;
    }

    /**
     * Gets the Type Information Block of the given object.
     *
     * @param object
     * @return TIB
     */
    public static Object[] getTIB(Object object) {
        return null;
    }

    /**
     * Gets all of the flags of the given object.
     *
     * @param object
     * @return the object flags
     */
    public static Word getObjectFlags(Object object) {
        return Word.zero();
    }

    /**
     * Gets the GC color flags of the given object.
     *
     * @param object
     * @return the GC color flags (as an int)
     */
    public static int getObjectColor(Object object) {
        return 0;
    }

    /**
     * Sets all of the flags of the given object.
     *
     * @param object the target object
     * @param flags the new flags value
     */
    public static void setObjectFlags(Object object, Word flags) {
    }

    /**
     * Gets the address of the first array element of the given array.
     *
     * @param array
     * @return the address of the first array element of the given array
     */
    public static Address getArrayData(Object array) {
        return null;
    }

    /**
     * Has the given object been finalized.
     *
     * @param src
     * @return if the given object has been finalized
     */
    public static boolean isFinalized(Object src) {
        return false;
    }

    /**
     * Gets the current stackframe
     *
     * @return The address of the stackframe of the current thread
     */
    @KernelSpace
    @Internal
    public static Address getCurrentFrame() {
        return null;
    }

    /**
     * Gets the timestamp of the current processor.
     *
     * @return the timestamp of the current processor
     */
    public static long getTimeStamp() {
        return 0;
    }

    /**
     * Type cast a 32-bit word to a Java float.
     * 
     * @param value a 32-bit word
     * @return the same bit pattern as a float
     */
    public static float intBitsToFloat(int value) {
        return 0;
    }

    /**
     * Type cast a Java float to a 32-bit word.
     * 
     * @param value a float 
     * @return the same bit pattern as a 32-bit word
     */
    public static int floatToRawIntBits(float value) {
        return 0;
    }

    /**
     * Type cast a 64-bit word to a Java double.
     * 
     * @param value a 64-bit word
     * @return the same bit pattern as a double
     */
    public static double longBitsToDouble(long value) {
        return 0;
    }

    /**
     * Type cast a Java double to a 64-bit word.
     * 
     * @param value a double 
     * @return the same bit pattern as a 64-bit word
     */
    public static long doubleToRawLongBits(double value) {
        return 0;
    }

    /**
     * Force a breakpoint
     */
    public static void breakPoint() {
    }

    /**
     * Gets the processor executing the current thread.
     *
     * @return the processor executing the current thread
     */
    @KernelSpace
    public static VmProcessor currentProcessor() {
        return null;
    }

    /**
     * Gets the address of a shared static field.
     *
     * @param index
     * @return the address of a shared static field
     */
    public static Address getSharedStaticFieldAddress(int index) {
        return null;
    }

    /**
     * Gets the address of a isolated static field.
     *
     * @param index
     * @return the address of a isolated static field
     */
    public static Address getIsolatedStaticFieldAddress(int index) {
        return null;
    }

    /**
     * Are we currently running JNode.
     *
     * @return true when running JNode, false when running any other VM.
     */
    public static boolean isRunningJNode() {
        return false;
    }

    /**
     * Do not instantiate this class.
     */
    private VmMagic() {
    }
}
