/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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

package org.jnode.vm.x86;

import java.nio.ByteOrder;
import java.util.Iterator;

import org.jnode.assembler.x86.X86Constants;
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
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.x86.compiler.l1a.X86Level1ACompiler;
import org.jnode.vm.x86.compiler.stub.X86StubCompiler;


/**
 * Architecture descriptor for the Intel X86 architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmX86Architecture extends VmArchitecture {

    /** The compilers */
    private final NativeCodeCompiler[] compilers;

    /** The compilers under test */
    private final NativeCodeCompiler[] testCompilers;

    /** The local APIC accessor, if any */
    private LocalAPIC localAPIC;

    /** The MP configuration table */
    private MPConfigTable mpConfigTable;

    /** The stackreader of this architecture */
    private final VmX86StackReader stackReader;

    /**
     * Initialize this instance using the default compiler.
     */
    public VmX86Architecture() {
        this("L1A");
    }

    /**
     * Initialize this instance.
     * 
     * @param compiler
     *            L1a to use L1A compiler, L1 compiler otherwise.
     */
    public VmX86Architecture(String compiler) {
        this.stackReader = new VmX86StackReader(getReferenceSize());
        this.compilers = new NativeCodeCompiler[2];
        this.compilers[0] = new X86StubCompiler();
        this.compilers[1] = new X86Level1ACompiler(getReferenceSize());
        this.testCompilers = null;
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
     * Gets the operating mode.
     * @return
     */
    public final X86Constants.Mode getMode() {
    	if (getReferenceSize() == 4) {
    		return X86Constants.Mode.BITS32;
    	} else {
    		return X86Constants.Mode.BITS64;
    	}
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
    protected final void initializeProcessors(ResourceManager rm) {

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
     * Create a processor instance for this architecture.
     * 
     * @return The processor
     */
    public abstract VmProcessor createProcessor(int id, VmStatics statics);

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
