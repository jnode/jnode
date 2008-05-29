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
import org.jnode.plugin.PluginDescriptor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginData {

    /**
     * The descriptor file of this plugin
     */
    private final File descriptorFile;

    /**
     * The complete id
     */
    private final String fullId;

    /**
     * The html documentation file of this plugin
     */
    private String htmlFile;

    /**
     * The parsed descriptor
     */
    private PluginDescriptor descriptor;

    /**
     * Initialize this instance.
     *
     * @param descriptorFile
     */
    public PluginData(File descriptorFile, String fullId) {
        this.descriptorFile = descriptorFile;
        this.fullId = fullId;
    }

    /**
     * @return Returns the descriptor.
     */
    public final PluginDescriptor getDescriptor() {
        return this.descriptor;
    }

    /**
     * @param descriptor The descriptor to set.
     */
    public final void setDescriptor(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * @return Returns the descriptorFile.
     */
    public final File getDescriptorFile() {
        return this.descriptorFile;
    }

    /**
     * @return Returns the htmlFile.
     */
    public final String getHtmlFile() {
        return this.htmlFile;
    }

    /**
     * @param htmlFile The htmlFile to set.
     */
    public final void setHtmlFile(String htmlFile) {
        this.htmlFile = htmlFile;
    }

    /**
     * @return Returns the fullId.
     */
    public final String getFullId() {
        return this.fullId;
    }
}
