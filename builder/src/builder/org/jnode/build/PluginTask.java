/*
 * $Id$
 */
package org.jnode.build;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.Runtime;
import org.jnode.plugin.model.PluginDescriptorModel;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginTask extends AbstractPluginTask {

	private LinkedList descriptorSets = new LinkedList();
	private File todir;
	private File tmpDir = new File(System.getProperty("java.io.tmpdir"));

	public ZipFileSet createDescriptors() {
		final ZipFileSet fs = new ZipFileSet();
		descriptorSets.add(fs);
		return fs;
	}

	/**
	 * @see org.apache.tools.ant.Task#execute()
	 * @throws BuildException
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

		for (Iterator i = descriptorSets.iterator(); i.hasNext();) {
			final FileSet fs = (FileSet) i.next();
			final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			final String[] files = ds.getIncludedFiles();
			for (int j = 0; j < files.length; j++) {
				buildPlugin(new File(ds.getBasedir(), files[j]));
			}
		}

	}

	protected void buildPlugin(File descriptor) throws BuildException {
		final PluginDescriptorModel descr;
		try {
			final XMLElement root = new XMLElement(new Hashtable(), true, false);
			try {
				final FileReader r = new FileReader(descriptor);
				try {
					root.parseFromReader(r);
				} finally {
					r.close();
				}
			} catch (IOException ex) {
				throw new BuildException("Building " + descriptor + " failed", ex);
			} catch (XMLParseException ex) {
				throw new BuildException("Building " + descriptor + " failed", ex);
			}
			descr = new PluginDescriptorModel(root);
		} catch (PluginException ex) {
			ex.printStackTrace();
			throw new BuildException("Building " + descriptor + " failed", ex);
		}

		final String fullId = descr.getId() + "_" + descr.getVersion();

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

		// Add runtime resources
		final Runtime rt = descr.getRuntime();
		if (rt != null) {
			final HashMap fileSets = new HashMap();
			final Library[] libs = rt.getLibraries();
			for (int l = 0; l < libs.length; l++) {
				processLibrary(jarTask, libs[l], fileSets, getPluginDir());
			}
		}

		jarTask.execute();
	}

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
