/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.build.x86;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.tools.ant.Project;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.NativeStream.ObjectInfo;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.boot.Main;
import org.jnode.build.AbstractBootImageBuilder;
import org.jnode.build.AsmSourceInfo;
import org.jnode.build.BuildException;
import org.jnode.jnasm.JNAsm;
import org.jnode.linker.Elf;
import org.jnode.linker.ElfLinker;
import org.jnode.plugin.PluginRegistry;
import org.jnode.util.NumberUtils;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmMethodCode;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.MonitorManager;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmScheduler;
import org.jnode.vm.scheduler.VmThread;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.VmX86Architecture32;
import org.jnode.vm.x86.VmX86Architecture64;
import org.jnode.vm.x86.VmX86Processor;
import org.jnode.vm.x86.VmX86Processor32;
import org.jnode.vm.x86.VmX86Processor64;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86JumpTable;

/**
 * @author epr
 */
public class BootImageBuilder extends AbstractBootImageBuilder implements
    X86CompilerConstants {

    public static final int LOAD_ADDR = 1024 * 1024;

    public static final int INITIAL_OBJREFS_CAPACITY = 750000;

    public static final int INITIAL_SIZE = 64 * 1024 * 1024;

    private VmX86Processor processor;

    private String processorId;

    private VmX86Architecture arch;

    private VmSharedStatics sharedStatics;

    private int bits = 32;

    private boolean useVbe = false;
    private int vbeWidth = 0;
    private int vbeHeight = 0;
    private int vbeDepth = 0;

    /**
     * The offset in our (java) image file to the initial jump to our
     * main-method
     */
    public static final int JUMP_MAIN_OFFSET32 = ObjectLayout
        .objectAlign((ObjectLayout.HEADER_SLOTS + 1)
            * VmX86Architecture32.SLOT_SIZE);

    public static final int JUMP_MAIN_OFFSET64 = ObjectLayout
        .objectAlign((ObjectLayout.HEADER_SLOTS + 1)
            * VmX86Architecture64.SLOT_SIZE);

    public final int JUMP_MAIN_OFFSET() {
        switch (bits) {
            case 32:
                return JUMP_MAIN_OFFSET32;
            case 64:
                return JUMP_MAIN_OFFSET64;
            default:
                throw new IllegalArgumentException("Unknown bits " + bits);
        }
    }

    public static final int INITIALIZE_METHOD_OFFSET = 8;

    // private final Label vmInvoke = new Label("vm_invoke");
    private final Label vmFindThrowableHandler = new Label(
        "vm_findThrowableHandler");

    private final Label vmReschedule = new Label("VmProcessor_reschedule");

    private final Label sbcSystemException = new Label(
        "SoftByteCodes_systemException");

    private final Label vmThreadRunThread = new Label("VmThread_runThread");

    private final Label vmCurProcessor = new Label("vmCurProcessor");

    /**
     * Construct a new BootImageBuilder
     */
    public BootImageBuilder() {
        System.setProperty("file.encoding", "8859_1");
    }

    /**
     * Create a platform specific native stream.
     *
     * @return The native stream
     */
    protected NativeStream createNativeStream() {
        X86Constants.Mode mode = ((VmX86Architecture) getArchitecture())
            .getMode();
        return new X86BinaryAssembler(getCPUID(), mode, LOAD_ADDR,
            INITIAL_OBJREFS_CAPACITY, INITIAL_SIZE, INITIAL_SIZE);
    }

    /**
     * Create the default processor for this architecture.
     *
     * @return The processor
     * @throws BuildException
     */
    protected VmProcessor createProcessor(Vm vm, VmSharedStatics statics, VmIsolatedStatics isolatedStatics)
        throws BuildException {
        this.sharedStatics = statics;
        VmScheduler scheduler = new VmScheduler(getArchitecture());
        vm.setScheduler(scheduler);
        if (processor == null) {
            switch (bits) {
                case 32:
                    processor = new VmX86Processor32(0,
                        (VmX86Architecture32) getArchitecture(), statics, isolatedStatics, scheduler,
                        getCPUID());
                    break;
                case 64:
                    processor = new VmX86Processor64(0,
                        (VmX86Architecture64) getArchitecture(), statics, isolatedStatics, scheduler,
                        getCPUID());
                    break;
                default:
                    throw new BuildException("Unknown bits " + bits);
            }
        }
        return processor;
    }

    /**
     * Gets the target architecture.
     *
     * @return The target architecture
     * @throws BuildException
     */
    protected final VmArchitecture getArchitecture() throws BuildException {
        if (arch == null) {
            switch (bits) {
                case 32:
                    arch = new VmX86Architecture32(getJnodeCompiler());
                    break;
                case 64:
                    arch = new VmX86Architecture64(getJnodeCompiler());
                    break;
                default:
                    throw new BuildException("Unknown bits " + bits);
            }
        }
        return arch;
    }

    /**
     * Copy the kernel native code into the native stream.
     *
     * @param os
     * @throws BuildException
     */
    protected void copyKernel(NativeStream os) throws BuildException {
        try {
            Elf elf = Elf.newFromFile(getKernelFile().getCanonicalPath());
            // elf.print();
            new ElfLinker((X86BinaryAssembler) os).loadElfObject(elf);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }

        // Link the jump table entries
        for (int i = 0; i < X86JumpTable.TABLE_LENGTH; i++) {
            final Label lbl = new Label(X86JumpTable.TABLE_ENTRY_LABELS[i]);
            final int idx = (arch.getMode().is32()) ? i : i * 2;
            sharedStatics.setAddress(idx, lbl);
        }
    }

    /**
     * Align the stream on a page boundary
     *
     * @param os
     * @throws BuildException
     */
    protected void pageAlign(NativeStream os) throws BuildException {
        ((X86BinaryAssembler) os).align(4096);
    }

    /**
     * Emit code to bootstrap the java image
     *
     * @param os
     * @param clInitCaller
     * @param pluginRegistry
     * @throws BuildException
     */
    protected void initImageHeader(NativeStream os, Label clInitCaller, Vm vm,
                                   PluginRegistry pluginRegistry) throws BuildException {
        try {
            int startLength = os.getLength();

            VmType vmCodeClass = loadClass(VmMethodCode.class);
            final X86BinaryAssembler.ObjectInfo initObject = os
                .startObject(vmCodeClass);
            final int offset = os.getLength() - startLength;
            if (offset != JUMP_MAIN_OFFSET()) {
                throw new BuildException("JUMP_MAIN_OFFSET is incorrect ["
                    + offset + " instead of " + JUMP_MAIN_OFFSET()
                    + "] (set to Object headersize)");
            }

            final X86BinaryAssembler os86 = (X86BinaryAssembler) os;
            final Label introCode = new Label("$$introCode");

            os86.setObjectRef(new Label("$$jmp-introCode"));
            os86.writeJMP(introCode);
            initObject.markEnd();

            // The loading of class can emit object in between, so first load
            // all required classes here.
            loadClass(Main.class);
            loadClass(MonitorManager.class);
            loadClass(SoftByteCodes.class);
            loadClass(Vm.class);
            loadClass(VmMethod.class);
            loadClass(VmProcessor.class);
            loadClass(VmThread.class);
            loadClass(VmType.class);
            loadClass(VmSystem.class);
            loadClass(VmSystemObject.class);

            final X86BinaryAssembler.ObjectInfo initCodeObject = os
                .startObject(vmCodeClass);
            os86.setObjectRef(introCode);
            initMain(os86, pluginRegistry);
            initVm(os86, vm);
            // initHeapManager(os86, vm);
            initVmThread(os86);

            os.setObjectRef(new Label("$$Initial call to clInitCaller"));
            os86.writeCALL(clInitCaller);

            initCallMain(os86);

            initCodeObject.markEnd();

        } catch (ClassNotFoundException ex) {
            throw new BuildException(ex);
        }
    }

    /**
     * Link all undefined symbols from the kernel native code.
     *
     * @param os
     * @throws ClassNotFoundException
     * @throws UnresolvedObjectRefException
     */
    protected void linkNativeSymbols(NativeStream os)
        throws ClassNotFoundException, UnresolvedObjectRefException {
        NativeStream.ObjectRef refJava;

        /* Link VmMethod_compile */
        VmType vmMethodClass = loadClass(VmMethod.class);
        refJava = os.getObjectRef(vmMethodClass.getMethod("recompile", "()V"));
        os.getObjectRef(new Label("VmMethod_recompile")).link(refJava);

        final VmType vmThreadClass = loadClass(VmThread.class);

        /* Link VmThread_systemException */
        refJava = os.getObjectRef(vmThreadClass.getMethod("systemException",
            "(II)Ljava/lang/Throwable;"));
        os.getObjectRef(sbcSystemException).link(refJava);

        /* Link VmThread_runThread */
        refJava = os.getObjectRef(vmThreadClass.getMethod("runThread",
            "(Lorg/jnode/vm/scheduler/VmThread;)V"));
        os.getObjectRef(vmThreadRunThread).link(refJava);

        /* Link VmProcessor_reschedule */
        VmType vmProcClass = loadClass(VmProcessor.class);
        refJava = os.getObjectRef(vmProcClass.getMethod("reschedule", "()V"));
        os.getObjectRef(vmReschedule).link(refJava);

        /* Link vmCurProcessor */
        refJava = os.getObjectRef(processor);
        os.getObjectRef(vmCurProcessor).link(refJava);

        /* Set statics index of VmSystem_currentTimeMillis */
        final VmType vmSystemClass = loadClass(VmSystem.class);
        final int staticsIdx = ((VmStaticField) vmSystemClass
            .getField("currentTimeMillis")).getSharedStaticsIndex();
        final X86BinaryAssembler os86 = (X86BinaryAssembler) os;
        os86.set32(os.getObjectRef(new Label("currentTimeMillisStaticsIdx"))
            .getOffset(), staticsIdx);

        /* Link vm_findThrowableHandler */
        refJava = os
            .getObjectRef(vmSystemClass
                .getMethod(
                "findThrowableHandler",
                "(Ljava/lang/Throwable;Lorg/vmmagic/unboxed/Address;Lorg/vmmagic/unboxed/Address;)" +
                    "Lorg/vmmagic/unboxed/Address;"));
        os.getObjectRef(vmFindThrowableHandler).link(refJava);

        // Link Luser_esp
        refJava = os.getObjectRef(initialStackPtr);
        os.getObjectRef(new Label("Luser_esp")).link(refJava);

        // Link freeMemoryStart
        refJava = os.getObjectRef(imageEnd);
        os.getObjectRef(new Label("freeMemoryStart")).link(refJava);

        // Link bootHeapStart
        refJava = os.getObjectRef(bootHeapStart);
        os.getObjectRef(new Label("bootHeapStart")).link(refJava);

        // Link bootHeapEnd
        refJava = os.getObjectRef(bootHeapEnd);
        os.getObjectRef(new Label("bootHeapEnd")).link(refJava);

        // Link VmX86Processor_applicationProcessorMain
        final VmType x86ProcessorClass = loadClass(VmX86Processor.class);
        refJava = os.getObjectRef(x86ProcessorClass.getMethod(
            "applicationProcessorMain", "()V"));
        os.getObjectRef(new Label("VmX86Processor_applicationProcessorMain"))
            .link(refJava);

        // Link VmX86Processor_broadcastTimeSliceInterrupt
        refJava = os.getObjectRef(x86ProcessorClass.getMethod(
            "broadcastTimeSliceInterrupt", "()V"));
        os.getObjectRef(new Label("VmX86Processor_broadcastTimeSliceInterrupt"))
            .link(refJava);
    }

    /**
     * Emit code to call Main.vmMain
     *
     * @param os
     * @throws BuildException
     * @throws ClassNotFoundException
     */
    protected void initCallMain(X86BinaryAssembler os) throws BuildException,
        ClassNotFoundException {
        final VmType vmMethodClass = loadClass(VmMethod.class);
        final VmType vmMainClass = loadClass(Main.class);
        final VmMethod mainMethod = vmMainClass.getMethod(
            Main.MAIN_METHOD_NAME, Main.MAIN_METHOD_SIGNATURE);
        final VmInstanceField nativeCodeField = (VmInstanceField) vmMethodClass
            .getField("nativeCode");

        final GPR aax = os.isCode32() ? (GPR) X86Register.EAX : X86Register.RAX;

        os.writeMOV_Const(aax, mainMethod);
        os.writeCALL(aax, nativeCodeField.getOffset());
        os.writeRET(); // RET instruction
    }

    /**
     * Emit code to initialize VmThread.
     *
     * @param os
     * @throws BuildException
     * @throws ClassNotFoundException
     */
    protected void initVmThread(X86BinaryAssembler os) throws BuildException,
        ClassNotFoundException {
        final VmType vmThreadClass = loadClass(VmThread.class);
        final VmInstanceField threadStackField = (VmInstanceField) vmThreadClass
            .getField("stack");
        final VmInstanceField threadStackEndField = (VmInstanceField) vmThreadClass
            .getField("stackEnd");
        final VmType vmProcessorClass = loadClass(VmProcessor.class);
        final VmInstanceField procStackEndField = (VmInstanceField) vmProcessorClass
            .getField("stackEnd");
        final VmThread initialThread = processor.getCurrentThread();

        final GPR abx = os.isCode32() ? (GPR) X86Register.EBX : X86Register.RBX;
        final GPR adx = os.isCode32() ? (GPR) X86Register.EDX : X86Register.RDX;
        final int slotSize = arch.getReferenceSize();

        os.setObjectRef(new Label("$$Setup initial thread"));
        os.writeMOV_Const(abx, initialThread);

        /** Initialize initialStack.stack to Luser_stack */
        os.writeMOV_Const(adx, initialStack);
        os.writeMOV(adx.getSize(), abx, threadStackField.getOffset(), adx);
        // Calculate and set stackEnd
        os.writeLEA(adx, adx, VmThread.STACK_OVERFLOW_LIMIT_SLOTS * slotSize);
        os.writeMOV(adx.getSize(), abx, threadStackEndField.getOffset(), adx);

        // Set stackend in current processor
        os.writeMOV_Const(abx, processor);
        os.writeMOV(adx.getSize(), abx, procStackEndField.getOffset(), adx);
    }

    /**
     * Create the initial stack space.
     *
     * @param os
     * @param stackLabel    Label to the start of the stack space (low address)
     * @param stackPtrLabel Label to the initial stack pointer (on x86 high address)
     * @throws BuildException
     * @throws ClassNotFoundException
     * @throws UnresolvedObjectRefException
     */
    protected void createInitialStack(NativeStream os, Label stackLabel,
                                      Label stackPtrLabel) throws BuildException, ClassNotFoundException,
        UnresolvedObjectRefException {
        final ObjectInfo objectInfo = os
            .startObject(loadClass(VmSystemObject.class));
        final int stackOffset = os.setObjectRef(stackLabel).getOffset();
        final int stackAddr = stackOffset + (int) os.getBaseAddr();
        final int slotSize = arch.getReferenceSize();

        // Low stack address
        os.writeWord(stackAddr + VmThread.STACK_OVERFLOW_LIMIT_SLOTS * slotSize);
        // High stack address
        os.writeWord(stackAddr + VmThread.DEFAULT_STACK_SLOTS * slotSize);
        // The actual stack space
        for (int i = 2; i < VmThread.DEFAULT_STACK_SLOTS; i++) {
            os.writeWord(0);
        }
        os.setObjectRef(stackPtrLabel);
        objectInfo.markEnd();
    }

    /**
     * Emit code to initialize Vm.
     *
     * @param os
     * @throws BuildException
     * @throws ClassNotFoundException
     */
    protected void initVm(X86BinaryAssembler os, Vm vm) throws BuildException,
        ClassNotFoundException {
        os.setObjectRef(new Label("$$Initialize Vm"));
        VmType vmClass = loadClass(Vm.class);
        VmStaticField vmField = (VmStaticField) vmClass.getField("instance");

        final GPR abx = os.isCode32() ? (GPR) X86Register.EBX : X86Register.RBX;
        final GPR adi = os.isCode32() ? (GPR) X86Register.EDI : X86Register.RDI;
        final int slotSize = os.isCode32() ? 4 : 8;

        // Setup STATICS register (EDI/RDI)
        os.writeMOV_Const(adi, sharedStatics.getTable());

        /* Set Vm.instance */
        os.writeMOV_Const(abx, vm);
        final int vmOffset = (VmArray.DATA_OFFSET * slotSize)
            + (vmField.getSharedStaticsIndex() << 2);
        log("vmOffset " + NumberUtils.hex(vmOffset), Project.MSG_VERBOSE);
        os.writeMOV(abx.getSize(), adi, vmOffset, abx);
    }

    /**
     * Emit code to initialize org.jnode.boot.Main.
     *
     * @param os
     * @param registry
     * @throws BuildException
     * @throws ClassNotFoundException
     */
    protected void initMain(X86BinaryAssembler os, PluginRegistry registry)
        throws BuildException, ClassNotFoundException {
        os.setObjectRef(new Label("$$Initialize Main"));
        final VmType mainClass = loadClass(Main.class);
        final VmStaticField registryField = (VmStaticField) mainClass
            .getField(Main.REGISTRY_FIELD_NAME);

        final GPR abx = os.isCode32() ? (GPR) X86Register.EBX : X86Register.RBX;
        final GPR adi = os.isCode32() ? (GPR) X86Register.EDI : X86Register.RDI;
        final int slotSize = os.isCode32() ? 4 : 8;

        // Setup STATICS register (EDI/RDI)
        os.writeMOV_Const(adi, sharedStatics.getTable());

        /* Set Main.pluginRegistry */
        os.writeMOV_Const(abx, registry);
        final int rfOffset = (VmArray.DATA_OFFSET * slotSize)
            + (registryField.getSharedStaticsIndex() << 2);
        log("rfOffset " + NumberUtils.hex(rfOffset), Project.MSG_VERBOSE);
        os.writeMOV(abx.getSize(), adi, rfOffset, abx);
    }

    protected void emitStaticInitializerCalls(NativeStream nativeOs,
                                              VmType[] bootClasses, Object clInitCaller)
        throws ClassNotFoundException {

        final X86BinaryAssembler os = (X86BinaryAssembler) nativeOs;
        X86BinaryAssembler.ObjectInfo initCallerObject = os
            .startObject(loadClass(VmMethodCode.class));

        os.setObjectRef(clInitCaller);

        // Call VmClass.loadFromBootClassArray
        final VmType vmClassClass = loadClass(VmType.class);
        final VmMethod lfbcaMethod = vmClassClass.getMethod(
            "loadFromBootClassArray", "([Lorg/jnode/vm/classmgr/VmType;)V");
        final VmType vmMethodClass = loadClass(VmMethod.class);
        final VmInstanceField nativeCodeField = (VmInstanceField) vmMethodClass
            .getField("nativeCode");

        final GPR aax = os.isCode32() ? (GPR) X86Register.EAX : X86Register.RAX;
        final GPR abx = os.isCode32() ? (GPR) X86Register.EBX : X86Register.RBX;

        os.writeMOV_Const(aax, bootClasses);
        os.writePUSH(aax);
        os.writeMOV_Const(aax, lfbcaMethod);
        os.writeMOV(abx.getSize(), abx, aax, nativeCodeField.getOffset());
        os.writeCALL(abx);

        // Now call all static initializers
        for (int i = 0; (i < bootClasses.length); i++) {
            VmType vmClass = bootClasses[i];
            if ((vmClass instanceof VmClassType)
                && (((VmClassType) vmClass).getInstanceCount() > 0)) {
                VmMethod clInit = vmClass.getMethod("<clinit>", "()V");
                if (clInit != null) {
                    // os.setObjectRef(clInitCaller + "$$" + vmClass.getName());
                    log("Missing static initializer in class "
                        + vmClass.getName(), Project.MSG_WARN);
                }
            }
        }
        os.writeRET(); // RET
        os.align(4096);

        initCallerObject.markEnd();
    }

    /**
     * Save the native stream to destFile.
     *
     * @param os
     * @throws BuildException
     */
    protected void storeImage(NativeStream os) throws BuildException {
        try {
            log("Creating image");
            FileOutputStream fos = new FileOutputStream(getDestFile());
            fos.write(os.getBytes(), 0, os.getLength());
            fos.close();

            /*
                * log.info("Creating ELF image"); final long start =
                * System.currentTimeMillis(); final Elf elf =
                * ((X86Stream)os).toElf(); final long end =
                * System.currentTimeMillis(); log.info("... took " + (end-start) +
                * "ms"); elf.store(getDestFile().getAbsolutePath() + ".elf");
                */

        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    /**
     * Patch the Multiboot header
     */
    private static final int MB_MAGIC = 0x1BADB002;

    private static final int MB_LOAD_ADDR = 4 * 4;

    private static final int MB_LOAD_END_ADDR = 5 * 4;

    private static final int MB_BSS_END_ADDR = 6 * 4;

    private static final int MODE_TYPE = 8 * 4;
    private static final int WIDTH = 9 * 4;
    private static final int HEIGHT = 10 * 4;
    private static final int DEPTH = 11 * 4;

    /**
     * Patch any fields in the header, just before the image is written to disk.
     *
     * @param nativeOs
     * @throws BuildException
     */
    protected void patchHeader(NativeStream nativeOs) throws BuildException {
        final X86BinaryAssembler os = (X86BinaryAssembler) nativeOs;
        int mb_hdr = -1;
        for (int i = 0; i < 1024; i += 4) {
            if (os.get32(i) == MB_MAGIC) {
                mb_hdr = i;
                break;
            }
        }
        if (mb_hdr < 0) {
            throw new BuildException("Cannot find Multiboot header");
        }

        int loadAddr = os.get32(mb_hdr + MB_LOAD_ADDR);
        if (loadAddr != os.getBaseAddr()) {
            throw new BuildException("Non-matching load address, found 0x"
                + Integer.toHexString(loadAddr) + ", expected 0x"
                + Long.toHexString(os.getBaseAddr()));
        }

        os.set32(mb_hdr + MB_LOAD_END_ADDR, (int) os.getBaseAddr()
            + os.getLength());
        os.set32(mb_hdr + MB_BSS_END_ADDR, (int) os.getBaseAddr()
            + os.getLength());

        // initial wanted video mode (if possible)
        os.set32(mb_hdr + MODE_TYPE, 0);
        os.set32(mb_hdr + WIDTH, vbeWidth);
        os.set32(mb_hdr + HEIGHT, vbeHeight);
        os.set32(mb_hdr + DEPTH, vbeDepth);
    }

    /**
     * @return Returns the processorId.
     */
    public final String getCpu() {
        return this.processorId;
    }

    /**
     * @param processorId The processorId to set.
     */
    public final void setCpu(String processorId) {
        this.processorId = processorId;
    }

    protected X86CpuID getCPUID() {
        return X86CpuID.createID(processorId);
    }

    protected void logStatistics(NativeStream os) {
        final X86BinaryAssembler os86 = (X86BinaryAssembler) os;
        final int count = os86.getObjectRefsCount();
        if (count > INITIAL_OBJREFS_CAPACITY) {
            log("Increase BootImageBuilder.INITIAL_OBJREFS_CAPACITY to "
                + count + " for faster build.", Project.MSG_WARN);
        }
        final int size = os86.getLength();
        if (size > INITIAL_SIZE) {
            log("Increase BootImageBuilder.INITIAL_SIZE to " + size
                + " for faster build.", Project.MSG_WARN);
        }
    }

    /**
     * @see org.jnode.build.AbstractBootImageBuilder#setupCompileHighOptLevelPackages()
     */
    protected void setupCompileHighOptLevelPackages() {
        super.setupCompileHighOptLevelPackages();
        addCompileHighOptLevel("org.jnode.assembler.x86");
        addCompileHighOptLevel("org.jnode.system.x86");
        addCompileHighOptLevel("org.jnode.vm.x86");
    }

    /**
     * @see org.jnode.build.AbstractBootImageBuilder#cleanup()
     */
    protected void cleanup() {
        super.cleanup();
        this.processor = null;
        this.sharedStatics = null;
    }

    /**
     * Gets the number of bits this builder targets.
     *
     * @return The number of bits (32, 64)
     */
    public final int getBits() {
        return this.bits;
    }

    /**
     * Sets the number of bits this builder targets.
     *
     * @param bits
     */
    public final void setBits(int bits) {
        if ((bits != 32) && (bits != 64)) {
            throw new IllegalArgumentException("Unknown bits value " + bits);
        }
        this.bits = bits;
    }

    public final void setVbeMode(String videoMode) {
        System.out.println("videoMode=" + videoMode);
        if ((videoMode == null) || (videoMode.trim().length() == 0)) {
            useVbe = false;
            vbeWidth = 0;
            vbeHeight = 0;
            vbeDepth = 0;
        } else {
            StringTokenizer stok = new StringTokenizer(videoMode.trim().toLowerCase(), "x", false);
            if (stok.countTokens() != 3) {
                throw new IllegalArgumentException("linearFrameBuffer must be of the form '<width>x<height>x<depth>'");
            }

            vbeWidth = Integer.parseInt(stok.nextToken());
            if (vbeWidth <= 0) {
                throw new IllegalArgumentException("vbeWidth must be > 0");
            }

            vbeHeight = Integer.parseInt(stok.nextToken());
            if (vbeWidth <= 0) {
                throw new IllegalArgumentException("vbeHeight must be > 0");
            }

            vbeDepth = Integer.parseInt(stok.nextToken());
            if (vbeWidth <= 0) {
                throw new IllegalArgumentException("vbeDepth must be > 0");
            }

            useVbe = true;
        }
    }

    /**
     * Initialize the statics table.
     *
     * @see org.jnode.build.AbstractBootImageBuilder#initializeStatics(org.jnode.vm.classmgr.VmSharedStatics)
     */
    protected void initializeStatics(VmSharedStatics statics) throws BuildException {
        for (int i = 0; i < X86JumpTable.TABLE_LENGTH; i++) {
            final int idx = statics.allocAddressField();
            if (getArchitecture().getReferenceSize() == 4) {
                if (i != idx) {
                    throw new BuildException("JumpTable entry " + i + " must be at index " + i + " not " + idx);
                }
            } else {
                if ((i * 2) != idx) {
                    throw new BuildException("JumpTable entry " + i + " must be at index " + (i * 2) + " not " + idx);
                }
            }
        }
    }

    protected void compileKernel(NativeStream os, AsmSourceInfo sourceInfo) throws BuildException {
        try {
            final String version = getVersion();
            final int i_bist = getBits();
            final String bits = "BITS" + i_bist;
            final Map<String, String> symbols = new HashMap<String, String>();
            symbols.put(bits, "");
            symbols.put("JNODE_VERSION", "'" + version + "'");

            if (useVbe) {
                symbols.put("SETUP_VBE", "");
                log("Grub will setup linear framebuffer mode " + vbeWidth + "x" + vbeHeight + "x" + vbeDepth);
            }

            log("Compiling native kernel with JNAsm, Version " + version + ", " + i_bist + " bits");
            JNAsm.assembler(os, sourceInfo, symbols);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
}
