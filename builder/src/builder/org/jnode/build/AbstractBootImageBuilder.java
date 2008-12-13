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

package org.jnode.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tools.ant.Project;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.NativeStream.ObjectRef;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.model.Factory;
import org.jnode.plugin.model.PluginDescriptorModel;
import org.jnode.plugin.model.PluginJar;
import org.jnode.plugin.model.PluginRegistryModel;
import org.jnode.util.BootableHashMap;
import org.jnode.util.NumberUtils;
import org.jnode.vm.JvmType;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VirtualMemoryRegion;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.Modifier;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmMethodCode;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.UnboxedObject;

/**
 * Build the boot image from an assembler compiled bootstrap (in ELF format)
 * combined with the precompiled Java classes.
 */
public abstract class AbstractBootImageBuilder extends AbstractPluginsTask {

    protected static final Label bootHeapEnd = new Label("$$bootHeapEnd");

    protected static final Label bootHeapStart = new Label("$$bootHeapStart");

    /**
     * System property set to indicate build time
     */
    public static final String BUILDTIME_PROPERTY = "org.jnode.buildtime";

    protected static final Label imageEnd = new Label("$$image_end");

    protected static final Label initialStack = new Label("$$initialStack");

    protected static final Label initialStackPtr = new Label(
        "$$initialStackPtr");

    private static final String zero8 = "00000000";

    private static final String zero16 = zero8 + zero8;

    /**
     * Set of jbects that should not yet be emitted
     */
    private final Set<Object> blockedObjects = new HashSet<Object>();

    private VmSystemClassLoader clsMgr;

    /**
     * Classname/packagename of those classes/packages that need highly
     * optimized compilation
     */
    private final HashSet<String> compileHighOptLevelPackages = new HashSet<String>();

    private final HashSet<String> preloadPackages = new HashSet<String>();

    protected boolean debug = true;

    private File debugFile;

    private File destFile;

    private String jnodeCompiler;

    private File kernelFile;

    private Set<String> legalInstanceClasses;

    private File listFile;

    private int totalHighMethods;

    private int totalHighMethodSize;

    private int totalLowMethods;

    private int totalLowMethodSize;

    private String version;

    /**
     * Plugin id of the memory manager plugin
     */
    private String memMgrPluginId;

    /**
     * Nano-kernel source information
     */
    private final AsmSourceInfo asmSourceInfo;

    /**
     * Enable the compilation of the nano-kernel source via jnasm
     */
    private boolean enableJNasm = false;

    /**
     * Construct a new BootImageBuilder
     */
    public AbstractBootImageBuilder() {
        asmSourceInfo = new AsmSourceInfo();
        legalInstanceClasses = setupLegalInstanceClasses();
    }

    /**
     * Create the kernel-sources element
     *
     * @return
     */
    public AsmSourceInfo createNanokernelsources() {
        return asmSourceInfo;
    }

    protected final void addCompileHighOptLevel(String name) {
        compileHighOptLevelPackages.add(name);
    }

    protected final void addPreloadPackage(String name) {
        preloadPackages.add(name);
    }

    protected void cleanup() {
        clsMgr = null;
        blockedObjects.clear();
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
        final NativeCodeCompiler compiler = compilers[optLevel];

        int oldCount;
        int newCount;
        boolean again;
        do {
            again = false;
            oldCount = clsMgr.getLoadedClassCount();
            for (VmType<?> vmClass : clsMgr.getLoadedClasses()) {
                vmClass.link();
                final boolean compHigh = isCompileHighOptLevel(vmClass);
                try {
                    if (!vmClass.isCpRefsResolved() && compHigh) {
                        // log("Resolving CP of " + vmClass.getName(),
                        // Project.MSG_VERBOSE);
                        vmClass.resolveCpRefs(/*clsMgr*/);
                        again = true;
                    }
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
                        mcnt = vmClass.compileBootstrap(compilers[0], os, 0);
                        totalLowMethods += mcnt;
                        totalLowMethodSize += (os.getLength() - startLength);
                    }
                    again |= (mcnt > 0);
                } catch (Throwable ex) {
                    throw new BuildException("Compile of " + vmClass.getName()
                        + " failed", ex);
                }
                if (!vmClass.isCompiled()) {
                    throw new BuildException(
                        "Class should have been compiled by now");
                }

            }
            newCount = clsMgr.getLoadedClassCount();
            if (false) {
                log("oldCount " + oldCount + ", newCount " + newCount,
                    Project.MSG_INFO);
            }
        } while ((oldCount != newCount) || again);
        log("End of compileClasses", Project.MSG_VERBOSE);
    }

    /**
     * Copy the jnode.jar file into a byte array that is added to the java
     * image.
     *
     * @param blockedObjects
     * @return The loaded resource names
     * @throws BuildException
     */
    protected final Collection<String> copyJarFile(Set<Object> blockedObjects, PluginRegistryModel piRegistry)
        throws BuildException {

        final BootableHashMap<String, byte[]> resources = new BootableHashMap<String, byte[]>();
//        try {
//            final JarFile jar = new JarFile(jarFile);
//            for (Enumeration< ? > e = jar.entries(); e.hasMoreElements();) {
//                final JarEntry entry = (JarEntry) e.nextElement();
//                final byte[] data = read(jar.getInputStream(entry));
//                resources.put(entry.getName().intern(), data);
//            }
//        } catch (IOException ex) {
//            throw new BuildException(ex);
//        }

        // Load all resources of all plugins
        for (PluginDescriptor descr : piRegistry) {
            if (!descr.isSystemPlugin()) {
                throw new BuildException("Non system plugin found " + descr.getId());
            }
            final PluginJar piJar = ((PluginDescriptorModel) descr).getJarFile();
            log("Plugin: " + descr.getId() + piJar.resourceNames().size());
            for (String name : piJar.resourceNames()) {
                final ByteBuffer buf = piJar.getResourceAsBuffer(name);
                final byte[] data = new byte[buf.limit()];
                buf.get(data);
                resources.put(name.intern(), data);
//                log("  " + name);
            }
            piJar.clearResources();
        }

        blockedObjects.add(resources);
        clsMgr.setSystemRtJar(resources);

        return Collections.unmodifiableCollection(resources.keySet());
    }

    /**
     * Copy the jnode.jar file into a byte array that is added to the java
     * image.
     *
     * @param blockedObjects
     * @return The loaded resource names
     * @throws BuildException
     */
    protected final Map<String, byte[]> loadSystemResource(PluginRegistryModel piRegistry)
        throws BuildException {

        final BootableHashMap<String, byte[]> resources = new BootableHashMap<String, byte[]>();

        // Load all resources of all plugins
        for (PluginDescriptor descr : piRegistry) {
            if (!descr.isSystemPlugin()) {
                throw new BuildException("Non system plugin found " + descr.getId());
            }
            final PluginJar piJar = ((PluginDescriptorModel) descr).getJarFile();
//            log("Plugin: " + descr.getId() + piJar.resourceNames().size());
            for (String name : piJar.resourceNames()) {
                final ByteBuffer buf = piJar.getResourceAsBuffer(name);
                final byte[] data = new byte[buf.limit()];
                buf.get(data);
                resources.put(name.intern(), data);
//                log("  " + name);
            }
            piJar.clearResources();
        }
        return resources;
    }

    /**
     * Copy the kernel native code into the native stream.
     *
     * @param os
     * @throws BuildException
     */
    protected abstract void copyKernel(NativeStream os) throws BuildException;

    /**
     * Compile the kernel native code into the native stream.
     *
     * @param os
     * @throws BuildException
     */
    protected void compileKernel(NativeStream os, AsmSourceInfo sourceInfo) throws BuildException {
        // TODO be implemented by Levente
        throw new BuildException("Not implemented");
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
    protected abstract void createInitialStack(NativeStream os,
                                               Label stackLabel, Label stackPtrLabel) throws BuildException,
        ClassNotFoundException, UnresolvedObjectRefException;

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
    protected abstract VmProcessor createProcessor(Vm vm, VmSharedStatics statics,
                                                   VmIsolatedStatics isolatedStatics) throws BuildException;

    private final void doExecute() throws BuildException {
        setupCompileHighOptLevelPackages();

        debug = (getProject().getProperty("jnode.debug") != null);

        final long lmKernel = kernelFile.lastModified();
        final long lmDest = destFile.lastModified();
        final long lmPIL = getPluginListFile().lastModified();

        if (version == null) {
            throw new BuildException("Version property must be set");
        }
        if (memMgrPluginId == null) {
            throw new BuildException("Memory manager plugin Id must be set");
        }

        final PluginList piList;
        final long lmPI;
        final URL memMgrPluginURL;
        try {
            log("plugin-list: " + getPluginListFile(), Project.MSG_DEBUG);
            piList = getPluginList();
            memMgrPluginURL = piList.createPluginURL(memMgrPluginId);
            lmPI = Math.max(piList.lastModified(), memMgrPluginURL.openConnection().getLastModified());
        } catch (PluginException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }

        if ((lmKernel < lmDest) && (lmPIL < lmDest) && (lmPI < lmDest)) {
            // No need to do anything, skip
            return;
        }

        if (debugFile != null) {
            debugFile.delete();
        }

        try {
            System.getProperties().setProperty(BUILDTIME_PROPERTY, "1");

            // Load the plugin descriptors
            final PluginRegistryModel piRegistry;
            piRegistry = Factory.createRegistry(piList.getPluginList());

            // Load the memory management plugin
            piRegistry.loadPlugin(memMgrPluginURL, true);

            // Test the set of system plugins
            testPluginPrerequisites(piRegistry);

            // Load all resources
            final Map<String, byte[]> resources = loadSystemResource(piRegistry);

            /* Now create the processor */
            final VmArchitecture arch = getArchitecture();
            final NativeStream os = createNativeStream();
            clsMgr = new VmSystemClassLoader(null/*classesURL*/, arch,
                new BuildObjectResolver(os, this));
            blockedObjects.add(clsMgr);
            blockedObjects.add(clsMgr.getSharedStatics());
            blockedObjects.add(clsMgr.getSharedStatics().getTable());
            blockedObjects.add(clsMgr.getIsolatedStatics());
            blockedObjects.add(clsMgr.getIsolatedStatics().getTable());
            blockedObjects.add(resources);
            clsMgr.setSystemRtJar(resources);

            // Initialize the statics table.
            initializeStatics(clsMgr.getSharedStatics());

            if (debug) {
                log("Building in DEBUG mode", Project.MSG_WARN);
            }

            // Create the VM
            final Vm vm = new Vm(version, arch, clsMgr.getSharedStatics(), debug, clsMgr, piRegistry);
            blockedObjects.add(vm);
            blockedObjects.add(Vm.getCompiledMethods());

            final VmProcessor proc = createProcessor(vm, clsMgr.getSharedStatics(),
                clsMgr.getIsolatedStatics());
            log("Building for " + proc.getCPUID());

            final Label clInitCaller = new Label("$$clInitCaller");
            VmType<?> systemClasses[] = VmType.initializeForBootImage(clsMgr);
            for (int i = 0; i < systemClasses.length; i++) {
                clsMgr.addLoadedClass(systemClasses[i].getName(),
                    systemClasses[i]);
            }

            // First copy the native kernel file
            if (enableJNasm) {
                compileKernel(os, asmSourceInfo);
            } else {
                copyKernel(os);
            }
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
            loadClass(VirtualMemoryRegion.class).link();
            Vm.getHeapManager().loadClasses(clsMgr);
            loadClass(VmHeapManager.class);
            loadClass(VmSharedStatics.class);
            loadClass(VmIsolatedStatics.class);
            loadClass(Vm.getHeapManager().getClass());
            loadClass(HeapHelper.class);
            loadClass("org.jnode.vm.HeapHelperImpl");
            loadClass(Vm.getCompiledMethods().getClass());
            loadClass(VmCompiledCode[].class);
            loadSystemClasses(resources.keySet());

            /* Now emit the processor */
            os.getObjectRef(proc);

            /* Let the compilers load its native symbol offsets */
            final NativeCodeCompiler[] cmps = arch.getCompilers();
            for (int i = 0; i < cmps.length; i++) {
                final NativeCodeCompiler cmp = cmps[i];
                cmp.initialize(clsMgr);
                os.getObjectRef(cmp);
            }
            /* Let the test compilers load its native symbol offsets */
            final NativeCodeCompiler[] testCmps = arch.getTestCompilers();
            if (testCmps != null) {
                for (int i = 0; i < testCmps.length; i++) {
                    final NativeCodeCompiler cmp = testCmps[i];
                    cmp.initialize(clsMgr);
                    os.getObjectRef(cmp);
                }
            }
            log("Compiling using " + cmps[0].getName() + " and "
                + cmps[cmps.length - 1].getName() + " compilers");
            // Initialize the IMT compiler.
            arch.getIMTCompiler().initialize(clsMgr);

            // Load the jarfile as byte-array
//            copyJarFile(blockedObjects, piRegistry);

            // Now emit all object images to the actual image
            emitObjects(os, arch, blockedObjects, false);

            // Disallow the loading of new classes
            clsMgr.setFailOnNewLoad(true);
            emitObjects(os, arch, blockedObjects, false);

            // Emit the vm
            log("Emit vm", Project.MSG_VERBOSE);
            blockedObjects.remove(vm);
            emitObjects(os, arch, blockedObjects, false);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects, false);

            // Emit the compiled method list
            log("Emit compiled methods", Project.MSG_VERBOSE);
            blockedObjects.remove(Vm.getCompiledMethods());
            final int compiledMethods = Vm.getCompiledMethods().size();
            emitObjects(os, arch, blockedObjects, false);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects, false);

            /* Set the bootclasses */
            log("prepare bootClassArray", Project.MSG_VERBOSE);
            final VmType bootClasses[] = clsMgr.prepareAfterBootstrap();
            os.getObjectRef(bootClasses);
            emitObjects(os, arch, blockedObjects, false);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects, false);

            // Emit the classmanager
            log("Emit clsMgr", Project.MSG_VERBOSE);
            // Turn auto-compilation on
            clsMgr.setCompileRequired();
            blockedObjects.remove(clsMgr);
            emitObjects(os, arch, blockedObjects, false);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects, false);

            // Emit the statics table
            log("Emit statics", Project.MSG_VERBOSE);
            blockedObjects.remove(clsMgr.getSharedStatics());
            blockedObjects.remove(clsMgr.getIsolatedStatics());
            emitObjects(os, arch, blockedObjects, true);
            // Twice, this is intended!
            emitObjects(os, arch, blockedObjects, true);

            // Emit the remaining objects
            log("Emit rest; blocked=" + blockedObjects, Project.MSG_VERBOSE);
            emitObjects(os, arch, null, true);

            // Verify no methods have been compiled after we wrote the
            // CompiledCodeList.
            if (Vm.getCompiledMethods().size() != compiledMethods) {
                throw new BuildException(
                    "Method have been compiled after CompiledCodeList was written.");
            }

            /* Write static initializer code */
            emitStaticInitializerCalls(os, bootClasses, clInitCaller);

            // This is the end of the image
            X86BinaryAssembler.ObjectInfo dummyObjectAtEnd = os
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
            printLabels(os, bootClasses, clsMgr.getSharedStatics());
            logLargeClasses(bootClasses);

            // Generate debug info
            for (int i = 0; i < cmps.length; i++) {
                cmps[i].dumpStatistics();
            }
            final int bootHeapSize = os.getObjectRef(bootHeapEnd).getOffset()
                - os.getObjectRef(bootHeapStart).getOffset();
            final int bootHeapBitmapSize = (bootHeapSize / ObjectLayout.OBJECT_ALIGN) >> 3;
            log("Boot heap size " + (bootHeapSize >>> 10) + "K bitmap size "
                + (bootHeapBitmapSize >>> 10) + "K");
            log("Shared statics");
            PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
            clsMgr.getSharedStatics().dumpStatistics(out);
            log("Isolated statics");
            clsMgr.getIsolatedStatics().dumpStatistics(out);
            vm.dumpStatistics(out);

            logStatistics(os);

            BytecodeParser.dumpStatistics();

            log("Optimized methods     : " + totalHighMethods + ", avg size "
                + (totalHighMethodSize / totalHighMethods) + ", tot size "
                + totalHighMethodSize);
            log("Ondemand comp. methods: " + totalLowMethods + ", avg size "
                + (totalLowMethodSize / totalLowMethods) + ", tot size "
                + totalLowMethodSize);

            log("Done.");

            os.clear();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new BuildException(ex);
        }

    }

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
                                   Set<Object> blockObjects, boolean skipCopyStatics)
        throws BuildException {
        log("Emitting objects", Project.MSG_DEBUG);
        PrintWriter debugOut = null;
        final TreeSet<String> emittedClassNames = new TreeSet<String>();
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
                if (!skipCopyStatics) {
                    copyStaticFields(clsMgr, clsMgr.getSharedStatics(), clsMgr
                        .getIsolatedStatics(), os, emitter);
                }
                final Collection<ObjectRef> objectRefs = new ArrayList<ObjectRef>(
                    os.getObjectRefs());
                int unresolvedFound = 0; // Number of unresolved references
                // found in the following
                // loop
                int emitted = 0; // Number of emitted objects in the
                // following
                // loop
                for (Iterator<ObjectRef> i = objectRefs.iterator(); i.hasNext();) {
                    X86BinaryAssembler.ObjectRef ref = i
                        .next();
                    if (!ref.isResolved()) {
                        final Object obj = ref.getObject();
                        if (!(obj instanceof Label)) {
                            unresolvedFound++;
                            if (obj instanceof VmType) {
                                final VmType<?> vmtObj = (VmType) obj;
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
                                    // log("emitObject " +
                                    // obj.getClass().getName());
                                }
                                // if (obj != skipMe) {
                                emitter.emitObject(obj);
                                emitted++;
                                X86BinaryAssembler.ObjectRef newRef = os
                                    .getObjectRef(obj);
                                if (ref != newRef) {
                                    throw new RuntimeException(
                                        "Object has changed during emitObject! type="
                                            + obj.getClass().getName());
                                }
                                if (!ref.isResolved()) {
                                    throw new RuntimeException("Unresolved reference to object " + ((obj == null) ?
                                        "null" : obj.getClass() .getName()));
                                }
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
                            // log("UnresolvedFound " + unresolvedFound + ",
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
                log("Emitted classes: " + emittedClassNames, Project.MSG_INFO);
            }
        } catch (ClassNotFoundException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    protected abstract void emitStaticInitializerCalls(NativeStream os,
                                                       VmType[] bootClasses, Object clInitCaller)
        throws ClassNotFoundException;

    public final void execute() throws BuildException {
        // Create the image
        doExecute();
        // Remove all garbage objects
        cleanup();
        System.gc();
        // Make sure that all finalizers are called, in order to remove tmp
        // files.
        Runtime.getRuntime().runFinalization();
    }

    /**
     * Gets the target architecture.
     *
     * @return The target architecture
     * @throws BuildException
     */
    protected abstract VmArchitecture getArchitecture() throws BuildException;

    /**
     * Gets the internal class loader
     *
     * @return The class loader
     */
    public VmSystemClassLoader getClsMgr() {
        return clsMgr;
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
     * @return Returns the jnodeCompiler.
     */
    public final String getJnodeCompiler() {
        return jnodeCompiler;
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
     * @return Returns the version.
     */
    public final String getVersion() {
        return this.version;
    }

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
     * Should the given type be compiled with the best compiler.
     *
     * @param vmClass
     * @return
     */
    protected boolean isCompileHighOptLevel(VmType<?> vmClass) {
        if (vmClass.isArray()) {
            return true;
        }

        final String name = vmClass.getName();
        if (compileHighOptLevelPackages.contains(name)) {
            return true;
        }

        final int lastDotIdx = name.lastIndexOf('.');
        final String pkg = (lastDotIdx > 0) ? name.substring(0, lastDotIdx)
            : "";

        if (compileHighOptLevelPackages.contains(pkg)) {
            return true;
        }

        return false;
    }

    /**
     * Link all undefined symbols from the kernel native code.
     *
     * @param os
     * @throws ClassNotFoundException
     * @throws UnresolvedObjectRefException
     */
    protected abstract void linkNativeSymbols(NativeStream os)
        throws ClassNotFoundException, UnresolvedObjectRefException;

    /**
     * Load a VmClass for a given java.lang.Class
     *
     * @param c
     * @return The loaded class
     * @throws ClassNotFoundException
     */
    public final VmType<?> loadClass(Class<?> c) throws ClassNotFoundException {
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
     * @return The loaded class
     * @throws ClassNotFoundException
     */
    public final VmType loadClass(String name)
        throws ClassNotFoundException {
        return loadClass(name, true);
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
     * Load all classes from the bootjar.
     */
    protected final void loadSystemClasses(Collection<String> resourceNames) throws IOException,
        ClassNotFoundException {
        for (String eName : new ArrayList<String>(resourceNames)) {
            if (eName.endsWith(".class")) {
                final String cName = eName.substring(0,
                    eName.length() - ".class".length()).replace('/', '.');
                boolean load = false;

                if (compileHighOptLevelPackages.contains(cName)) {
                    load = true;
                } else if (preloadPackages.contains(cName)) {
                    load = true;
                }

                final int lastDotIdx = cName.lastIndexOf('.');
                final String pkg = (lastDotIdx > 0) ? cName.substring(0,
                    lastDotIdx) : "";

                if (compileHighOptLevelPackages.contains(pkg)) {
                    load = true;
                } else if (preloadPackages.contains(pkg)) {
                    load = true;
                }

                if (load) {
                    loadClass(cName, true);
                }
            }
        }

    }

    protected abstract void logStatistics(NativeStream os);

    /**
     * Align the stream on a page boundary
     *
     * @param os
     * @throws BuildException
     */
    protected abstract void pageAlign(NativeStream os) throws BuildException;

    /**
     * Patch any fields in the header, just before the image is written to disk.
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
                                     VmSharedStatics statics) throws BuildException,
        UnresolvedObjectRefException {
        if (System.getProperty("bootimage.log") == null) {
            return;
        }

        try {
            int unresolvedCount = 0;
            final PrintWriter w = new PrintWriter(new FileWriter(listFile));
            // Print a list of boot classes.
            for (int i = 0; i < bootClasses.length; i++) {
                final VmType<?> vmClass = bootClasses[i];
                w.print("bootclass ");
                w.print(i);
                w.print(": ");
                w.print(vmClass.getName());
                if (vmClass instanceof VmClassType) {
                    final int cnt = ((VmClassType<?>) vmClass)
                        .getInstanceCount();
                    if (cnt > 0) {
                        w.print(", ");
                        w.print(cnt);
                        w.print(" instances");
                        if (vmClass instanceof VmNormalClass) {
                            long objSize = ((VmNormalClass<?>) vmClass)
                                .getObjectSize();
                            long totalSize = objSize * cnt;
                            w.print(", ");
                            w.print(objSize);
                            w.print(" objsize ");
                            w.print(totalSize);
                            w.print(" totsize");
                            if (totalSize > 200000) {
                                log(vmClass.getName() + " is large ("
                                    + totalSize + " , #" + cnt + ")",
                                    Project.MSG_WARN);
                            }
                        }
                    }
                }
                if (vmClass.isArray()) {
                    final long len = ((VmArrayClass<?>) vmClass)
                        .getTotalLength();
                    if (len > 0) {
                        w.print(", ");
                        w.print(len);
                        w.print(" total length ");
                        w.print(len
                            / ((VmArrayClass<?>) vmClass)
                            .getInstanceCount());
                        w.print(" avg length ");
                        w.print(((VmArrayClass<?>) vmClass)
                            .getMaximumLength());
                        w.print(" max length ");
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
                w.print(NumberUtils.hex(table[i]));
                w.println();
            }

            // Look for unresolved labels and put all resolved
            // label into the sorted map. This will be used later
            // to print to the listing file.
            final Collection<? extends ObjectRef> xrefs = os.getObjectRefs();
            final SortedMap<Integer, ObjectRef> map = new TreeMap<Integer, ObjectRef>();
            for (ObjectRef ref : xrefs) {
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

            if (unresolvedCount > 0) {
                throw new BuildException("There are " + unresolvedCount
                    + " unresolved labels");
            } // Print the
            // listing
            // file.
            for (ObjectRef ref : map.values()) {
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

    /**
     * Print any unresolved labels to the out stream and generate a list file
     * for all public labels
     *
     * @param os
     * @param bootClasses
     * @throws BuildException
     * @throws UnresolvedObjectRefException
     */
    protected final void logLargeClasses(VmType[] bootClasses) {
        final Comparator<Long> reverseComp = Collections.reverseOrder();
        final TreeMap<Long, VmType<?>> sortedTypes = new TreeMap<Long, VmType<?>>(reverseComp);
        for (VmType<?> vmType : bootClasses) {
            if (vmType instanceof VmNormalClass) {
                final VmNormalClass<?> nc = (VmNormalClass<?>) vmType;
                final long objSize = nc.getObjectSize();
                final int cnt = nc.getInstanceCount();
                final long totalSize = objSize * cnt;
                sortedTypes.put(totalSize, nc);
            } else if (vmType.isArray()) {
                final VmArrayClass<?> ac = (VmArrayClass<?>) vmType;
                final long len = ac.getTotalLength();
                final int typeSize = ac.getComponentType().getTypeSize();
                sortedTypes.put(len * typeSize, ac);
            }
        }

        int cnt = 1;
        log("Large classes:");
        for (Map.Entry<Long, VmType<?>> entry : sortedTypes.entrySet()) {
            log("  " + entry.getValue().getName() + " " + NumberUtils.size(entry.getKey()));
            if (++cnt > 10) {
                return;
            }
        }
    }

    /**
     * Sets the debugFile.
     *
     * @param debugFile The debugFile to set
     */
    public final void setDebugFile(File debugFile) {
        this.debugFile = debugFile;
    }

    /**
     * Sets the destFile.
     *
     * @param destFile The destFile to set
     */
    public final void setDestFile(File destFile) {
        this.destFile = destFile;
    }

    /**
     * @param jnodeCompiler The jnodeCompiler to set.
     */
    public final void setJnodeCompiler(String jnodeCompiler) {
        this.jnodeCompiler = jnodeCompiler;
    }

    /**
     * Sets the kernelFile.
     *
     * @param kernelFile The kernelFile to set
     */
    public final void setKernelFile(File kernelFile) {
        this.kernelFile = kernelFile;
    }

    /**
     * Sets the listFile.
     *
     * @param listFile The listFile to set
     */
    public void setListFile(File listFile) {
        this.listFile = listFile;
    }

    protected void setupCompileHighOptLevelPackages() {
        addCompileHighOptLevel("java.io");
        addCompileHighOptLevel("java.lang");
        addCompileHighOptLevel("java.lang.ref");
        addCompileHighOptLevel("java.lang.reflect");
        addCompileHighOptLevel("java.net");
        addCompileHighOptLevel("java.nio");
        addCompileHighOptLevel("java.security");
        addCompileHighOptLevel("java.util");
        addCompileHighOptLevel("java.util.jar");
        addCompileHighOptLevel("java.util.zip");

        addCompileHighOptLevel("javax.isolate");
        addCompileHighOptLevel("javax.naming");

        addCompileHighOptLevel("gnu.classpath");
        addCompileHighOptLevel("gnu.java.io");
        addCompileHighOptLevel("gnu.java.io.decode");
        addCompileHighOptLevel("gnu.java.io.encode");
        addCompileHighOptLevel("gnu.java.lang");
        addCompileHighOptLevel("gnu.java.lang.reflect");
        addCompileHighOptLevel("gnu.java.locale");

        addCompileHighOptLevel("org.jnode.assembler");
        addCompileHighOptLevel("org.jnode.boot");
        addCompileHighOptLevel("org.jnode.naming");
        addCompileHighOptLevel("org.jnode.plugin");
        addCompileHighOptLevel("org.jnode.plugin.manager");
        addCompileHighOptLevel("org.jnode.plugin.model");
        addCompileHighOptLevel("org.jnode.protocol.plugin");
        addCompileHighOptLevel("org.jnode.protocol.system");
        addCompileHighOptLevel("org.jnode.security");
        addCompileHighOptLevel("org.jnode.system");
        addCompileHighOptLevel("org.jnode.system.event");
        addCompileHighOptLevel("org.jnode.util");
        addCompileHighOptLevel("org.jnode.vm");
        addCompileHighOptLevel("org.jnode.vm.bytecode");
        addCompileHighOptLevel("org.jnode.vm.classmgr");
        addCompileHighOptLevel("org.jnode.vm.compiler");
        addCompileHighOptLevel("org.jnode.vm.isolate");
        addCompileHighOptLevel("org.jnode.vm.scheduler");
        for (NativeCodeCompiler compiler : getArchitecture().getCompilers()) {
            for (String packageName : compiler.getCompilerPackages()) {
                addCompileHighOptLevel(packageName);
            }
        }

        addCompileHighOptLevel("org.jnode.vm.memmgr");
        addCompileHighOptLevel("org.jnode.vm.memmgr.def");
        addCompileHighOptLevel("org.jnode.vm.memmgr.mmtk");
        addCompileHighOptLevel("org.jnode.vm.memmgr.mmtk.genrc");
        addCompileHighOptLevel("org.jnode.vm.memmgr.mmtk.nogc");
        addCompileHighOptLevel("org.jnode.vm.memmgr.mmtk.ms");

        //todo review for boot image size reduction
        addCompileHighOptLevel("sun.misc");
//        addCompileHighOptLevel("sun.reflect");  <-- // this kills jnode while booting, maybe Reflection static{...}
        addCompileHighOptLevel("sun.reflect.annotation");
        addCompileHighOptLevel("sun.reflect.generics");
        addCompileHighOptLevel("sun.reflect.generics.factory");
        addCompileHighOptLevel("sun.reflect.generics.parser");
        addCompileHighOptLevel("sun.reflect.generics.reflectiveObjects");
        addCompileHighOptLevel("sun.reflect.generics.repository");
        addCompileHighOptLevel("sun.reflect.generics.scope");
        addCompileHighOptLevel("sun.reflect.generics.tree");
        addCompileHighOptLevel("sun.reflect.generics.visitor");
        addCompileHighOptLevel("sun.reflect.misc");
        addCompileHighOptLevel("sun.nio");

        if (false) {
            addCompileHighOptLevel("org.mmtk.plan");
            addCompileHighOptLevel("org.mmtk.policy");
            addCompileHighOptLevel("org.mmtk.utility");
            addCompileHighOptLevel("org.mmtk.utility.alloc");
            addCompileHighOptLevel("org.mmtk.utility.deque");
            addCompileHighOptLevel("org.mmtk.utility.gcspy");
            addCompileHighOptLevel("org.mmtk.utility.gcspy.drivers");
            addCompileHighOptLevel("org.mmtk.utility.heap");
            addCompileHighOptLevel("org.mmtk.utility.options");
            addCompileHighOptLevel("org.mmtk.utility.scan");
            addCompileHighOptLevel("org.mmtk.utility.statistics");
            addCompileHighOptLevel("org.mmtk.vm");
            addCompileHighOptLevel("org.mmtk.vm.gcspy");

            addCompileHighOptLevel("java.awt");
            addCompileHighOptLevel("java.awt.event");
            addCompileHighOptLevel("java.awt.peer");
            addCompileHighOptLevel("java.awt.font");
            addCompileHighOptLevel("java.awt.geom");

            addCompileHighOptLevel("gnu.javax.swing.text.html.parser");
            addCompileHighOptLevel("gnu.javax.swing.text.html.parser.models");
            addCompileHighOptLevel("gnu.javax.swing.text.html.parser.support");
            addCompileHighOptLevel("gnu.javax.swing.text.html.parser.support.low");

            addCompileHighOptLevel("javax.swing");
            addCompileHighOptLevel("javax.swing.border");
            addCompileHighOptLevel("javax.swing.event");
            addCompileHighOptLevel("javax.swing.plaf");
            addCompileHighOptLevel("javax.swing.plaf.basic");
            addCompileHighOptLevel("javax.swing.plaf.metal");
            addCompileHighOptLevel("javax.swing.text");
            addCompileHighOptLevel("javax.swing.text.html");
            addCompileHighOptLevel("javax.swing.text.html.parser");
            addCompileHighOptLevel("javax.swing.text.rtf");
            addCompileHighOptLevel("javax.swing.table");
            addCompileHighOptLevel("javax.swing.tree");
            addCompileHighOptLevel("javax.swing.colorchooser");
            addCompileHighOptLevel("javax.swing.filechooser");
            addCompileHighOptLevel("javax.swing.undo");

            addCompileHighOptLevel("org.jnode.awt");
            addCompileHighOptLevel("org.jnode.awt.swingpeers");

            addCompileHighOptLevel("gnu.java.locale");

            addCompileHighOptLevel("javax.net");
            addCompileHighOptLevel("javax.net.ssl");

            addCompileHighOptLevel("javax.security");
            addCompileHighOptLevel("javax.security.auth");
            addCompileHighOptLevel("javax.security.auth.callback");
            addCompileHighOptLevel("javax.security.auth.login");
            addCompileHighOptLevel("javax.security.auth.spi");
            addCompileHighOptLevel("javax.security.cert");
            addCompileHighOptLevel("javax.security.sasl");

            addCompileHighOptLevel("org.ietf.jgss");

        }
    }

    /**
     * Create a set of the names of those classes that can be safely
     * instantiated during the boot process (and written as instance to the boot
     * image). Usually java.xxx classes cannot be used, since Sun may have
     * implemented them different from our implementation. If the implementation
     * is difference, the image will contain incorrect fiels and values.
     *
     * @return Set&lt;String&gt;
     */
    protected Set<String> setupLegalInstanceClasses() {
        final HashSet<String> set = new HashSet<String>();
        set.add("java.lang.Integer");
        set.add("java.lang.Long");
        set.add("java.lang.Float");
        set.add("java.lang.Double");
        set.add("java.lang.String");
        set.add("org.jnode.util.Logger");
        return set;
    }

    /**
     * @param version The version to set.
     */
    public final void setVersion(String version) {
        this.version = version;
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

    protected void copyStaticFields(VmSystemClassLoader cl,
                                    VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
                                    NativeStream os, ObjectEmitter emitter)
        throws ClassNotFoundException {
        for (VmType<?> type : cl.getLoadedClasses()) {
            final String name = type.getName();
            final int cnt = type.getNoDeclaredFields();
            if ((cnt > 0) && !name.startsWith("java.")) {
                final Class javaType = Class.forName(type.getName());
                try {
                    final FieldInfo fieldInfo = emitter.getFieldInfo(javaType);
                    final Field[] jdkFields = fieldInfo.getJdkStaticFields();
                    final int max = jdkFields.length;

                    for (int k = 0; k < max; k++) {
                        final Field jdkField = jdkFields[k];
                        if (jdkField != null) {
                            final VmField f = fieldInfo.getJNodeStaticField(k);
                            if (!f.isTransient()) {
                                try {
                                    copyStaticField(type, f, jdkField,
                                        sharedStatics, isolatedStatics, os,
                                        emitter);
                                } catch (IllegalAccessException ex) {
                                    throw new BuildException(ex);
                                }
                            }
                        }
                    }
                    if (name.startsWith("org.mmtk.") || type.isEnum()) {
                        type.setAlwaysInitialized();
                    }
                } catch (JNodeClassNotFoundException ex) {
                    log("JNode class not found " + ex.getMessage());
                }
            }
        }
    }

    private void copyStaticField(VmType<?> type, VmField f, Field jf,
                                 VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
                                 NativeStream os, ObjectEmitter emitter)
        throws IllegalAccessException, JNodeClassNotFoundException {
        jf.setAccessible(true);
        final Object val = jf.get(null);
        final int fType = JvmType.SignatureToType(f.getSignature());
        final int idx;
        final VmStaticField sf = (VmStaticField) f;
        final VmStatics statics;
        if (sf.isShared()) {
            idx = sf.getSharedStaticsIndex();
            statics = sharedStatics;
        } else {
            idx = sf.getIsolatedStaticsIndex();
            statics = isolatedStatics;
        }

        if (f.isPrimitive()) {
            if (f.isWide()) {
                final long lval;
                switch (fType) {
                    case JvmType.LONG:
                        lval = ((Long) val).longValue();
                        break;
                    case JvmType.DOUBLE:
                        lval = Double.doubleToRawLongBits(((Double) val)
                            .doubleValue());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown wide type "
                            + fType);
                }
                statics.setLong(idx, lval);
            } else {
                final int ival;
                final Class jfType = jf.getType();
                if (jfType == boolean.class) {
                    ival = ((Boolean) val).booleanValue() ? 1 : 0;
                } else if (jfType == byte.class) {
                    ival = ((Byte) val).byteValue();
                } else if (jfType == char.class) {
                    ival = ((Character) val).charValue();
                } else if (jfType == short.class) {
                    ival = ((Short) val).shortValue();
                } else if (jfType == int.class) {
                    ival = ((Number) val).intValue();
                } else if (jfType == float.class) {
                    ival = Float.floatToRawIntBits(((Float) val).floatValue());
                } else {
                    throw new IllegalArgumentException("Unknown wide type "
                        + fType);
                }
                statics.setInt(idx, ival);
            }
        } else if (f.isAddressType()) {
            if (val == null) {
                // Just do nothing
            } else if (val instanceof UnboxedObject) {
                final UnboxedObject uobj = (UnboxedObject) val;
                statics.setAddress(idx, uobj);
            } else if (val instanceof Label) {
                final Label lbl = (Label) val;
                statics.setAddress(idx, lbl);
            } else {
                throw new BuildException("Cannot handle magic type " + val.getClass().getName());
            }
        } else {
            if (!Modifier.isAddressType(f.getSignature())) {
                if (val != null) {
                    emitter.testForValidEmit(val, type.getName());
                    os.getObjectRef(val);
                }
                statics.setObject(idx, val);
            }
        }
    }

    /**
     * Initialize the statics table.
     *
     * @param statics
     */
    protected abstract void initializeStatics(VmSharedStatics statics)
        throws BuildException;

    /**
     * @return Returns the memMgrPluginId.
     */
    public final String getMemMgrPluginId() {
        return memMgrPluginId;
    }

    /**
     * @param memMgrPluginId The memMgrPluginId to set.
     */
    public final void setMemMgrPluginId(String memMgrPluginId) {
        this.memMgrPluginId = memMgrPluginId;
    }

    /**
     * @return Returns the enableJNasm.
     */
    public final boolean isEnableJNasm() {
        return enableJNasm;
    }

    /**
     * @param enableJNasm The enableJNasm to set.
     */
    public final void setEnableJNasm(boolean enableJNasm) {
        this.enableJNasm = enableJNasm;
    }
}
