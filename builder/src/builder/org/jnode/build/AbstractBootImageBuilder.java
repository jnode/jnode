/**
 * $Id$
 */

package org.jnode.build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.tools.ant.Project;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.util.NumberUtils;
import org.jnode.vm.HeapHelperImpl;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmMethodCode;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.memmgr.def.DefaultHeapManager;
import org.jnode.vm.memmgr.def.VmBootHeap;
import org.jnode.vm.memmgr.def.VmDefaultHeap;

/**
 * Build the boot image from an assembler compiled bootstrap (in ELF format)
 * combined with the precompiled Java classes.
 */
public abstract class AbstractBootImageBuilder extends AbstractPluginsTask {

    /** System property set to indicate build time */
    public static final String BUILDTIME_PROPERTY = "org.jnode.buildtime";

    /**
     * Classname/packagename of those classes/packages that need highly
     * optimized compilation
     */
    private final HashSet compileHighOptLevelPackages = new HashSet();

    private File destFile;

    private File kernelFile;

    private File listFile;

    private File debugFile;

    private File jarFile;

    private VmSystemClassLoader clsMgr;

    private URL classesURL = null;

    private Set legalInstanceClasses;

    /** Set of jbects that should not yet be emitted */
    private final Set blockedObjects = new HashSet();

    protected static boolean debug = false;

    protected static final Label bootHeapStart = new Label("$$bootHeapStart");

    protected static final Label bootHeapEnd = new Label("$$bootHeapEnd");

    protected static final Label imageEnd = new Label("$$image_end");

    protected static final Label initialStack = new Label("$$initialStack");

    protected static final Label initialStackPtr = new Label(
            "$$initialStackPtr");

    private int totalHighMethodSize;

    private int totalHighMethods;

    private int totalLowMethodSize;

    private int totalLowMethods;

    /**
     * Construct a new BootImageBuilder
     */
    public AbstractBootImageBuilder() {
        setupCompileHighOptLevelPackages();
        legalInstanceClasses = setupLegalInstanceClasses();
    }

    public final void execute() throws BuildException {

        final long lmJar = jarFile.lastModified();
        final long lmKernel = kernelFile.lastModified();
        final long lmDest = destFile.lastModified();
        final long lmPIL = getPluginListFile().lastModified();

        final PluginList piList;
        final long lmPI;
        try {
            log("plugin-list: " + getPluginListFile(), Project.MSG_DEBUG);
            piList = getPluginList();
            lmPI = piList.lastModified();
        } catch (PluginException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }

        if ((lmJar < lmDest) && (lmKernel < lmDest) && (lmPIL < lmDest)
                && (lmPI < lmDest)) { 
        // No need to do anything, skip
        return; }

        if (debugFile != null) {
            debugFile.delete();
        }

        try {
            System.getProperties().setProperty(BUILDTIME_PROPERTY, "1");

            // Load the plugin descriptors
            final PluginRegistry piRegistry;
            piRegistry = new PluginRegistryModel(piList.getPluginList());
            //piRegistry = new
            // PluginRegistryModel(piList.getDescriptorUrlList());
            testPluginPrerequisites(piRegistry);

            /* Now create the processor */
            final VmArchitecture arch = getArchitecture();
            final NativeStream os = createNativeStream();
            clsMgr = new VmSystemClassLoader(classesURL, arch,
                    new BuildObjectResolver(os, this));
            blockedObjects.add(clsMgr);
            blockedObjects.add(clsMgr.getStatics());
            blockedObjects.add(clsMgr.getStatics().getTable());

            if (debug) {
                log("Building in DEBUG mode", Project.MSG_WARN);
            }

            // Create the VM
            final HeapHelper helper = new HeapHelperImpl(arch);
            final Vm vm = new Vm(arch, new DefaultHeapManager(clsMgr, helper,
                    clsMgr.getStatics()), clsMgr.getStatics(), debug);
            blockedObjects.add(vm);

            final VmProcessor proc = createProcessor(clsMgr.getStatics());
            log("Building for " + proc.getCPUID());

            final Label clInitCaller = new Label("$$clInitCaller");
            VmType systemClasses[] = VmType.initializeForBootImage(clsMgr);
            for (int i = 0; i < systemClasses.length; i++) {
                clsMgr.addLoadedClass(systemClasses[ i].getName(),
                        systemClasses[ i]);
            }

            // First copy the native kernel file
            copyKernel(os);
            os.setObjectRef(bootHeapStart);

            // Setup a call to our first java method
            initImageHeader(os, clInitCaller, vm, piRegistry);

            // Create the initial stack
            createInitialStack(os, initialStack, initialStackPtr);

            /* Now load the classes */
            loadClass(VmMethodCode.class);
            loadClass(Unsafe.class);
            loadClass(VmSystemClassLoader.class);
            loadClass(VmType[].class);
            loadClass(Vm.class);
            loadClass(VmBootHeap.class);
            loadClass(VmDefaultHeap.class);
            loadClass(VmHeapManager.class);
            loadClass(VmStatics.class);
            loadClass(vm.getHeapManager().getClass());
            loadClass(HeapHelper.class);
            loadClass(HeapHelperImpl.class);

            loadSystemClasses();

            /* Now emit the processor */
            os.getObjectRef(proc);

            /* Let the compilers load its native symbol offsets */
            final NativeCodeCompiler[] cmps = arch.getCompilers();
            for (int i = 0; i < cmps.length; i++) {
                final NativeCodeCompiler cmp = cmps[ i];
                cmp.initialize(clsMgr);
                os.getObjectRef(cmp);
            }

            // Load the jarfile as byte-array
            copyJarFile(os);

            // Now emit all object images to the actual image
            emitObjects(os, arch, blockedObjects);

            // Disallow the loading of new classes
            clsMgr.setFailOnNewLoad(true);
            emitObjects(os, arch, blockedObjects);

            // Emit the classmanager
            log("Emit vm", Project.MSG_VERBOSE);
            blockedObjects.remove(vm);
            emitObjects(os, arch, blockedObjects);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects);

            /* Set the bootclasses */
            log("prepare bootClassArray", Project.MSG_VERBOSE);
            final VmType bootClasses[] = clsMgr.prepareAfterBootstrap();
            os.getObjectRef(bootClasses);
            emitObjects(os, arch, blockedObjects);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects);

            // Emit the classmanager
            log("Emit clsMgr", Project.MSG_VERBOSE);
            // Turn auto-compilation on
            clsMgr.setCompileRequired();
            blockedObjects.remove(clsMgr);
            emitObjects(os, arch, blockedObjects);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects);

            // Emit the classmanager
            log("Emit statics", Project.MSG_VERBOSE);
            blockedObjects.remove(clsMgr.getStatics());
            emitObjects(os, arch, blockedObjects);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects);

            // Emit the remaining objects
            log("Emit rest; blocked=" + blockedObjects, Project.MSG_VERBOSE);
            emitObjects(os, arch, null);
            log("statics table 0x"
                    + NumberUtils.hex(os.getObjectRef(
                            clsMgr.getStatics().getTable()).getOffset()),
                    Project.MSG_VERBOSE);

            /* Write static initializer code */
            emitStaticInitializerCalls(os, bootClasses, clInitCaller);

            // This is the end of the image
            X86Stream.ObjectInfo dummyObjectAtEnd = os
                    .startObject(loadClass(VmMethodCode.class));
            pageAlign(os);
            dummyObjectAtEnd.markEnd();
            os.setObjectRef(imageEnd);
            os.setObjectRef(bootHeapEnd);

            /* Link all native symbols */
            linkNativeSymbols(os);

            // Patch multiboot header
            patchHeader(os);

            // Store the image
            storeImage(os);

            // Generate the listfile
            printLabels(os, bootClasses, clsMgr.getStatics());

            // Generate debug info
            for (int i = 0; i < cmps.length; i++) {
                cmps[ i].dumpStatistics();
            }
            final int bootHeapSize = os.getObjectRef(bootHeapEnd).getOffset()
                    - os.getObjectRef(bootHeapStart).getOffset();
            final int bootHeapBitmapSize = (bootHeapSize / ObjectLayout.OBJECT_ALIGN) >> 3;
            log("Boot heap size " + (bootHeapSize >>> 10) + "K bitmap size "
                    + (bootHeapBitmapSize >>> 10) + "K");
            clsMgr.getStatics().dumpStatistics(System.out);

            logStatistics(os);

            log("Optimized methods     : " + totalHighMethods + ", avg size "
                    + (totalHighMethodSize / totalHighMethods) + ", tot size "
                    + totalHighMethodSize);
            log("Ondemand comp. methods: " + totalLowMethods + ", avg size "
                    + (totalLowMethodSize / totalLowMethods) + ", tot size "
                    + totalLowMethodSize);

            log("Done.");
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new BuildException(ex);
        }
    }

    /**
     * Create a platform specific native stream.
     * 
     * @return NativeStream
     */
    protected abstract NativeStream createNativeStream();

    /**
     * Create the default processor for this architecture.
     * 
     * @return The processor
     * @throws BuildException
     */
    protected abstract VmProcessor createProcessor(VmStatics statics)
            throws BuildException;

    /**
     * Gets the target architecture.
     * 
     * @return The target architecture
     * @throws BuildException
     */
    protected abstract VmArchitecture getArchitecture() throws BuildException;

    /**
     * Copy the kernel native code into the native stream.
     * 
     * @param os
     * @throws BuildException
     */
    protected abstract void copyKernel(NativeStream os) throws BuildException;

    /**
     * Align the stream on a page boundary
     * 
     * @param os
     * @throws BuildException
     */
    protected abstract void pageAlign(NativeStream os) throws BuildException;

    /**
     * Copy the jnode.jar file into a byte array that is added to the java
     * image.
     * 
     * @param os
     * @throws BuildException
     */
    protected final void copyJarFile(NativeStream os) throws BuildException {

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(jarFile);
            final byte[] buf = new byte[ 4096];
            int len;
            while ((len = fis.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            fis.close();
            bos.close();

            final byte[] systemRtJar = bos.toByteArray();
            os.getObjectRef(systemRtJar);
            clsMgr.setSystemRtJar(systemRtJar);

        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    /**
     * Emit code to bootstrap the java image
     * 
     * @param os
     * @param clInitCaller
     * @param vm
     * @param pluginRegistry
     * @throws BuildException
     */
    protected abstract void initImageHeader(NativeStream os,
            Label clInitCaller, Vm vm, PluginRegistry pluginRegistry)
            throws BuildException;

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
    protected abstract void createInitialStack(NativeStream os,
            Label stackLabel, Label stackPtrLabel) throws BuildException,
            ClassNotFoundException, UnresolvedObjectRefException;

    /**
     * Link all undefined symbols from the kernel native code.
     * 
     * @param os
     * @throws ClassNotFoundException
     * @throws UnresolvedObjectRefException
     */
    protected abstract void linkNativeSymbols(NativeStream os)
            throws ClassNotFoundException, UnresolvedObjectRefException;

    protected abstract void emitStaticInitializerCalls(NativeStream os,
            VmType[] bootClasses, Object clInitCaller)
            throws ClassNotFoundException;

    /**
     * Emit all objects to the native stream that have not yet been emitted to
     * this stream.
     * 
     * @param os
     * @param arch
     * @param blockObjects
     * @throws BuildException
     */
    private final void emitObjects(NativeStream os, VmArchitecture arch,
            Set blockObjects) throws BuildException {
        log("Emitting objects", Project.MSG_DEBUG);
        PrintWriter debugOut = null;
        final TreeSet emittedClassNames = new TreeSet();
        try {
            if (debug) {
                debugOut = new PrintWriter(new FileWriter(debugFile, true));
            }
            final ObjectEmitter emitter = new ObjectEmitter(clsMgr, os,
                    debugOut, legalInstanceClasses);
            final long start = System.currentTimeMillis();
            int cnt = 0;
            int lastUnresolved = -1;
            int loops = 0;
            while (true) {
                loops++;
                compileClasses(os, arch);
                final Collection objectRefs = new ArrayList(os.getObjectRefs());
                int unresolvedFound = 0; // Number of unresolved references
                // found in the following
                // loop
                int emitted = 0; // Number of emitted objects in the following
                // loop
                for (Iterator i = objectRefs.iterator(); i.hasNext();) {
                    X86Stream.ObjectRef ref = (X86Stream.ObjectRef) i.next();
                    if (!ref.isResolved()) {
                        final Object obj = ref.getObject();
                        if (!(obj instanceof Label)) {
                            unresolvedFound++;
                            if (obj instanceof VmType) {
                                final VmType vmtObj = (VmType) obj;
                                vmtObj.link();
                                if (!vmtObj.isCompiled()) {
                                    compileClasses(os, arch);
                                }
                            }

                            boolean skip;
                            if (blockObjects == null) {
                                skip = false;
                            } else {
                                skip = blockObjects.contains(obj);
                            }
                            /*
                             * if (obj instanceof VmMethod) { final VmMethod
                             * mObj = (VmMethod)obj; if (!mObj.hasNativeCode()) {
                             * compileClasses(os, arch); } if
                             * (!mObj.getDeclaringClass().isCompiled()) {
                             * log("Oops"); }
                             */

                            if (!skip) {
                                if (blockObjects == null) {
                                    emittedClassNames.add(obj.getClass()
                                            .getName());
                                    //log("emitObject " +
                                    // obj.getClass().getName());
                                }
                                //if (obj != skipMe) {
                                emitter.emitObject(obj);
                                emitted++;
                                X86Stream.ObjectRef newRef = os
                                        .getObjectRef(obj);
                                if (ref != newRef) { throw new RuntimeException(
                                        "Object has changed during emitObject! type="
                                                + obj.getClass().getName()); }
                                if (!ref.isResolved()) { throw new RuntimeException(
                                        "Unresolved reference to object "
                                                + ((obj == null) ? "null" : obj
                                                        .getClass().getName())); }
                            }
                        }
                    }
                }
                if (unresolvedFound == lastUnresolved) {
                    if (unresolvedFound == 0) {
                        break;
                    }
                    if (blockedObjects != null) {
                        if (unresolvedFound == (emitted + blockObjects.size())) {
                            //log("UnresolvedFound " + unresolvedFound + ",
                            // emitted " + emitted + ",blocked " +
                            // blockObjects.size());
                            break;
                        }
                        if ((emitted == 0) && !blockObjects.isEmpty()) {
                            break;
                        }
                    }
                }
                lastUnresolved = unresolvedFound;
                cnt += emitted;
            }
            final long end = System.currentTimeMillis();
            log("Emitted " + cnt + " objects, took " + (end - start) + "ms in "
                    + loops + " loops");
            if (debugOut != null) {
                debugOut.close();
                debugOut = null;
            }
            if (blockObjects == null) {
                log("Emitted classes: " + emittedClassNames,
                        Project.MSG_VERBOSE);
            }
        } catch (ClassNotFoundException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    /**
     * Load a VmClass for a given java.lang.Class
     * 
     * @param c
     * @return The loaded class
     * @throws ClassNotFoundException
     */
    public final VmType loadClass(Class c) throws ClassNotFoundException {
        String name = c.getName();
        VmType cls = clsMgr.findLoadedClass(name);
        if (cls != null) {
            return cls;
        } else if (c.isPrimitive()) {
            if ("boolean".equals(name)) {
                cls = VmType.getPrimitiveClass('Z');
            } else if ("byte".equals(name)) {
                cls = VmType.getPrimitiveClass('B');
            } else if ("char".equals(name)) {
                cls = VmType.getPrimitiveClass('C');
            } else if ("short".equals(name)) {
                cls = VmType.getPrimitiveClass('S');
            } else if ("int".equals(name)) {
                cls = VmType.getPrimitiveClass('I');
            } else if ("float".equals(name)) {
                cls = VmType.getPrimitiveClass('F');
            } else if ("long".equals(name)) {
                cls = VmType.getPrimitiveClass('J');
            } else if ("double".equals(name)) {
                cls = VmType.getPrimitiveClass('D');
            } else {
                throw new ClassNotFoundException(name
                        + " is not a primitive type");
            }
            clsMgr.addLoadedClass(name, cls);
            return cls;
        } else {
            return loadClass(name, true);
        }
    }

    /**
     * Load a VmClass with a given name
     * 
     * @param name
     * @param resolve
     * @return The loaded class
     * @throws ClassNotFoundException
     */
    public final VmType loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        /*
         * if (clsMgr == null) { clsMgr = new VmClassLoader(classesURL);
         */
        return clsMgr.loadClass(name, resolve);
    }

    /**
     * Compile the methods in the given class to native code.
     * 
     * @param os
     * @throws ClassNotFoundException
     */
    private final void compileClasses(NativeStream os, VmArchitecture arch)
            throws ClassNotFoundException {
        final NativeCodeCompiler[] compilers = arch.getCompilers();
        final int optLevel = compilers.length - 1;
        // Use the most optimizing compiler here
        final NativeCodeCompiler compiler = compilers[ optLevel];

        int oldCount;
        int newCount;
        boolean again;
        do {
            again = false;
            oldCount = clsMgr.getLoadedClassCount();
            for (Iterator i = clsMgr.getLoadedClasses().iterator(); i.hasNext();) {
                final VmType vmClass = (VmType) i.next();
                vmClass.link();
                final boolean compHigh = isCompileHighOptLevel(vmClass);
                if (!vmClass.isCpRefsResolved() && compHigh) {
                    //log("Resolving CP of " + vmClass.getName(),
                    //Project.MSG_VERBOSE);
                    vmClass.resolveCpRefs(clsMgr);
                    again = true;
                }
                try {
                    final int mcnt;
                    final int startLength = os.getLength();
                    if (compHigh) {
                        log("Full Compile " + vmClass.getName(),
                                Project.MSG_VERBOSE);
                        mcnt = vmClass.compileBootstrap(compiler, os, optLevel);
                        totalHighMethods += mcnt;
                        totalHighMethodSize += (os.getLength() - startLength);
                    } else {
                        log("Min. Compile " + vmClass.getName(),
                                Project.MSG_VERBOSE);
                        mcnt = vmClass.compileBootstrap(compilers[ 0], os, 0);
                        totalLowMethods += mcnt;
                        totalLowMethodSize += (os.getLength() - startLength);
                    }
                    again |= (mcnt > 0);
                } catch (Throwable ex) {
                    throw new BuildException("Compile of " + vmClass.getName()
                            + " failed", ex);
                }
                if (!vmClass.isCompiled()) { throw new BuildException(
                        "Class should have been compiled by now"); }

            }
            newCount = clsMgr.getLoadedClassCount();
            if (false) {
                log("oldCount " + oldCount + ", newCount " + newCount,
                        Project.MSG_INFO);
            }
        } while ((oldCount != newCount) || again);
    }

    /**
     * Should the given type be compiled with the best compiler.
     * 
     * @param vmClass
     * @return
     */
    protected boolean isCompileHighOptLevel(VmType vmClass) {
        if (vmClass.isArray()) { return true; }

        final String name = vmClass.getName();
        if (compileHighOptLevelPackages.contains(name)) { return true; }

        final int lastDotIdx = name.lastIndexOf('.');
        final String pkg = (lastDotIdx > 0) ? name.substring(0, lastDotIdx)
                : "";

        if (compileHighOptLevelPackages.contains(pkg)) { return true; }

        return false;
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
            FileOutputStream fos = new FileOutputStream(destFile);
            fos.write(os.getBytes(), 0, os.getLength());
            fos.close();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    protected void setupCompileHighOptLevelPackages() {
        addCompileHighOptLevel("java.io");
        addCompileHighOptLevel("java.lang");
        addCompileHighOptLevel("java.lang.ref");
        addCompileHighOptLevel("java.lang.reflect");
        addCompileHighOptLevel("java.net");
        addCompileHighOptLevel("java.util");
        addCompileHighOptLevel("java.util.jar");
        addCompileHighOptLevel("java.util.zip");

        addCompileHighOptLevel("javax.naming");

        addCompileHighOptLevel("gnu.java.io");
        addCompileHighOptLevel("gnu.java.io.decode");
        addCompileHighOptLevel("gnu.java.io.encode");
        addCompileHighOptLevel("gnu.java.lang");
        addCompileHighOptLevel("gnu.java.lang.reflect");

        addCompileHighOptLevel("org.jnode.assembler");
        addCompileHighOptLevel("org.jnode.boot");
        addCompileHighOptLevel("org.jnode.plugin");
        addCompileHighOptLevel("org.jnode.plugin.manager");
        addCompileHighOptLevel("org.jnode.plugin.model");
        addCompileHighOptLevel("org.jnode.protocol.plugin");
        addCompileHighOptLevel("org.jnode.protocol.system");
        addCompileHighOptLevel("org.jnode.system");
        addCompileHighOptLevel("org.jnode.system.event");
        addCompileHighOptLevel("org.jnode.system.util");
        addCompileHighOptLevel("org.jnode.util");
        addCompileHighOptLevel("org.jnode.vm");
        addCompileHighOptLevel("org.jnode.vm.bytecode");
        addCompileHighOptLevel("org.jnode.vm.classmgr");
        addCompileHighOptLevel("org.jnode.vm.compiler");
        addCompileHighOptLevel("org.jnode.vm.compiler.ir");
        addCompileHighOptLevel("org.jnode.vm.compiler.ir.quad");
        addCompileHighOptLevel("org.jnode.vm.memmgr");
        addCompileHighOptLevel("org.jnode.vm.memmgr.def");
    }

    protected final void addCompileHighOptLevel(String name) {
        compileHighOptLevelPackages.add(name);
    }

    /**
     * Create a set of the names of those classes that can be safely
     * instantiated during the boot process (and written as instance to the
     * boot image). Usually java.xxx classes cannot be used, since Sun may have
     * implemented them different from our implementation. If the
     * implementation is difference, the image will contain incorrect fiels and
     * values.
     * 
     * @return Set&lt;String&gt;
     */
    protected Set setupLegalInstanceClasses() {
        final HashSet set = new HashSet();
        set.add("java.lang.Integer");
        set.add("java.lang.Long");
        set.add("java.lang.String");
        set.add("org.jnode.util.Logger");
        return set;
    }

    /**
     * Patch any fields in the header, just before the image is written to
     * disk.
     * 
     * @param os
     * @throws BuildException
     */
    protected abstract void patchHeader(NativeStream os) throws BuildException;

    /**
     * Print any unresolved labels to the out stream and generate a list file
     * for all public labels
     * 
     * @param os
     * @param bootClasses
     * @throws BuildException
     * @throws UnresolvedObjectRefException
     */
    protected final void printLabels(NativeStream os, VmType[] bootClasses,
            VmStatics statics) throws BuildException,
            UnresolvedObjectRefException {
        try {
            int unresolvedCount = 0;
            final PrintWriter w = new PrintWriter(new FileWriter(listFile));
            // Print a list of boot classes.
            for (int i = 0; i < bootClasses.length; i++) {
                final VmType vmClass = bootClasses[ i];
                w.print("bootclass ");
                w.print(i);
                w.print(": ");
                w.print(vmClass.getName());
                if (vmClass instanceof VmClassType) {
                    final int cnt = ((VmClassType) vmClass).getInstanceCount();
                    if (cnt > 0) {
                        w.print(", ");
                        w.print(cnt);
                        w.print(" instances");
                    }
                }
                int cnt = vmClass.getNoInterfaces();
                if (cnt > 0) {
                    w.print(", ");
                    w.print(cnt);
                    w.print(" interfaces");
                }
                w.print(vmClass.isInitialized() ? "" : ", not initialized");
                w.println();
            }
            w.println();

            // Print the statics table
            final int[] table = (int[]) statics.getTable();
            for (int i = 0; i < table.length; i++) {
                w.print(NumberUtils.hex((VmArray.DATA_OFFSET + i) << 2));
                w.print(":");
                w.print(NumberUtils.hex(statics.getType(i), 2));
                w.print("\t");
                w.print(NumberUtils.hex(table[ i]));
                w.println();
            }

            // Look for unresolved labels and put all resolved
            // label into the sorted map. This will be used later
            // to print to the listing file.
            final Collection xrefs = os.getObjectRefs();
            final SortedMap map = new TreeMap();
            for (Iterator i = xrefs.iterator(); i.hasNext();) {
                NativeStream.ObjectRef ref;
                ref = (NativeStream.ObjectRef) i.next();
                if (!ref.isResolved()) {
                    StringBuffer buf = new StringBuffer();
                    buf.append("  $" + Integer.toHexString(ref.getOffset()));
                    buf.append("\t" + ref.getObject());
                    System.err.println("Unresolved label " + buf.toString());
                    unresolvedCount++;
                } else {
                    map.put(new Integer(ref.getOffset()), ref);
                }
            }

            if (unresolvedCount > 0) { throw new BuildException("There are "
                    + unresolvedCount + " unresolved labels"); } // Print the
            // listing
            // file.
            for (Iterator i = map.values().iterator(); i.hasNext();) {
                final NativeStream.ObjectRef ref;
                ref = (NativeStream.ObjectRef) i.next();
                final Object object = ref.getObject();
                w.print('$');
                w.print(hex(ref.getOffset() + os.getBaseAddr()));
                w.print('\t');
                w.print(object);
                w.print(" (");
                if (object instanceof VmSystemObject) {
                    final String info = ((VmSystemObject) object)
                            .getExtraInfo();
                    if (info != null) {
                        w.print(info);
                        w.print(", ");
                    }
                }
                w.print(object.getClass().getName());
                w.println(')');
            }
            w.close();
        } catch (IOException ex) {
            throw new BuildException("Writing list", ex);
        }
    }

    private static final String zero8 = "00000000";

    private static final String zero16 = zero8 + zero8;

    /**
     * Convert a given int to an hexidecimal representation of 8 characters
     * long.
     * 
     * @param v
     * @return The hex string
     */
    protected final String hex(int v) {
        String s = Integer.toHexString(v);
        return zero8.substring(s.length()) + s;
    }

    /**
     * Convert a given int to an hexidecimal representation of 16 characters
     * long.
     * 
     * @param v
     * @return The hex string
     */
    protected final String hex(long v) {
        String s = Long.toHexString(v);
        return zero16.substring(s.length()) + s;
    }

    /**
     * Returns the classesURL.
     * 
     * @return URL
     */
    public final URL getClassesURL() {
        return classesURL;
    }

    /**
     * Returns the debugFile.
     * 
     * @return File
     */
    public final File getDebugFile() {
        return debugFile;
    }

    /**
     * Returns the destFile.
     * 
     * @return File
     */
    public final File getDestFile() {
        return destFile;
    }

    /**
     * Returns the kernelFile.
     * 
     * @return File
     */
    public final File getKernelFile() {
        return kernelFile;
    }

    /**
     * Returns the listFile.
     * 
     * @return File
     */
    public final File getListFile() {
        return listFile;
    }

    /**
     * Sets the classesURL.
     * 
     * @param classesURL
     *            The classesURL to set
     */
    public final void setClassesURL(URL classesURL) {
        this.classesURL = classesURL;
    }

    /**
     * Sets the debugFile.
     * 
     * @param debugFile
     *            The debugFile to set
     */
    public final void setDebugFile(File debugFile) {
        this.debugFile = debugFile;
    }

    /**
     * Sets the destFile.
     * 
     * @param destFile
     *            The destFile to set
     */
    public final void setDestFile(File destFile) {
        this.destFile = destFile;
    }

    /**
     * Sets the kernelFile.
     * 
     * @param kernelFile
     *            The kernelFile to set
     */
    public final void setKernelFile(File kernelFile) {
        this.kernelFile = kernelFile;
    }

    /**
     * Sets the listFile.
     * 
     * @param listFile
     *            The listFile to set
     */
    public void setListFile(File listFile) {
        this.listFile = listFile;
    }

    /**
     * @return File
     */
    public final File getJarFile() {
        return jarFile;
    }

    /**
     * Sets the jarFile.
     * 
     * @param jarFile
     *            The jarFile to set
     */
    public final void setJarFile(File jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * Gets the internal class loader
     * 
     * @return The class loader
     */
    public VmSystemClassLoader getClsMgr() {
        return clsMgr;
    }

    protected abstract void logStatistics(NativeStream os);

    /**
     * Load all classes from the bootjar.
     */
    protected void loadSystemClasses() throws IOException,
            ClassNotFoundException {
        final JarInputStream jis = new JarInputStream(new FileInputStream(
                jarFile));
        JarEntry entry;
        while ((entry = jis.getNextJarEntry()) != null) {
            final String eName = entry.getName();
            if (eName.endsWith(".class")) {
                final String cName = eName.substring(0,
                        eName.length() - ".class".length()).replace('/', '.');
                boolean load = false;

                if (compileHighOptLevelPackages.contains(cName)) {
                    load = true;
                }

                final int lastDotIdx = cName.lastIndexOf('.');
                final String pkg = (lastDotIdx > 0) ? cName.substring(0,
                        lastDotIdx) : "";

                if (compileHighOptLevelPackages.contains(pkg)) {
                    load = true;
                }

                if (load) {
                    loadClass(cName, true);
                }
            }
        }

    }
}
