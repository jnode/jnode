/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Abstract class for reading information from stack frames.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmStackReader extends VmSystemObject {

	/**
	 * Gets the previous frame (if any)
	 * @param sf The stackframe to get the previous frame from.
	 * @return The previous frame or null.
	 */
	final Address getPrevious(Address sf) {
		if (isValid(sf)) {
			return Unsafe.getAddress(sf, getPreviousOffset(sf));
		} else {
			return null;
		}
	}

	/**
	 * Gets the method of a given stackframe.
	 * @param sf Stackframe pointer
	 * @return The method
	 */
	final VmMethod getMethod(Address sf) {
		return (VmMethod)Unsafe.getObject(sf, getMethodOffset(sf));
	}

	/**
	 * Gets the java program counter of a given stackframe.
	 * @param sf Stackframe pointer
	 * @return the pc
	 */
	final int getPC(Address sf) {
		return Unsafe.getInt(sf, getPCOffset(sf));
	}

	/**
	 * Sets the java program counter of a given stackframe.
	 * @param sf Stackframe pointer
	 * @param pc
	 */
	final void setPC(Address sf, int pc) {
		Unsafe.setInt(sf, getPCOffset(sf), pc);
	}

	/**
	 * Gets the magic constant of a given stackframe.
	 * @param sf Stackframe pointer
	 * @return The magic
	 */
	final int getMagic(Address sf) {
		return Unsafe.getInt(sf, getMagicOffset(sf));
	}

	/**
	 * Gets the return address of a given stackframe.
	 * @param sf Stackframe pointer
	 * @return The address
	 */
	final Address getReturnAddress(Address sf) {
		return Unsafe.getAddress(sf, getReturnAddressOffset(sf));
	}

	/**
	 * Is a given stackframe valid?
	 * @param sf
	 * @return boolean
	 */
	final boolean isValid(Address sf) {
		if (sf == null) {
			return false;
		}
		if (getMethod(sf) == null) {
			return false;
		}
		final int magic = getMagic(sf) & VmStackFrame.MAGIC_MASK;
		if ((magic != VmStackFrame.MAGIC_COMPILED) && (magic != VmStackFrame.MAGIC_INTERPRETED)) {
			return false;
		}
		return true;
	}

	/**
	 * Is the given frame a bottom of stack marker?
	 * @param sf
	 * @return boolean
	 */
	final boolean isStackBottom(Address sf) {
		if (sf == null) {
			return true;
		}
		return (getMethod(sf) == null)
			&& (getMagic(sf) == 0)
			&& (getPrevious(sf) == null);
	}

	/**
	 * Gets the stacktrace for a given current frame.
	 * @param argFrame
	 * @param ip The instruction pointer of the given frame
	 * @param limit Maximum length of returned array.
	 * @return VmStackFrame[]
	 */
	final VmStackFrame[] getVmStackTrace(Address argFrame, Address ip, int limit) {

		final Address frame = argFrame;
		Address f = frame;
		int count = 0;
		while (isValid(f) && (count < limit)) {
			count++;
			f = getPrevious(f);
		}
		if ((f != null) && !isStackBottom(f) && (count < limit)) {
			Unsafe.debug("Corrupted stack!, st.length=");
			Unsafe.debug(count);
			Unsafe.debug(" f.magic=");
			Unsafe.debug(getMagic(f));
			//Unsafe.die();
		}

		final VmStackFrame[] stack = new VmStackFrame[count];
		f = frame;
		for (int i = 0; i < count; i++) {
			stack[i] = new VmStackFrame(f, this, ip);
			// Subtract 1, because the return address is directly after
			// the location where the previous frame was executing.
			ip = Address.add(stack[i].getReturnAddress(), -1);
			f = getPrevious(f);
		}

		return stack;
	}

	/**
	 * Count the number of stackframe from a given frame.
	 * @param sf
	 * @return int
	 */
	final int countStackFrames(Address sf) {
		int count = 0;
		while (isValid(sf)) {
			count++;
			sf = getPrevious(sf);
		}
		return count;
	}

	/**
	 * Show the current stacktrace using Screen.debug.
	 */	
	public final void debugStackTrace() {
		Address f = Unsafe.getCurrentFrame();
		Unsafe.debug("Debug stacktrace: ");
		boolean first = true;
		int max = 20;
		while (isValid(f) && (max > 0)) {
			if (first) {
				first = false;
			} else {
				Unsafe.debug(", ");
			}
			final VmMethod method = getMethod(f);
			final int pc = getPC(f);
			final int lineNr = method.getBytecode().getLineNr(pc);
			final VmType vmClass = method.getDeclaringClass();
			Unsafe.debug(vmClass.getName());
			Unsafe.debug("::");
			Unsafe.debug(method.getName()); 
			Unsafe.debug(lineNr);
			f = getPrevious(f);
			max--;
		}
		if (isValid(f)) {
		    Unsafe.debug("...");
		}
	}
	
	/**
	 * Show the stacktrace of the given thread using Screen.debug.
	 */	
	public final void debugStackTrace(VmThread thread) throws UninterruptiblePragma {
		Address f = thread.getStackFrame();
		Unsafe.debug("Debug stacktrace: ");
		boolean first = true;
		int max = 20;
		while (isValid(f) && (max > 0)) {
			if (first) {
				first = false;
			} else {
				Unsafe.debug(", ");
			}
			final VmMethod method = getMethod(f);
			final int pc = getPC(f);
			final int lineNr = method.getBytecode().getLineNr(pc);
			final VmType vmClass = method.getDeclaringClass();
			Unsafe.debug(vmClass.getName());
			Unsafe.debug("::");
			Unsafe.debug(method.getName()); 
			Unsafe.debug(lineNr);
			f = getPrevious(f);
			max--;
		}
		if (isValid(f)) {
		    Unsafe.debug("...");
		}
	}
	
	/**
	 * Gets the offset within the frame of previous frame (if any)
	 * @param sf The stackframe to get the previous frame from.
	 * @return The previous frame or null.
	 */
	protected abstract int getPreviousOffset(Address sf);

	/**
	 * Gets the offset within the stackframe of method.
	 * @param sf Stackframe pointer
	 * @return The method offset
	 */
	protected abstract int getMethodOffset(Address sf);

	/**
	 * Gets the offset within the stackframe of java program counter.
	 * @param sf Stackframe pointer
	 * @return The pc offset
	 */
	protected abstract int getPCOffset(Address sf);

	/**
	 * Gets the offset within the stackframe of the magic constant.
	 * @param sf Stackframe pointer
	 * @return The magic offset
	 */
	protected abstract int getMagicOffset(Address sf);

	/**
	 * Gets the offset within the stackframe of the return address.
	 * @param sf Stackframe pointer
	 * @return The return address offset
	 */
	protected abstract int getReturnAddressOffset(Address sf);
}
