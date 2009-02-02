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
 
package org.jnode.awt.font.truetype.tables;

import java.io.IOException;
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * VERSION Table.
 *
 * @author Simon Fischer
 * @version $Id$
 */
abstract class VersionTable extends TTFTable {

    private int minorVersion;
    private int majorVersion;

    /**
     * @param font
     * @param input
     */
    protected VersionTable(TTFFontData font, TTFInput input) {
        super(font, input);
    }

    protected void readVersion(TTFInput ttf) throws IOException {
        majorVersion = ttf.readUShort();
        minorVersion = ttf.readUShort();
    }

    public String toString() {
        return super.toString() + " v" + majorVersion + "." + minorVersion;
    }

    /**
     * @return The major version
     */
    public int getMajorVersion() {
        return this.majorVersion;
    }

    /**
     * @return The minor version
     */
    public int getMinorVersion() {
        return this.minorVersion;
    }

}
