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
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import nanoxml.XMLElement;
import nanoxml.XMLParseException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.types.ZipFileSet;
import org.jnode.plugin.Library;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.model.Factory;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class AbstractPluginTask extends Task {

	protected String targetArch;
	private final LinkedList<LibAlias> aliases = new LinkedList<LibAlias>();

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

    protected PluginDescriptor readDescriptor(File descriptor)
    {
        final PluginDescriptor descr;
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
            descr = Factory.parseDescriptor(root);
        } catch (PluginException ex) {
            ex.printStackTrace();
            throw new BuildException("Building " + descriptor + " failed", ex);
        }        
        
        return descr;
    }
    
	protected void processLibrary(Jar jarTask, Library lib, HashMap<File, ZipFileSet> fileSets, File srcDir) {

		final LibAlias libAlias = getAlias(lib.getName());
		final File f;
		if (libAlias == null) {
			f = new File(srcDir, lib.getName());
            if(!f.exists())
            {
                throw new BuildException("file not found "+f.getAbsoluteFile()+" because "+lib.getName()+" has no alias");
            }
		} else {
			f = libAlias.getAlias();
		}

		ZipFileSet fs = fileSets.get(f);
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
        fs.createExclude().setName("**/package.html");

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
		for (LibAlias a : aliases) {
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
