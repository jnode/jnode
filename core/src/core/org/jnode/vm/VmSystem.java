/**
 * $Id$
 */

package org.jnode.vm;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.vm.classmgr.AbstractExceptionHandler;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmCompiledExceptionHandler;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.VmWriteBarrier;

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

    private static VmSystemClassLoader systemLoader;

    private static String cmdLine;

    private static volatile long currentTimeMillis;

    private static long rtcIncrement;

    private static RTCService rtcService;

    private static SystemOutputStream bootOut;

    private static MemoryResource initJar;

    private static PrintStream out;

    private static final String LAYOUT = "%-5p [%c{1}]: %m%n";

    /**
     * Initialize the Virtual Machine
     */
    public static void initialize() {
        if (!inited) {
            // Initialize resource manager
            final ResourceManager rm = ResourceManagerImpl.initialize();

            /* Set System.err, System.out */
            bootOut = new SystemOutputStream();
            //final ScreenOutputStream os = new
            // ScreenOutputStream(Screen.getInstance());
            final PrintStream ps = new PrintStream(bootOut, true);
            VmSystem.out = ps;
            System.setOut(ps);
            System.setErr(ps);

            /* Initialize the system classloader */
            VmSystemClassLoader loader = (VmSystemClassLoader) (getVmClass(Unsafe
                    .getCurrentProcessor()).getLoader());
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

            // Load the command line
            final Properties props = System.getProperties();
            props.setProperty("jnode.cmdline", getCmdLine());

            // Make sure that we have the default locale,
            // otherwise String.toLowerCase fails because it needs itself
            // via Locale.getDefault.
            Locale.getDefault();

            // Calibrate the processors
            Unsafe.getCurrentProcessor().calibrate();

            // Load the initial jarfile
            initJar = loadInitJar(rm);

            // Start the compilation manager
            vm.startHotMethodManager();
            
            // Initialize log4j
    		final Logger root = Logger.getRootLogger();
    		final ConsoleAppender infoApp = new ConsoleAppender(new PatternLayout(LAYOUT));
    		root.addAppender(infoApp);
        }
    }

    /**
     * Load the initial jarfile.
     * 
     * @param rm
     * @return The initial jarfile resource, or null if no initial jarfile is
     *         available.
     */
    private static MemoryResource loadInitJar(ResourceManager rm) {
        final Address start = Unsafe.getInitJarStart();
        final Address end = Unsafe.getInitJarEnd();
        final long size = Address.distance(end, start);
        if (size == 0L) {
            // No initial jarfile
            BootLog.info("No initial jarfile found");
            return null;
        } else {
            BootLog.info("Found initial jarfile of " + size + "b");
            try {
                final ResourceOwner owner = new SimpleResourceOwner("System");
                return rm.claimMemoryResource(owner, start, size,
                        ResourceManager.MEMMODE_NORMAL);
            } catch (ResourceNotFreeException ex) {
                BootLog.error("Cannot claim initjar resource", ex);
                return null;
            }
        }
    }

    // ------------------------------------------
    // Information
    // ------------------------------------------

    public static Properties getInitProperties() {

        final String arch;
        final Vm vm = Vm.getVm();
        arch = vm.getArch().getName();

        final Properties res = new Properties();
        // Java properties
        res.put("java.version", "1.1.0");
        res.put("java.vendor", "JNode.org");
        res.put("java.vendor.url", "http://jnode.org");
        res.put("java.home", "/");
        res.put("java.vm.specification.version", "1.4");
        res.put("java.vm.specification.vendor", "JNode.org");
        res.put("java.vm.specification.name", "jnode");
        res.put("java.vm.version", vm.getVersion());
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
        res.put("os.version", vm.getVersion());
        res.put("file.separator", "/");
        res.put("path.separator", ":");
        res.put("line.separator", "\n");
        res.put("user.name", "System");
        res.put("user.home", "/");
        res.put("user.dir", "/");

        // Log4j properties
        res.put("log4j.defaultInitOverride", "true");
        
        // GNU properties
        res.put("gnu.java.io.encoding_scheme_alias.US-ASCII", "ISO8859-1");
        res.put("gnu.java.io.encoding_scheme_alias.UTF-16LE", "UTF16LE");
        res.put("gnu.java.io.encoding_scheme_alias.UTF-16BE", "UTF16BE");

        return res;
    }

    private static String getCmdLine() {
        if (cmdLine == null) {
            /* Load the command line */
            final int cmdLineSize = Unsafe.getCmdLine(null);
            final byte[] cmdLineArr = new byte[ cmdLineSize];
            Unsafe.getCmdLine(cmdLineArr);
            cmdLine = new String(cmdLineArr).trim();
        }
        return cmdLine;
    }

    /**
     * Gets the log of the bootstrap phase.
     * 
     * @return String
     */
    public static String getBootLog() {
        return bootOut.getData();
    }

    // ------------------------------------------
    // java.lang.Object support
    // ------------------------------------------

    /**
     * Gets the class of the given object
     * 
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
        return getContextClassLoader().asClassLoader().loadClass(className);
    }

    /**
     * Gets the first non-system classloader out of the current stacktrace, or
     * the system classloader if no other classloader is found in the current
     * stacktrace.
     * 
     * @return The classloader
     */
    protected static VmClassLoader getContextClassLoader() {
        final VmStackReader reader = Unsafe.getCurrentProcessor()
                .getArchitecture().getStackReader();
        final VmSystemClassLoader systemLoader = VmSystem.systemLoader;
        Address f = Unsafe.getCurrentFrame();
        while (reader.isValid(f)) {
            final VmMethod method = reader.getMethod(f);
            final VmClassLoader loader = method.getDeclaringClass().getLoader();
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
        final VmStackReader reader = Unsafe.getCurrentProcessor()
                .getArchitecture().getStackReader();
        final VmStackFrame[] stack = reader.getVmStackTrace(Unsafe
                .getCurrentFrame(), null, STACKTRACE_LIMIT);
        final int count = stack.length;
        final Class[] result = new Class[ count];

        for (int i = 0; i < count; i++) {
            result[ i] = stack[ i].getMethod().getDeclaringClass().asClass();
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
            return Vm.getVm().getHeapManager()
                    .newInstance(
                            systemLoader.loadClass(
                                    "org.jnode.vm.VmSystemObject", true), size);
        } catch (ClassNotFoundException ex) {
            throw (NoClassDefFoundError) new NoClassDefFoundError()
                    .initCause(ex);
        }
    }

    /**
     * Gets the stacktrace of a given thread.
     * 
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

        if (Vm.getVm().getHeapManager().isLowOnMemory()) { return null; }

        final VmProcessor proc = Unsafe.getCurrentProcessor();
        final VmStackReader reader = proc.getArchitecture().getStackReader();
        final VmStackFrame[] mt;
        //Address lastIP = null;
        if (current.isInSystemException()) {
            proc.disableReschedule();
            try {
                mt = reader.getVmStackTrace(current.getExceptionStackFrame(),
                        current.getExceptionInstructionPointer(),
                        STACKTRACE_LIMIT);
            } finally {
                proc.enableReschedule();
            }
        } else if (current == proc.getCurrentThread()) {
            final Address curFrame = Unsafe.getCurrentFrame();
            mt = reader.getVmStackTrace(reader.getPrevious(curFrame), reader
                    .getReturnAddress(curFrame), STACKTRACE_LIMIT);
        } else {
            proc.disableReschedule();
            try {
                mt = reader.getVmStackTrace(current.getStackFrame(), current
                        .getInstructionPointer(), STACKTRACE_LIMIT);
                //lastIP = current.getInstructionPointer();
            } finally {
                proc.enableReschedule();
            }
        }
        final int cnt = (mt == null) ? 0 : mt.length;

        VmType lastClass = null;

        int i = 0;
        while (i < cnt) {

            final VmStackFrame f = mt[ i];
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
            if ((lastClass != null) && (sClass != lastClass)
                    && (vmClass != lastClass)) {
                break;
            }
            final String mname = method.getName();
            if (mname == null) {
                break;
            }
            if (!("<init>".equals(mname) || "fillInStackTrace".equals(mname) || "getStackTrace"
                    .equals(mname))) {
                break;
            }
            lastClass = vmClass;
            i++;
        }

        final VmStackFrame[] st = new VmStackFrame[ cnt - i];
        int j = 0;
        for (; i < cnt; i++) {
            st[ j++] = mt[ i];
        }

        current.inException = false;
        return st;
    }

    /**
     * Find an exception handler to handle the given exception in the given
     * frame at the given address.
     * 
     * @param ex
     * @param frame
     * @param address
     * @return Object
     */
    public static Address findThrowableHandler(Throwable ex, Address frame,
            Address address) throws PragmaPrivilegedAction {

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
            final VmStackReader reader = proc.getArchitecture()
                    .getStackReader();
            final int magic = reader.getMagic(frame);
            final boolean interpreted = ((magic & VmStackFrame.MAGIC_MASK) == VmStackFrame.MAGIC_INTERPRETED);
            final boolean compiled = ((magic & VmStackFrame.MAGIC_MASK) == VmStackFrame.MAGIC_COMPILED);
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
            final VmCompiledCode cc = method.getCompiledCode(magic);
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
                        final VmType handlerClass = catchType
                                .getResolvedVmClass();
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
                            System.err
                                    .println("Warning: handler class==null in "
                                            + method.getName());
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
            try {
                ex2.printStackTrace();
            } finally {
                Unsafe.die("findThrowableHandler");
            }
            return null;
        }
    }

    // ------------------------------------------
    // java.lang.System support
    // ------------------------------------------

    public static void arrayCopy(Object src, int srcPos, Object dst,
            int dstPos, int length) throws PragmaPrivilegedAction {
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

        if (srcPos < 0) { throw new IndexOutOfBoundsException("srcPos < 0"); }
        if (dstPos < 0) { throw new IndexOutOfBoundsException("dstPos < 0"); }
        if (length < 0) { throw new IndexOutOfBoundsException("length < 0"); }

        final int slotSize = Unsafe.getCurrentProcessor().getArchitecture()
                .getReferenceSize();
        final int lengthOffset = VmArray.LENGTH_OFFSET * slotSize;
        final int dataOffset = VmArray.DATA_OFFSET * slotSize;
        final int srcLen = Unsafe.getInt(Unsafe.addressOf(src), lengthOffset);
        final int dstLen = Unsafe.getInt(Unsafe.addressOf(dst), lengthOffset);

        if (srcPos + length > srcLen) { throw new IndexOutOfBoundsException(
                "srcPos+length > src.length (" + srcPos + "+" + length + " > "
                        + srcLen + ")"); }
        if (dstPos + length > dstLen) { throw new IndexOutOfBoundsException(
                "dstPos+length > dst.length"); }

        final int elemsize;
        final boolean isObjectArray;
        switch (src_type) {
        case 'Z':
        // Boolean
        case 'B':
            // Byte
            elemsize = 1;
            isObjectArray = false;
            break;
        case 'C':
        // Character
        case 'S':
            // Short
            elemsize = 2;
            isObjectArray = false;
            break;
        case 'I':
        // Integer
        case 'F':
            // Float
            elemsize = 4;
            isObjectArray = false;
            break;
        case 'L':
            // Object
            elemsize = slotSize;
            isObjectArray = true;
            break;
        case 'J':
        // Long
        case 'D':
            // Double
            elemsize = 8;
            isObjectArray = false;
            break;
        default:
            //Unsafe.debug("uat:");
            //Unsafe.debug(src_type);
            //Unsafe.debug(src_name);
            throw new ArrayStoreException("Unknown array type");
        }

        Address srcPtr = Unsafe.add(Unsafe.addressOf(src), dataOffset
                + (srcPos * elemsize));
        Address dstPtr = Unsafe.add(Unsafe.addressOf(dst), dataOffset
                + (dstPos * elemsize));

        Unsafe.copy(srcPtr, dstPtr, length * elemsize);

        if (isObjectArray) {
            final VmWriteBarrier wb = Vm.getVm().getHeapManager()
                    .getWriteBarrier();
            if (wb != null) {
                wb.arrayCopyWriteBarrier(src, srcPos, srcPos + length);
            }
        }
    }

    /**
     * Returns the current time in milliseconds. Note that while the unit of
     * time of the return value is a millisecond, the granularity of the value
     * depends on the underlying operating system and may be larger. For
     * example, many operating systems measure time in units of tens of
     * milliseconds. See the description of the class Date for a discussion of
     * slight discrepancies that may arise between "computer time" and
     * coordinated universal time (UTC).
     * 
     * This method does call other methods and CANNOT be used in the low-level
     * system environment, where synchronization cannot be used. *
     * 
     * @return the difference, measured in milliseconds, between the current
     *         time and midnight, January 1, 1970 UTC
     */
    public static long currentTimeMillis() {

        if (rtcIncrement == 0) {
            try {
                final RTCService rtcService = VmSystem.rtcService;
                if (rtcService != null) {
                    final long rtcTime = rtcService.getTime();
                    if (rtcTime == 0L) {
                        // We don't have an RTC service yet, return an invalid,
                        // but for now good enough value
                        return currentTimeMillis;
                    } else {
                        rtcIncrement = rtcTime - currentTimeMillis;
                    }
                }
            } catch (Exception ex) {
                BootLog.error("Error getting rtcIncrement ", ex);
                rtcIncrement = 1;
            }
        }
        return currentTimeMillis + rtcIncrement;
    }

    /**
     * Returns the number of milliseconds since booting the kernel of JNode.
     * 
     * This method does not call any other method and CAN be used in the
     * low-level system environment, where synchronization cannot be used.
     * 
     * @return The current time of the kernel
     * @throws PragmaUninterruptible
     */
    public static long currentKernelMillis() throws PragmaUninterruptible {
        return currentTimeMillis;
    }

    /**
     * @return VmClassLoader
     */
    public static VmSystemClassLoader getSystemClassLoader() {
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

    static class SystemOutputStream extends OutputStream {

        private final StringBuffer data = new StringBuffer();

        /**
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b) throws IOException {
            final char ch = (char) (b & 0xFF);
            Unsafe.debug(ch);
            data.append(ch);
        }

        public String getData() {
            return data.toString();
        }
    }

    /**
     * @param rtcService
     *            The rtcService to set.
     */
    public static final void setRtcService(RTCService rtcService) {
        if (VmSystem.rtcService == null) {
            VmSystem.rtcService = rtcService;
        }
    }

    /**
     * @param rtcService
     *            The rtcService previously set.
     */
    public static final void resetRtcService(RTCService rtcService) {
        if (VmSystem.rtcService == rtcService) {
            VmSystem.rtcService = null;
        }
    }

    /**
     * @return Returns the initJar.
     */
    public static final MemoryResource getInitJar() {
        return initJar;
    }

    /**
     * @return Returns the out.
     */
    public static final PrintStream getOut() {
        return out;
    }
    
    /**
     * Calculate the speed of the current processor.
     * @return
     */
    final static float calculateJNodeMips() throws PragmaUninterruptible {
        final long millis = currentTimeMillis % 1000;
        while (millis == (currentTimeMillis % 1000)) {
            // Wait
        }
        long count = 0;
        float dummy = 0.0f;
        while (millis != (currentTimeMillis % 1000)) {
            count++;
            dummy += 0.5f;
        }
        return count / 100000.0f;
    }
}