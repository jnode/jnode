/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
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

/**
 * Task used to check dependencies between plugins.
 *
 * @author Fabien DUMINY
 * @author Sebastian Lohmeier
 */
public class BCELDependencyChecker extends AbstractPluginTask {
    private List descriptorSets = new ArrayList(256);
    private List pluginSets = new ArrayList(256);

    private List plugins;
    private List fragments;
    private Set systemPlugins;

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

        Map containedClasses = new HashMap();
        if (createPlugins(containedClasses)) {
            System.err.println("\nCan't proceed with more in-depth checks. Please fix above errors first.");
        } else {
            analyzePlugins(containedClasses, plugins);
        }
    }

    private Plugin findPlugin(String fullPluginId) {
        Iterator iter = plugins.iterator();
        while (iter.hasNext()) {
            Plugin plugin = (Plugin) iter.next();
            if (plugin.fullPluginId.equals(fullPluginId))
                return plugin;
        }
        return null;
    }

    private Set findFragmentsOwnedByPlugin(Plugin plugin) {
        String fullPluginId = plugin.getFullPluginId();
        Set results = new HashSet();
        Iterator iter = fragments.iterator();
        while (iter.hasNext()) {
            Fragment fragment = (Fragment) iter.next();
            if (fragment.getFullPluginIdOfOwningPlugin().equals(fullPluginId))
                results.add(fragment);
        }
        return results;
    }

    private Collection findSystemPlugins() {
        if (systemPlugins != null) {
            return systemPlugins;
        } else {
            systemPlugins = new HashSet();
            Iterator iter = plugins.iterator();
            while (iter.hasNext()) {
                Plugin plugin = (Plugin) iter.next();
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
    private void analyzePlugins(Map containedClasses, List plugins) {
        Iterator iter = plugins.iterator();
        while (iter.hasNext()) {
            analyzePlugin(containedClasses, (Plugin) iter.next());
        }
    }

    /**
     * @param containedClasses
     * @param plugin
     */
    private void analyzePlugin(Map containedClasses, Plugin plugin) {
        plugin.verifyDescriptor(containedClasses);
    }

    /**
     * Creates the Plugin instances (e.g. reads and parses the XML descriptors
     * and the JAR files of all plugins.
     *
     * @param containedClasses
     * @return The list of plugins.
     */
    private boolean createPlugins(Map containedClasses) {
        JarFiles jarFiles = new JarFiles(pluginSets);
        plugins = new ArrayList();
        fragments = new ArrayList();
        File[] descFiles = getDescriptorFiles();
        boolean duplicateClasses = false;
        for (int j = 0; j < descFiles.length; j++) { //maxPlugins; j++) {
            Plugin plugin = processPlugin(descFiles[j], jarFiles);
            if (plugin != null) {
                plugins.add(plugin);

                if (plugin instanceof Fragment)
                    fragments.add(plugin);

                Iterator iter = plugin.containedClasses.iterator();
                while (iter.hasNext()) {
                    String containedClass = (String) iter.next();
                    if (!containedClasses.containsKey(containedClass)) {
                        containedClasses.put(containedClass, plugin);
                    } else {
                        System.err.println("WARNING: Class " + containedClass + " contained in both plugins:");
                        System.err.println(((Plugin) containedClasses.get(containedClass)).getFullPluginId() + " and " +
                            plugin.getFullPluginId());
                        duplicateClasses |= true;
                    }
                }
            }
        }
        return duplicateClasses;
    }

    protected File[] getDescriptorFiles() {
        List files = new ArrayList();
        for (Iterator i = descriptorSets.iterator(); i.hasNext();) {
            final FileSet fs = (FileSet) i.next();
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            final String[] incFiles = ds.getIncludedFiles();
            for (int j = 0; j < incFiles.length; j++) {
                files.add(new File(ds.getBasedir(), incFiles[j]));
            }
        }
        return (File[]) files.toArray(new File[files.size()]);
    }


    /**
     * @param sb
     * @param depends
     * @param descriptors
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

    private class Plugin {
        private final String classSuffix = ".class";
        private final Pattern typePattern = Pattern.compile("\u004C[a-zA-Z_0-9/\u002E\u0024]*;");
        protected final String fullPluginId;
        protected final PluginDescriptor descr;
        protected final Set containedClasses = new HashSet();
        protected final Map usedClasses = new HashMap();

        private Plugin(JarFile jarFile, PluginDescriptor descr) {
            this.fullPluginId = createFullPluginId(descr.getId(), descr.getVersion());
            this.descr = descr;
            initContainedClasses(jarFile);
            initUsedClasses(jarFile);
        }

        private boolean isSystemPlugin() {
            return descr.isSystemPlugin();
        }

        protected String createFullPluginId(String id, String version) {
            return id + "_" + version;
        }

        protected String getFullPluginId() {
            return fullPluginId;
        }

        protected void verifyDescriptor(Map containedClasses) {
            boolean errorFound = false;
            StringBuffer buffer = new StringBuffer("The plugin " + fullPluginId + "");
            buffer.append("\n------------------------------------------------------------------\n");

            errorFound |= collectUnmatchedDependencies(buffer, containedClasses);
            errorFound |= exportsOrIsExtension(buffer);

            Set allClasses = getAllUsedClasses();
            errorFound |= assortClassesContainedInImportedPlugins(buffer, this, true, allClasses);

            Iterator iter = findSystemPlugins().iterator();
            while (iter.hasNext()) {
                allClasses.removeAll(((Plugin) iter.next()).containedClasses);
            }

            //errorFound |= isMissingImportDeclarations(buffer, allClasses);

            if (errorFound) {
                System.out.println(buffer.toString());
            }
        }

        private boolean exportsOrIsExtension(StringBuffer buffer) {
            if (descr.getRuntime() == null
                && (descr.getExtensions() == null || descr.getExtensions().length == 0)) {
                buffer.append(" * neither exports classes, nor is an extension\n");
                return true;
            } else {
                return false;
            }
        }

        /**
         * @param containedClasses
         * @param plugin
         * @param unmatchedDependencies
         */
        private boolean collectUnmatchedDependencies(StringBuffer buffer, Map containedClasses) {
            Map unmatchedDependencies = new HashMap();
            Iterator iter1 = usedClasses.keySet().iterator();
            while (iter1.hasNext()) {
                String usingClass = (String) iter1.next();
                Iterator iter2 = ((Set) usedClasses.get(usingClass)).iterator();
                while (iter2.hasNext()) {
                    String className = (String) iter2.next();
                    if (!containedClasses.containsKey(className)) {
                        if (unmatchedDependencies.containsKey(className)) {
                            ((List) unmatchedDependencies.get(className)).add(usingClass);
                        } else {
                            List list = new ArrayList();
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
         * @param plugin
         * @param unmatchedDependencies
         */
        private void dumpUnmatchedDependencies(StringBuffer buffer, Map unmatchedDependencies) {
            buffer.append(" * has unresolved dependencies:\n");
            List keys = new ArrayList(unmatchedDependencies.keySet());
            Collections.sort(keys);
            Iterator iter3 = keys.iterator();
            while (iter3.hasNext()) {
                String className = (String) iter3.next();
                List usedClasses = (List) unmatchedDependencies.get(className);
                Collections.sort(usedClasses);
                Iterator iter4 = usedClasses.iterator();
                buffer.append("   " + className + "\n\tused by\t" + iter4.next() + "\n");
                while (iter4.hasNext()) {
                    buffer.append("   \t\t" + iter4.next() + "\n");
                }
            }
        }

        private boolean assortClassesContainedInImportedPlugins(StringBuffer buffer, Plugin plugin,
                                                                boolean isFirst, Set allClasses) {
            boolean error = false;
            Map usedPlugins = new HashMap();

            if (plugin instanceof Fragment) {
                String idOfOwningPlugin = ((Fragment) plugin).getFullPluginIdOfOwningPlugin();
                usedPlugins.put(idOfOwningPlugin, findPlugin(idOfOwningPlugin));
            }

            Set ownedFragments = findFragmentsOwnedByPlugin(plugin);
            Iterator iter = ownedFragments.iterator();
            while (iter.hasNext()) {
                Fragment fragment = (Fragment) iter.next();
                usedPlugins.put(fragment.getFullPluginId(), fragment);
            }

            PluginPrerequisite[] prerequisites = plugin.descr.getPrerequisites();
            for (int i = 0; i < prerequisites.length; i++) {
                String idOfUsedPlugin =
                    createFullPluginId(prerequisites[i].getPluginId(), prerequisites[i].getPluginVersion());
                usedPlugins.put(idOfUsedPlugin, findPlugin(idOfUsedPlugin));
            }

            Iterator iter1 = usedPlugins.keySet().iterator();
            while (iter1.hasNext()) {
                String idOfUsedPlugin = (String) iter1.next();
                Plugin usedPlugin = (Plugin) usedPlugins.get(idOfUsedPlugin);

                if (usedPlugin == null) {
                    buffer.append(" * references unknown plugin " + idOfUsedPlugin + "\n");
                    error |= true;
                    continue;
                }

                if (usedPlugin.containedClasses.size() > 0
                    && !allClasses.removeAll(usedPlugin.containedClasses)) {
                    if (isFirst && !(plugin instanceof Fragment) &&
                        (usedPlugin.descr.getPrerequisites() == null
                            || usedPlugin.descr.getPrerequisites().length == 0)) {
                        buffer.append(" * references plugin " + idOfUsedPlugin + ", which is not actually required\n");
                        error |= true;
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

        private boolean isMissingImportDeclarations(StringBuffer buffer, Set allClasses) {
            if (!allClasses.isEmpty()) {
                buffer.append(" * is missing import declarations covering the following classes: " + allClasses + "\n");
                return true;
            } else {
                return false;
            }
        }

        private Set getAllUsedClasses() {
            Set allClasses = new HashSet();
            Iterator iter = usedClasses.values().iterator();
            while (iter.hasNext()) {
                allClasses.addAll((Set) iter.next());
            }
            return allClasses;
        }

        private void initContainedClasses(JarFile jarFile) {
            Enumeration entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String entryName = entry.getName();
                if (!entry.isDirectory() && entryName.endsWith(classSuffix)) {
                    String name = entryName.substring(0, entryName.length()
                        - classSuffix.length());
                    if (name.indexOf(".") != -1)
                        System.err.println(name);
                    name = name.replace('/', '.');
                    name = name.replace('\\', '.');
                    containedClasses.add(name);
                }
            }
        }

        private void initUsedClasses(JarFile jarFile) {
            SyntheticRepository repository = SyntheticRepository.getInstance(new ClassPath(jarFile.getName()));
            Iterator iter = containedClasses.iterator();
            while (iter.hasNext()) {
                try {
                    String className = (String) iter.next();
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
            Field[] fields = javaClass.getFields();
            for (int i = 0; i < fields.length; i++) {
                addUsedClasses(usingClass, decodeSignature(fields[i].getType().getSignature()));
            }
        }

        private void analyzeMethods(String usingClass, JavaClass javaClass) {
            Method[] methods = javaClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Type[] arguments = methods[i].getArgumentTypes();
                for (int j = 0; j < arguments.length; j++) {
                    String typeName = decodeTypeName(arguments[j].getSignature());
                    if (typeName != null)
                        addUsedClass(usingClass, typeName);
                }
                addUsedClasses(usingClass, decodeSignature(methods[i].getReturnType().getSignature()));
            }
        }

        private void analyzeConstantPool(String usingClass, JavaClass javaClass) {
            final ConstantPool constantPool = javaClass.getConstantPool();
            Constant[] constants = constantPool.getConstantPool();
            for (int i = 0; i < constants.length; i++) {
                if (constants[i] instanceof ConstantClass) {
                    String signature = ((ConstantClass) constants[i]).getBytes(constantPool);
                    if (signature.startsWith("["))
                        signature = decodeTypeName(signature);
                    if (signature != null)
                        addUsedClass(usingClass, signature);
                } else if (constants[i] instanceof ConstantNameAndType) {
                    Iterator iter =
                        decodeSignature(((ConstantNameAndType) constants[i]).getSignature(constantPool)).iterator();
                    while (iter.hasNext()) {
                        String typeName = (String) iter.next();
                        if (typeName != null)
                            addUsedClass(usingClass, typeName);
                    }
                }
            }
        }

        private void addUsedClasses(String usingClass, Collection classNames) {
            Iterator iter = classNames.iterator();
            while (iter.hasNext()) {
                addUsedClass(usingClass, (String) iter.next());
            }
        }

        private void addUsedClasses(String usingClass, String[] classNames) {
            for (int i = 0; i < classNames.length; i++) {
                addUsedClass(usingClass, classNames[i]);
            }
        }

        private void addUsedClass(String usingClass, String usedClass) {
            usedClass = usedClass.replace('/', '.');
            if (!containedClasses.contains(usedClass)) {
                if (usedClasses.containsKey(usingClass)) {
                    ((Set) usedClasses.get(usingClass)).add(usedClass);
                } else {
                    Set set = new HashSet();
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
        private List decodeSignature(String signature) {
            return getObjectTypeNames(signature);
        }

        private List getObjectTypeNames(String signature) {
            List objectTypeNames = new ArrayList();
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
    }

    private class Fragment extends Plugin {
        private String fullPluginIdOfOwningPlugin;

        private Fragment(JarFile jarFile, FragmentDescriptor descr) {
            super(jarFile, descr);
            this.fullPluginIdOfOwningPlugin = createFullPluginId(((FragmentDescriptor) descr).getPluginId(),
                ((FragmentDescriptor) descr).getPluginVersion());
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
        private final Map jarFiles = new HashMap();

        private JarFiles(List pluginSets) {
            for (Iterator i = pluginSets.iterator(); i.hasNext();) {
                addFileSet((FileSet) i.next());
            }
        }

        private void addFileSet(FileSet fileSet) {
            final DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
            final String[] files = ds.getIncludedFiles();
            String baseDir = ds.getBasedir().getAbsolutePath();
            //System.err.println("getAllJars() iterating ... basedir="+baseDir+" files.length="+files.length);
            for (int j = 0; j < files.length; j++) {
                jarFiles.put(files[j], baseDir + File.separatorChar + files[j]);
            }
        }

        private boolean hasPluginJar(String fullPluginId) {
            return jarFiles.containsKey(fullPluginId + ".jar");
        }

        private JarFile getPluginJar(String fullPluginId) throws IOException {
            return new JarFile((String) jarFiles.get(fullPluginId + ".jar"));
        }

        private int size() {
            return jarFiles.size();
        }
    }
}

