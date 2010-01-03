/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.driver.video.vesa;

import java.nio.ByteBuffer;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class PMInfoBlock {
    private static final int ENTRY_POINT = 0;
    private static final int PM_INITIALIZE = ENTRY_POINT + 2;
    private static final int BIOS_DATA_SEL = PM_INITIALIZE + 2;
    private static final int A0000_SEL = BIOS_DATA_SEL + 2;
    private static final int B0000_SEL = A0000_SEL + 2;
    private static final int B8000_SEL = B0000_SEL + 2;
    private static final int CODE_SEG_SEL = B8000_SEL + 2;
    private static final int IN_PROTECT_MODE = CODE_SEG_SEL + 2;

    private final ByteBuffer buffer;

    public PMInfoBlock(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public void setBiosDataSel(short biosDataSel) {
        buffer.putShort(BIOS_DATA_SEL, biosDataSel);
    }

    public short getBiosDataSel() {
        return buffer.getShort(BIOS_DATA_SEL);
    }

    public short getEntryPoint() {
        return buffer.getShort(ENTRY_POINT);
    }

    public short getPmInitialize() {
        return buffer.getShort(PM_INITIALIZE);
    }

    public short getA0000Sel() {
        return buffer.getShort(A0000_SEL);
    }

    public short getB0000Sel() {
        return buffer.getShort(B0000_SEL);
    }

    public short getB8000Sel() {
        return buffer.getShort(B8000_SEL);
    }

    public short getCodeSegSel() {
        return buffer.getShort(CODE_SEG_SEL);
    }

    public boolean isInProtectMode() {
        return (buffer.get(IN_PROTECT_MODE) == 1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nentryPoint=").append(getEntryPoint());
        sb.append("\npmInitialize=").append(getPmInitialize());
        sb.append("\nbiosDataSel=").append(getBiosDataSel());
        sb.append("\na0000Sel=").append(getA0000Sel());
        sb.append("\nb0000Sel=").append(getB0000Sel());
        sb.append("\nb8000Sel=").append(getB8000Sel());
        sb.append("\ncodeSegSel=").append(getCodeSegSel());
        sb.append("\ninProtectMode=").append(isInProtectMode());

        return sb.toString();
    }
}
