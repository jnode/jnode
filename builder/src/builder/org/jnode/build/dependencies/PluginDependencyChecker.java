/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

package org.jnode.build.dependencies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassPath;
import org.apache.bcel.util.SyntheticRepository;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jnode.build.AbstractPluginTask;
import org.jnode.plugin.FragmentDescriptor;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginReference;

/**
 * Task used to check dependencies between plugins.
 *
 * @author Fabien DUMINY
 * @author Sebastian Lohmeier
 */
public class PluginDependencyChecker extends AbstractPluginTask {
    private List<FileSet> descriptorSets = new ArrayList<FileSet>(256);
    private List<FileSet> pluginSets = new ArrayList<FileSet>(256);

    private List<Plugin> plugins;
    private List<Fragment> fragments;
    private Set<Plugin> systemPlugins;

    public FileSet createDescriptors() {
        final FileSet fs = new FileSet();
        descriptorSets.add(fs);
        return fs;
    }

    public FileSet createPlugins() {
        final FileSet fs = new FileSet();
        pluginSets.add(fs);
        return fs;
    }

    /**
     * @throws BuildException
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        if (descriptorSets.isEmpty()) {
            throw new BuildException("At least 1 descriptors element is required");
        }

        if (pluginSets.isEmpty()) {
            throw new BuildException("At least 1 plugins element is required");
        }

        Map<String, Plugin> containedClasses = new HashMap<String, Plugin>();
        if (createPlugins(containedClasses) > 0) {
            System.err.println("\nCan't proceed with more in-depth checks. Please fix above errors first.");
        } else {
            analyzePlugins(containedClasses, plugins);
        }
    }

    private Plugin findPlugin(String fullPluginId) {
        // TODO ... consider replacing this with a map.
        for (Plugin plugin : plugins) {
            if (plugin.fullPluginId.equals(fullPluginId)) {
                return plugin;
            }
        }
        return null;
    }

    private Set<Fragment> findFragmentsOwnedByPlugin(Plugin plugin) {
        String fullPluginId = plugin.getFullPluginId();
        Set<Fragment> results = new HashSet<Fragment>();
        for (Fragment fragment : fragments) {
            if (fragment.getFullPluginIdOfOwningPlugin().equals(fullPluginId)) {
                results.add(fragment);
            }
        }
        return results;
    }

    private Collection<Plugin> findSystemPlugins() {
        if (systemPlugins != null) {
            return systemPlugins;
        } else {
            systemPlugins = new HashSet<Plugin>();
            for (Plugin plugin : plugins) {
                if (plugin.isSystemPlugin()) {
                    systemPlugins.add(plugin);
                }
            }
            return systemPlugins;
        }
    }

    /**
     * @param containedClasses
     * @param plugins
     */
    private void analyzePlugins(Map<String, Plugin> containedClasses, List<Plugin> plugins) {
        Collections.sort(plugins);
        for (Plugin plugin : plugins) {
            analyzePlugin(containedClasses, plugin);
        }
    }

    /**
     * @param containedClasses
     * @param plugin
     */
    private void analyzePlugin(Map<String, Plugin> containedClasses, Plugin plugin) {
        plugin.verifyDescriptor(containedClasses);
    }

    /**
     * Creates the Plugin instances, reading and parsing the XML descriptors
     * and the JAR files of all plugins.
     *
     * @param containedClasses
     * @return The number of errors found.
     */
    private int createPlugins(Map<String, Plugin> containedClasses) {
        JarFiles jarFiles = new JarFiles(pluginSets);
        plugins = new ArrayList<Plugin>();
        fragments = new ArrayList<Fragment>();
        int errorCount = 0;
        for (File descFile : getDescriptorFiles()) {
            Plugin plugin = processPlugin(descFile, jarFiles);
            if (plugin != null) {
                plugins.add(plugin);

                if (plugin instanceof Fragment) {
                    fragments.add((Fragment) plugin);
                }

                for (String containedClass : plugin.containedClasses) {
                    if (!containedClasses.containsKey(containedClass)) {
                        containedClasses.put(containedClass, plugin);
                    } else {
                        System.err.println("WARNING: Class " + containedClass + " contained in both plugins:");
                        System.err.println(containedClasses.get(containedClass).getFullPluginId() + " and " +
                            plugin.getFullPluginId());
                        errorCount++;
                    }
                }
            }
        }
        return errorCount;
    }

    protected File[] getDescriptorFiles() {
        List<File> files = new ArrayList<File>();
        for (FileSet fs : descriptorSets) {
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            for (String incFile : ds.getIncludedFiles()) {
                files.add(new File(ds.getBasedir(), incFile));
            }
        }
        return files.toArray(new File[files.size()]);
    }


    /**
     * @param jarFiles
     * @param descriptor
     * @throws BuildException
     */
    protected Plugin processPlugin(File descriptor, JarFiles jarFiles)
        throws BuildException {
        try {
            final PluginDescriptor descr = readDescriptor(descriptor);
            String fullPluginId = descr.getId() + "_" + descr.getVersion();

            if (!jarFiles.hasPluginJar(fullPluginId)) {
                System.out.println("WARNING: no Jar file found for plugin " + fullPluginId);
                return null;
            }

            if (descr instanceof FragmentDescriptor) {
                return new Fragment(jarFiles.getPluginJar(fullPluginId), (FragmentDescriptor) descr);
            } else {
                return new Plugin(jarFiles.getPluginJar(fullPluginId), descr);
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    private class Plugin implements Comparable<Plugin> {
        private final String classSuffix = ".class";
        private final Pattern typePattern = Pattern.compile("\u004C[a-zA-Z_0-9/\u002E\u0024]*;");
        protected final String fullPluginId;
        protected final PluginDescriptor descr;
        protected final Set<String> containedClasses = new HashSet<String>();
        protected final Map<String, Set<String>> usedClasses = new HashMap<String, Set<String>>();

        private Plugin(JarFile jarFile, PluginDescriptor descr) {
            this.fullPluginId = createFullPluginId(descr.getPluginReference());
            this.descr = descr;
            initContainedClasses(jarFile);
            initUsedClasses(jarFile);
        }

        private boolean isSystemPlugin() {
            return descr.isSystemPlugin();
        }

        protected String createFullPluginId(PluginReference reference) {
            return reference.getId() + "_" + reference.getVersion();
        }

        protected String getFullPluginId() {
            return fullPluginId;
        }

        protected void verifyDescriptor(Map<String, Plugin> containedClasses) {
            boolean errorFound = false;
            StringBuffer buffer = new StringBuffer("The plugin " + fullPluginId + "");
            buffer.append("\n------------------------------------------------------------------\n");

            errorFound |= collectUnmatchedDependencies(buffer, containedClasses);
            errorFound |= isNotUseful(buffer);

            Set<String> allClasses = getAllUsedClasses();
            errorFound |= assortClassesContainedInImportedPlugins(buffer, this, true, allClasses);

            for (Plugin plugin : findSystemPlugins()) {
                allClasses.removeAll(plugin.containedClasses);
            }

            //errorFound |= isMissingImportDeclarations(buffer, allClasses);

            if (errorFound) {
                System.out.println(buffer.toString());
            }
        }

        private boolean isNotUseful(StringBuffer buffer) {
            if (descr.getRuntime() != null)
                return false;
            if ((descr.getExtensions() != null) && (descr.getExtensions().length > 0))
                return false;
            if (descr.hasCustomPluginClass())
                return false;
            buffer.append(" * neither exports classes, nor is an extension, nor provides a plugin class\n");
            return true;
        }

        /**
         * @param buffer
         * @param containedClasses
         */
        private boolean collectUnmatchedDependencies(StringBuffer buffer, Map<String, Plugin> containedClasses) {
            Map<String, List<String>> unmatchedDependencies = new HashMap<String, List<String>>();
            for (String usingClass : usedClasses.keySet()) {
                for (String className : usedClasses.get(usingClass)) {
                    if (!containedClasses.containsKey(className)) {
                        if (unmatchedDependencies.containsKey(className)) {
                            unmatchedDependencies.get(className).add(usingClass);
                        } else {
                            List<String> list = new ArrayList<String>();
                            list.add(usingClass);
                            unmatchedDependencies.put(className, list);
                        }
                    }
                }
            }

            if (!unmatchedDependencies.isEmpty()) {
                dumpUnmatchedDependencies(buffer, unmatchedDependencies);
                return true;
            } else {
                return false;
            }
        }

        /**
         * @param buffer
         * @param unmatchedDependencies
         */
        private void dumpUnmatchedDependencies(StringBuffer buffer, Map<String, List<String>> unmatchedDependencies) {
            buffer.append(" * has unresolved class dependencies:\n");
            List<String> keys = new ArrayList<String>(unmatchedDependencies.keySet());
            Collections.sort(keys);
            for (String className : keys) {
                List<String> usedClasses = unmatchedDependencies.get(className);
                Collections.sort(usedClasses);
                buffer.append("     " + className + "\n");
                boolean first = true;
                for (String usedClass : usedClasses) {
                    buffer.append("       " + (first ? "is used by " : "and        ") + usedClass + "\n");
                    first = false;
                }
            }
        }

        private boolean assortClassesContainedInImportedPlugins(StringBuffer buffer, Plugin plugin,
                                                                boolean isFirst, Set<String> allClasses) {
            boolean error = false;
            Map<String, Plugin> usedPlugins = new HashMap<String, Plugin>();

            if (plugin instanceof Fragment) {
                String idOfOwningPlugin = ((Fragment) plugin).getFullPluginIdOfOwningPlugin();
                usedPlugins.put(idOfOwningPlugin, findPlugin(idOfOwningPlugin));
            }

            for (Fragment fragment : findFragmentsOwnedByPlugin(plugin)) {
                usedPlugins.put(fragment.getFullPluginId(), fragment);
            }

            for (PluginPrerequisite prerequisite : plugin.descr.getPrerequisites()) {
                String idOfUsedPlugin = createFullPluginId(prerequisite.getPluginReference());
                usedPlugins.put(idOfUsedPlugin, findPlugin(idOfUsedPlugin));
            }

            for (String idOfUsedPlugin : usedPlugins.keySet()) {
                Plugin usedPlugin = usedPlugins.get(idOfUsedPlugin);

                if (usedPlugin == null) {
                    buffer.append(" * references unknown plugin " + idOfUsedPlugin + "\n");
                    error = true;
                    continue;
                }

                if (usedPlugin.containedClasses.size() > 0
                    && !allClasses.removeAll(usedPlugin.containedClasses)) {
                    if (isFirst && !(plugin instanceof Fragment) &&
                        (usedPlugin.descr.getPrerequisites() == null
                            || usedPlugin.descr.getPrerequisites().length == 0)) {
                        buffer.append(" * references plugin " + idOfUsedPlugin + ", which is not actually required\n");
                        error = true;
                        // don't need to analyze recursively, since the plugin
                        // imports no other plugins
                    }
                } else {
                    // recursively analyze this plugin ...
                    error |= assortClassesContainedInImportedPlugins(buffer, usedPlugin, false, allClasses);
                }
            }
            return error;
        }

        @SuppressWarnings("unused")
        private boolean isMissingImportDeclarations(StringBuffer buffer, Set<String> allClasses) {
            if (!allClasses.isEmpty()) {
                buffer.append(" * is missing import declarations covering the following classes: " + allClasses + "\n");
                return true;
            } else {
                return false;
            }
        }

        private Set<String> getAllUsedClasses() {
            HashSet<String> allClasses = new HashSet<String>();
            for (Set<String> classNames : usedClasses.values()) {
                allClasses.addAll(classNames);
            }
            return allClasses;
        }

        private void initContainedClasses(JarFile jarFile) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (!entry.isDirectory() && entryName.endsWith(classSuffix)) {
                    String name = entryName.substring(0, entryName.length()
                        - classSuffix.length());
                    if (name.indexOf(".") != -1) {
                        System.err.println(name);
                    }
                    name = name.replace('/', '.');
                    name = name.replace('\\', '.');
                    containedClasses.add(name);
                }
            }
        }

        private void initUsedClasses(JarFile jarFile) {
            SyntheticRepository repository = SyntheticRepository.getInstance(new ClassPath(jarFile.getName()));
            for (String className : containedClasses) {
                try {
                    analyzeClass(className, repository.loadClass(className));
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
            }
        }

        private void analyzeClass(String className, JavaClass javaClass) {
            analyzeSuperClass(className, javaClass);
            analyzeInterfaces(className, javaClass);
            analyzeFields(className, javaClass);
            analyzeMethods(className, javaClass);
            analyzeConstantPool(className, javaClass);
        }

        private void analyzeSuperClass(String usingClass, JavaClass javaClass) {
            addUsedClass(usingClass, javaClass.getSuperclassName());
        }

        private void analyzeInterfaces(String usingClass, JavaClass javaClass) {
            addUsedClasses(usingClass, javaClass.getInterfaceNames());
        }

        private void analyzeFields(String usingClass, JavaClass javaClass) {
            for (Field field : javaClass.getFields()) {
                addUsedClasses(usingClass, decodeSignature(field.getType().getSignature()));
            }
        }

        private void analyzeMethods(String usingClass, JavaClass javaClass) {
            for (Method method : javaClass.getMethods()) {
                for (Type argument : method.getArgumentTypes()) {
                    String typeName = decodeTypeName(argument.getSignature());
                    if (typeName != null) {
                        addUsedClass(usingClass, typeName);
                    }
                }
                addUsedClasses(usingClass, decodeSignature(method.getReturnType().getSignature()));
            }
        }

        private void analyzeConstantPool(String usingClass, JavaClass javaClass) {
            final ConstantPool constantPool = javaClass.getConstantPool();
            for (Constant constant : constantPool.getConstantPool()) {
                if (constant instanceof ConstantClass) {
                    String signature = ((ConstantClass) constant).getBytes(constantPool);
                    if (signature != null) {
                        if (signature.startsWith("[")) {
                            signature = decodeTypeName(signature);
                        }
                        if (signature != null) {
                            addUsedClass(usingClass, signature);
                        }
                    }
                } else if (constant instanceof ConstantNameAndType) {
                    for (String typeName : decodeSignature(
                        ((ConstantNameAndType) constant).getSignature(constantPool))) {
                        if (typeName != null) {
                            addUsedClass(usingClass, typeName);
                        }
                    }
                }
            }
        }

        private void addUsedClasses(String usingClass, Collection<String> classNames) {
            for (String className : classNames) {
                addUsedClass(usingClass, className);
            }
        }

        private void addUsedClasses(String usingClass, String[] classNames) {
            for (String className : classNames) {
                addUsedClass(usingClass, className);
            }
        }

        private void addUsedClass(String usingClass, String usedClass) {
            usedClass = usedClass.replace('/', '.');
            if (!containedClasses.contains(usedClass)) {
                if (usedClasses.containsKey(usingClass)) {
                    usedClasses.get(usingClass).add(usedClass);
                } else {
                    Set<String> set = new HashSet<String>();
                    set.add(usedClass);
                    usedClasses.put(usingClass, set);
                }
            }
        }

        private String decodeTypeName(String signature) {
            Matcher matcher = typePattern.matcher(signature);
            if (matcher.find()) {
                String objectTypeName = matcher.group();
                return objectTypeName.substring(1, objectTypeName.length() - 1);
            } else {
                return null;
            }
        }

        /**
         * @return Returns a list of classnames referenced by the
         *         signature.
         */
        private List<String> decodeSignature(String signature) {
            return getObjectTypeNames(signature);
        }

        private List<String> getObjectTypeNames(String signature) {
            List<String> objectTypeNames = new ArrayList<String>();
            Matcher matcher = typePattern.matcher(signature);
            while (matcher.find()) {
                String objectTypeName = matcher.group();
                objectTypeName = objectTypeName.substring(1, objectTypeName.length() - 1);
                objectTypeNames.add(objectTypeName);
            }
            return objectTypeNames;
        }

        public String toString() {
            return "Plugin " + fullPluginId + " contained=" + containedClasses + " used=" + usedClasses;
        }

        @Override
        public int compareTo(Plugin other) {
            return fullPluginId.compareTo(other.fullPluginId);
        }
    }

    private class Fragment extends Plugin {
        private String fullPluginIdOfOwningPlugin;

        private Fragment(JarFile jarFile, FragmentDescriptor descr) {
            super(jarFile, descr);
            this.fullPluginIdOfOwningPlugin = createFullPluginId(descr.getPluginReference());
        }

        private String getFullPluginIdOfOwningPlugin() {
            return fullPluginIdOfOwningPlugin;
        }

        public String toString() {
            return "Fragment " + fullPluginId + " owned by=" + fullPluginIdOfOwningPlugin + " contained=" +
                containedClasses + " used=" + usedClasses;
        }
    }

    private class JarFiles {
        /**
         * Maps plugin names to paths of jar files.
         */
        private final Map<String, String> jarFiles = new HashMap<String, String>();

        private JarFiles(List<FileSet> pluginSets) {
            for (FileSet fs : pluginSets) {
                addFileSet(fs);
            }
        }

        private void addFileSet(FileSet fileSet) {
            final DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
            final String[] files = ds.getIncludedFiles();
            String baseDir = ds.getBasedir().getAbsolutePath();
            //System.err.println("getAllJars() iterating ... basedir="+baseDir+" files.length="+files.length);
            for (String file : files) {
                jarFiles.put(file, baseDir + File.separatorChar + file);
            }
        }

        private boolean hasPluginJar(String fullPluginId) {
            return jarFiles.containsKey(fullPluginId + ".jar");
        }

        private JarFile getPluginJar(String fullPluginId) throws IOException {
            return new JarFile(jarFiles.get(fullPluginId + ".jar"));
        }
    }
}

