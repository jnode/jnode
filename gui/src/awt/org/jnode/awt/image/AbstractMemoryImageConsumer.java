/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.awt.image;

import java.awt.Dimension;
import java.awt.Rectangle;
import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class AbstractMemoryImageConsumer extends AbstractImageConsumer {

    protected final MemoryResource target;
    protected final int targetWidth;
    protected final int targetHeight;
    protected final int destX;
    protected final int destY;
    protected final int destWidth;
    protected final int destHeight;
    protected final int bytesPerLine;

    /**
     * @param target
     * @param targetDimension
     * @param dest
     * @param bytesPerLine
     */
    public AbstractMemoryImageConsumer(MemoryResource target, Dimension targetDimension, Rectangle dest,
                                       int bytesPerLine) {
        this.target = target;
        this.targetWidth = targetDimension.width;
        this.targetHeight = targetDimension.height;
        this.destX = dest.x;
        this.destY = dest.y;
        this.destWidth = dest.width;
        this.destHeight = dest.height;
        this.bytesPerLine = bytesPerLine;
    }

    /**
     * @param target
     * @param targetW
     * @param targetH
     * @param destX
     * @param destY
     * @param destW
     * @param destH
     * @param bytesPerLine
     */
    public AbstractMemoryImageConsumer(MemoryResource target, int targetW, int targetH, int destX, int destY, int destW,
                                       int destH, int bytesPerLine) {
        this.target = target;
        this.targetWidth = targetW;
        this.targetHeight = targetH;
        this.destX = destX;
        this.destY = destY;
        this.destWidth = destW;
        this.destHeight = destH;
        this.bytesPerLine = bytesPerLine;
    }

}
