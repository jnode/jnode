/**
 * $Id$
 */

package org.jnode.vm;

import java.io.PrintStream;
import java.util.Properties;

import org.jnode.driver.cmos.RTCUtils;
import org.jnode.driver.console.Screen;
import org.jnode.driver.console.ScreenOutputStream;
import org.jnode.vm.classmgr.AbstractExceptionHandler;
import org.jnode.vm.classmgr.AbstractVmClassLoader;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmCompiledExceptionHandler;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * System support for the Virtual Machine
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmSystem {

	public static final int RC_HANDLER = 0xFFFFFFFB;
	public static final int RC_DEFHANDLER = 0xFFFFFFF1;

	private static final int STACKTRACE_LIMIT = 256;
	
	private static boolean inited;
	private static VmClassLoader systemLoader;
	private static String cmdLine;
	private static volatile long currentTimeMillis;
	private static long rtcIncrement;

	/**
	 * Initialize the Virtual Machine
	 */
	public static void initialize() {
		if (!inited) {
			// Initialize resource manager
			Unsafe.debug("1");
			ResourceManagerImpl.initialize();
			Unsafe.debug("2");

			/* Set System.err, System.out */
			//final SystemOutputStream os = new SystemOutputStream();
			final ScreenOutputStream os = new ScreenOutputStream(Screen.getInstance());
			final PrintStream ps = new PrintStream(os, true);
			System.setOut(ps);
			System.setErr(ps);
			
			Unsafe.debug("3");
			
			/* Initialize the system classloader */
			VmClassLoader loader = (VmClassLoader) (getVmClass(Unsafe.getCurrentProcessor()).getLoader());
			systemLoader = loader;
			loader.initialize();

			// Initialize VmThread
			VmThread.initialize();

			/* Initialize the monitor system */
			MonitorManager.initialize();

			final Vm vm = Vm.getVm();
			
			// Initialize the monitors for the heap manager
			vm.getHeapManager().start();
			
			/* We're done initializing */
			inited = true;
			Unsafe.getCurrentProcessor().systemReadyForThreadSwitch();

			// Start the compilation manager
			vm.startHotMethodManager();

			// Load the command line
			System.getProperties().setProperty("jnode.cmdline", getCmdLine());
		}
	}

	// ------------------------------------------
	// Information
	// ------------------------------------------

	public static Properties getInitProperties() {

		final String arch;
		arch = Unsafe.getCurrentProcessor().getArchitecture().getName();
		Unsafe.debug("arch=");
		Unsafe.debug(arch);

		final Properties res = new Properties();
		res.put("java.version", "1.1.0");
		res.put("java.vendor", "JNode.org");
		res.put("java.vendor.url", "http://jnode.org");
		res.put("java.home", "/");
		res.put("java.vm.specification.version", "1.4");
		res.put("java.vm.specification.vendor", "JNode.org");
		res.put("java.vm.specification.name", "jnode");
		res.put("java.vm.version", "0.1.5");
		res.put("java.vm.vendor", "JNode.org");
		res.put("java.vm.name", "JNode");
		res.put("java.class.version", "1.1");
		res.put("java.class.path", "");
		res.put("java.library.path", "");
		res.put("java.io.tmpdir", "/tmp");
		res.put("java.compiler", "Internal");
		res.put("java.ext.dirs", "");
		res.put("os.name", "JNode");
		res.put("os.arch", arch);
		res.put("os.version", "0.1.5");
		res.put("file.separator", "/");
		res.put("path.separator", ":");
		res.put("line.separator", "\n");
		res.put("user.name", "System");
		res.put("user.home", "/");
		res.put("user.dir", "/");

		return res;
	}

	private static String getCmdLine() {
		if (cmdLine == null) {
			/* Load the command line */
			final int cmdLineSize = Unsafe.getCmdLine(null);
			final byte[] cmdLineArr = new byte[cmdLineSize];
			Unsafe.getCmdLine(cmdLineArr);
			cmdLine = new String(cmdLineArr).trim();
		}
		return cmdLine;
	}

	// ------------------------------------------
	// java.lang.Object support
	// ------------------------------------------

	/**
	 * Gets the class of the given object
	 * @param obj
	 * @return The class
	 */
	public static Class getClass(Object obj) {
		return getVmClass(obj).asClass();
	}

	/**
	 * Gets the VmClass of the given object.
	 * 
	 * @param obj
	 * @return VmClass
	 */
	public static VmType getVmClass(Object obj) {
		if (obj == null) {
			throw new NullPointerException();
		} else {
			return Unsafe.getVmClass(obj);
		}
	}

	/**
	 * Gets the memory address of the given object
	 * 
	 * @param obj
	 * @return int
	 */
	public static Address addressOf(Object obj) {
		return Unsafe.addressOf(obj);
	}

	/**
	 * Clone the given object
	 * 
	 * @param obj
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public static Object clone(Object obj) throws CloneNotSupportedException {
		return Vm.getVm().getHeapManager().clone(obj);
	}

	/**
	 * Gets the hashcode of the given object
	 * 
	 * @param obj
	 * @return int
	 */
	public static int getHashCode(Object obj) {
		if (obj == null) {
			throw new NullPointerException();
		} else {
			return Unsafe.addressToInt(Unsafe.addressOf(obj));
		}
	}

	// ------------------------------------------
	// java.lang.Class support
	// ------------------------------------------

	public static Class forName(String className) throws ClassNotFoundException {
		return getContextClassLoader().loadClass(className, true).asClass();
	}

	/**
	 * Gets the first non-system classloader out of the current stacktrace, or the system
	 * classloader if no other classloader is found in the current stacktrace.
	 * @return The classloader
	 */
	protected static AbstractVmClassLoader getContextClassLoader() {
		final VmStackReader reader = Unsafe.getCurrentProcessor().getArchitecture().getStackReader();
		final VmClassLoader systemLoader = VmSystem.systemLoader;
		Address f = Unsafe.getCurrentFrame();
		while (reader.isValid(f)) {
			final VmMethod method = reader.getMethod(f);
			final AbstractVmClassLoader loader = method.getDeclaringClass().getLoader();
			if ((loader != null) && (loader != systemLoader)) {
				return loader;
			} else {
				f = reader.getPrevious(f);
			}
		}
		return systemLoader;
	}

	// ------------------------------------------
	// java.lang.SecurityManager support
	// ------------------------------------------

	/**
	 * Gets the current stacktrace as array of classes.
	 * 
	 * @return Class[]
	 */
	public static Class[] getClassContext() {
		final VmStackReader reader = Unsafe.getCurrentProcessor().getArchitecture().getStackReader();
		final VmStackFrame[] stack = reader.getVmStackTrace(Unsafe.getCurrentFrame(), null, STACKTRACE_LIMIT);
		final int count = stack.length;
		final Class[] result = new Class[count];

		for (int i = 0; i < count; i++) {
			result[i] = stack[i].getMethod().getDeclaringClass().asClass();
		}

		return result;
	}

	/**
	 * Do nothing, until interrupted by an interrupts.
	 */
	public static void idle() {
		Unsafe.idle();
	}

	protected static Object allocStack(int size) {
		try {
			return Vm.getVm().getHeapManager().newInstance(systemLoader.loadClass("org.jnode.vm.VmSystemObject", true), size);
		} catch (ClassNotFoundException ex) {
			throw (NoClassDefFoundError) new NoClassDefFoundError().initCause(ex);
		}
	}

	/**
	 * Gets the stacktrace of a given thread.
	 * @param current
	 * @return The stacktrace
	 */
	public static Object[] getStackTrace(VmThread current) {
		if (current.inException) {
			Unsafe.debug("Exception in getStackTrace");
			Unsafe.die("getStackTrace");
			return null;
		} else {
			current.inException = true;
		}

		if (Vm.getVm().getHeapManager().isLowOnMemory()) {
			return null;
		}

		final VmProcessor proc = Unsafe.getCurrentProcessor();
		final VmStackReader reader = proc.getArchitecture().getStackReader();
		final VmStackFrame[] mt;
		//Address lastIP = null;
		if (current == proc.getCurrentThread()) {
			final Address curFrame = Unsafe.getCurrentFrame();
			mt = reader.getVmStackTrace(reader.getPrevious(curFrame), reader.getReturnAddress(curFrame), STACKTRACE_LIMIT);
		} else {
			proc.disableReschedule();
			try {
				mt = reader.getVmStackTrace(current.getStackFrame(), current.getInstructionPointer(), STACKTRACE_LIMIT);
				//lastIP = current.getInstructionPointer();
			} finally {
				proc.enableReschedule();
			}
		}
		final int cnt = (mt == null) ? 0 : mt.length;

		VmType lastClass = null;

		int i = 0;
		while (i < cnt) {

			final VmStackFrame f = mt[i];
			if (f == null) {
				break;
			}
			final VmMethod method = f.getMethod();
			if (method == null) {
				break;
			}
			final VmType vmClass = method.getDeclaringClass();
			if (vmClass == null) {
				break;
			}
			final VmType sClass = vmClass.getSuperClass();
			if ((lastClass != null) && (sClass != lastClass) && (vmClass != lastClass)) {
				break;
			}
			final String mname = method.getName();
			if (mname == null) {
				break;
			}
			if (!("<init>".equals(mname) || "fillInStackTrace".equals(mname) || "getStackTrace".equals(mname))) {
				break;
			}
			lastClass = vmClass;
			i++;
		}

		final VmStackFrame[] st = new VmStackFrame[cnt - i];
		int j = 0;
		for (; i < cnt; i++) {
			st[j++] = mt[i];
		}

		current.inException = false;
		return st;
	}

	/**
	 * Find an exception handler to handle the given exception in the given frame at the given
	 * address.
	 * 
	 * @param ex
	 * @param frame
	 * @param address
	 * @return Object
	 */
	public static Address findThrowableHandler(Throwable ex, Address frame, Address address) {

		try {
			if (ex == null) {
				Unsafe.debug("NPE");
				throw new NullPointerException();
			}
			if (frame == null) {
				Unsafe.debug("frame==null");
				return null;
			}
			final VmProcessor proc = Unsafe.getCurrentProcessor();
			final VmStackReader reader = proc.getArchitecture().getStackReader();
			final int magic = reader.getMagic(frame);
			final boolean interpreted = (magic == VmStackFrame.MAGIC_INTERPRETED);
			final boolean compiled = (magic == VmStackFrame.MAGIC_COMPILED);
			if (!(interpreted || compiled)) {
				Unsafe.debug("Unknown magic");
				return null;
			}

			final VmType exClass = Unsafe.getVmClass(ex);
			final VmMethod method = reader.getMethod(frame);
			final int pc = reader.getPC(frame);
			if (method == null) {
				Unsafe.debug("Unknown method");
				return null;
			}

			//if (interpreted) {
			/*
			 * Screen.debug("{ex at pc:"); Screen.debug(pc); Screen.debug(" of " +
			 * method.getBytecodeSize()); Screen.debug(method.getName());
			 */
			//}

			final int count;
			final VmByteCode bc = method.getBytecode();
			final VmCompiledCode cc = method.getCompiledCode();
			if (bc != null) {
				count = bc.getNoExceptionHandlers();
			} else {
				count = 0;
			}
			//Screen.debug("eCount=" + count);
			for (int i = 0; i < count; i++) {
				final AbstractExceptionHandler eh;
				final VmCompiledExceptionHandler ceh;
				final VmInterpretedExceptionHandler ieh;
				if (compiled) {
					ceh = cc.getExceptionHandler(i);
					eh = ceh;
					ieh = null;
				} else {
					ieh = bc.getExceptionHandler(i);
					eh = ieh;
					ceh = null;
				}
				boolean match;

				if (compiled) {
					match = ceh.isInScope(address);
				} else {
					match = ieh.isInScope(pc);
				}

				if (match) {
					final VmConstClass catchType = eh.getCatchType();

					if (catchType == null) {
						/* Catch all exceptions */
						if (compiled) {
							return ceh.getHandler();
						} else {
							reader.setPC(frame, ieh.getHandlerPC());
							//Screen.debug("Found ANY handler");
							return Unsafe.intToAddress(RC_HANDLER);
						}
					} else {
						if (!catchType.isResolved()) {
							SoftByteCodes.resolveClass(catchType);
						}
						final VmType handlerClass = catchType.getResolvedVmClass();
						if (handlerClass != null) {
							if (handlerClass.isAssignableFrom(exClass)) {
								if (compiled) {
									return ceh.getHandler();
								} else {
									reader.setPC(frame, ieh.getHandlerPC());
									//Screen.debug("Found specific handler");
									return Unsafe.intToAddress(RC_HANDLER);
								}
							}
						} else {
							System.err.println("Warning: handler class==null in " + method.getName());
						}
					}
				}
			}

			if (compiled) {
				if (cc.contains(address)) {
					return cc.getDefaultExceptionHandler();
				} else {
					return null;
				}
			} else {
				//Screen.debug("Def.handler");
				return Unsafe.intToAddress(RC_DEFHANDLER);
			}
		} catch (Throwable ex2) {
			Unsafe.debug("Exception in findThrowableHandler");
			Unsafe.die("findThrowableHandler");
			return null;
		}
	}

	// ------------------------------------------
	// java.lang.System support
	// ------------------------------------------

	public static void arrayCopy(Object src, int srcPos, Object dst, int dstPos, int length) {
		Class src_class = src.getClass();
		Class dst_class = dst.getClass();

		if (!src_class.isArray()) {
			Unsafe.debug('!');
			throw new ArrayStoreException("src is not an array");
		}

		if (!dst_class.isArray()) {
			Unsafe.debug("dst is not an array:");
			Unsafe.debug(dst_class.getName());
			throw new ArrayStoreException("dst is not an array");
		}

		String src_name = src_class.getName();
		String dst_name = dst_class.getName();

		char src_type = src_name.charAt(1);
		char dst_type = dst_name.charAt(1);

		if (src_type == '[') {
			src_type = 'L';
		}
		if (dst_type == '[') {
			dst_type = 'L';
		}

		if (src_type != dst_type) {
			Unsafe.debug("invalid array types:");
			Unsafe.debug(src_class.getName());
			Unsafe.debug(dst_class.getName());
			throw new ArrayStoreException("Invalid array types");
		}

		if (srcPos < 0) {
			throw new IndexOutOfBoundsException("srcPos < 0");
		}
		if (dstPos < 0) {
			throw new IndexOutOfBoundsException("dstPos < 0");
		}
		if (length < 0) {
			throw new IndexOutOfBoundsException("length < 0");
		}

		final int slotSize = Unsafe.getCurrentProcessor().getArchitecture().getReferenceSize();
		final int lengthOffset = VmArray.LENGTH_OFFSET * slotSize;
		final int dataOffset = VmArray.DATA_OFFSET * slotSize;
		final int srcLen = Unsafe.getInt(Unsafe.addressOf(src), lengthOffset);
		final int dstLen = Unsafe.getInt(Unsafe.addressOf(dst), lengthOffset);

		if (srcPos + length > srcLen) {
			throw new IndexOutOfBoundsException("srcPos+length > src.length (" + srcPos + "+" + length + " > " + srcLen + ")");
		}
		if (dstPos + length > dstLen) {
			throw new IndexOutOfBoundsException("dstPos+length > dst.length");
		}

		int elemsize;
		switch (src_type) {
			case 'Z' : // Boolean
			case 'B' : // Byte
				elemsize = 1;
				break;
			case 'C' : // Character
			case 'S' : // Short
				elemsize = 2;
				break;
			case 'I' : // Integer
			case 'F' : // Float
				elemsize = 4;
				break;
			case 'L' : // Object
				elemsize = slotSize;
				break;
			case 'J' : // Long
			case 'D' : // Double
				elemsize = 8;
				break;
			default :
				//Unsafe.debug("uat:");
				//Unsafe.debug(src_type);
				//Unsafe.debug(src_name);
				throw new ArrayStoreException("Unknown array type");
		}

		Address srcPtr = Unsafe.add(Unsafe.addressOf(src), dataOffset + (srcPos * elemsize));
		Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), dataOffset + (dstPos * elemsize));

		Unsafe.copy(srcPtr, dstPtr, length * elemsize);
	}

	/**
	 * Returns the current time in milliseconds. Note that while the unit of time of the return
	 * value is a millisecond, the granularity of the value depends on the underlying operating
	 * system and may be larger. For example, many operating systems measure time in units of tens
	 * of milliseconds. See the description of the class Date for a discussion of slight
	 * discrepancies that may arise between "computer time" and coordinated universal time (UTC).
	 * 
	 * This method does call other methods and CANNOT be used in the low-level system environment,
	 * where synchronization cannot be used.
	 *  *
	 * @return the difference, measured in milliseconds, between the current time and midnight,
	 *         January 1, 1970 UTC
	 */
	public static long currentTimeMillis() {

		if (rtcIncrement == 0) {
			final long rtcTime = RTCUtils.getTime();
			if (rtcTime == 0L) {
				// We don't have an RTC service yet, return an invalid,
				// but for now good enough value
				return currentTimeMillis;
			} else {
				rtcIncrement = rtcTime - currentTimeMillis;
			}
		}
		return currentTimeMillis + rtcIncrement;
	}

	/**
	 * Returns the number of milliseconds since booting the kernel of JNode.
	 * 
	 * This method does not call any other method and CAN be used in the low-level system
	 * environment, where synchronization cannot be used.
	 * @return The current time of the kernel
	 * @throws PragmaUninterruptible
	 */
	public static long currentKernelMillis() throws PragmaUninterruptible {
		return currentTimeMillis;
	}

	/**
	 * @return VmClassLoader
	 */
	public static VmClassLoader getSystemClassLoader() {
		return systemLoader;
	}
	
	public static long freeMemory() {
		return Vm.getVm().getHeapManager().getFreeMemory();
	}
	
	public static long totalMemory() {
		return Vm.getVm().getHeapManager().getTotalMemory();
	}
	
	public static void gc() {
		Vm.getVm().getHeapManager().gc();
	}
}
