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

package org.jnode.build.documentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.jnode.plugin.PluginPrerequisite;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class DotBuilder {

    private final File pngFile;
    private final File dotFile;
    private final PrintWriter dot;

    public DotBuilder(File dotFile, File pngFile) throws IOException {
        this.pngFile = pngFile;
        this.dotFile = dotFile;
        dot = new PrintWriter(new FileWriter(dotFile));
        dot.println("digraph G {");
        dot.println("  rankdir=RL;");
    }

    public void add(PluginData data) {
        final String id = data.getDescriptor().getId();
        // Set attributes
        dot.print("  " + fixId(id) + " [label=\"" + id + "\"");
        if (data.getDescriptor().isFragment()) {
            dot.print(" shape=box");
        }
        dot.println("]");

        // Set dependencies
        PluginPrerequisite[] prereqs = data.getDescriptor().getPrerequisites();
        if (prereqs != null) {
            for (PluginPrerequisite pr : prereqs) {
                dot.println("  " + fixId(id) + " -> " + fixId(pr.getPluginId()) + ";");
            }
        }
    }

    private final String fixId(String id) {
        return id.replace('.', '_').replace('-', '_').replace(' ', '_');
    }

    public void close() throws IOException {
        dot.println("}");
        dot.close();

        // Run dot
        String[] cmd = {"dot", "-Tpng", "-o", pngFile.getAbsolutePath(), dotFile.getAbsolutePath()};
        try {
//            System.out.println("Running dot");
            Runtime.getRuntime().exec(cmd).waitFor();
        } catch (InterruptedException ex) {
            throw (IOException) new IOException("dot interrupted").initCause(ex);
        }
    }
}
