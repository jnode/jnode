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
 
package org.jnode.awt.font.truetype;

import java.awt.font.OpenType;
import java.io.IOException;

/**
 * Conrete implementation of a TrueType font, attached to a JavaFont which
 * implements the OpenType interface.
 * <p/>
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
