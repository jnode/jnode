/*
 * $Id$
 */
package org.jnode.vm.x86;

import java.nio.ByteOrder;
import java.util.Iterator;

import org.jnode.system.BootLog;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmStackReader;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.x86.compiler.X86IMTCompiler;
import org.jnode.vm.x86.compiler.l1.X86Level1Compiler;
import org.jnode.vm.x86.compiler.l1a.X86Level1ACompiler;
import org.jnode.vm.x86.compiler.stub.X86StubCompiler;

/**
 * Architecture descriptor for the Intel X86 architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Architecture extends VmArchitecture {

	/** Size of an object reference */
	public static final int SLOT_SIZE = 4;

	/** The compilers */
	private final NativeCodeCompiler[] compilers;
	
	/** The compilers under test */
	private final NativeCodeCompiler[] testCompilers;
	
	/** The IMT compiler */
	private final X86IMTCompiler imtCompiler;

	/** The local APIC accessor, if any */
	private LocalAPIC localAPIC;

	/** The MP configuration table */
	private MPConfigTable mpConfigTable;

	/** The stackreader of this architecture */
	private final VmX86StackReader stackReader = new VmX86StackReader();

	/**
	 * Initialize this instance using the default compiler.
	 */
	public VmX86Architecture() {
		this("L1");
	}
	
	/**
	 * Initialize this instance.
	 * @param compiler L1a to use L1A compiler, L1 compiler otherwise.
	 */
	public VmX86Architecture(String compiler) {
		final boolean useL1A = ((compiler != null) && compiler.equalsIgnoreCase("L1A"));
		imtCompiler = new X86IMTCompiler();
		compilers = new NativeCodeCompiler[2];
		compilers[0] = new X86StubCompiler();
		if (useL1A) {
			compilers[1] = new X86Level1ACompiler();
			testCompilers = null;
		} else {
			compilers[1] = new X86Level1Compiler();			
			testCompilers = new NativeCodeCompiler[1];
			testCompilers[0] = new X86Level1ACompiler();
		}	
	}
	
	/**
	 * Create a processor instance for this architecture.
	 * 
	 * @return The processor
	 */
	public VmProcessor createProcessor(int id, VmStatics statics) {
		return new VmX86Processor(id, this, statics, null);
	}

	/**
	 * Gets the byte ordering of this architecture.
	 * 
	 * @return ByteOrder
	 */
	public final ByteOrder getByteOrder() {
		return ByteOrder.LITTLE_ENDIAN;
	}

	/**
	 * Gets all compilers for this architecture.
	 * 
	 * @return The compilers, sorted by optimization level, from least
	 *         optimizations to most optimizations.
	 */
	public final NativeCodeCompiler[] getCompilers() {
		return compilers;
	}

	/**
	 * Gets all test compilers for this architecture.
	 * 
	 * @return The compilers, sorted by optimization level, from least
	 *         optimizations to most optimizations.
	 */
	public final NativeCodeCompiler[] getTestCompilers() {
		return testCompilers;
	}

	/**
	 * Gets the compiler of IMT's.
	 * @return
	 */
	public final IMTCompiler getIMTCompiler() {
		return imtCompiler;
	}
	
	/**
	 * Gets the name of this architecture.
	 * 
	 * @return name
	 */
	public final String getName() {
		return "x86";
	}

	/**
	 * Gets the size in bytes of an object reference.
	 * 
	 * @return Size of reference, always 4 here
	 */
	public final int getReferenceSize() {
		return SLOT_SIZE;
	}

	/**
	 * Gets the stackreader for this architecture.
	 * 
	 * @return Stack reader
	 */
	public final VmStackReader getStackReader() {
		return stackReader;
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#initializeProcessors(ResourceManager)
	 */
	protected void initializeProcessors(ResourceManager rm) {

		final String cmdLine = VmSystem.getCmdLine();
		if (cmdLine.indexOf("mp=no") >= 0) {
			return;
		}
		//

		final MPFloatingPointerStructure mp = MPFloatingPointerStructure.find(
				rm, ResourceOwner.SYSTEM);
		if (mp == null) {
			BootLog.info("No MP table found");
			// No MP table found.
			return;
		}
		try {
			BootLog.info("Found " + mp);
			this.mpConfigTable = mp.getMPConfigTable();
		} finally {
			mp.release();
		}

		if (mpConfigTable == null) {
			return;
		}

		mpConfigTable.dump(System.out);
		final ResourceOwner owner = ResourceOwner.SYSTEM;
		try {
			// Create the local APIC accessor
			localAPIC = new LocalAPIC(rm, owner, mpConfigTable
					.getLocalApicAddress());
		} catch (ResourceNotFreeException ex) {
			BootLog.error("Cannot claim APIC region");
			return;
		}

		// Set the APIC reference of the current (bootstrap) processor
		final VmX86Processor cpu = (VmX86Processor) Unsafe
				.getCurrentProcessor();
		cpu.setApic(localAPIC);
		cpu.loadAndSetApicID();

		// Find & initialize this I/O APIC.
		for (Iterator i = mpConfigTable.entries().iterator(); i.hasNext();) {
			final MPEntry entry = (MPEntry) i.next();
			if (entry instanceof MPIOAPICEntry) {
				final MPIOAPICEntry apicEntry = (MPIOAPICEntry) entry;
				if (apicEntry.getFlags() != 0) {
					try {
						// We found an enabled I/O APIC.
						final IOAPIC apic = new IOAPIC(rm, owner, apicEntry
								.getAddress());
						apic.dump(System.out);
						break;
					} catch (ResourceNotFreeException ex) {
						BootLog.error("Cannot claim I/O APIC region ", ex);
					}
				}
			}
		}

		try {
			// Detect Hyper threading on current (bootstrap) processor
			VmX86Processor.detectAndstartLogicalProcessors(rm);
		} catch (ResourceNotFreeException ex) {
			BootLog.error("Cannot claim region for logical processor startup",
					ex);
		}

		// Find all CPU's
		for (Iterator i = mpConfigTable.entries().iterator(); i.hasNext();) {
			final MPEntry e = (MPEntry) i.next();
			if (e.getEntryType() == 0) {
				final MPProcessorEntry cpuEntry = (MPProcessorEntry) e;
				if (cpuEntry.isEnabled() && !cpuEntry.isBootstrap()) {
					// New CPU
					final VmX86Processor newCpu = (VmX86Processor) createProcessor(
							cpuEntry.getApicID(), Vm.getVm().getStatics());
					initX86Processor(newCpu);
					try {
						newCpu.startup(rm);
					} catch (ResourceNotFreeException ex) {
						BootLog
								.error(
										"Cannot claim region for processor startup",
										ex);
					}
				}
			}
		}
	}

	/**
	 * Initialize a processor wrt. APIC and add it to the list of processors.
	 * 
	 * @param cpu
	 */
	final void initX86Processor(VmX86Processor cpu) {
		cpu.setApic(localAPIC);
		super.addProcessor(cpu);
	}
}