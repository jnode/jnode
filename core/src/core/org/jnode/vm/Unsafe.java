/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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

import org.jnode.assembler.ObjectResolver;
import org.jnode.security.JNodePermission;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Class that allows directy hardware access.
 * 
 * @author epr
 */
public final class Unsafe {
    
    private static final JNodePermission GET_JUMP_TABLE_PERM = new JNodePermission("getJumpTable");

	public static class UnsafeObjectResolver extends ObjectResolver {

		public UnsafeObjectResolver() {
		}

		/**
		 * @param object
		 * @see org.jnode.assembler.ObjectResolver#addressOf32(Object)
		 * @return int
		 */
		public int addressOf32(Object object) {
			return Unsafe.addressToInt(Unsafe.addressOf(object));
		}

		/**
		 * @param object
		 * @see org.jnode.assembler.ObjectResolver#addressOf64(Object)
		 * @return long
		 */
		public long addressOf64(Object object) {
			return Unsafe.addressToLong(Unsafe.addressOf(object));
		}

		/**
		 * @param ptr
		 * @see org.jnode.assembler.ObjectResolver#objectAt32(int)
		 * @return Object
		 */
		public Object objectAt32(int ptr) {
			return Unsafe.objectAt(Unsafe.intToAddress(ptr));
		}

		/**
		 * @param ptr
		 * @see org.jnode.assembler.ObjectResolver#objectAt64(int)
		 * @return Object
		 */
		public Object objectAt64(long ptr) {
			return Unsafe.objectAt(Unsafe.longToAddress(ptr));
		}

		/**
		 * Gets the address of the given object.
		 * 
		 * @param object
		 * @return Address
		 */
		public VmAddress addressOf(Object object) {
			return Unsafe.addressOf(object);
		}

		/**
		 * Gets the object at a given address.
		 * 
		 * @param ptr
		 * @return Object
		 */
		public Object objectAt(VmAddress ptr) {
			return Unsafe.objectAt(ptr);
		}

		/**
		 * Gets the given address incremented by the given offset.
		 * 
		 * @param address
		 * @param offset
		 * @return Address
		 */
		public VmAddress add(VmAddress address, int offset) {
			return Unsafe.add(address, offset);
		}

		/**
		 * Gets the address of the first element of the given array.
		 * 
		 * @param array
		 * @return The address of the array data.
		 */
		public VmAddress addressOfArrayData(Object array) {
			return VmAddress.addressOfArrayData(array);
		}
	}

	/**
	 * Gets the memory address of the given object.
	 * 
	 * @param object
	 * @return int
	 */
	protected static native VmAddress addressOf(Object object);

	/**
	 * Gets object at the given memory address
	 * 
	 * @param memPtr
	 * @return Object
	 */
	protected static native Object objectAt(VmAddress memPtr);

	/**
	 * Gets the Super Classes Array of the given object.
	 * 
	 * @param object
	 * @return VmType[]
	 */
	protected static native VmType[] getSuperClasses(Object object);

	/**
	 * Gets the GC flags of the given object
	 * 
	 * @param object
	 * @return int
	 * @see org.jnode.classmgr.VMObject#GC_MARKED
	 * @see org.jnode.classmgr.VMObject#GC_FROMSTACK
	 */
	//protected static native int getObjectGCFlags(Object object);

	/**
	 * Sets the GC flags of the given object
	 * 
	 * @param object
	 * @param newFlags
	 *            The new GC flags. Old flags not included are removed.
	 * @see org.jnode.classmgr.VMObject#GC_MARKED
	 * @see org.jnode.classmgr.VMObject#GC_FROMSTACK
	 */
	//protected static native void setObjectGCFlags(Object object, int newFlags);

	/**
	 * Gets a boolean at the given memory address
	 * 
	 * @param memPtr
	 * @return boolean
	 */
	protected static native boolean getBoolean(VmAddress memPtr);

	/**
	 * Gets a boolean at the given offset in a given object.
	 * 
	 * @param object
	 * @param offset
	 * @return boolean
	 */
	protected static native boolean getBoolean(Object object, int offset);

	/**
	 * Gets a 8-bit signed byte at the given memory address
	 * 
	 * @param memPtr
	 * @return byte
	 */
	protected static native byte getByte(VmAddress memPtr);

	/**
	 * Gets a 8-bit signed byte at the given offset in the given object.
	 * 
	 * @param object
	 * @param offset
	 * @return byte
	 */
	protected static native byte getByte(Object object, int offset);

	/**
	 * Gets a 16-bit signed short at the given memory address
	 * 
	 * @param memPtr
	 * @return short
	 */
	protected static native short getShort(VmAddress memPtr);

	/**
	 * Gets a 16-bit signed short at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return short
	 */
	protected static native short getShort(Object object, int offset);

	/**
	 * Gets a 16-bit unsigned char at the given memory address
	 * 
	 * @param memPtr
	 * @return char
	 */
	protected static native char getChar(VmAddress memPtr);

	/**
	 * Gets a 16-bit unsigned char at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return char
	 */
	protected static native char getChar(Object object, int offset);

	/**
	 * Gets a 32-bit signed int at the given memory address
	 * 
	 * @param memPtr
	 * @return int
	 */
	protected static native int getInt(VmAddress memPtr);

	/**
	 * Gets a 32-bit signed int at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return int
	 */
	protected static native int getInt(Object object, int offset);

	/**
	 * Gets a 64-bit signed long at the given memory address
	 * 
	 * @param memPtr
	 * @return long
	 */
	protected static native long getLong(VmAddress memPtr);

	/**
	 * Gets a 64-bit signed long at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return long
	 */
	protected static native long getLong(Object object, int offset);

	/**
	 * Gets a float at the given memory address
	 * 
	 * @param memPtr
	 * @return float
	 */
	protected static native float getFloat(VmAddress memPtr);

	/**
	 * Gets a float at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return float
	 */
	protected static native float getFloat(Object object, int offset);

	/**
	 * Gets a double at the given memory address
	 * 
	 * @param memPtr
	 * @return double
	 */
	protected static native double getDouble(VmAddress memPtr);

	/**
	 * Gets a double at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return double
	 */
	protected static native double getDouble(Object object, int offset);

	/**
	 * Gets a object reference at the given memory address
	 * 
	 * @param memPtr
	 * @return Object
	 */
	protected static native Object getObject(VmAddress memPtr);

	/**
	 * Gets a address at the given memory address
	 * 
	 * @param memPtr
	 * @return Address
	 */
	protected static native VmAddress getAddress(VmAddress memPtr);

	/**
	 * Gets a object reference at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return Object
	 */
	protected static native Object getObject(Object object, int offset);

	/**
	 * Gets an address at the given memory address
	 * 
	 * @param object
	 * @param offset
	 * @return Object
	 */
	static native VmAddress getAddress(Object object, int offset);

	/**
	 * Sets a boolean at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setBoolean(VmAddress memPtr, boolean value);

	/**
	 * Sets a boolean at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setBoolean(Object object, int offset, boolean value);

	/**
	 * Sets a byte at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setByte(VmAddress memPtr, byte value);

	/**
	 * Sets a byte at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setBytes(VmAddress memPtr, byte value, int count);

	/**
	 * Perform a bitwise AND of the byte at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void andByte(VmAddress memPtr, byte value, int count);

	/**
	 * Perform a bitwise OR of the byte at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void orByte(VmAddress memPtr, byte value, int count);

	/**
	 * Perform a bitwise XOR of the byte at the given address and the given value While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void xorByte(VmAddress memPtr, byte value, int count);

	/**
	 * Sets a byte at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setByte(Object object, int offset, byte value);

	/**
	 * Sets a short at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setShort(VmAddress memPtr, short value);

	/**
	 * Sets a short at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setShorts(VmAddress memPtr, short value, int count);

	/**
	 * Perform a bitwise AND of the short at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void andShort(VmAddress memPtr, short value, int count);

	/**
	 * Perform a bitwise OR of the short at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void orShort(VmAddress memPtr, short value, int count);

	/**
	 * Perform a bitwise XOR of the short at the given address and the given value While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void xorShort(VmAddress memPtr, short value, int count);

	/**
	 * Sets a short at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setShort(Object object, int offset, short value);

	/**
	 * Sets a char at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setChar(VmAddress memPtr, char value);

	/**
	 * Sets a char at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setChars(VmAddress memPtr, char value, int count);

	/**
	 * Perform a bitwise AND of the char at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void andChar(VmAddress memPtr, char value, int count);

	/**
	 * Perform a bitwise OR of the char at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void orChar(VmAddress memPtr, char value, int count);

	/**
	 * Perform a bitwise XOR of the char at the given address and the given value While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void xorChar(VmAddress memPtr, char value, int count);

	/**
	 * Sets a char at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setChar(Object object, int offset, char value);

	/**
	 * Sets an int at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setInt(VmAddress memPtr, int value);

	/**
	 * Sets an int at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setInts(VmAddress memPtr, int value, int count);

	/**
	 * Perform a bitwise AND of the int at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void andInt(VmAddress memPtr, int value, int count);

	/**
	 * Perform a bitwise OR of the int at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void orInt(VmAddress memPtr, int value, int count);

	/**
	 * Perform a bitwise XOR of the int at the given address and the given value While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void xorInt(VmAddress memPtr, int value, int count);

	/**
	 * Sets a 24-bit int at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setInts24(VmAddress memPtr, int value, int count);

	/**
	 * Perform a bitwise AND of the 24-bit int at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void andInt24(VmAddress memPtr, int value, int count);

	/**
	 * Perform a bitwise OR of the 24-bit int at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void orInt24(VmAddress memPtr, int value, int count);

	/**
	 * Perform a bitwise XOR of the 24-bit int at the given address and the given value While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void xorInt24(VmAddress memPtr, int value, int count);

	/**
	 * Sets an int at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setInt(Object object, int offset, int value);

	/**
	 * Sets a long at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setLong(VmAddress memPtr, long value);

	/**
	 * Sets a long at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setLongs(VmAddress memPtr, long value, int count);

	/**
	 * Perform a bitwise AND of the long at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void andLong(VmAddress memPtr, long value, int count);

	/**
	 * Perform a bitwise OR of the long at the given address and the given value. While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void orLong(VmAddress memPtr, long value, int count);

	/**
	 * Perform a bitwise XOR of the long at the given address and the given value While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 *            The number of times to repeat this operation
	 */
	protected static native void xorLong(VmAddress memPtr, long value, int count);

	/**
	 * Sets a long at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setLong(Object object, int offset, long value);

	/**
	 * Sets a float at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setFloat(VmAddress memPtr, float value);

	/**
	 * Sets a float at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setFloats(VmAddress memPtr, float value, int count);

	/**
	 * Sets a float at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setFloat(Object object, int offset, float value);

	/**
	 * Sets a double at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setDouble(VmAddress memPtr, double value);

	/**
	 * Sets a double at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setDoubles(VmAddress memPtr, double value, int count);

	/**
	 * Sets a double at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setDouble(Object object, int offset, double value);

	/**
	 * Sets a Object at a given memory address
	 * 
	 * @param memPtr
	 * @param value
	 */
	protected static native void setObject(VmAddress memPtr, Object value);

	/**
	 * Sets a Object at a given memory address While count is greater then 1, the address is incremented and the process repeats.
	 * 
	 * @param memPtr
	 * @param value
	 * @param count
	 */
	protected static native void setObjects(VmAddress memPtr, Object value, int count);

	/**
	 * Sets a Object at a given memory address
	 * 
	 * @param object
	 * @param offset
	 * @param value
	 */
	protected static native void setObject(Object object, int offset, Object value);

	/**
	 * Fill the memory at the given memory address with size times 0 bytes.
	 * 
	 * memPtr must be VmObject.SLOT_SIZE aligned
	 * 
	 * size % VmObject.SLOT_SIZE must be 0
	 * 
	 * @param memPtr
	 * @param size
	 */
	protected static native void clear(VmAddress memPtr, int size);

	/**
	 * Copy size bytes of memory from srcMemPtr to destMemPtr. The memory areas must not overlap.
	 * 
	 * @param srcMemPtr
	 * @param destMemPtr
	 * @param size
	 */
	protected static native void copy(VmAddress srcMemPtr, VmAddress destMemPtr, int size);

	/**
	 * Push an integer onto the execution stack
	 * 
	 * @param value
	 */
	protected static native void pushInt(int value);

	/**
	 * Push a long onto the execution stack
	 * 
	 * @param value
	 */
	protected static native void pushLong(long value);

	/**
	 * Push an Object onto the execution stack
	 * 
	 * @param value
	 */
	protected static native void pushObject(Object value);

	/**
	 * Invoke the given method without any parameters
	 * 
	 * @param method
	 */
	protected static native void invokeVoid(VmMethod method);

	/**
	 * Invoke the given method without any parameters
	 * 
	 * @param method
	 * @return int
	 */
	protected static native int invokeInt(VmMethod method);

	/**
	 * Invoke the given method without any parameters
	 * 
	 * @param method
	 * @return long
	 */
	protected static native long invokeLong(VmMethod method);

	/**
	 * Invoke the given method without any parameters
	 * 
	 * @param method
	 * @return Object
	 */
	protected static native Object invokeObject(VmMethod method);

	/**
	 * Gets the current stackframe
	 * 
	 * @return The address of the stackframe of the current thread
	 */
	protected static native VmAddress getCurrentFrame();

	/**
	 * Halt the processor until the next interrupt arrives.
	 */
	protected static native void idle();

	/**
	 * Cause the system to stop TODO Protect me again
	 */
	public static void die(String msg) {
		debug("Real panic: ");
		if (msg != null) {
			debug(msg);
		}
		die();
	}

	/**
	 * Cause the system to stop TODO Protect me again
	 */
	private static native void die();

	/**
	 * Print the given string on the screen.
	 */
	public static native void debug(String str);

	/**
	 * Print the given value on the screen.
	 */
	public static native void debug(char value);

	/**
	 * Print the given value on the screen.
	 */
	public static native void debug(int value);

	/**
	 * Print the given value on the screen.
	 */
	public static native void debug(long value);

	/**
	 * Initialize the new Thread.
	 * 
	 * @param curThread
	 * @param newStack
	 * @param stackSize
	 */
	protected static native void initThread(VmThread curThread, Object newStack, int stackSize);

	/**
	 * Atomic compare and swap. Compares the int value addressed by the given address with the given old value. If they are equal, the value at the given address is replace by the new value and true
	 * is returned, otherwise nothing is changed and false is returned.
	 * 
	 * @param address
	 * @param oldValue
	 * @param newValue
	 * @return boolean true if the value at address is changed, false otherwise.
	 */
	protected static native boolean atomicCompareAndSwap(VmAddress address, int oldValue, int newValue);

	/**
	 * Atomic AND. *((int*)address) &= value.
	 * 
	 * @param address
	 * @param value
	 * @return boolean
	 */
	protected static native boolean atomicAnd(VmAddress address, int value);

	/**
	 * Atomic OR. *((int*)address) |= value.
	 * 
	 * @param address
	 * @param value
	 * @return boolean
	 */
	protected static native boolean atomicOr(VmAddress address, int value);

	/**
	 * Atomic SUB. *((int*)address) -= value.
	 * 
	 * @param address
	 * @param value
	 * @return boolean
	 */
	protected static native boolean atomicSub(VmAddress address, int value);

	protected static native int inPortByte(int portNr);
	protected static native int inPortWord(int portNr);
	protected static native int inPortDword(int portNr);
	protected static native void outPortByte(int portNr, int value);
	protected static native void outPortWord(int portNr, int value);
	protected static native void outPortDword(int portNr, int value);

	public static native float intBitsToFloat(int value);
	public static native int floatToRawIntBits(float value);
	public static native double longBitsToDouble(long value);
	public static native long doubleToRawLongBits(double value);

	protected static native int compare(VmAddress a1, VmAddress a2) throws UninterruptiblePragma;
	protected static native VmAddress add(VmAddress addr, int incValue) throws UninterruptiblePragma;
	protected static native VmAddress add(VmAddress a1, VmAddress a2) throws UninterruptiblePragma;

	protected static native VmAddress intToAddress(int addr32) throws UninterruptiblePragma;
	protected static native VmAddress longToAddress(long addr64) throws UninterruptiblePragma;
	protected static native int addressToInt(VmAddress addr) throws UninterruptiblePragma;
	protected static native long addressToLong(VmAddress addr) throws UninterruptiblePragma;

	/**
	 * Gets the minimum valid address in the addressspace of the current architecture.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getMinAddress();

	/**
	 * Gets the maximum valid address in the addressspace of the current architecture.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getMaxAddress();

	/**
	 * Gets the (inclusive) start address of the available memory.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getMemoryStart();

	/**
	 * Gets the (exclusive) end address of the available memory.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getMemoryEnd();

	/**
	 * Gets the (inclusive) start address of the kernel.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getKernelStart();

	/**
	 * Gets the (exclusive) end address of the kernel.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getKernelEnd();

	/**
	 * Gets the (inclusive) start address of the initial jarfile.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getInitJarStart();

	/**
	 * Gets the (exclusive) end address of the initial jarfile.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getInitJarEnd();

	/**
	 * Gets the (inclusive) start address of the boot heap.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getBootHeapStart();

	/**
	 * Gets the (exclusive) end address of the boot heap.
	 * 
	 * @return Address
	 */
	protected static native VmAddress getBootHeapEnd();

	public static native long getTimeStampCounter();

	/**
	 * Gets information of the JNode kernel command line.
	 * 
	 * @param destination
	 *            If non-null, the commandline is copied into this array.
	 * @return The maximum length of the command line
	 */
	protected static native int getCmdLine(byte[] destination);

	/**
	 * Gets the processor that currently runs the active thread.
	 * 
	 * @return The current processor.
	 * @throws UninterruptiblePragma
	 */
	public static native VmProcessor getCurrentProcessor() throws UninterruptiblePragma;

	/**
	 * Trigger a yieldpoint
	 */
	static native void yieldPoint();

	/**
	 * Gets the address of the system dependent jump table used for native method indirection.
	 * 
	 * @return The address of the system dependent jump table.
	 */
	private static native VmAddress getJumpTable0();

	/**
	 * Gets the address of the system dependent jump table used for native method indirection.
	 * 
	 * @return The address of the system dependent jump table.
	 */
	public static final VmAddress getJumpTable() {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(GET_JUMP_TABLE_PERM);
	    }
	    return getJumpTable0();
	}

	/**
	 * Gets a jumptable entry.
	 * This method can only be called at runtime.
	 * @param offset
	 * @return The jumptable entry.
	 */
	public static final VmAddress getJumpTableEntry(int offset) {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(GET_JUMP_TABLE_PERM);
	    }
		return Unsafe.getAddress(Unsafe.getJumpTable0(), offset);
	}

	/**
	 * Read CPU identification data.
	 * 
	 * If id is null, this method will return the length of the id array that is required to fit all data. If id is not null and long enough, it is filled with all identification data.
	 * 
	 * @param id
	 * @return The required length of id.
	 */
	static native int getCPUID(int[] id);

	/**
	 * Force a breakpoint
	 */
	public static native void breakPoint();
}
