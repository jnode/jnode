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
 
package org.jnode.awt.font.truetype.glyph;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import org.jnode.awt.font.spi.Glyph;
import org.jnode.awt.font.spi.ShapedGlyph;
import org.jnode.awt.font.truetype.TTFInput;

public abstract class TTFGlyph extends Glyph implements ShapedGlyph {
    /**
     * Initialize this instance;
     *
     * @param in
     * @throws IOException
     */
    protected TTFGlyph(TTFInput in) throws IOException {
        super(in.readFWord(), in.readFWord(), in.readFWord(), in.readFWord());
    }

    public abstract String getType();

    public abstract GeneralPath getShape();
}
