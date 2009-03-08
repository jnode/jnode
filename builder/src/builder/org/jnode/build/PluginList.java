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
import java.util.List;

import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.ManifestException;
import org.jnode.nanoxml.XMLElement;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public final class PluginList {

    private final List<URL> descrList;

    private final List<URL> pluginList;

    private final String name;
    
    private Manifest manifest;

    private List<PluginList> includes = new ArrayList<PluginList>();

    private final File defaultDir;

    public PluginList(File file, File defaultDir, String targetArch)
        throws PluginException, MalformedURLException {
        this.defaultDir = defaultDir;
        descrList = new ArrayList<URL>();
        pluginList = new ArrayList<URL>();
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

        this.name = (String) root.getAttribute("name");
        if (name == null) {
            throw new PluginException("name attribute is missing in " + file);
        }

        for (Iterator<?> i = root.getChildren().iterator(); i.hasNext();) {

            final XMLElement e = (XMLElement) i.next();
            if (e.getName().equals("plugin")) {
                final String id = e.getStringAttribute("id");

                if (id == null) {
                    throw new PluginException("id attribute expected on "
                        + e.getName());
                }
                
                // version attribute is optional
                // if not specified, then the latest version found will be used.
                final String version = e.getStringAttribute("version");

                addPlugin(descrList, pluginList, id, version);
            } else if (e.getName().equals("manifest")) {
                manifest = parseManifest(e);
            } else if (e.getName().equals("include")) {
                parseInclude(e, file.getParentFile(), defaultDir, targetArch,
                    descrList, pluginList);
            } else {
                throw new PluginException("Unknown element " + e.getName());
            }
        }
    }
    
    /**
     * 
     * @param id
     * @param version optional
     * @throws MalformedURLException
     * @throws PluginException
     */
    public void addPlugin(String id, String version) throws MalformedURLException, PluginException {
        addPlugin(descrList, pluginList, id, version);
    }
    
    /**
     * 
     * @param descrList
     * @param pluginList
     * @param id
     * @param version optional parameter
     * @throws MalformedURLException
     * @throws PluginException
     */
    private void addPlugin(List<URL> descrList, List<URL> pluginList, String id, String version)
        throws MalformedURLException, PluginException {
        final File f = findPlugin(defaultDir, id, version);
        final URL pluginUrl = f.toURL();
        final URL descrUrl = new URL("jar:" + pluginUrl + "!/plugin.xml");
        
        if (pluginList.contains(pluginUrl)) {
            String versionStr = (version == null) ? "unspecified" : version;
            throw new PluginException("can't use the same id(" + id + 
                    ") and version(" + versionStr + ") for multiple plugins");
        }

        descrList.add(descrUrl);
        pluginList.add(pluginUrl);
    }

    private File findPlugin(File dir, final String id, String version) {
        // System.out.println("Find " + id + " in " + dir);
        final String begin = id + "_";
        final String end = ".jar";
        
        String[] names = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(begin) && name.endsWith(end);
            }
        });

        if (names.length == 0) {
            throw new IllegalArgumentException("Cannot find plugin " + id
                + " in " + dir + " for list " + this.name);
        }

        String filename = null;
        if (version != null) {
            // version specified, try to find it
            for (String name : names) {
                String v = name.substring(begin.length(), name.length() - end.length());
                if (version.equals(v)) {
                    filename = name; // found exact version
                    break;
                }
            }            
        }
        
        if (filename == null) {
            // by default, take the latest version
            filename = names[names.length - 1];
        }
        
        return new File(dir, filename);
    }

    private Manifest parseManifest(XMLElement me) throws PluginException {
        Manifest mf = this.manifest;
        if (mf == null) {
            mf = new Manifest();
        }
        for (Iterator<?> i = me.getChildren().iterator(); i.hasNext();) {
            final XMLElement e = (XMLElement) i.next();
            if (e.getName().equals("attribute")) {
                final String k = e.getStringAttribute("key");
                final String v = e.getStringAttribute("value");
                try {
                    mf.addConfiguredAttribute(new Manifest.Attribute(k, v));
                } catch (ManifestException ex) {
                    throw new PluginException("Error in manifest", ex);
                }
            } else {
                throw new PluginException("Unknown element " + e.getName());
            }
        }
        return mf;
    }

    private void parseInclude(XMLElement e, File curDir, File defaultDir,
                              String targetArch, List<URL> descrList, List<URL> pluginList)
        throws MalformedURLException, PluginException {
        File file = new File(e.getStringAttribute("file"));
        if (!file.isAbsolute()) {
            file = new File(curDir, file.getPath());
        }

        final PluginList inc = new PluginList(file, defaultDir, targetArch);
        includes.add(inc);

        if (this.manifest == null) {
            this.manifest = inc.manifest;
        } else if (inc.manifest != null) {
            try {
                final Manifest merged = new Manifest();
                merged.merge(inc.manifest);
                merged.merge(this.manifest);
                this.manifest = merged;
            } catch (ManifestException ex) {
                throw new PluginException(ex);
            }
        }

        descrList.addAll(inc.descrList);
        pluginList.addAll(inc.pluginList);
    }

    /**
     * Gets the maximum last modification date of all URL's.
     *
     * @return last modification date
     * @throws IOException
     */
    public long lastModified() throws IOException {
        long max = 0;
        for (URL url : descrList) {
            final URLConnection conn2 = url.openConnection();
            max = Math.max(max, conn2.getLastModified());
        }
        for (PluginList inc : includes) {
            max = Math.max(max, inc.lastModified());
        }
        return max;
    }

    /**
     * Gets all URL's to plugin descriptors.
     *
     * @return URL[]
     */
    public URL[] getDescriptorUrlList() {
        return descrList.toArray(new URL[descrList.size()]);
    }

    /**
     * Gets all URL's to the plugin files (jar format).
     *
     * @return URL[]
     */
    public URL[] getPluginList() {
        return pluginList.toArray(new URL[pluginList.size()]);
    }

    /**
     * @return Returns the manifest.
     */
    public final Manifest getManifest() {
        return this.manifest;
    }

    /**
     * @return Returns the name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Create an URL to a plugin with a given id and version.
     */
    public final URL createPluginURL(String id, String version) {
        try {
            return findPlugin(defaultDir, id, version).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
