/*
 * Created on Feb 20, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.assembler;

import org.jnode.vm.Address;


/**
 * @author epr
 */
public abstract class ObjectResolver {
	
	/**
	 * Gets the address of the given object.
	 * @param object
	 * @return int
	 */
	public abstract int addressOf32(Object object);
	
	/**
	 * Gets the address of the given object.
	 * @param object
	 * @return long
	 */
	public abstract long addressOf64(Object object);
	
	/**
	 * Gets the object at a given address.
	 * @param ptr
	 * @return Object
	 */	
	public abstract Object objectAt32(int ptr);

	/**
	 * Gets the object at a given address.
	 * @param ptr
	 * @return Object
	 */	
	public abstract Object objectAt64(long ptr);

	/**
	 * Gets the address of the given object.
	 * @param object
	 * @return Address
	 */
	public abstract Address addressOf(Object object);
	
	/**
	 * Gets the object at a given address.
	 * @param ptr
	 * @return Object
	 */	
	public abstract Object objectAt(Address ptr);
	
	/**
	 * Gets the given address incremented by the given offset.
	 * @param address
	 * @param offset
	 * @return Address
	 */
	public abstract Address add(Address address, int offset);

	/**
	 * Gets the address of the first element of the given array.
	 * @param array
	 * @return The address of the array data.
	 */
	public abstract Address addressOfArrayData(Object array);
}
