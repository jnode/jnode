/**
 * $Id$
 */

package org.jnode.vm;

import org.jnode.vm.classmgr.VmMethod;

/**
 * A VmFrame is the execution frame (locals & stack) for a method during
 * execution. Note that this class is not meant to be a normal java class,
 * instead it is a record structure that maps directly on how a method frame is
 * push on the stack by the compiler. Don't add any methods, since during
 * runtime instances of this class will have no header and thus no virtual
 * method table.
 */
public final class VmStackFrame extends VmSystemObject {

	public static final int MAGIC_MASK = 0xFFFFFF00;
	public static final int MAGIC_INTERPRETED = 0xF25A1200;
	public static final int MAGIC_COMPILED = 0x21A52F00;

	/** The method executing in this frame */
	private final VmMethod sfMethod;
	/** The program counter, only valid for interpreted methods */
	private final int sfPc;
	/** A magic value used to detect stack errors */
	private final int sfMagic;
	/** A reference to the return address */
	private final Address sfReturnAddress;

	/**
	 * Initialize this instance.
	 * @param src
	 * @param reader
	 */
	protected VmStackFrame(Address src, VmStackReader reader) {
		this.sfMethod = reader.getMethod(src);
		this.sfPc = reader.getPC(src);
		this.sfMagic = reader.getMagic(src);
		this.sfReturnAddress = reader.getReturnAddress(src);
		/**
		 * Do no clone previousFrame!
		 * This is because they are actually addresses and not references
		 * to objects.
		 */
	}

	/**
	 * Is a given stackframe of a interpreted method?
	 * @return boolean
	 */
	protected final boolean isInterpreted() {
		return (sfMagic == MAGIC_INTERPRETED);
	}

	/**
	 * @return Returns the magic.
	 */
	public final int getMagic() {
		return this.sfMagic;
	}

	/**
	 * @return Returns the method.
	 */
	public final VmMethod getMethod() {
		return this.sfMethod;
	}

	/**
	 * @return Returns the pc.
	 */
	public final int getPc() {
		return this.sfPc;
	}

	/**
	 * @return Returns the returnAddress.
	 */
	public final Address getReturnAddress() {
		return this.sfReturnAddress;
	}

}
