/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.ant.taskdefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.condition.Os;

/**
 *  Description of the Class
 *
 * @author     epr
 * @version    $Revision$
 */
public class Asm extends MatchingTask {

	private File destdir;
	private File srcdir;
	private String outputFormat;
	private Set includeDirs = new HashSet();
	private String ext = "o";
	private File listFile;

	/**
	 * Description of the Method
	 *
	 * @throws BuildException  Description of Exception
	 */
	public void execute() throws BuildException {

		if (srcdir == null) {
			throw new BuildException("Error: srcdir attribute must be set!", getLocation());
		} else if (!srcdir.isDirectory()) {
			throw new BuildException("Error: srcdir directory is not valid!", getLocation());
		}

		if (destdir == null) {
			throw new BuildException("Error: destdir attribute must be set!", getLocation());
		} else if (!destdir.isDirectory()) {
			throw new BuildException("Error: destdir directory is not valid!", getLocation());
		}

		try {
			executeAsm();
		} catch (IOException ex) {
			throw new BuildException(ex);
		}
	}

	/**
	 *  Sets the Destdir attribute of the JspC object
	 *
	 * @param  destDir  The new Destdir value
	 */
	public void setDestdir(File destDir) {
		this.destdir = destDir;
	}

	/**
	 *  Description of the Method
	 *
	 * @throws  BuildException  Description of Exception
	 * @throws  IOException     Description of the Exception
	 */
	protected void executeAsm() throws BuildException, IOException {

		DirectoryScanner scanner = getDirectoryScanner(getSrcdir());
		String allFiles[] = scanner.getIncludedFiles();
		Map compileMap = new HashMap();
		scanDir(srcdir, destdir, allFiles, compileMap);

		if (compileMap.size() > 0) {
			log("Compiling " + compileMap.size() + " source files to " + destdir);

			for (Iterator i = compileMap.keySet().iterator(); i.hasNext();) {
				File srcFile = (File) i.next();
				File dstFile = (File) compileMap.get(srcFile);
				doNasm(srcFile, dstFile);
			}
		}
	}

	/**
	 *  Scans the directory looking for source files to be compiled. The results
	 *  are returned in the class variable compileList
	 *
	 * @param  srcDir           Description of Parameter
	 * @param  destDir          Description of Parameter
	 * @param  files            Description of Parameter
	 * @param compileMap
	 * @throws  IOException  Description of the Exception
	 */
	protected void scanDir(File srcDir, File destDir, String[] files, Map compileMap) throws IOException {
		long now = System.currentTimeMillis();

		for (int c = 0; c < files.length; c++) {
			File aFile = new File(srcdir, files[c]);

			// get the path within the uriroot
			String fileDirString = aFile.getParentFile().toString();
			String rootDirString = srcdir.toString();

			int diff = fileDirString.compareTo(rootDirString);
			String destSubDir = fileDirString.substring(fileDirString.length() - diff, fileDirString.length()).replace('\\', '/');

			//				  log(destSubDir);
			File fileDir = new File(destDir.toString() + destSubDir);

			String objName = aFile.getName().substring(0, aFile.getName().indexOf(".asm")) + "." + ext;
			File objFile = new File(fileDir, objName);

			// check if the file has been modified in the future, error?
			if (aFile.lastModified() > now) {
				log("Warning: file modified in the future: " + aFile.getName(), Project.MSG_WARN);
			}

			if (!objFile.exists() || aFile.lastModified() > objFile.lastModified()) {
				if (!objFile.exists()) {
					log("Compiling " + aFile.getPath() + " because class file " + objFile.getPath() + " does not exist", Project.MSG_VERBOSE);
				} else {
					log("Compiling " + aFile.getPath() + " because it is out of date with respect to " + objFile.getPath(), Project.MSG_VERBOSE);
				}

				compileMap.put(aFile, objFile);
				log(aFile.toString(), Project.MSG_VERBOSE);
			}
		}
	}

	/**
	 *  Description of the Method
	 *
	 * @param srcFile
	 * @param dstFile
	 * @throws  BuildException  Description of Exception
	 * @throws  IOException     Description of the Exception
	 */
	private void doNasm(File srcFile, File dstFile) throws BuildException, IOException {

		Execute exec = new Execute();
		ArrayList cmdLine = new ArrayList();
		if (Os.isFamily("windows")) {
			cmdLine.add("nasmw.exe");
		} else {
			cmdLine.add("nasm");
		}

		cmdLine.add("-o");
		cmdLine.add(dstFile.toString());

		if (outputFormat != null) {
			cmdLine.add("-f");
			cmdLine.add(outputFormat);
		}

		if (listFile != null) {
			cmdLine.add("-l");
			cmdLine.add(listFile.toString());
		}

		for (Iterator i = includeDirs.iterator(); i.hasNext();) {
			IncludeDir dir = (IncludeDir) i.next();
			cmdLine.add("-I" + postFixSlash(dir.getDir().toString()));
		}

		cmdLine.add(srcFile.toString());

		log("cmdLine=" + cmdLine, Project.MSG_VERBOSE);

		exec.setCommandline((String[]) cmdLine.toArray(new String[cmdLine.size()]));

		dstFile.getParentFile().mkdirs();
		int rc = exec.execute();

		if (rc != 0) {
			throw new BuildException("Asm failed on " + srcFile.getAbsolutePath());
		}
	}
	/**
	 * Returns the destdir.
	 * @return File
	 */
	public File getDestdir() {
		return destdir;
	}

	/**
	 * Returns the srcdir.
	 * @return File
	 */
	public File getSrcdir() {
		return srcdir;
	}

	/**
	 * Sets the srcdir.
	 * @param srcdir The srcdir to set
	 */
	public void setSrcdir(File srcdir) {
		this.srcdir = srcdir;
	}

	/**
	 * Add an includedir
	 * @param dir
	 */
	public void addConfiguredIncludeDir(IncludeDir dir) {
		includeDirs.add(dir);
	}

	public static class IncludeDir {
		private File dir;
		/**
		 * Constructor for IncludeDir.
		 */
		public IncludeDir() {
			super();
		}

		/**
		 * Returns the dir.
		 * @return File
		 */
		public File getDir() {
			return dir;
		}

		/**
		 * Sets the dir.
		 * @param dir The dir to set
		 */
		public void setDir(File dir) {
			this.dir = dir;
		}
	}
	/**
	 * Returns the outputFormat.
	 * @return String
	 */
	public String getOutputFormat() {
		return outputFormat;
	}

	/**
	 * Sets the outputFormat.
	 * @param outputFormat The outputFormat to set
	 */
	public void setOutputFormat(String outputFormat) {
		this.outputFormat = outputFormat;
	}

	/**
	 * Returns the ext.
	 * @return String
	 */
	public String getExtension() {
		return ext;
	}

	/**
	 * Sets the ext.
	 * @param ext The ext to set
	 */
	public void setExtension(String ext) {
		this.ext = ext;
	}

	/**
	 * Returns the listFile.
	 * @return File
	 */
	public File getListFile() {
		return listFile;
	}

	/**
	 * Sets the listFile.
	 * @param listFile The listFile to set
	 */
	public void setListFile(File listFile) {
		this.listFile = listFile;
	}

	private static String postFixSlash(String s) {
		if (!s.endsWith(File.separator)) {
			return s + File.separator;
		} else {
			return s;
		}
	}
}
