/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

package org.jnode.build.packager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.AllPermission;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.jnode.build.AbstractPluginTask.LibAlias;
import org.jnode.build.BuildException;
import org.jnode.build.PluginTask;
import org.jnode.plugin.AutoUnzipPlugin;
import org.jnode.plugin.model.PluginDescriptorBuilder;

/**
 * Class building new jnode plugins from third party jars/resources.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public class PluginBuilder extends PackagerTask {
    private final Task parent;

    /**
     * List of user plugin ids.
     */
    private StringBuilder userPluginIds = new StringBuilder();

    /**
     * {@link Path} to third party jars for compilation purpose.
     */
    private Path path;

    /**
     * Construct a PluginBuilder from the given {@link Task},
     * which will be used as a delegate to access ant context.
     *
     * @param parent
     */
    public PluginBuilder(Task parent) {
        this.parent = parent;
    }

    /**
     * Define the path reference for compilation.
     *
     * @param pathRefId
     */
    public void setPathRefId(String pathRefId) {
        this.path = (Path) parent.getProject().getReference(pathRefId);
    }

    /**
     * Main method for build the jnode plugin.
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
            Properties properties = getProperties();
            properties.put(USER_PLUGIN_IDS, userPluginIds.toString());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(getPropertiesFile());
                properties.store(fos, "File automatically generated by JNode Packager");
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
     * Attention : userPluginList must be a StringBuilder because it's accessed from multiple threads.
     *
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
                final String pluginDesc = pluginId + ".xml";

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

                if (userJar.isDirectory()) {
                    ScriptBuilder.build(userJar, getProperties());
                }

                task.buildPlugin(descriptors, descriptorFile);
            }
        });
    }

    /**
     * Build the plugin descriptor.
     *
     * @param userJar
     * @param descriptorFile
     * @param pluginId
     * @param alias
     */
    private void buildDescriptor(File userJar, File descriptorFile, String pluginId, String alias) {
        Writer out = null;
        boolean success = false;
        try {
            PluginDescriptorBuilder builder = new PluginDescriptorBuilder(pluginId, pluginId, "unspecified", "1.0");
            builder.setPluginClass(AutoUnzipPlugin.class);
            builder.setAutoStart(true);
            builder.addRuntimeLibrary(alias, "*");

            if (userJar.isFile()) {
                List<String> mainClasses = MainFinder.searchMain(userJar);
                if (!mainClasses.isEmpty()) {
                    PluginDescriptorBuilder.ExtensionBuilder
                        extension = builder.addExtension("org.jnode.shell.aliases", "alias");
                    for (String mainClass : mainClasses) {
                        int idx = mainClass.lastIndexOf('.');
                        String name = (idx < 0) ? mainClass : mainClass.substring(idx + 1);
                        extension.newElement().addAttribute("name", name).addAttribute("class", mainClass);
                        log(pluginId + " : added alias " + name + " for class " + mainClass, Project.MSG_INFO);
                    }
                } else {
                    log("no main found for plugin " + pluginId, Project.MSG_WARN);
                }
            }

            // FIXME : use more restricted permissions
            PluginDescriptorBuilder.ExtensionBuilder
                extension = builder.addExtension("org.jnode.security.permissions", "permission");
            extension.newElement().addAttribute("class", AllPermission.class);

            out = new FileWriter(descriptorFile);
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            out.write("<!DOCTYPE plugin SYSTEM \"jnode.dtd\">\n");
            builder.buildXmlElement().write(out);

            success = true;
        } catch (Exception ioe) {
            throw new BuildException("failed to write plugin descriptor", ioe);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new BuildException("failed to close writer", e);
                }
            }

            if (!success) {
                // in case of failure, delete the incomplete descriptor file
                descriptorFile.delete();
            }
        }
    }

}
