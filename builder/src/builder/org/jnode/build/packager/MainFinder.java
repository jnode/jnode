package org.jnode.build.packager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class for searching main methods in a jar
 * 
 * @author fabien
 *
 */
public class MainFinder {
    
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
    public static List<String> searchMain(File userJar) throws FileNotFoundException, IOException {
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
