/*
 * $Id$
 */
package org.jnode.build.x86;

import java.io.FileOutputStream;
import java.io.IOException;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.NativeStream.ObjectInfo;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.boot.Main;
import org.jnode.build.AbstractBootImageBuilder;
import org.jnode.build.BootClassInfo;
import org.jnode.build.BuildException;
import org.jnode.linker.Elf;
import org.jnode.linker.ElfLinker;
import org.jnode.plugin.PluginRegistry;
import org.jnode.vm.HeapManager;
import org.jnode.vm.MathSupport;
import org.jnode.vm.MonitorManager;
import org.jnode.vm.SoftByteCodes;
import org.jnode.vm.VmBootHeap;
import org.jnode.vm.VmDefaultHeap;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.VmThread;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmMethodCode;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.VmX86Processor;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author epr
 */
public class BootImageBuilder extends AbstractBootImageBuilder implements X86CompilerConstants {

	public static final int LOAD_ADDR = 1024 * 1024;
	private static final VmX86Processor processor = new VmX86Processor(0);

	/** The offset in our (java) image file to the initial jump to our main-method */
	public static final int JUMP_MAIN_OFFSET = ObjectLayout.objectAlign((ObjectLayout.HEADER_SLOTS + 1) * processor.getArchitecture().getReferenceSize());

	public static final int INITIALIZE_METHOD_OFFSET = 8;

	private final Label vmInvoke = new Label("vm_invoke");
	private final Label vmClassCompile = new Label("VmClass_compile");
	private final Label vmMethodCompile = new Label("VmMethod_compile");
	private final Label vmMethodRecordInvoke = new Label("VmMethod_recordInvoke");
	private final Label vmClassInitialize = new Label("VmClass_initialize");
	private final Label vmFindThrowableHandler = new Label("vm_findThrowableHandler");
	private final Label vmCurrentTimeMillis = new Label("VmSystem_currentTimeMillis");
	private final Label vmReschedule = new Label("VmProcessor_reschedule");
	private final Label sbcSystemException = new Label("SoftByteCodes_systemException");
	private final Label vmThreadRunThread = new Label("VmThread_runThread");
	private final Label vmCurProcessor = new Label("vmCurProcessor");

	/**
	 * Construct a new BootImageBuilder
	 */
	public BootImageBuilder() {
	}

	/**
	 * Create a platform specific native stream.
	 * @return The native stream
	 */
	protected NativeStream createNativeStream() {
		return new X86Stream(LOAD_ADDR, 64 * 1024, 16 * 1024 * 1024);
	}

	/**
	 * Create the default processor for this architecture.
	 * @return The processor
	 * @throws BuildException
	 */
	protected VmProcessor createProcessor() throws BuildException {
		return processor;
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
			new ElfLinker((X86Stream) os).loadElfObject(elf);
		} catch (IOException ex) {
			throw new BuildException(ex);
		}
	}

	/**
	 * Align the stream on a page boundary
	 * 
	 * @param os
	 * @throws BuildException
	 */
	protected void pageAlign(NativeStream os) throws BuildException {
		((X86Stream) os).align(4096);
	}

	/**
	 * Emit code to bootstrap the java image
	 * 
	 * @param os
	 * @param clInitCaller
	 * @param pluginRegistry
	 * @throws BuildException
	 */
	protected void initImageHeader(NativeStream os, Object clInitCaller, PluginRegistry pluginRegistry) throws BuildException {
		try {
			int startLength = os.getLength();

			VmType vmCodeClass = loadClass(VmMethodCode.class);
			X86Stream.ObjectInfo initObject = os.startObject(vmCodeClass);
			final int offset = os.getLength() - startLength;
			if (offset != JUMP_MAIN_OFFSET) {
				throw new BuildException("JUMP_MAIN_OFFSET is incorrect [" + offset + "] (set to Object headersize)");
			}

			final X86Stream os86 = (X86Stream) os;

			initMain(os86, pluginRegistry);
			initHeapManager(os86);
			initVmThread(os86);

			os.setObjectRef(new Label("$$Initial call to clInitCaller"));
			os86.writeCALL(clInitCaller);

			initCallMain(os86);

			initObject.markEnd();

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
	protected void linkNativeSymbols(NativeStream os) throws ClassNotFoundException, UnresolvedObjectRefException {
		/* Link VmClass_compile */
		VmType vmClassClass = loadClass(VmType.class);
		NativeStream.ObjectRef refJava = os.getObjectRef(vmClassClass.getMethod("compile", "()I"));
		os.getObjectRef(vmClassCompile).link(refJava);

		/* Link vm_class_initialize */
		refJava = os.getObjectRef(vmClassClass.getMethod("initialize", "()V"));
		os.getObjectRef(vmClassInitialize).link(refJava);

		/* Link VmMethod_compile */
		VmType vmMethodClass = loadClass(VmMethod.class);
		refJava = os.getObjectRef(vmMethodClass.getMethod("compile", "()V"));
		os.getObjectRef(vmMethodCompile).link(refJava);

		/* Link VmMethod_recordInvoke */
		refJava = os.getObjectRef(vmMethodClass.getMethod("recordInvoke", "()V"));
		os.getObjectRef(vmMethodRecordInvoke).link(refJava);

		/* Link VmMethod_Class */
		refJava = os.getObjectRef(vmMethodClass);
		os.getObjectRef(new Label("VmMethod_Class")).link(refJava);

		/* Link SoftByteCodes_systemException */
		VmType sbcClass = loadClass(SoftByteCodes.class);
		refJava = os.getObjectRef(sbcClass.getMethod("systemException", "(II)Ljava/lang/Throwable;"));
		os.getObjectRef(sbcSystemException).link(refJava);

		/* Link SoftByteCodes_resolveClass */
		refJava = os.getObjectRef(sbcClass.getMethod("resolveClass", "(Lorg/jnode/vm/classmgr/VmMethod;Lorg/jnode/vm/classmgr/VmConstClass;)Lorg/jnode/vm/classmgr/VmType;"));
		os.getObjectRef(new Label("SoftByteCodes_resolveClass")).link(refJava);

		/* Link SoftByteCodes_resolveField */
		refJava = os.getObjectRef(sbcClass.getMethod("resolveField", "(Lorg/jnode/vm/classmgr/VmMethod;Lorg/jnode/vm/classmgr/VmConstFieldRef;Z)Lorg/jnode/vm/classmgr/VmField;"));
		os.getObjectRef(new Label("SoftByteCodes_resolveField")).link(refJava);

		/* Link SoftByteCodes_resolveMethod */
		refJava =
			os.getObjectRef(sbcClass.getMethod("resolveMethod", "(Lorg/jnode/vm/classmgr/VmMethod;Lorg/jnode/vm/classmgr/VmConstMethodRef;)Lorg/jnode/vm/classmgr/VmMethod;"));
		os.getObjectRef(new Label("SoftByteCodes_resolveMethod")).link(refJava);

		/* Link SoftByteCodes_allocArray */
		refJava = os.getObjectRef(sbcClass.getMethod("allocArray", "(Lorg/jnode/vm/classmgr/VmType;I)Ljava/lang/Object;"));
		os.getObjectRef(new Label("SoftByteCodes_allocArray")).link(refJava);

		/* Link SoftByteCodes_allocMultiArray */
		refJava = os.getObjectRef(sbcClass.getMethod("allocMultiArray", "(Lorg/jnode/vm/classmgr/VmType;[I)Ljava/lang/Object;"));
		os.getObjectRef(new Label("SoftByteCodes_allocMultiArray")).link(refJava);

		/* Link SoftByteCodes_allocObject */
		refJava = os.getObjectRef(sbcClass.getMethod("allocObject", "(Lorg/jnode/vm/classmgr/VmType;I)Ljava/lang/Object;"));
		os.getObjectRef(new Label("SoftByteCodes_allocObject")).link(refJava);

		/* Link SoftByteCodes_allocPrimitiveArray */
		refJava = os.getObjectRef(sbcClass.getMethod("allocPrimitiveArray", "(II)Ljava/lang/Object;"));
		os.getObjectRef(new Label("SoftByteCodes_allocPrimitiveArray")).link(refJava);

		/* Link SoftByteCodes_anewarray */
		refJava = os.getObjectRef(sbcClass.getMethod("anewarray", "(Lorg/jnode/vm/classmgr/VmMethod;Lorg/jnode/vm/classmgr/VmType;I)Ljava/lang/Object;"));
		os.getObjectRef(new Label("SoftByteCodes_anewarray")).link(refJava);

		/* Link SoftByteCodes_unknownOpcode */
		refJava = os.getObjectRef(sbcClass.getMethod("unknownOpcode", "(II)V"));
		os.getObjectRef(new Label("SoftByteCodes_unknownOpcode")).link(refJava);

		VmType vmThreadClass = loadClass(VmThread.class);

		/* Link VmThread_runThread */
		refJava = os.getObjectRef(vmThreadClass.getMethod("runThread", "(Lorg/jnode/vm/VmThread;)V"));
		os.getObjectRef(vmThreadRunThread).link(refJava);

		/* Link VmProcessor_reschedule */
		VmType vmProcClass = loadClass(VmProcessor.class);
		refJava = os.getObjectRef(vmProcClass.getMethod("reschedule", "()V"));
		os.getObjectRef(vmReschedule).link(refJava);

		/* Link vmCurProcessor */
		refJava = os.getObjectRef(processor);
		os.getObjectRef(vmCurProcessor).link(refJava);

		/* Link VmSystem_currentTimeMillis */
		VmType vmSystemClass = loadClass(VmSystem.class);
		refJava = os.getObjectRef(((VmStaticField) vmSystemClass.getField("currentTimeMillis")).getStaticData());
		os.getObjectRef(vmCurrentTimeMillis).link(refJava);

		/* Link vm_findThrowableHandler */
		refJava =
			os.getObjectRef(vmSystemClass.getMethod("findThrowableHandler", "(Ljava/lang/Throwable;Lorg/jnode/vm/Address;Lorg/jnode/vm/Address;)Lorg/jnode/vm/Address;"));
		os.getObjectRef(vmFindThrowableHandler).link(refJava);

		/* Link MonitorManager_monitorEnter */
		VmType monMgrClass = loadClass(MonitorManager.class);
		refJava = os.getObjectRef(monMgrClass.getMethod("monitorEnter", "(Ljava/lang/Object;)V"));
		os.getObjectRef(new Label("MonitorManager_monitorEnter")).link(refJava);

		/* Link MonitorManager_monitorExit */
		refJava = os.getObjectRef(monMgrClass.getMethod("monitorExit", "(Ljava/lang/Object;)V"));
		os.getObjectRef(new Label("MonitorManager_monitorExit")).link(refJava);

		/* Link MathSupport_ldiv */
		VmType mathSupportClass = loadClass(MathSupport.class);
		refJava = os.getObjectRef(mathSupportClass.getMethod("ldiv", "(JJ)J"));
		os.getObjectRef(new Label("MathSupport_ldiv")).link(refJava);

		/* Link MathSupport_lrem */
		refJava = os.getObjectRef(mathSupportClass.getMethod("lrem", "(JJ)J"));
		os.getObjectRef(new Label("MathSupport_lrem")).link(refJava);

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

	}

	/**
	 * Emit code to call Main.vmMain
	 * 
	 * @param os
	 * @throws BuildException
	 * @throws ClassNotFoundException
	 */
	protected void initCallMain(X86Stream os) throws BuildException, ClassNotFoundException {
		VmType vmMainClass = loadClass(Main.class);
		VmMethod mainMethod = vmMainClass.getMethod(Main.MAIN_METHOD_NAME, Main.MAIN_METHOD_SIGNATURE);

		os.writeMOV_Const(Register.EAX, mainMethod);
		os.writeCALL(vmInvoke);
		os.writeRET(); // RET instruction
	}

	/**
	 * Emit code to initialize VmThread.
	 * 
	 * @param os
	 * @throws BuildException
	 * @throws ClassNotFoundException
	 */
	protected void initVmThread(X86Stream os) throws BuildException, ClassNotFoundException {
		VmType vmThreadClass = loadClass(VmThread.class);
		VmInstanceField threadStackField = (VmInstanceField) vmThreadClass.getField("stack");
		VmThread initialThread = processor.getCurrentThread();

		os.setObjectRef(new Label("$$Setup initial thread"));
		os.writeMOV_Const(Register.EBX, initialThread);

		/** Initialize initialStack.stack to Luser_stack */
		//os.writeMOV(Register.ECX, threadStackField.getOffset());
		os.writeMOV_Const(Register.EDX, initialStack);
		os.writeMOV(INTSIZE, Register.EBX, threadStackField.getOffset(), Register.EDX);
		//asm.assemble(os, "mov [ebx+ecx],edx");
	}

	/**
	 * Create the initial stack space.
	 * 
	 * @param os
	 * @param stackLabel
	 *            Label to the start of the stack space (low address)
	 * @param stackPtrLabel
	 *            Label to the initial stack pointer (on x86 high address)
	 * @throws BuildException
	 * @throws ClassNotFoundException
	 * @throws UnresolvedObjectRefException
	 */
	protected void createInitialStack(NativeStream os, Label stackLabel, Label stackPtrLabel) throws BuildException, ClassNotFoundException, UnresolvedObjectRefException {
		ObjectInfo objectInfo = os.startObject(loadClass(VmSystemObject.class));
		int stackOffset = os.setObjectRef(stackLabel).getOffset();
		int stackAddr = stackOffset + (int) os.getBaseAddr();
		// Low stack address
		os.write32(stackAddr + VmThread.STACK_OVERFLOW_LIMIT);
		// High stack address
		os.write32(stackAddr + VmThread.DEFAULT_STACK_SIZE);
		// The actual stack space
		for (int i = 8; i < VmThread.DEFAULT_STACK_SIZE; i++) {
			os.write8(0);
		}
		os.setObjectRef(stackPtrLabel);
		objectInfo.markEnd();
	}

	/**
	 * Emit code to initialize VmHeap.
	 * 
	 * @param os
	 * @throws BuildException
	 * @throws ClassNotFoundException
	 */
	protected void initHeapManager(X86Stream os) throws BuildException, ClassNotFoundException {
		os.setObjectRef(new Label("$$Initialize VmHeap"));
		VmType heapManagerClass = loadClass(HeapManager.class);
		loadClass(VmBootHeap.class);
		VmStaticField bootHeapField = (VmStaticField) heapManagerClass.getField(HeapManager.BOOT_HEAP_FIELD_NAME);
		VmStaticField firstHeapField = (VmStaticField) heapManagerClass.getField(HeapManager.FIRST_HEAP_FIELD_NAME);

		VmBootHeap bootHeap = new VmBootHeap();
		VmDefaultHeap firstHeap = new VmDefaultHeap();

		/* Set VmHeap.bootHeap */
		os.writeMOV_Const(Register.EBX, bootHeap);
		os.writeMOV_Const(Register.EAX, bootHeapField.getStaticData());
		os.writeMOV(INTSIZE, Register.EAX, 0, Register.EBX);
		//asm.assemble(os, "mov [eax],ebx");

		/* Set VmHeap.firstHeap */
		os.writeMOV_Const(Register.EBX, firstHeap);
		os.writeMOV_Const(Register.EAX, firstHeapField.getStaticData());
		os.writeMOV(INTSIZE, Register.EAX, 0, Register.EBX);
		//asm.assemble(os, "mov [eax],ebx");
	}

	/**
	 * Emit code to initialize org.jnode.boot.Main.
	 * 
	 * @param os
	 * @param registry
	 * @throws BuildException
	 * @throws ClassNotFoundException
	 */
	protected void initMain(X86Stream os, PluginRegistry registry) throws BuildException, ClassNotFoundException {
		os.setObjectRef(new Label("$$Initialize Main"));
		VmType mainClass = loadClass(Main.class);
		VmStaticField registryField = (VmStaticField) mainClass.getField(Main.REGISTRY_FIELD_NAME);

		/* Set Main.pluginRegistry */
		os.writeMOV_Const(Register.EBX, registry);
		os.writeMOV_Const(Register.EAX, registryField.getStaticData());
		os.writeMOV(INTSIZE, Register.EAX, 0, Register.EBX);
		//asm.assemble(os, "mov [eax],ebx");
	}

	protected void emitStaticInitializerCalls(NativeStream nativeOs, VmType[] bootClasses, Object clInitCaller) throws ClassNotFoundException {

		final X86Stream os = (X86Stream) nativeOs;
		X86Stream.ObjectInfo initCallerObject = os.startObject(loadClass(VmMethodCode.class));

		os.setObjectRef(clInitCaller);

		// Call VmClass.loadFromBootClassArray
		VmType vmClassClass = loadClass(VmType.class);
		VmMethod lfbcaMethod = vmClassClass.getMethod("loadFromBootClassArray", "([Lorg/jnode/vm/classmgr/VmType;)V");
		os.writeMOV_Const(Register.EAX, bootClasses);
		os.writePUSH(Register.EAX);
		os.writeMOV_Const(Register.EAX, lfbcaMethod);
		os.writeCALL(vmInvoke);

		// Now call all static initializers
		for (int i = 0;(i < bootClasses.length); i++) {
			VmType vmClass = bootClasses[i];
			if ((vmClass instanceof VmClassType) && (((VmClassType) vmClass).getInstanceCount() > 0)) {
				VmMethod clInit = vmClass.getMethod("<clinit>", "()V");
				if (clInit != null) {
					os.setObjectRef(clInitCaller + "$$" + vmClass.getName());
					log.warn("Missing static initializer in class " + vmClass.getName());
					/*
					 * os.writeMOV(X86Constants.rEAX, clInit);
					 */

				}
			}
		}
		os.write8(0xc3); // RET
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
			log.info("Creating image");
			FileOutputStream fos = new FileOutputStream(getDestFile());
			fos.write(os.getBytes(), 0, os.getLength());
			fos.close();

			/*
			 * log.info("Creating ELF image"); final long start = System.currentTimeMillis(); final
			 * Elf elf = ((X86Stream)os).toElf(); final long end = System.currentTimeMillis();
			 * log.info("... took " + (end-start) + "ms");
			 * elf.store(getDestFile().getAbsolutePath() + ".elf");
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

	/**
	 * Patch any fields in the header, just before the image is written to disk.
	 * 
	 * @param nativeOs
	 * @throws BuildException
	 */
	protected void patchHeader(NativeStream nativeOs) throws BuildException {
		final X86Stream os = (X86Stream) nativeOs;
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
			throw new BuildException("Non-matching load address, found 0x" + Integer.toHexString(loadAddr) + ", expected 0x" + Long.toHexString(os.getBaseAddr()));
		}

		os.set32(mb_hdr + MB_LOAD_END_ADDR, (int) os.getBaseAddr() + os.getLength());
		os.set32(mb_hdr + MB_BSS_END_ADDR, (int) os.getBaseAddr() + os.getLength());
	}
	
	/**
	 * Setup the boot classes
	 */
	protected void setupBootClasses() {
		super.setupBootClasses();
		final int core = BootClassInfo.F_ALL;
		final int nonCore = BootClassInfo.F_RESOLVEALL;
		add(new BootClassInfo("org.jnode.vm.compiler.x86", true, nonCore));
		add(new BootClassInfo("org.jnode.vm.x86", true, core));
	}
}
