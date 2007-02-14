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

package org.jnode.vm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteOrder;
import java.util.Locale;
import java.util.Properties;

import javax.naming.NameNotFoundException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginManager;
import org.jnode.security.JNodePermission;
import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.util.SystemInputStream;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.PrivilegedActionPragma;
import org.jnode.vm.annotation.SharedStatics;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.classmgr.AbstractExceptionHandler;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmCompiledExceptionHandler;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.VmWriteBarrier;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * System support for the Virtual Machine
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@SharedStatics
@MagicPermission
public final class VmSystem {

    public static final int RC_HANDLER = 0xFFFFFFFB;

    public static final int RC_DEFHANDLER = 0xFFFFFFF1;

    private static boolean inited;

    private static VmSystemClassLoader systemLoader;

    private static String cmdLine;

    private static volatile long currentTimeMillis;

    private static long mhz = -1;

    private static long rtcIncrement;

    private static RTCService rtcService;

    private static SystemOutputStream bootOut;

    private static PrintStream bootOutStream;

    private static MemoryResource initJar;

    private static PrintStream out;

    private static final String LAYOUT = "%-5p [%c{1}]: %m%n";

    private static boolean inShutdown = false;

    private static int exitCode = 0;

    static int debug = 0;

    /**
     * Initialize the Virtual Machine
     */
    public static void initialize() {
        if (!inited) {
            // Initialize resource manager
            final ResourceManager rm = ResourceManagerImpl.initialize();

            VmSystem.out = getSystemOut();

            /* Initialize the system classloader */
            VmSystemClassLoader loader = (VmSystemClassLoader) (getVmClass(VmProcessor
                    .current()).getLoader());
            systemLoader = loader;
            loader.initialize();

            // Initialize VmThread
            VmThread.initialize();

            final Vm vm = Vm.getVm();

            // Initialize the monitors for the heap manager
            Vm.getHeapManager().start();

            // Find & start all processors
            vm.initializeProcessors(rm);

            /* We're done initializing */
            inited = true;
            VmProcessor.current().systemReadyForThreadSwitch();

            // Load the command line
            final Properties props = System.getProperties();
            props.setProperty("jnode.cmdline", getCmdLine());

            // Make sure that we have the default locale,
            // otherwise String.toLowerCase fails because it needs itself
            // via Locale.getDefault.
            Locale.getDefault();

            // Calibrate the processors
            VmProcessor.current().calibrate();

            // Setup class loading & compilation service
            LoadCompileService.start();

            // Load the initial jarfile
            initJar = loadInitJar(rm);

            // Initialize log4j
            final Logger root = Logger.getRootLogger();
            final ConsoleAppender infoApp = new ConsoleAppender(
                    new PatternLayout(LAYOUT));
            root.addAppender(infoApp);
        }
    }

    final static boolean isInitialized() {
        return inited;
    }

    /**
     * Gets the system output stream.
     *
     * @return the system output stream
     */
    public static PrintStream getSystemOut() {
        if (bootOut == null) {
            bootOut = new SystemOutputStream();
            bootOutStream = new PrintStream(bootOut, true);
        }
        return bootOutStream;
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
        final Extent size = end.toWord().sub(start.toWord()).toExtent();
        if (size.toWord().isZero()) {
            // No initial jarfile
            BootLog.info("No initial jarfile found");
            return null;
        } else {
            BootLog.info("Found initial jarfile of " + size.toInt() + "b");
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

    /**
     * This method adds some default system properties
     * @param res
     */
    public static void insertSystemProperties(Properties res) {

        final Vm vm = Vm.getVm();
        final VmArchitecture arch = Vm.getArch();

        // Java properties
        res.put("java.version", "1.5");
        res.put("java.vendor", "JNode.org");
        res.put("java.vendor.url", "http://jnode.org");
        res.put("java.home", "/jifs/");
        res.put("java.vm.specification.version", "1.4");
        res.put("java.vm.specification.vendor", "JNode.org");
        res.put("java.vm.specification.name", "jnode");
        res.put("java.vm.version", vm.getVersion());
        res.put("java.vm.vendor", "JNode.org");
        res.put("java.vm.name", "JNode");
        res.put("java.class.version", "1.1");
        res.put("java.class.path", "");
        res.put("java.library.path", "");
        res.put("java.io.tmpdir", "/jnode/tmp");
        res.put("java.compiler", "Internal");
        res.put("java.ext.dirs", "");
        res.put("os.name", "JNode");
        res.put("os.arch", arch.getName());
        res.put("os.version", vm.getVersion());
        res.put("file.separator", "/");
        res.put("path.separator", ":");
        res.put("line.separator", "\n");
        res.put("user.name", "System");
        res.put("user.home", "/jnode/home");
        res.put("user.dir", "/");

        // Log4j properties
        res.put("log4j.defaultInitOverride", "true");

        // GNU properties
        res.put("gnu.cpu.endian",
                (arch.getByteOrder() == ByteOrder.BIG_ENDIAN) ? "big"
                        : "little");
        res.put("gnu.classpath.home.url", "system://");
        res.put("gnu.classpath.vm.shortname", "jnode");
        res.put("gnu.javax.swing.noGraphics2D", "true");
        // keep this property until transparency support works fine with all drivers
        res.put("org.jnode.awt.transparency", "true");
    }

    /**
     * Returns the commandline appended to the kernel by the bootloader (e.g. grub)
     * @return the commandline appended to the kernel
     */
    public static String getCmdLine() {
        if (cmdLine == null) {
            /* Load the command line */
            final int cmdLineSize = Unsafe.getCmdLine(null);
            final byte[] cmdLineArr = new byte[cmdLineSize];
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
    public static Class< ? > getClass(Object obj) {
        return getVmClass(obj).asClass();
    }

    /**
     * Gets the VmClass of the given object.
     * 
     * @param obj
     * @return VmClass
     */
    public static VmType< ? > getVmClass(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        } else {
            return VmMagic.getObjectType(obj);
        }
    }

    /**
     * Clone the given object
     * 
     * @param obj
     * @return Object
     */
    public static Object clone(Cloneable obj) {
        return Vm.getHeapManager().clone(obj);
    }

    /**
     * Gets the hashcode of the given object
     * 
     * @param obj
     * @return int
     */
    public static int getHashCode(Object obj) {
        if (obj == null) {
            // According to spec, null has zero as hashcode.
            return 0;
        } else {
            return ObjectReference.fromObject(obj).toAddress().toInt();
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
        final VmStackReader reader = VmProcessor.current().getArchitecture()
                .getStackReader();
        final VmSystemClassLoader systemLoader = VmSystem.systemLoader;
        Address f = VmMagic.getCurrentFrame();
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
        final VmStackReader reader = VmProcessor.current().getArchitecture()
                .getStackReader();
        final VmStackFrame[] stack = reader.getVmStackTrace(VmMagic
                .getCurrentFrame(), null, VmThread.STACKTRACE_LIMIT);
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

    @Internal
    public static final Object allocStack(int size) {
        try {
            return Vm.getHeapManager()
                    .newInstance(
                            systemLoader.loadClass(
                                    "org.jnode.vm.VmSystemObject", true), size);
        } catch (ClassNotFoundException ex) {
            throw (NoClassDefFoundError) new NoClassDefFoundError()
                    .initCause(ex);
        }
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
    @PrivilegedActionPragma
    public static Address findThrowableHandler(Throwable ex, Address frame,
            Address address) {

        try {
            debug++;

            if (ex == null) {
                Unsafe.debug("NPE");
                throw new NullPointerException();
            }
            if (frame == null) {
                Unsafe.debug("frame==null");
                return null;
            }
            final VmProcessor proc = VmProcessor.current();
            final VmStackReader reader = proc.getArchitecture()
                    .getStackReader();

            final VmType exClass = VmMagic.getObjectType(ex);
            final VmMethod method = reader.getMethod(frame);
            if (method == null) {
                Unsafe.debug("Unknown method");
                return null;
            }

            // if (interpreted) {
            /*
             * Screen.debug("{ex at pc:"); Screen.debug(pc); Screen.debug(" of " +
             * method.getBytecodeSize()); Screen.debug(method.getName());
             */
            // }
            final int count;
            final VmByteCode bc = method.getBytecode();
            final VmCompiledCode cc = reader.getCompiledCode(frame);
            if (bc != null) {
                count = bc.getNoExceptionHandlers();
            } else {
                count = 0;
            }
            // Screen.debug("eCount=" + count);
            for (int i = 0; i < count; i++) {
                final AbstractExceptionHandler eh;
                final VmCompiledExceptionHandler ceh;
                ceh = cc.getExceptionHandler(i);
                eh = ceh;
                boolean match;

                match = ceh.isInScope(address);

                if (match) {
                    final VmConstClass catchType = eh.getCatchType();

                    if (catchType == null) {
                        /* Catch all exceptions */
                        return Address.fromAddress(ceh.getHandler());
                    } else {
                        if (!catchType.isResolved()) {
                            SoftByteCodes.resolveClass(catchType);
                        }
                        final VmType handlerClass = catchType
                                .getResolvedVmClass();
                        if (handlerClass != null) {
                            if (handlerClass.isAssignableFrom(exClass)) {
                                return Address.fromAddress(ceh.getHandler());
                            }
                        } else {
                            System.err
                                    .println("Warning: handler class==null in "
                                            + method.getName());
                        }
                    }
                }
            }

            if (cc.contains(address)) {
                return Address.fromAddress(cc.getDefaultExceptionHandler());
            } else {
                return null;
            }
        } catch (Throwable ex2) {
            Unsafe.debug("Exception in findThrowableHandler");
            try {
                ex2.printStackTrace();
            } finally {
                Unsafe.die("findThrowableHandler");
            }
            return null;
        } finally {
            debug--;
        }
    }

    // ------------------------------------------
    // java.lang.System support
    // ------------------------------------------

    /**
     * Copy one array to another. This is the implementation for System.arraycopy in JNode
     * @param src
     * @param srcPos
     * @param dst
     * @param dstPos
     * @param length
     */
    @PrivilegedActionPragma
    public static void arrayCopy(Object src, int srcPos, Object dst,
            int dstPos, int length) {
        Class< ? > src_class = src.getClass();
        Class< ? > dst_class = dst.getClass();

        if (!src_class.isArray()) {
            // Unsafe.debug('!');
            throw new ArrayStoreException("src is not an array");
        }

        if (!dst_class.isArray()) {
            // Unsafe.debug("dst is not an array:");
            // Unsafe.debug(dst_class.getName());
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
            // Unsafe.debug("invalid array types:");
            // Unsafe.debug(src_class.getName());
            // Unsafe.debug(dst_class.getName());
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

        final int slotSize = VmProcessor.current().getArchitecture()
                .getReferenceSize();
        final Offset lengthOffset = Offset
                .fromIntSignExtend(VmArray.LENGTH_OFFSET * slotSize);
        final int dataOffset = VmArray.DATA_OFFSET * slotSize;

        final Address srcAddr = ObjectReference.fromObject(src).toAddress();
        final Address dstAddr = ObjectReference.fromObject(dst).toAddress();

        final int srcLen = srcAddr.loadInt(lengthOffset);
        final int dstLen = dstAddr.loadInt(lengthOffset);

        // Calc end index (if overflow, then will be < 0 )
        final int srcEnd = srcPos + length;
        final int dstEnd = dstPos + length;

        if ((srcEnd > srcLen) || (srcEnd < 0)) {
            throw new IndexOutOfBoundsException("srcPos+length > src.length ("
                    + srcPos + "+" + length + " > " + srcLen + ")");
        }
        if ((dstEnd > dstLen) || (dstEnd < 0)) {
            throw new IndexOutOfBoundsException("dstPos+length > dst.length");
        }

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
            // Unsafe.debug("uat:");
            // Unsafe.debug(src_type);
            // Unsafe.debug(src_name);
            throw new ArrayStoreException("Unknown array type");
        }

        final Address srcPtr = srcAddr.add(dataOffset + (srcPos * elemsize));
        final Address dstPtr = dstAddr.add(dataOffset + (dstPos * elemsize));
        final Extent size = Extent.fromIntZeroExtend(length * elemsize);

        Unsafe.copy(srcPtr, dstPtr, size);

        if (isObjectArray) {
            final VmWriteBarrier wb = Vm.getHeapManager().getWriteBarrier();
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
   * <p>
   * Returns the current value of a nanosecond-precise system timer.
   * The value of the timer is an offset relative to some arbitrary fixed
   * time, which may be in the future (making the value negative).  This
   * method is useful for timing events where nanosecond precision is
   * required.  This is achieved by calling this method before and after the
   * event, and taking the difference betweent the two times:
   * </p>
   * <p>
   * <code>long startTime = System.nanoTime();</code><br />
   * <code>... <emph>event code</emph> ...</code><br />
   * <code>long endTime = System.nanoTime();</code><br />
   * <code>long duration = endTime - startTime;</code><br />
   * </p>
   * <p>
   * Note that the value is only nanosecond-precise, and not accurate; there
   * is no guarantee that the difference between two values is really a
   * nanosecond.  Also, the value is prone to overflow if the offset
   * exceeds 2^63.
   * </p>
   *
   * @return the time of a system timer in nanoseconds.
   * @since 1.5
   */
    public static long nanoTime(){
	if (mhz == -1) {
	    long start = Unsafe.getCpuCycles();
	    try {
		Thread.sleep(1000);
	    } catch (Exception e) {
		// set some "random" value
		mhz = 1000;
	    }
	    long end = Unsafe.getCpuCycles();
	    mhz = end - start;
	    mhz = mhz / 1000000;
	}
	return Unsafe.getCpuCycles() / mhz;
    }

    /**
     * Returns the number of milliseconds since booting the kernel of JNode.
     * 
     * This method does not call any other method and CAN be used in the
     * low-level system environment, where synchronization cannot be used.
     * 
     * @return The current time of the kernel
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    public static long currentKernelMillis() {
        return currentTimeMillis;
    }

    /**
     * @return VmClassLoader
     */
    public static VmSystemClassLoader getSystemClassLoader() {
        return systemLoader;
    }

    /**
     * Returns the free memory in system ram
     * @return free memory in system ram
     */
    public static long freeMemory() {
        return Vm.getHeapManager().getFreeMemory();
    }

    /**
     * Returns the total amount of system memory
     * @return the total amount of system memory
     */
    public static long totalMemory() {
        return Vm.getHeapManager().getTotalMemory();
    }

    /**
     * Call the garbage collector
     */
    public static void gc() {
        Vm.getHeapManager().gc();
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

        /**
         * Returns the data written to the system output stream
         * @return data written to the system output stream
         */
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
     * 
     * @return the speed of the current processor in "JNodeMips"
     */
    @Uninterruptible
    public final static float calculateJNodeMips() {
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

    /**
     * Is the system shutting down.
     * @return if the system is shutting down 
     */
    public static boolean isShuttingDown() {
        return inShutdown;
    }

    /**
     * Gets the system exit code.
     * @return the system exit code
     */
    public static int getExitCode() {
        return exitCode;
    }

    /**
     * Halt the system. This method requires a JNodePermission("halt").
     * 
     * @param reset
     */
    @PrivilegedActionPragma
    public static void halt(boolean reset) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JNodePermission("halt"));
        }
        exitCode = (reset ? 1 : 0);
        inShutdown = true;
        try {
            final PluginManager pm = (PluginManager) InitialNaming
                    .lookup(PluginManager.NAME);
            pm.stopPlugins();
        } catch (NameNotFoundException ex) {
            System.err.println("Cannot find ServiceManager");
        }
    }

    /**
     * Set in to a new InputStream.
     * 
     * @param in
     *            the new InputStream
     * @see #setIn(InputStream)
     */
    public static void setIn(InputStream in) {
        SystemInputStream.getInstance().setIn(in);
    }

    /**
     * Set {@link #out}to a new PrintStream.
     * 
     * @param out
     *            the new PrintStream
     * @see #setOut(PrintStream)
     */
    @PrivilegedActionPragma
    public static void setOut(PrintStream out) {
        setStaticField(System.class, "out", out);
    }

    /**
     * Set err to a new PrintStream.
     * 
     * @param err
     *            the new PrintStream
     * @see #setErr(PrintStream)
     */
    @PrivilegedActionPragma
    public static void setErr(PrintStream err) {
        setStaticField(System.class, "err", err);
    }

    @PrivilegedActionPragma
    private static void setStaticField(Class< ? > clazz, String fieldName,
            Object value) {
        final VmStaticField f = (VmStaticField) clazz.getVmClass().getField(
                fieldName);
        final Object staticsTable;
        final Offset offset;
        if (f.isShared()) {
            staticsTable = VmMagic.currentProcessor().getSharedStaticsTable();
            offset = Offset.fromIntZeroExtend(f.getSharedStaticsIndex() << 2);
        } else {
            staticsTable = VmMagic.currentProcessor().getIsolatedStaticsTable();
            offset = Offset.fromIntZeroExtend(f.getIsolatedStaticsIndex() << 2);
        }
        final Address ptr = VmMagic.getArrayData(staticsTable);
        ptr.store(ObjectReference.fromObject(value), offset);
    }
}
