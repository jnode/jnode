/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.vm.VmThread;
import org.jnode.vm.classmgr.VmStatics;
import org.vmmagic.unboxed.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Processor64 extends VmX86Processor {

	/**
	 * @param id
	 * @param arch
	 * @param statics
	 * @param cpuId
	 */
	public VmX86Processor64(int id, VmX86Architecture64 arch, VmStatics statics,
			X86CpuID cpuId) {
		super(id, arch, statics, cpuId);
		// TODO Auto-generated constructor stub
	}

	protected final VmThread createThread() {
		return new VmX86Thread64();
	}

	protected final VmX86Thread createThread(byte[] stack) {
		return new VmX86Thread64(stack);
	}

	public final VmThread createThread(Thread javaThread) {
		return new VmX86Thread64(javaThread);
	}

	protected Address setupBootCode(ResourceManager rm, GDT gdt)
			throws ResourceNotFreeException {
		// TODO Auto-generated method stub
		return null;
	}

	protected void setupGDT(GDT gdt) {
		// TODO Auto-generated method stub

	}

	protected void setupUserStack(byte[] userStack) {
		// TODO Auto-generated method stub

	}
}
