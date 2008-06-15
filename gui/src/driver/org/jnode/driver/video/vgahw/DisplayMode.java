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

package org.jnode.driver.video.vgahw;

import java.util.StringTokenizer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DisplayMode {

    /** Clock frequency in kHz */
    private final int freq;

    /** Visible display width in pixels */
    private final int width;
    /** h-sync pulse start */
    private final int hsyncStart;
    /** h-sync pulse end */
    private final int hsyncEnd;
    /** Total pixels per line */
    private final int hTotal;

    /** Visible display height in lines */
    private final int height;
    /** v-sync pulse start */
    private final int vsyncStart;
    /** v-sync pulse end */
    private final int vsyncEnd;
    /** Total lines per frame */
    private final int vTotal;

    /**
     * Create a new instance
     * 
     * @param arg In the form of: freq width hsync-s hsync-e htotal height
     *            v-syncs v-synce vtotal
     */
    public DisplayMode(String arg) {
        final StringTokenizer tok = new StringTokenizer(arg);
        freq = Integer.parseInt(tok.nextToken());
        width = Integer.parseInt(tok.nextToken());
        hsyncStart = Integer.parseInt(tok.nextToken());
        hsyncEnd = Integer.parseInt(tok.nextToken());
        hTotal = Integer.parseInt(tok.nextToken());
        height = Integer.parseInt(tok.nextToken());
        vsyncStart = Integer.parseInt(tok.nextToken());
        vsyncEnd = Integer.parseInt(tok.nextToken());
        vTotal = Integer.parseInt(tok.nextToken());
    }

    /**
     * @param freq
     * @param width
     * @param hsyncStart
     * @param hsyncEnd
     * @param total
     * @param height
     * @param vsyncStart
     * @param vsyncEnd
     * @param total2
     */
    public DisplayMode(int freq, int width, int hsyncStart, int hsyncEnd, int hTotal, int height,
            int vsyncStart, int vsyncEnd, int vTotal) {
        super();
        this.freq = freq;
        this.width = width;
        this.hsyncStart = hsyncStart;
        this.hsyncEnd = hsyncEnd;
        this.hTotal = hTotal;
        this.height = height;
        this.vsyncStart = vsyncStart;
        this.vsyncEnd = vsyncEnd;
        this.vTotal = vTotal;
    }

    /**
     * Gets the pixel frequency in kHz
     */
    public final int getFreq() {
        return this.freq;
    }

    /**
     * Gets the visible display height in lines
     */
    public final int getHeight() {
        return this.height;
    }

    /**
     * Gets the end of the h-sync pulse
     */
    public final int getHsyncEnd() {
        return this.hsyncEnd;
    }

    /**
     * Gets the start of the h-sync pulse
     */
    public final int getHsyncStart() {
        return this.hsyncStart;
    }

    /**
     * Gets the total pixels per line
     */
    public final int getHTotal() {
        return this.hTotal;
    }

    /**
     * Gets the end of the v-sync pulse
     */
    public final int getVsyncEnd() {
        return this.vsyncEnd;
    }

    /**
     * Gets the start of the v-sync pulse
     */
    public final int getVsyncStart() {
        return this.vsyncStart;
    }

    /**
     * Gets the total lines per frame
     */
    public final int getVTotal() {
        return this.vTotal;
    }

    /**
     * Gets the visible display width in pixels per line
     */
    public final int getWidth() {
        return this.width;
    }

    public String toString() {
        return "" + freq + " " + width + " " + hsyncStart + " " + hsyncEnd + " " + hTotal + " " +
                height + " " + vsyncStart + " " + vsyncEnd + " " + vTotal;
    }
}
