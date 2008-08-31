package org.jnode.build.packager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.jnode.build.BuildException;
import org.jnode.build.PluginTask;
import org.jnode.build.AbstractPluginTask.LibAlias;

/**
 * Class building new jnode plugins from third party jars/resources
 * 
 * @author fabien
 *
 */
public class PluginBuilder extends PackagerTask {
    private final Task parent;
   
    /**
     * List of user plugin ids 
     */
    private StringBuilder userPluginIds = new StringBuilder();
    
    /**
     * {@link Path} to third party jars for compilation purpose
     */
    private Path path;
    
    public PluginBuilder(Task parent) {
        this.parent = parent;
    }

    /**
     * Define the path reference for compilation
     * @param pathRefId
     */
    public void setPathRefId(String pathRefId) {
        this.path = (Path) parent.getProject().getReference(pathRefId);
    }
    
    /**
     * Main method for build the jnode plugin
     * 
     * @param executor
     * @param descriptors
     */
    public void execute(ThreadPoolExecutor executor, final Map<String, File> descriptors) {
        if (isEnabled()) {
            if (path == null) {
                throw new BuildException("pathRefId is mandatory");
            }
            
            File[] userJars = userApplicationsDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar") || pathname.isDirectory();
                }

            });
            
            for (File userJar : userJars) {
                processUserJar(executor, descriptors, userJar, userPluginIds);
            }
        }
    }

    /**
     * Do finalization tasks. For instance, it's writing the plugin ids to the properties file
     */
    public void finish() {
        if (isEnabled()) {
            if ((userPluginIds.length() > 0) && (userPluginIds.charAt(userPluginIds.length() - 1) == ',')) {
                userPluginIds.deleteCharAt(userPluginIds.length() - 1);
            }        
            
            // write properties
            Properties properties = new Properties();
            properties.put(USER_PLUGIN_IDS, userPluginIds.toString());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(getPropertiesFile());
                properties.store(fos, "");
            } catch (IOException e) {
                throw new BuildException("failed to write properties file", e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        throw new BuildException("failed to close properties file", e);
                    }
                }
            }
        }
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
        final PluginTask task = (PluginTask) parent;
        executor.execute(new Runnable() {
            public void run() {
                final String jarName = userJar.getName();
                final String pluginId;
                
                if (userJar.isFile()) {
                    pluginId = jarName.substring(0, jarName.length() - 4); // remove ".jar"
                } else {
                    pluginId = jarName; // use directory name as plugin id
                }
                
                userPluginList.append(pluginId + ",");
                
                // replace ".jar" by ".xml"
                final String pluginDesc =  pluginId + ".xml";
                
                path.createPathElement().setLocation(userJar);
                                
                // create the lib alias
                final String alias = pluginId + ".jar";
                LibAlias libAlias = task.createLibAlias();
                libAlias.setName(alias);
                libAlias.setAlias(userJar);
                                
                final File descriptorFile = new File(userJar.getParent(), pluginDesc);
                if (!descriptorFile.exists()) {
                    // build the descriptor from scratch
                    buildDescriptor(userJar, descriptorFile, pluginId, alias);
                }
                
                task.buildPlugin(descriptors, descriptorFile);
            }
        });
    }
    
    /**
     * Build the plugin descriptor
     * 
     * @param userJar
     * @param descriptorFile
     * @param pluginId
     * @param alias
     */
    private void buildDescriptor(File userJar, File descriptorFile, String pluginId, String alias) {
        PrintStream out = null;
        boolean success = false;
        try {
            out = new PrintStream(descriptorFile);
            
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<!DOCTYPE plugin SYSTEM \"jnode.dtd\">");
    
            out.println("<plugin id=\"" + pluginId + "\"");
            out.println("  name=\"" + pluginId + "\"");
            out.println("  version=\"\"");
            out.println("  class=\"org.jnode.plugin.AutoUnzipPlugin\"");
            out.println("  auto-start=\"true\"");            
            out.println("  license-name=\"unspecified\">");
    
            out.println("  <runtime>");
            out.println("    <library name=\"" + alias + "\">");
            out.println("      <export name=\"*\"/>");
            out.println("    </library>");
            out.println("  </runtime>");
    
            if (userJar.isFile()) {
                List<String> mainClasses = searchMain(userJar);
                if (!mainClasses.isEmpty()) {
                    out.println("  <extension point=\"org.jnode.shell.aliases\">");
                    for (String mainClass : mainClasses) {
                        int idx = mainClass.lastIndexOf('.');
                        String name = (idx < 0) ? mainClass : mainClass.substring(idx + 1);
                        out.println("    <alias name=\"" + name + "\" class=\"" + mainClass + "\"/>");
                        log(pluginId + " : added alias " + name + " for class " + mainClass, Project.MSG_INFO);
                    }
                    
                    out.println("  </extension>");
                } else {
                    log("no main found for plugin " + pluginId, Project.MSG_WARN);
                }
            }
            
            out.println("  <!-- FIXME : use more restricted permissions -->");
            out.println("  <extension point=\"org.jnode.security.permissions\">");
            out.println("    <permission class=\"java.security.AllPermission\" />");
            out.println("  </extension>");
            
            out.println("</plugin>");
            success = true;
        } catch (IOException ioe) {
            throw new BuildException("failed to write plugin descriptor", ioe);
        } finally {
            if (out != null) {
                out.close();
            }
            
            if (!success) {
                // in case of failure, remove the incomplete descriptor file
                descriptorFile.delete();
            }
        }
    }
    
    /**
     * Search for the main classes in the jars/resources.
     * Starts by looking in the jars manifests and, if nothing is found,
     * then scans the jars/resources for main classes.  
     * 
     * @param userJar
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private List<String> searchMain(File userJar) throws FileNotFoundException, IOException {
        List<String> mainList = new ArrayList<String>();
        
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(userJar);
            
            // try to find the main class from the manifest
            Object value = null;
            
            // do we have a manifest ?
            if (jarFile.getManifest() != null) {
                value = jarFile.getManifest().getMainAttributes().get(Attributes.Name.MAIN_CLASS);
                if (value == null) {
                    String name = Attributes.Name.MAIN_CLASS.toString();
                    final Attributes attr = jarFile.getManifest().getAttributes(name);
                    
                    // we have a manifest but do we have a main class defined inside ?
                    if (attr != null) {
                        value = attr.get(Attributes.Name.MAIN_CLASS);
                    }
                }
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
    
    /**
     * Custom {@link ClassLoader} used to load classes from an InputStream. 
     * It helps finding a main class in a jar file. 
     * @author fabien
     *
     */
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
}
