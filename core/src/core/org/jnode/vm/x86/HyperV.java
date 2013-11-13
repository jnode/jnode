package org.jnode.vm.x86;

/**
 * Hyper-V constants
 * @author epr@jnode.org
 */
class HyperV {
	/** 
	 * MSR index for identifying the guest OS 
	 */
	static final int HV_X64_MSR_GUEST_OS_ID = 0x40000000;
}
