/*
 * $Id$
 */
package org.jnode.build;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import nanoxml.XMLElement;

import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public class PluginList {

	private final URL[] descrList;
	private final URL[] pluginList;

	public PluginList(File file, File defaultDir, String targetArch) throws PluginException, MalformedURLException {

		final ArrayList descrList = new ArrayList();
		final ArrayList pluginList = new ArrayList();
		final XMLElement root = new XMLElement(new Hashtable(), true, false);
		try {
			final FileReader r = new FileReader(file);
			try {
				root.parseFromReader(r);
			} finally {
				r.close();
			}
		} catch (IOException ex) {
			throw new PluginException(ex);
		}
		if (!root.getName().equals("plugin-list")) {
			throw new PluginException("plugin-list element expected");
		}

		for (Iterator i = root.getChildren().iterator(); i.hasNext();) {

			final XMLElement e = (XMLElement) i.next();
			if (e.getName().equals("plugin")) {
				final String id = e.getStringAttribute("id");

				final URL descrUrl;
				final URL pluginUrl;
				if (id != null) {
					File f = findPlugin(defaultDir, id);
					pluginUrl = f.toURL();
					descrUrl = new URL("jar:" + pluginUrl + "!/plugin.xml");
				} else {
					throw new PluginException("id attribute expected on " + e.getName());
				}
				descrList.add(descrUrl);
				pluginList.add(pluginUrl);
			}
		}
		this.descrList = (URL[]) descrList.toArray(new URL[descrList.size()]);
		this.pluginList = (URL[]) pluginList.toArray(new URL[pluginList.size()]);
	}

	private File findPlugin(File dir, final String id) {
		//System.out.println("Find " + id + " in " + dir);
		String[] names = dir.list(new FilenameFilter() {
			/**
			 * @param dir
			 * @param name
			 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
			 * @return boolean
			 */
			public boolean accept(File dir, String name) {
				return name.startsWith(id + "_") && name.endsWith(".jar");
			}
		});

		if (names.length == 0) {
			throw new IllegalArgumentException("Cannot find plugin " + id + " in " + dir);
		} else {
			Arrays.sort(names);
			return new File(dir, names[names.length - 1]);
		}
	}

	/**
	 * Gets the maximum last modification date of all URL's
	 * @return last modification date
	 * @throws IOException
	 */
	public long lastModified() throws IOException {
		long max = 0;
		for (int i = 0; i < descrList.length; i++) {
			final URLConnection conn2 = pluginList[i].openConnection();
			max = Math.max(max, conn2.getLastModified());
		}
		return max;
	}

	/**
	 * Gets all URL's to plugin descriptors
	 * @return URL[]
	 */
	public URL[] getDescriptorUrlList() {
		return descrList;
	}

	/**
	 * Gets all URL's to the plugin files (jar format)
	 * @return URL[]
	 */
	public URL[] getPluginList() {
		return pluginList;
	}
}
