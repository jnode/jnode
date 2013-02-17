/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jnode.annotation.PrivilegedActionPragma;
import org.jnode.assembler.ObjectResolver;
import org.jnode.util.ByteBufferInputStream;
import org.jnode.vm.classmgr.ClassDecoder;
import org.jnode.vm.classmgr.IMTBuilder;
import org.jnode.vm.classmgr.SelectorMap;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.CompiledIMT;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.isolate.VmIsolate;
import org.jnode.vm.objects.BootableArrayList;
import org.jnode.vm.scheduler.VmProcessor;

/**
 * Default classloader.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmSystemClassLoader extends VmAbstractClassLoader {

    private transient TreeMap<String, ClassInfo> classInfos;

    private VmType[] bootClasses;

    private transient URL[] classesURL;

    private static transient boolean verbose = false;

    private transient boolean failOnNewLoad = false;

    private transient ClassLoader classLoader;

    private transient ObjectResolver resolver;

    private Map<String, byte[]> systemRtJar;

    // private static JarFile systemJarFile;

    private final ClassLoader parent;

    /**
     * Our mapping from method signatures to selectors
     */
    private final SelectorMap selectorMap;

    private final BaseVmArchitecture arch;

    private boolean requiresCompile = false;

    private final VmSharedStatics sharedStatics;

    private transient VmIsolatedStatics isolatedStatics;

    private transient HashSet<String> failedClassNames;

    private List<ResourceLoader> resourceLoaders;

    /**
     * Constructor for VmClassLoader.
     *
     * @param classesURL
     * @param arch
     */
    public VmSystemClassLoader(URL classesURL, BaseVmArchitecture arch) {
        this(new URL[]{classesURL}, arch, null);
    }

    /**
     * Constructor for VmClassLoader.
     *
     * @param classesURL
     * @param arch
     */
    public VmSystemClassLoader(URL[] classesURL, BaseVmArchitecture arch) {
        this(classesURL, arch, null);
    }

    /**
     * Constructor for VmClassLoader.
     *
     * @param classesURL
     * @param arch
     * @param resolver
     */
    public VmSystemClassLoader(URL[] classesURL, BaseVmArchitecture arch,
                               ObjectResolver resolver) {
        this.classesURL = classesURL;
        this.classInfos = new TreeMap<String, ClassInfo>();
        this.parent = null;
        this.selectorMap = new SelectorMap();
        this.arch = arch;
        this.resolver = resolver;
        this.resourceLoaders = new BootableArrayList<ResourceLoader>();
        this.sharedStatics = new VmSharedStatics(arch, resolver);
        this.isolatedStatics = new VmIsolatedStatics(arch, resolver);
    }

    /**
     * Constructor for VmClassLoader.
     *
     * @param classLoader
     */
    public VmSystemClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.classInfos = new TreeMap<String, ClassInfo>();
        this.parent = classLoader.getParent();
        final VmSystemClassLoader sysCl = VmSystem.getSystemClassLoader();
        this.selectorMap = sysCl.selectorMap;
        this.arch = sysCl.arch;
        this.sharedStatics = sysCl.sharedStatics;
    }

    /**
     * Gets the collection with all currently loaded classes. All collection
     * elements are instanceof VmClass.
     *
     * @return Collection
     */
    public Collection<VmType> getLoadedClasses() {
        if (classInfos != null) {
            final ArrayList<VmType> list = new ArrayList<VmType>();
            for (ClassInfo ci : classInfos.values()) {
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
            final ArrayList<VmType> list = new ArrayList<VmType>();
            final VmType[] arr = bootClasses;
            final int count = arr.length;
            list.addAll(Arrays.asList(arr).subList(0, count));
            return list;
        }
    }

    /**
     * Gets the number of loaded classes.
     *
     * @return the number of loaded classes
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
    public VmType<?> findLoadedClass(String name) {
        if (classInfos != null) {
            if (name.indexOf('/') >= 0) {
                //throw new IllegalArgumentException("name contains '/'");
                //return null here
                return null;
            }
            final ClassInfo ci = getClassInfo(name, false);
            if (ci != null) {
                try {
                    return ci.getVmClass();
                } catch (ClassNotFoundException ex) {
                    // throw new RuntimeException(ex);
                    return null;
                }
            } else {
                return null;
            }
        } else {
            final VmType[] list = bootClasses;
            final int count = list.length;
            for (int i = 0; i < count; i++) {
                VmType vmClass = list[i];
                if (vmClass.nameEquals(name)) {
                    return vmClass;
                }
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
            final VmType[] result = new VmType[classInfos.size()];
            int j = 0;
            for (ClassInfo ci : classInfos.values()) {
                result[j++] = ci.getVmClass();
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
        if (failOnNewLoad) {
            throw new RuntimeException("Cannot load a new class when failOnNewLoad is set (" + name + ')');
        }
        if (classInfos != null) {
            classInfos.put(name, new ClassInfo(cls));
        }
    }

    /**
     * Gets the ClassInfo for the given name. If not found and create is True, a
     * new ClassInfo is created, added to the list and returned. If not found
     * and create is False, null is returned.
     *
     * @param name
     * @param create
     * @return the ClassInfo for the given name
     */
    private synchronized ClassInfo getClassInfo(String name, boolean create) {
        if (name == null) {
            Unsafe.debug(" getClassInfo(null)!! ");
        }
        ClassInfo ci = classInfos.get(name);
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
     * @return The loaded class
     * @throws ClassNotFoundException
     * @see org.jnode.vm.classmgr.VmClassLoader#loadClass(String, boolean)
     */
    @PrivilegedActionPragma
    public VmType<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {

        // Also implement the java.lang.ClassLoader principals here
        // otherwise they cannot work in java.lang.ClassLoader.
        if ((parent != null) && !parent.skipParentLoader(name)) {
            try {
                final Class<?> cls = parent.loadClass(name);
                return VmType.fromClass((Class<?>) cls);
            } catch (ClassNotFoundException ex) {
                // Don't care, try it ourselves.
            }
        }

        VmType cls = findLoadedClass(name);
        if (cls != null) {
            return cls;
        }
        if (classInfos == null) {
            // Unsafe.debug("classInfos==null");
            throw new ClassNotFoundException(name);
        }

        // BootLogInstance.get().debug("load class" + name);

        if (name.indexOf('/') >= 0) {
            //throw new IllegalArgumentException("name contains '/'");
            //throw CNFE here
            throw new ClassNotFoundException(name);
        }

        if ((failedClassNames != null) && (failedClassNames.contains(name))) {
            throw new ClassNotFoundException(name);
        }

        final ClassInfo ci = getClassInfo(name, true);

        if (!ci.isLoaded()) {
            try {
                if (name.charAt(0) == '[') {
                    ci.setVmClass(loadArrayClass(name, resolve));
                } else {
                    ci.setVmClass(loadNormalClass(name));
                }
                if (failOnNewLoad) {
                    throw new RuntimeException("Cannot load a new class when failOnNewLoad is set (" + name + ')');
                }
            } catch (ClassNotFoundException ex) {
                ci.setLoadError(ex.toString());
                classInfos.remove(ci.getName());
                addFailedClassName(name);
                throw new ClassNotFoundException(name, ex);
            } catch (IOException ex) {
                ci.setLoadError(ex.toString());
                classInfos.remove(ci.getName());
                addFailedClassName(name);
                throw new ClassNotFoundException(name, ex);
            }
            if (resolve) {
                ci.getVmClass().link();
            }
        }
        return ci.getVmClass();
    }

    private void addFailedClassName(String name) {
        if (failedClassNames == null) {
            failedClassNames = new HashSet<String>();
        }
        failedClassNames.add(name);
    }

    /**
     * Gets the ClassLoader belonging to this loader.
     *
     * @return ClassLoader
     */
    public final ClassLoader asClassLoader() {
        if (VmIsolate.isRoot()) {
            if (classLoader == null) {
                classLoader = new ClassLoaderWrapper(this);
            }
            return classLoader;
        } else {
            ClassLoader loader = VmIsolate.currentIsolate().getSystemClassLoader();
            if (loader == null) {
                loader = new ClassLoaderWrapper(this);
                VmIsolate.currentIsolate().setSystemClassLoader(loader);
            }
            return loader;
        }
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

        final String archN = arch.getName();
        boolean allowNatives = VmUtils.allowNatives(name, archN);

        final ByteBuffer image = getClassData(name);

        if (failOnNewLoad) {
            throw new RuntimeException("Cannot load a new class when failOnNewLoad is set (" + name + ')');
        }

        return ClassDecoder.defineClass(name, image, !allowNatives, this, null);
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
     * Gets an inputstream for the class file that contains the given classname.
     *
     * @param clsName
     * @return InputStream
     * @throws MalformedURLException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private ByteBuffer getClassData(String clsName) throws IOException, ClassNotFoundException {

        final String fName = clsName.replace('.', '/') + ".class";

        if (systemRtJar != null) {
            // Try the system RT jar first
            final byte[] data = systemRtJar.get(fName);
            if (data != null) {
                return ByteBuffer.wrap(data);
            }

            for (ResourceLoader l : resourceLoaders) {
                final ByteBuffer buf = l.getResourceAsBuffer(fName);
                if (buf != null) {
                    return buf;
                }
            }

            throw new ClassNotFoundException("System class " + clsName
                + " not found.");
        } else {
            final InputStream is = getResourceAsStream(fName);
            if (is == null) {
                throw new ClassNotFoundException("Class resource of " + clsName
                    + " not found.");
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int len;
                byte[] buf = new byte[4096];
                while ((len = is.read(buf)) > 0) {
                    bos.write(buf, 0, len);
                }
                is.close();

                return ByteBuffer.wrap(bos.toByteArray());
            }
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#resourceExists(java.lang.String)
     */
    public final boolean resourceExists(String resName) {
        try {
            for (ResourceLoader l : resourceLoaders) {
                if (l.containsResource(resName)) {
                    if (verbose) {
                        System.out.println("resourceExists(" + resName + ")->true");
                    }
                    return true;
                }
            }
            final InputStream is = getResourceAsStream(resName);
            if (is != null) {
                if (verbose) {
                    System.out.println("resourceExists(" + resName + "), using getResourceAsStream->true");
                }
                is.close();
                return true;
            } else {
                if (verbose) {
                    System.out.println("resourceExists(" + resName + "), using getResourceAsStream->false");
                }
                return false;
            }
        } catch (IOException ex) {
            if (verbose) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    public URL findResource(String name) {
        if (verbose) {
            System.out.println("VmSystemClassLoader.findResource(" + name + ')');
        }
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (classesURL != null) {
            if (verbose) {
                System.out.println("Loading resource " + name + " from " + classesURL);
            }
            try {
                for (URL u : classesURL) {
                    URL url = new URL(u, name);
                    try {
                        url.openStream().close();
                        return url;
                    } catch (IOException e) {
                        //continue
                    }
                }
                return null;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        } else if (systemRtJar != null) {
            if (verbose) {
                System.out.println("Loading resource " + name + " from systemRtJar");
            }
            final byte[] data = systemRtJar.get(name);
            if (verbose) {
                System.out.println(">>>>>> findResource(" + name + "), (data==null)=" + (data == null));
            }

            if (data != null) {
                if (verbose) {
                    System.out.println(">>>>>> resource: " + new String(data));
                }
                return ClassLoader.getSystemResource(name);
            } else {
                for (ResourceLoader l : resourceLoaders) {
                    final URL url = l.getResource(name);
                    if (url != null) {
                        return url;
                    }
                }
                return null;
            }
        } else {
            if (verbose) {
                System.out.println("!!!! findResource(" + name + ") : ERROR");
            }
            return null;
        }
    }


    /**
     * Gets an inputstream for a resource with the given name.
     *
     * @param name The name of the resource
     * @return An opened inputstream to the resource with the given name, or
     *         null if not found.
     * @throws MalformedURLException
     * @throws IOException
     */
    public InputStream getResourceAsStream(String name) throws IOException {
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        if (classesURL != null) {
            if (verbose) {
                System.out.println("Loading resource " + name + " from " + classesURL);
            }
            for (URL u : classesURL) {
                URL url = new URL(u, name);
                try {
                    return url.openStream();
                } catch (IOException e) {
                    //continue
                }
            }
            return null;
        } else if (systemRtJar != null) {
            if (verbose) {
                System.out.println("Loading resource " + name + " from systemRtJar");
            }
            final byte[] data = systemRtJar.get(name);
            if (data != null) {
                return new ByteArrayInputStream(data);
            } else {
                for (ResourceLoader l : resourceLoaders) {
                    final ByteBuffer buf = l.getResourceAsBuffer(name);
                    if (buf != null) {
                        return new ByteBufferInputStream(buf);
                    }
                }
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
     * @param verbose The verbose to set
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
     * @param failOnNewLoad The failOnNewLoad to set
     */
    public void setFailOnNewLoad(boolean failOnNewLoad) {
        if (classesURL != null) {
            this.failOnNewLoad = failOnNewLoad;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see org.jnode.vm.classmgr.VmClassLoader#disassemble(org.jnode.vm.classmgr.VmMethod,
     * int, boolean, java.io.Writer)
     */
    public void disassemble(VmMethod vmMethod, int optLevel,
                            boolean enableTestCompilers, Writer writer) {
        final NativeCodeCompiler cmps[];
        int index;
        if (enableTestCompilers) {
            index = optLevel;
            optLevel += arch.getCompilers().length;
            cmps = arch.getTestCompilers();
        } else {
            index = optLevel;
            cmps = arch.getCompilers();
        }

        final NativeCodeCompiler cmp;
        if (index < 0) {
            index = 0;
        } else if (index >= cmps.length) {
            index = cmps.length - 1;
        }
        cmp = cmps[index];
        cmp.disassemble(vmMethod, getResolver(), optLevel, writer);
    }

    /**
     * Compile the given IMT.
     *
     * @param builder
     * @return the compiled IMT
     */
    public CompiledIMT compileIMT(IMTBuilder builder) {
        final IMTCompiler cmp = arch.getIMTCompiler();
        return cmp.compile(resolver, builder.getImt(), builder.getImtCollisions());
    }

    /**
     * Initialize this classloader during the initialization of the VM. If
     * needed, the tree of classes is generated from the boot class list.
     */
    protected void initialize() {
        if (classInfos == null) {
            final TreeMap<String, ClassInfo> classInfos = new TreeMap<String, ClassInfo>();
            final VmType[] list = bootClasses;
            final int count = list.length;
            for (int i = 0; i < count; i++) {
                final VmType vmClass = list[i];
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
     *
     * @param resolver
     */
    public void setResolver(ObjectResolver resolver) {
        if (this.resolver == null) {
            this.resolver = resolver;
        } else {
            throw new SecurityException("Cannot overwrite resolver");
        }
    }

    /**
     * Sets the systemRtJar. The given map must contains the resource names as
     * keys of type String, and the actual resources as byte array.
     *
     * @param resources The systemRtJar to set
     */
    public void setSystemRtJar(Map<String, byte[]> resources) {
        if (this.systemRtJar == null) {
            this.systemRtJar = resources;
        } else {
            throw new SecurityException("Cannot override system RT jar");
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
        //TODO maybe it should be declared as 'protected' in ClassLoader ???
        private final VmSystemClassLoader vmClassLoader;

        public ClassLoaderWrapper(VmSystemClassLoader vmClassLoader) {
            super(vmClassLoader, 0);
            this.vmClassLoader = vmClassLoader;
        }

        protected URL findResource(String name) {
            return vmClassLoader.findResource(name);
        }
    }

    /**
     * Class that holds information of a loading &amp; loaded class.
     *
     * @author epr
     */
    static class ClassInfo {

        /**
         * Name of the class
         */
        private final String name;

        /**
         * The class itself
         */
        private VmType<?> vmClass;

        /**
         * Classloading got an error?
         */
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
        public ClassInfo(VmType<?> vmClass) {
            this.name = vmClass.getName();
            this.vmClass = vmClass;
            if (name.indexOf('/') >= 0) {
                throw new IllegalArgumentException(
                    "vmClass.getName() contains '/'");
            }
        }

        /**
         * Returns the name of the class
         *
         * @return the name of the class
         */
        public final String getName() {
            return name;
        }

        /**
         * @return Type
         * @throws ClassNotFoundException
         */
        public final synchronized VmType<?> getVmClass()
            throws ClassNotFoundException {
            while (vmClass == null) {
                if (error) {
                    throw new ClassNotFoundException(name + "; " + errorMsg);
                }
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
         * Signal a class loading error. This will release other threads waiting
         * for this class with a ClassNotFoundException.
         *
         * @param errorMsg
         */
        public final synchronized void setLoadError(String errorMsg) {
            this.error = true;
            this.errorMsg = errorMsg;
            notifyAll();
        }

        /**
         * Has the class wrapped by this object been loaded?
         *
         * @return if the class wrapped by this object has been loaded
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
    public final VmSharedStatics getSharedStatics() {
        return sharedStatics;
    }

    /**
     * Gets the isolated statics table (of the current isolate)
     *
     * @return The statics table
     */
    public final VmIsolatedStatics getIsolatedStatics() {
        if (isolatedStatics != null) {
            return isolatedStatics;
        } else {
            return VmProcessor.current().getIsolatedStatics();
        }
    }

    /**
     * Gets the architecture used by this loader.
     *
     * @return The architecture
     */
    public final BaseVmArchitecture getArchitecture() {
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

    /**
     * @param loader
     */
    public void add(ResourceLoader loader) {
        resourceLoaders.add(loader);
    }

    /**
     * @param loader
     */
    public void remove(ResourceLoader loader) {
        resourceLoaders.remove(loader);
    }
}
