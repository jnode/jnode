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

package org.jnode.awt.font.truetype.tables;

import java.io.IOException;
import org.jnode.awt.font.truetype.TTFFontData;
import org.jnode.awt.font.truetype.TTFInput;

/**
 * Concrete instances derived from this class hold data stored in true type tables.
 * Right now the data is accessible as public attributes.
 * In some cases methods may return more convenient objects
 * (such as Shapes instead of point arrays).
 *
 * @author Simon Fischer
 * @version $Id$
 */
public abstract class TTFTable {

    private final TTFFontData font;
    private final TTFInput input;
    private boolean isRead = false;

    /**
     * Initialize this instance.
     *
     * @param font
     * @param input
     */
    protected TTFTable(TTFFontData font, TTFInput input) {
        this.font = font;
        this.input = input;
    }

    /**
     * Read the table.
     *
     * @throws IOException
     */
    public final void read() throws IOException {
        if (!isRead) {
            input.pushPos();
            input.seek(0);
            readTable(input);
            isRead = true;
            input.popPos();
        }
    }

    /**
     * Gets the input handler for this table.
     *
     * @return
     */
    protected final TTFInput getInput() {
        return input;
    }

    /**
     * Read the table from the given input.
     *
     * @throws IOException
     */
    protected abstract void readTable(TTFInput input) throws IOException;

    /**
     * Gets the tag of this table.
     *
     * @return
     */
    public abstract String getTag();

    public String toString() {
        return "[" + getTag() + "/" + getClass().getName() + "]";
    }

    /**
     * @return The font data this table belongs to
     */
    public final TTFFontData getFont() {
        return this.font;
    }
}
