/*
 * $Id$
 */
package org.vmmagic.unboxed;

import org.jnode.vm.Vm;
import org.vmmagic.pragma.InlinePragma;
import org.vmmagic.pragma.InterruptiblePragma;
import org.vmmagic.pragma.Uninterruptible;

/**
 * The VM front end is not capable of correct handling an array of Address,
 * VM_Word, .... For now, we provide special types to handle these situations.
 * 
 * @author Perry Cheng
 * @modified Daniel Frampton
 */

final public class AddressArray implements Uninterruptible {

	private Address[] data;

	static public AddressArray create(int size) throws InterruptiblePragma {
		if (Vm.isRunningVm())
			Vm._assert(false); // should be hijacked
		return new AddressArray(size);
	}

	private AddressArray(int size) throws InterruptiblePragma {
		data = new Address[size];
		Address zero = Address.zero();
		for (int i = 0; i < size; i++) {
			data[i] = zero;
		}
	}

	public Address get(int index) throws InlinePragma {
		if (Vm.isRunningVm() || Vm.isWritingImage())
			Vm._assert(false); // should be hijacked
		return data[index];
	}

	public void set(int index, Address v) throws InlinePragma {
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
		if (!Vm.isWritingImage()) {
			Vm.sysFail("AddressArray.getBacking called when not writing boot image");
		}
		return data;
	}
}