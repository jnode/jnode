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
import java.io.IOException;
import java.net.URL;
import java.util.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.GZip;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.types.FileSet;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.model.PluginJar;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InitJarBuilder extends AbstractPluginsTask {

    private File destDir;

    private File destFile;

    public void execute() throws BuildException {

        final long start = System.currentTimeMillis();

        final PluginList piList;
        final PluginList systemPluginList;
        final long lmPI;
        try {
            piList = getPluginList();
            systemPluginList = getSystemPluginList();
            if ((destFile == null) && (destDir != null)) {
                destFile = new File(destDir, piList.getName() + ".jgz");
            }
            lmPI = Math.max(piList.lastModified(), systemPluginList.lastModified());
        } catch (PluginException ex) {
            throw new BuildException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        }

        final long lmDest = destFile.lastModified();
        final long lmPIL = getPluginListFile().lastModified();

        if ((lmPIL < lmDest) && (lmPI < lmDest)) {
            // No need to do anything, skip
            return;
        }
        destFile.delete();
        final File tmpFile = new File(destFile + ".tmp");
        tmpFile.delete();

        try {
            // Load the plugin descriptors
            /*
             * final PluginRegistry piRegistry; piRegistry = new
             * PluginRegistryModel(piList.getDescriptorUrlList());
             */

            final Jar jarTask = new Jar();
            jarTask.setProject(getProject());
            jarTask.setTaskName(getTaskName());
            jarTask.setDestFile(tmpFile);
            jarTask.setCompress(false);

            final Manifest mf = piList.getManifest();
            if (mf != null) {
                jarTask.addConfiguredManifest(mf);
            }

            final URL[] systemPlugins = systemPluginList.getPluginList();
            final ArrayList<PluginJar> pluginJars = new ArrayList<PluginJar>();
            for (URL url : systemPlugins) {
                final BuildPluginJar piJar = new BuildPluginJar(url);
                if (!piJar.getDescriptor().isSystemPlugin()) {
                    log("Non-system plugin " + piJar.getDescriptor().getId()
                            + " in plugin-list will be ignored",
                            Project.MSG_WARN);
                } else {
                    pluginJars.add(piJar);
                }
            }

            final URL[] pluginList = piList.getPluginList();
            for (URL url : pluginList) {
                final BuildPluginJar piJar = new BuildPluginJar(url);
                if (piJar.getDescriptor().isSystemPlugin()) {
                    log("System plugin " + piJar.getDescriptor().getId()
                            + " in plugin-list will be ignored",
                            Project.MSG_WARN);
                } else {
                    pluginJars.add(piJar);
                }
            }
            testPluginPrerequisites(pluginJars);
            final List<PluginJar> sortedPluginJars = sortPlugins(pluginJars);

            for (Iterator<PluginJar> i = sortedPluginJars.iterator(); i
                    .hasNext();) {
                final BuildPluginJar piJar = (BuildPluginJar) i.next();
                if (!piJar.getDescriptor().isSystemPlugin()) {
//                    pluginJars.add(piJar);
                    final File f = new File(piJar.getPluginUrl().getPath());
                    final FileSet fs = new FileSet();
                    fs.setDir(f.getParentFile());
                    fs.setIncludes(f.getName());
                    jarTask.addFileset(fs);
                }
            }

            /*
             * for (Iterator i = piRegistry.getDescriptorIterator();
             * i.hasNext(); ) { final PluginDescriptor descr =
             * (PluginDescriptor)i.next(); final Runtime rt =
             * descr.getRuntime(); if (rt != null) { final Library[] libs =
             * rt.getLibraries(); for (int l = 0; l < libs.length; l++) {
             * processLibrary(jarTask, libs[l], fileSets, getPluginDir()); } }
             */

            // Now create the jar file
            jarTask.execute();

            // Now zip it
            final GZip gzipTask = new GZip();
            gzipTask.setProject(getProject());
            gzipTask.setTaskName(getTaskName());
            gzipTask.setSrc(tmpFile);
            gzipTask.setZipfile(destFile);
            gzipTask.execute();
            tmpFile.delete();

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BuildException(ex);
        }
        final long end = System.currentTimeMillis();
        log("Building initjar took " + (end - start) + "ms");
    }

    /**
     * @param file
     */
    public void setDestFile(File file) {
        destFile = file;
    }

    /**
     * Ensure that all plugin prerequisites are met.
     * 
     * @throws BuildException
     */
    protected void testPluginPrerequisites(List<PluginJar> pluginJars)
            throws BuildException {
        final HashSet<String> ids = new HashSet<String>();

        for (PluginJar piJar : pluginJars) {
            final PluginDescriptor descr = piJar.getDescriptor();
            ids.add(descr.getId());
        }
        for (PluginJar piJar : pluginJars) {
            final PluginDescriptor descr = piJar.getDescriptor();
            final PluginPrerequisite[] prereqs = descr.getPrerequisites();
            for (int j = 0; j < prereqs.length; j++) {
                if (!ids.contains(prereqs[j].getPluginId())) {
                    throw new BuildException("Cannot find plugin "
                            + prereqs[j].getPluginId()
                            + ", which is required by " + descr.getId());
                }
            }
        }
    }

    /**
     * Sort the plugins based on dependencies.
     * 
     * @param pluginJars
     */
    protected List<PluginJar> sortPlugins(List<PluginJar> pluginJars) {
        final ArrayList<PluginJar> result = new ArrayList<PluginJar>(pluginJars.size());
        final HashSet<String> ids = new HashSet<String>();
        while (!pluginJars.isEmpty()) {
        	boolean somethingRemoved = false;
            
            for (Iterator<PluginJar> i = pluginJars.iterator(); i.hasNext();) {
                final BuildPluginJar piJar = (BuildPluginJar) i.next();
                if (piJar.hasAllPrerequisitesInSet(ids)) {
                    log(piJar.getDescriptor().getId(), Project.MSG_VERBOSE);
                    result.add(piJar);
                    ids.add(piJar.getDescriptor().getId());
                    i.remove();
                    somethingRemoved = true;
                }
            }
            
            if(!somethingRemoved)
            {
            	StringBuilder sb = new StringBuilder("cycle in plugin dependencies :\n");
                for (Iterator<PluginJar> i = pluginJars.iterator(); i.hasNext();) 
                {
                    final BuildPluginJar piJar = (BuildPluginJar) i.next();
                    sb.append(piJar.getDescriptor().getId()).append('\n');
                }
                throw new BuildException(sb.toString());
            }
        }
        return result;
    }

    static class BuildPluginJar extends PluginJar {

        private final URL pluginUrl;

        /**
         * @param pluginUrl
         * @throws PluginException
         * @throws IOException
         */
        BuildPluginJar(URL pluginUrl) throws PluginException, IOException {
            super(null, pluginUrl);
            this.pluginUrl = pluginUrl;
        }

        /**
         * @return Returns the pluginUrl.
         */
        final URL getPluginUrl() {
            return this.pluginUrl;
        }

        public boolean hasAllPrerequisitesInSet(Set ids) {
            final PluginDescriptor descr = getDescriptor();
            final PluginPrerequisite[] prereqs = descr.getPrerequisites();
            for (int j = 0; j < prereqs.length; j++) {
                if (!ids.contains(prereqs[j].getPluginId())) {
                    return false;
                }
            }
            return true;
        }
    }
    

    /**
     * @return Returns the destDir.
     */
    public final File getDestDir() {
        return destDir;
    }
    

    /**
     * @param destDir
     *            The destDir to set.
     */
    public final void setDestDir(File destDir) {
        this.destDir = destDir;
    }
}
