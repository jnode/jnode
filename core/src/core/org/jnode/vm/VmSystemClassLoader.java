/**
 * $Id$
 * 
 * Copyright 2001-2003, E.W. Prangsma
 * 
 * All rights reserved
 */
package org.jnode.vm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.classmgr.ClassDecoder;
import org.jnode.vm.classmgr.SelectorMap;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.NativeCodeCompiler;

/**
 * Default classloader.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmSystemClassLoader extends VmAbstractClassLoader {

    private transient TreeMap classInfos;

    private VmType[] bootClasses;

    private transient URL classesURL;

    private transient boolean verbose = false;

    private transient boolean failOnNewLoad = false;

    private transient ClassLoader classLoader;

    private transient ObjectResolver resolver;

    private byte[] systemRtJar;

    private static JarFile systemJarFile;

    private final ClassLoader parent;

    /** Our mapping from method signatures to selectors */
    private final SelectorMap selectorMap;

    private final VmArchitecture arch;

    private boolean requiresCompile = false;

    private final VmStatics statics;

    /**
     * Constructor for VmClassLoader.
     * 
     * @param classesURL
     * @param arch
     */
    public VmSystemClassLoader(URL classesURL, VmArchitecture arch) {
        this(classesURL, arch, null);
    }

    /**
     * Constructor for VmClassLoader.
     * 
     * @param classesURL
     * @param arch
     */
    public VmSystemClassLoader(URL classesURL, VmArchitecture arch,
            ObjectResolver resolver) {
        this.classesURL = classesURL;
        this.classInfos = new TreeMap();
        this.parent = null;
        this.selectorMap = new SelectorMap();
        this.arch = arch;
        this.resolver = resolver;
        this.statics = new VmStatics(arch, resolver);
    }

    /**
     * Constructor for VmClassLoader.
     * 
     * @param classLoader
     */
    public VmSystemClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.classInfos = new TreeMap();
        this.parent = classLoader.getParent();
        final VmSystemClassLoader sysCl = VmSystem.getSystemClassLoader();
        this.selectorMap = sysCl.selectorMap;
        this.arch = sysCl.arch;
        this.statics = sysCl.statics;
    }

    /**
     * Gets the collection with all currently loaded classes. All collection
     * elements are instanceof VmClass.
     * 
     * @return Collection
     */
    public Collection getLoadedClasses() {
        if (classInfos != null) {
            final ArrayList list = new ArrayList();
            for (Iterator i = classInfos.values().iterator(); i.hasNext();) {
                final ClassInfo ci = (ClassInfo) i.next();
                if (ci.isLoaded()) {
                    try {
                        list.add(ci.getVmClass());
                    } catch (ClassNotFoundException ex) {
                        /* ignore */
                    }
                }
            }
            return list;
        } else {
            final ArrayList list = new ArrayList();
            final VmType[] arr = bootClasses;
            final int count = arr.length;
            for (int i = 0; i < count; i++) {
                list.add(arr[ i]);
            }
            return list;
        }
    }

    /**
     * Gets the number of loaded classes.
     */
    public int getLoadedClassCount() {
        if (classInfos != null) {
            return classInfos.size();
        } else {
            return bootClasses.length;
        }
    }

    /**
     * Gets the loaded class with a given name, or null if no such class has
     * been loaded.
     * 
     * @param name
     * @return VmClass
     */
    public VmType findLoadedClass(String name) {
        if (classInfos != null) {
            if (name.indexOf('/') >= 0) { throw new IllegalArgumentException(
                    "name contains '/'"); }
            final ClassInfo ci = getClassInfo(name, false);
            if (ci != null) {
                try {
                    return ci.getVmClass();
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                return null;
            }
        } else {
            final VmType[] list = bootClasses;
            final int count = list.length;
            for (int i = 0; i < count; i++) {
                VmType vmClass = list[ i];
                if (vmClass.nameEquals(name)) { return vmClass; }
            }
            return null;
        }
    }

    /**
     * Result all loaded classes as an array of VmClass entries.
     * 
     * @return VmClass[]
     * @throws ClassNotFoundException
     */
    public VmType[] prepareAfterBootstrap() throws ClassNotFoundException {
        if (this.classInfos != null) {
            final VmType[] result = new VmType[ classInfos.size()];
            int j = 0;
            for (Iterator i = classInfos.values().iterator(); i.hasNext();) {
                final ClassInfo ci = (ClassInfo) i.next();
                result[ j++] = ci.getVmClass();
            }
            bootClasses = result;
            return result;
        } else {
            return bootClasses;
        }
    }

    /**
     * Add a class that has been loaded.
     * 
     * @param name
     * @param cls
     */
    public synchronized void addLoadedClass(String name, VmType cls) {
        if (failOnNewLoad) { throw new RuntimeException(
                "Cannot load a new class when failOnNewLoad is set (" + name
                        + ")"); }
        if (classInfos != null) {
            classInfos.put(name, new ClassInfo(cls));
        }
    }

    /**
     * Gets the ClassInfo for the given name. If not found and create is True,
     * a new ClassInfo is created, added to the list and returned. If not found
     * and create is False, null is returned.
     * 
     * @param name
     * @param create
     * @return
     */
    private synchronized ClassInfo getClassInfo(String name, boolean create) {
        ClassInfo ci = (ClassInfo) classInfos.get(name);
        if (ci != null) {
            return ci;
        } else if (create) {
            ci = new ClassInfo(name);
            classInfos.put(name, ci);
        }
        return ci;
    }

    /**
     * Load a class with a given name
     * 
     * @param name
     * @param resolve
     * @see org.jnode.vm.classmgr.VmClassLoader#loadClass(String, boolean)
     * @return The loaded class
     * @throws ClassNotFoundException
     */
    public VmType loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        // Also implement the java.lang.ClassLoader principals here
        // otherwise they cannot work in java.lang.ClassLoader.
        if ((parent != null) && !parent.skipParentLoader(name)) {
            try {
                final Class cls = parent.loadClass(name);
                return cls.getVmClass();
            } catch (ClassNotFoundException ex) {
                // Don't care, try it ourselves.
            }
        }

        VmType cls = findLoadedClass(name);
        if (cls != null) { return cls; }
        if (classInfos == null) { 
        //Unsafe.debug("classInfos==null");
        throw new ClassNotFoundException(name); }

        //BootLog.debug("load class" + name);

        if (name.indexOf('/') >= 0) { throw new IllegalArgumentException(
                "name contains '/'"); }
        final ClassInfo ci = getClassInfo(name, true);
        if (!ci.isLoaded()) {
            try {
                if (failOnNewLoad) { throw new RuntimeException(
                        "Cannot load a new class when failOnNewLoad is set ("
                                + name + ")"); }
                if (name.charAt(0) == '[') {
                    ci.setVmClass(loadArrayClass(name, resolve));
                } else {
                    ci.setVmClass(loadNormalClass(name));
                }
            } catch (ClassNotFoundException ex) {
                ci.setLoadError(ex.toString());
                classInfos.remove(ci.getName());
                throw new ClassNotFoundException(name, ex);
            } catch (IOException ex) {
                ci.setLoadError(ex.toString());
                classInfos.remove(ci.getName());
                throw new ClassNotFoundException(name, ex);
            }
            if (resolve) {
                ci.getVmClass().link();
            }
        }
        return ci.getVmClass();
    }

    /**
     * Gets the ClassLoader belonging to this loader.
     * 
     * @return ClassLoader
     */
    public final ClassLoader asClassLoader() {
        if (classLoader == null) {
            classLoader = new ClassLoaderWrapper(this);
        }
        return classLoader;
    }

    /**
     * Load a normal (non-array) class with a given name
     * 
     * @param name
     * @return VmClass
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private VmType loadNormalClass(String name) throws IOException,
            ClassNotFoundException {

        if (failOnNewLoad) { throw new RuntimeException(
                "Cannot load a new class when failOnNewLoad is set (" + name
                        + ")"); }

        boolean rejectNatives = (!name.equals("org.jnode.vm.Unsafe"));

        //System.out.println("bvi.loadClass: " +name);
        byte[] image = getClassStream(name);
        return ClassDecoder.defineClass(name, image, 0, image.length,
                rejectNatives, this);
    }

    /**
     * Gets the number of loaded classes.
     * 
     * @return int
     */
    public int size() {
        if (classInfos != null) {
            return classInfos.size();
        } else {
            return bootClasses.length;
        }
    }

    /**
     * Gets an inputstream for the class file that contains the given
     * classname.
     * 
     * @param clsName
     * @return InputStream
     * @throws MalformedURLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private byte[] getClassStream(String clsName) throws MalformedURLException,
            IOException, ClassNotFoundException {
        final String fName = clsName.replace('.', '/') + ".class";
        final InputStream is = getResourceAsStream(fName);
        if (is == null) {
            throw new ClassNotFoundException("Class resource of " + clsName
                    + " not found.");
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int len;
            byte[] buf = new byte[ 4096];
            while ((len = is.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            buf = null;
            is.close();

            return bos.toByteArray();
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#resourceExists(java.lang.String)
     */
    public final boolean resourceExists(String resName) {
        try {
            return (getResourceAsStream(resName) != null);
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Gets an inputstream for a resource with the given name.
     * 
     * @param name
     *            The name of the resource
     * @return An opened inputstream to the resource with the given name, or
     *         null if not found.
     * @throws MalformedURLException
     * @throws IOException
     */
    public InputStream getResourceAsStream(String name)
            throws MalformedURLException, IOException {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (classesURL != null) {
            if (verbose) {
                System.out.println("Loading resource " + name);
            }
            URL url = new URL(classesURL, name);
            //System.out.println("url=" + url);
            return url.openStream();
        } else if ((systemJarFile != null) || (systemRtJar != null)) {
            if (systemJarFile == null) {
                systemJarFile = new JarFile(systemRtJar);
            }
            JarEntry entry = systemJarFile.getJarEntry(name);
            if (entry != null) {
                return systemJarFile.getInputStream(entry);
            } else {
                return null;
            }
        } else {
            throw new IOException("Don't no how to load " + name);
        }
    }

    /**
     * Returns the verbose.
     * 
     * @return boolean
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the verbose.
     * 
     * @param verbose
     *            The verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns the failOnNewLoad.
     * 
     * @return boolean
     */
    public boolean isFailOnNewLoad() {
        return failOnNewLoad;
    }

    /**
     * Sets the failOnNewLoad.
     * 
     * @param failOnNewLoad
     *            The failOnNewLoad to set
     */
    public void setFailOnNewLoad(boolean failOnNewLoad) {
        if (classesURL != null) {
            this.failOnNewLoad = failOnNewLoad;
        }
    }

    /**
     * Compile the given method
     * 
     * @param vmMethod
     *            The method to compile
     * @param optLevel
     *            The optimization level
     */
    public void compileRuntime(VmMethod vmMethod, int optLevel) {
        final NativeCodeCompiler cmps[] = arch.getCompilers();
        final NativeCodeCompiler cmp;
        if (optLevel < 0) {
            optLevel = 0;
        } else if (optLevel >= cmps.length) {
            optLevel = cmps.length - 1;
        }
        if (vmMethod.getNativeCodeOptLevel() < optLevel) {
            cmp = cmps[ optLevel];
            cmp.compileRuntime(vmMethod, getResolver(), optLevel, null);
        }
    }

    /**
     * Initialize this classloader during the initialization of the VM. If
     * needed, the tree of classes is generated from the boot class list.
     */
    protected void initialize() {
        if (classInfos == null) {
            final TreeMap classInfos = new TreeMap();
            final VmType[] list = bootClasses;
            final int count = list.length;
            for (int i = 0; i < count; i++) {
                final VmType vmClass = list[ i];
                final ClassInfo ci = new ClassInfo(vmClass);
                classInfos.put(ci.getName(), ci);
            }
            this.classInfos = classInfos;
        }
    }

    /**
     * @return ObjectResolver
     */
    public ObjectResolver getResolver() {
        if (resolver == null) {
            resolver = new Unsafe.UnsafeObjectResolver();
        }
        return resolver;
    }

    /**
     * Set the object resolver. This can be called only once.
     */
    public void setResolver(ObjectResolver resolver) {
        if (this.resolver == null) {
            this.resolver = resolver;
        } else {
            throw new SecurityException("Cannot overwrite resolver");
        }
    }

    /**
     * Sets the systemRtJar.
     * 
     * @param systemRtJar
     *            The systemRtJar to set
     */
    public void setSystemRtJar(byte[] systemRtJar) {
        if (this.systemRtJar == null) {
            this.systemRtJar = systemRtJar;
        }
    }

    /**
     * Is this loader the system classloader?
     * 
     * @return boolean
     */
    public boolean isSystemClassLoader() {
        VmSystemClassLoader systemLoader = VmSystem.getSystemClassLoader();
        return ((systemLoader == this) || (systemLoader == null));
    }

    static class ClassLoaderWrapper extends ClassLoader {

        public ClassLoaderWrapper(VmSystemClassLoader vmClassLoader) {
            super(vmClassLoader);
        }
    }

    /**
     * Class that holds information of a loading &amp; loaded class.
     * 
     * @author epr
     */
    static class ClassInfo {

        /** Name of the class */
        private final String name;

        /** The class itself */
        private VmType vmClass;

        /** Classloading got an error? */
        private boolean error = false;

        private String errorMsg;

        /**
         * Create a new instance
         * 
         * @param name
         */
        public ClassInfo(String name) {
            this.name = name;
        }

        /**
         * Create a new instance
         * 
         * @param vmClass
         */
        public ClassInfo(VmType vmClass) {
            this.name = vmClass.getName();
            this.vmClass = vmClass;
            if (name.indexOf('/') >= 0) { throw new IllegalArgumentException(
                    "vmClass.getName() contains '/'"); }
        }

        /**
         * @return
         */
        public final String getName() {
            return name;
        }

        /**
         * @return Type
         * @throws ClassNotFoundException
         */
        public final synchronized VmType getVmClass()
                throws ClassNotFoundException {
            while (vmClass == null) {
                if (error) { throw new ClassNotFoundException(name + "; "
                        + errorMsg); }
                try {
                    wait();
                } catch (InterruptedException ex) {
                    // Just ignore
                }
            }
            return vmClass;
        }

        /**
         * @param class1
         */
        public final synchronized void setVmClass(VmType class1) {
            if (this.vmClass == null) {
                this.vmClass = class1;
                notifyAll();
            } else {
                throw new SecurityException("Can only set the VmClass once.");
            }
        }

        /**
         * Signal a class loading error. This will release other threads
         * waiting for this class with a ClassNotFoundException.
         */
        public final synchronized void setLoadError(String errorMsg) {
            this.error = true;
            this.errorMsg = errorMsg;
            notifyAll();
        }

        /**
         * Has the class wrapped by this object been loaded?
         * 
         * @return
         */
        public boolean isLoaded() {
            return (vmClass != null);
        }
    }

    /**
     * Gets the mapping between method name&types and selectors.
     * 
     * @return The map
     */
    public final SelectorMap getSelectorMap() {
        return selectorMap;
    }

    /**
     * Gets the statics table.
     * 
     * @return The statics table
     */
    public final VmStatics getStatics() {
        return statics;
    }

    /**
     * Gets the architecture used by this loader.
     * 
     * @return The architecture
     */
    public final VmArchitecture getArchitecture() {
        return arch;
    }

    /**
     * Should prepared classes be compiled.
     * 
     * @return boolean
     */
    public boolean isCompileRequired() {
        return requiresCompile;
    }

    /**
     * Should prepared classes be compiled.
     */
    public void setCompileRequired() {
        requiresCompile = true;
    }

}
