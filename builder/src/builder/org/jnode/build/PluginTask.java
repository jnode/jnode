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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.jnode.nanoxml.XMLElement;
import org.jnode.nanoxml.XMLParseException;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.Runtime;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginTask extends AbstractPluginTask {

    private LinkedList<ZipFileSet> descriptorSets = new LinkedList<ZipFileSet>();
    private File todir;
    private File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    
    private File userApplicationsDir;
    private String userApplicationsProperty;

    public ZipFileSet createDescriptors() {
        final ZipFileSet fs = new ZipFileSet();
        descriptorSets.add(fs);
        return fs;
    }

    /**
     * @throws BuildException
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {

        if (descriptorSets.isEmpty()) {
            throw new BuildException("At at least 1 descriptorset element");
        }
        if (todir == null) {
            throw new BuildException("The todir attribute must be set");
        }
        if (getPluginDir() == null) {
            throw new BuildException("The pluginDir attribute must be set");
        }
        if (!todir.exists()) {
            todir.mkdirs();
        } else if (!todir.isDirectory()) {
            throw new BuildException("todir must be a directory");
        }

        int max_thread_count = 10;
        int max_plugin_count = 500;
        StringBuilder userPlugins = new StringBuilder();

        final AtomicBoolean failure = new AtomicBoolean(false);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(max_thread_count, max_thread_count, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(max_plugin_count)) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if (t != null) {
                    failure.set(true);
                }
            }
        };
        
        final Map<String, File> descriptors = new HashMap<String, File>();
        for (FileSet fs : descriptorSets) {
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            final String[] files = ds.getIncludedFiles();
            for (final String file : files) {
                executor.execute(new Runnable() {
                    public void run() {
                        buildPlugin(descriptors, new File(ds.getBasedir(), file));
                    }
                });
            }
        }
        if ((userApplicationsDir != null) && userApplicationsDir.exists() && userApplicationsDir.isDirectory()) {
            File[] userJars = userApplicationsDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");
                }

            });
            
            for (File userJar : userJars) {
                processUserJar(executor, descriptors, userJar, userPlugins);
            }
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            throw new RuntimeException("Building plugins interrupted");
        }
        
        if (failure.get()) {
            throw new RuntimeException("At least one plugin task failed : see above errors");
        }
        
        // that must be called after completion of all plugin tasks
        if ((userPlugins.length() > 0) && (userPlugins.charAt(userPlugins.length() - 1) == ',')) {
            userPlugins.deleteCharAt(userPlugins.length() - 1);
        }        
        getProject().setProperty(userApplicationsProperty, userPlugins.toString());
    }

    /**
     * Attention : userPluginList must be a StringBuilder because it's accessed from multiple threads
     * @param executor
     * @param descriptors
     * @param userJar
     * @param userPluginList
     */
    private void processUserJar(ExecutorService executor, final Map<String, File> descriptors, final File userJar, 
            final StringBuilder userPluginList) {
        executor.execute(new Runnable() {
            public void run() {
                final String jarName = userJar.getName();
                final String pluginId = jarName.substring(0, jarName.length() - 4); // remove ".jar"  
                
                userPluginList.append(pluginId + ",");
                
                // replace ".jar" by ".xml"
                final String pluginDesc =  pluginId + ".xml";
                
                // FIXME remove the explicit reference to "cp" 
                // add user jar to path named "cp" (used in build.xml) 
                Path path = (Path) getProject().getReference("cp");
                path.createPathElement().setLocation(userJar);
                                
                // create the lib alias
                final String alias = pluginId + ".jar";
                LibAlias libAlias = createLibAlias();
                libAlias.setName(alias);
                libAlias.setAlias(userJar);
                                
                final File descriptorFile = new File(userJar.getParent(), pluginDesc);
                if (!descriptorFile.exists()) {
                    // build the descriptor from scratch
                    buildDescriptor(userJar, descriptorFile, pluginId, alias);
                }
                
                buildPlugin(descriptors, descriptorFile);
            }
        });
    }
    
    private void buildDescriptor(File userJar, File descriptorFile, String pluginId, String alias) {
        PrintStream out = null;
        try {
            out = new PrintStream(descriptorFile);
            
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<!DOCTYPE plugin SYSTEM \"jnode.dtd\">");
    
            out.println("<plugin id=\"" + pluginId + "\"");
            out.println("  name=\"" + pluginId + "\"");
            out.println("  version=\"\"");
            out.println("  license-name=\"unspecified\">");
    
            out.println("  <runtime>");
            out.println("    <library name=\"" + alias + "\">");
            out.println("      <export name=\"*\"/>");
            out.println("    </library>");
            out.println("  </runtime>");
    
            List<String> mainClasses = searchMain(userJar);
            if (!mainClasses.isEmpty()) {
                out.println("  <extension point=\"org.jnode.shell.aliases\">");
                for (String mainClass : mainClasses) {
                    int idx = mainClass.lastIndexOf('.');
                    String name = (idx < 0) ? mainClass : mainClass.substring(idx + 1);
                    out.println("    <alias name=\"" + name + "\" class=\"" + mainClass + "\"/>");
                    System.out.println(pluginId + " : added alias " + name + " for class " + mainClass);
                }
                
                out.println("  </extension>");
            } else {
                System.err.println("WARNING : no main found for plugin " + pluginId);
            }
            
            // FIXME using AllPermission is bad ! we must avoid that 
            out.println("  <extension point=\"org.jnode.security.permissions\">");
            out.println("    <permission class=\"java.security.AllPermission\" />");
            out.println("  </extension>");
            
            out.println("</plugin>");
        } catch (IOException ioe) {
            throw new BuildException(ioe);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    private List<String> searchMain(File userJar) throws FileNotFoundException, IOException {
        List<String> mainList = new ArrayList<String>();
        
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(userJar);
            
            // try to find the main class from the manifest
            Object value = jarFile.getManifest().getMainAttributes().get(Attributes.Name.MAIN_CLASS);
            if (value == null) {
                String name = Attributes.Name.MAIN_CLASS.toString();
                value = jarFile.getManifest().getAttributes(name).get(Attributes.Name.MAIN_CLASS);
            }
            
            if (value != null) {
                mainList.add(String.valueOf(value));
            } else {
                // scan the jar to find the main classes
                for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                    final JarEntry entry = e.nextElement(); 
                    final String name = entry.getName();
                    InputStream is = null;
                    
                    try {
                        if (name.endsWith(".class")) {
                            String className = name.substring(0, name.length() - ".class".length());
                            className = className.replace('/', '.');

                            is = jarFile.getInputStream(entry);
                            ClassLoader cl = new InputStreamLoader(is, (int) entry.getSize());
                            Class<?> clazz = Class.forName(className, false, cl);
                            Method m = clazz.getMethod("main", new Class<?>[]{String[].class});
                            if ((m.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                                mainList.add(className);
                            }
                        }
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                        // ignore
                    } catch (SecurityException se) {
                        se.printStackTrace();
                        // ignore
                    } catch (NoSuchMethodException nsme) {
                        // such error is expected for non-main classes => ignore
                    } catch (Throwable t) {
                        t.printStackTrace();
                        // ignore
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }
            }
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
        
        return mainList;
    }
    
    private static class InputStreamLoader extends ClassLoader {
        private InputStream inputStream;
        private int size;

        public InputStreamLoader(InputStream inputStream, int size) {
            this.inputStream = inputStream;
            this.size = size;
        }

        public Class loadClass(String className) throws ClassNotFoundException {
            return loadClass(className, true);
        }

        public synchronized Class loadClass(String className, boolean resolve)
            throws ClassNotFoundException {
            Class<?> result;

            try {

                result = super.findSystemClass(className);

            } catch (ClassNotFoundException e) {
                byte[] classData = null;
                
                try {
                    classData = new byte[size];
                    inputStream.read(classData);
                } catch (IOException ioe) {
                    throw new ClassNotFoundException(className, ioe);
                }
                
                if (classData == null) {
                    throw new ClassNotFoundException(className);
                }

                result = defineClass(className, classData, 0, classData.length);

                if (result == null) {
                    throw new ClassFormatError();
                }

                if (resolve) {
                    resolveClass(result);
                }
            }

            return result;
        }
    }
        

    /**
     * @param descriptors map of fullPluginId to File descriptor
     * @param descriptor  the plugin descriptor XML
     * @throws BuildException on failure
     */
    protected void buildPlugin(Map<String, File> descriptors, File descriptor) throws BuildException {
        final PluginDescriptor descr = readDescriptor(descriptor);

        final String fullId = descr.getId() + "_" + descr.getVersion();
        if (descriptors.containsKey(fullId)) {
            File otherDesc = descriptors.get(fullId);
            throw new BuildException("Same id(" + fullId + ") for 2 plugins: " + otherDesc + ", " + descriptor);
        }
        descriptors.put(fullId, descriptor);

        File destFile = new File(todir, fullId + ".jar");

        final Jar jarTask = new Jar();
        jarTask.setProject(getProject());
        jarTask.setTaskName(getTaskName());
        jarTask.setDestFile(destFile);
        jarTask.setCompress(false);

        // Add plugin.xml
        final File tmpPluginDir;
        final File tmpPluginXmlFile;
        try {
            tmpPluginDir = new File(tmpDir, "jnode-plugins" + File.separator + fullId);
            tmpPluginDir.mkdirs();
            tmpPluginXmlFile = new File(tmpPluginDir, "plugin.xml");
            FileUtils.newFileUtils().copyFile(descriptor, tmpPluginXmlFile);
            FileSet fs = new FileSet();
            fs.setDir(tmpPluginDir);
            fs.createInclude().setName("plugin.xml");
            jarTask.addFileset(fs);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }

        // Create manifest
        try {
            jarTask.addConfiguredManifest(createManifest(descr));
        } catch (ManifestException ex) {
            throw new BuildException(ex);
        }

        // Add runtime resources
        final Runtime rt = descr.getRuntime();
        if (rt != null) {
            final HashMap<File, ZipFileSet> fileSets = new HashMap<File, ZipFileSet>();
            final Library[] libs = rt.getLibraries();
            for (int l = 0; l < libs.length; l++) {
                processLibrary(jarTask, libs[l], fileSets, getPluginDir());
            }
        }

        jarTask.execute();
    }

    /**
     * Create a manifest for the given descriptor
     *
     * @param descr plugin descriptor object
     * @return the manifest
     * @throws ManifestException
     */
    protected Manifest createManifest(PluginDescriptor descr) throws ManifestException {
        Manifest mf = new Manifest();

        mf.addConfiguredAttribute(new Manifest.Attribute("Bundle-SymbolicName", descr.getId()));
        mf.addConfiguredAttribute(new Manifest.Attribute("Bundle-ManifestVersion", "2"));
        mf.addConfiguredAttribute(new Manifest.Attribute("Bundle-Version", descr.getVersion()));

        return mf;
    }

    protected void addResourceList(File pluginDescr, Collection<ZipFileSet> resources)
        throws XMLParseException, FileNotFoundException, IOException {
        final XMLElement xml = new XMLElement();
        xml.parseFromReader(new FileReader(pluginDescr));

//        XMLElement runtime = xml.g

    }

//    private final XMLElement getRuntimeElement(XMLElement xml) {
//    }

    /**
     * @return The destination directory
     */
    public final File getTodir() {
        return this.todir;
    }

    /**
     * @param todir
     */
    public final void setTodir(File todir) {
        this.todir = todir;
    }

    /**
     * @return The temp directory
     */
    public final File getTmpDir() {
        return this.tmpDir;
    }

    /**
     * @param tmpDir
     */
    public final void setTmpDir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    /**
     * @param file
     */
    public void setUserApplicationsDir(File file) {
        userApplicationsDir = file;
    }
    
    /**
     * @param file
     */
    public void setUserApplicationsProperty(String name) {
        userApplicationsProperty = name;
    }
}
