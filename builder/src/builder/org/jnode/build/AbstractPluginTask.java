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
 
package org.jnode.build;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.ZipFileSet;
import org.jnode.plugin.Library;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class AbstractPluginTask extends Task {

	protected String targetArch;
	private final LinkedList aliases = new LinkedList();

	/**
	 * @return The target architecture
	 */
	protected String getTargetArch() {
		return targetArch;
	}

	/**
	 * @param string
	 */
	public void setTargetArch(String string) {
		targetArch = string;
	}

	protected void processLibrary(Jar jarTask, Library lib, HashMap fileSets, File srcDir) {

		final LibAlias libAlias = getAlias(lib.getName());
		final File f;
		if (libAlias == null) {
			f = new File(srcDir, lib.getName());
		} else {
			f = libAlias.getAlias();
		}

		ZipFileSet fs = (ZipFileSet) fileSets.get(f);
		if (fs == null) {
			fs = new ZipFileSet();
			if (f.isFile()) {
				fs.setSrc(f);
			} else {
				fs.setDir(f);
			}
			fileSets.put(f, fs);
			jarTask.addFileset(fs);
		}

		final String[] exports = lib.getExports();
		for (int i = 0; i < exports.length; i++) {
			final String export = exports[i];
			if (export.equals("*")) {
				fs.createInclude().setName("**/*");
			} else {
				fs.createInclude().setName(export.replace('.', '/') + ".*");
			}
		}
	}

	protected File pluginDir;

	/**
	 * @param file
	 */
	public void setPluginDir(File file) {
		pluginDir = file;
	}

	/**
	 * @return The plugin directory
	 */
	protected File getPluginDir() {
		return pluginDir;
	}

	public LibAlias createLibAlias() {
		LibAlias a = new LibAlias();
		aliases.add(a);
		return a;
	}

	public LibAlias getAlias(String name) {
		for (Iterator i = aliases.iterator(); i.hasNext();) {
			final LibAlias a = (LibAlias) i.next();
			if (name.equals(a.getName())) {
				return a;
			}
		}
		return null;
	}

	public static class LibAlias {
		private String name;
		private File alias;

		/**
		 * @return The alias
		 */
		public final File getAlias() {
			return this.alias;
		}

		/**
		 * @param alias
		 */
		public final void setAlias(File alias) {
			this.alias = alias;
		}

		/**
		 * @return The name
		 */
		public final String getName() {
			return this.name;
		}

		/**
		 * @param name
		 */
		public final void setName(String name) {
			this.name = name;
		}
	}
}
