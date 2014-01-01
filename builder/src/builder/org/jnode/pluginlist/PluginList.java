/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.pluginlist;

import java.util.Properties;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.io.Reader;
import java.io.Writer;
import java.io.PrintWriter;
import org.jnode.nanoxml.XMLElement;

/**
 *
 */
class PluginList {
    private String name;
    private Set<String> plugins = new HashSet<String>();
    private Properties attributes = new Properties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getPlugins() {
        return plugins;
    }

    public void setPlugins(Set<String> plugins) {
        this.plugins = plugins;
    }

    void read(Reader in) throws Exception {

        final XMLElement root = new XMLElement(new Hashtable<Object, Object>(), true, false);
        root.parseFromReader(in);
        String rname = root.getName();
        if (rname.equals("plugin-list")) {
            name = (String) root.getAttribute("name");
            if (name == null) {
                throw new RuntimeException("Invalid plugin list");
            }

            for (XMLElement obj : root.getChildren()) {
                String name = obj.getName();
                if (name.equals("manifest")) {
                    for (XMLElement ch : obj.getChildren()) {
                        String xname = ch.getName();
                        if (xname.equals("attribute")) {
                            String key = (String) ch.getAttribute("key");
                            String value = (String) ch.getAttribute("value");
                            if (key == null || value == null)
                                throw new RuntimeException("Invalid attribute in plugin list");
                            attributes.setProperty(key, value);
                        }
                    }
                } else if (name.equals("plugin")) {
                    String plugin = (String) obj.getAttribute("id");
                    if (plugin == null)
                        throw new RuntimeException("Invalid plugin in plugin list");

                    plugins.add(plugin);
                }
            }
        }
    }

    void write(Writer w) throws Exception {
        PrintWriter pw = new PrintWriter(w);
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println();
        pw.println("<plugin-list name=\"" + name + "\">");
        pw.print("  ");
        pw.println("<manifest>");
        for (Object key : attributes.keySet()) {
            pw.print("  ");
            pw.print("  ");
            pw.println("<attribute key=\"" + key + "\" value=\"" + attributes.get(key) + "\"/>");
        }
        pw.print("  ");
        pw.println("</manifest>");
        pw.print("  ");
        pw.println();
        for (String p : plugins) {
            pw.print("  ");
            pw.println("<plugin id=\"" + p + "\"/>");
        }
        pw.println("</plugin-list>");
        pw.flush();
    }
}
