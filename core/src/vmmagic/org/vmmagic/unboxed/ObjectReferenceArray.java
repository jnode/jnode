/*
 * $Id$
 */
package org.vmmagic.unboxed;

import org.jnode.vm.Vm;
import org.vmmagic.pragma.InlinePragma;
import org.vmmagic.pragma.InterruptiblePragma;
import org.vmmagic.pragma.Uninterruptible;

/**
 * The Vm front end is not capable of correct handling an array of
 * ObjectReferences ...
 * 
 * @author Daniel Frampton
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final public class ObjectReferenceArray implements Uninterruptible {

	private ObjectReference[] data;

	static public ObjectReferenceArray create(int size)
			throws InterruptiblePragma {
		if (Vm.isRunningVm())
			Vm._assert(false); // should be hijacked
		return new ObjectReferenceArray(size);
	}

	private ObjectReferenceArray(int size) throws InterruptiblePragma {
		data = new ObjectReference[size];
		for (int i = 0; i < size; i++) {
			data[i] = ObjectReference.nullReference();
		}
	}

	public ObjectReference get(int index) throws InlinePragma {
		if (Vm.isRunningVm() || Vm.isWritingImage())
			Vm._assert(false); // should be hijacked
		return data[index];
	}

	public void set(int index, ObjectReference v) throws InlinePragma {
		if (Vm.isRunningVm() || Vm.isWritingImage())
			Vm._assert(false); // should be hijacked
		data[index] = v;
	}

	public int length() throws InlinePragma {
		if (Vm.isRunningVm() || Vm.isWritingImage())
			Vm._assert(false); // should be hijacked
		return data.length;
	}

	public Object getBacking() throws InlinePragma {
		if (!Vm.isWritingImage())
			Vm
					.sysFail("ObjectReferenceArray.getBacking called when not writing boot image");
		return data;
	}
}
