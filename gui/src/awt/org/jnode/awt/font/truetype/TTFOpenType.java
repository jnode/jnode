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

package org.jnode.awt.font.truetype;

import java.awt.font.OpenType;
import java.io.IOException;

/**
 * Conrete implementation of a TrueType font, attached to a JavaFont which
 * implements the OpenType interface.
 * 
 * FIXME: Test as soon as some Java Fonts implements OpenType. Probably
 * TTFMemoryInput won't work. Tag names may be different for OpenType and
 * TrueType.
 * 
 * @author Simon Fischer
 * @version $Id$
 */
public class TTFOpenType extends TTFFontData {

    private OpenType openType;

    public TTFOpenType(OpenType openType) throws IOException {
        this.openType = openType;
        for (TableClass tc : TableClass.values()) {
            byte[] data = openType.getFontTable(tc.getTag());
            if (data != null) {
                newTable(tc, new TTFMemoryInput(data));
            } else {
                System.err.println("No table found for '" + tc.getTag() + "'.");
            }

        }
    }

    public int getFontVersion() {
        return openType.getVersion();
    }
}
