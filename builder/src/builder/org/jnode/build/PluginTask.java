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
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jnode.nanoxml.XMLElement;
import org.jnode.nanoxml.XMLParseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
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

        ThreadPoolExecutor executor = new ThreadPoolExecutor(max_thread_count, max_thread_count, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(max_plugin_count));
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
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            throw new RuntimeException("Building plugins interrupted");
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

}
