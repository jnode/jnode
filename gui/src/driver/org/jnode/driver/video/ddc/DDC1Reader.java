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

package org.jnode.driver.video.ddc;

/**
 * Class used to read DDC1 data from a display device
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DDC1Reader implements EDIDConstants {

    private final DisplayDataChannelAPI api;

    /**
     * Create a new instance
     * 
     * @param api
     */
    public DDC1Reader(DisplayDataChannelAPI api) {
        this.api = api;
    }

    /**
     * Read a DDC1 EDID packet
     * 
     * @return The parsed EDID packet
     * @throws DDC1NoSignalException No DDC1 signal is found, the monitor does
     *             not send it, or the videocard does not support it.
     * @throws DDC1ParseException No valid EDID datablock was found. This can be
     *             the result of a small hickup in the connection, so is does
     *             not hurd to try it again.
     */
    public EDID read() throws DDC1NoSignalException, DDC1ParseException {
        api.setupDDC1();
        try {
            testSignal();

            final boolean[] bits = readBits();
            final int start = findStartBit(bits);
            final int length = bits.length;
            final byte[] data = new byte[EDID1_LEN];

            // Convert bits to bytes
            int idx = start;
            for (int i = 0; i < EDID1_LEN; i++) {
                int v = 0;
                for (int j = 0; j < 8; j++) {
                    v = v << 1;
                    if (bits[idx++]) {
                        v |= 0x01;
                    }
                    if (idx == length) {
                        idx = 0;
                    }
                }
                data[i] = (byte) v;
                idx++;
                if (idx == length) {
                    idx = 0;
                }
            }

            return new EDID(resort(data));
        } finally {
            api.closeDDC1();
        }
    }

    /**
     * Read a complete packet of EDID1 bits
     * 
     * @return
     */
    private boolean[] readBits() {
        final int max = EDID1_LEN * BITS_PER_BYTE;
        final boolean[] bits = new boolean[max];
        for (int i = 0; i < max; i++) {
            bits[i] = api.getDDC1Bit();
        }
        return bits;
    }

    /**
     * Find the start bit
     * 
     * @param bits
     * @return
     */
    private int findStartBit(boolean[] bits) throws DDC1ParseException {
        final boolean[] comp = new boolean[BITS_PER_BYTE];
        final boolean[] test = new boolean[BITS_PER_BYTE];
        int idx = 0;
        for (int i = 0; i < 9; i++) {
            comp[i] = bits[idx++];
            test[i] = true;
        }
        for (int i = 0; i < 127; i++) {
            for (int j = 0; j < 9; j++) {
                test[j] = test[j] & !(comp[j] ^ bits[idx++]);
            }
        }
        for (int i = 0; i < 9; i++) {
            if (test[i]) {
                return (i + 1);
            }
        }
        throw new DDC1ParseException("Start bit not found");
    }

    private byte[] resort(byte[] data) throws DDC1ParseException {
        final int max = data.length;
        final byte[] dbldata = new byte[max * 2];
        System.arraycopy(data, 0, dbldata, 0, max);
        System.arraycopy(data, 0, dbldata, max, max);
        final int hdrmax = HEADER_SIGNATURE.length;
        for (int i = 0; i < max; i++) {
            int j;
            for (j = 0; j < hdrmax; j++) {
                if (data[i + j] != HEADER_SIGNATURE[j]) {
                    break;
                }
            }
            if (j == hdrmax) {
                // We found the header signature at index i
                final byte[] result = new byte[max];
                System.arraycopy(dbldata, i, result, 0, max);
                return result;
            }
        }
        throw new DDC1ParseException("Header not found");
    }

    /**
     * Test for the presence of a DDC1 signal
     * 
     * @throws DDC1NoSignalException
     */
    private void testSignal() throws DDC1NoSignalException {
        final boolean old = api.getDDC1Bit();
        for (int count = HEADER * BITS_PER_BYTE; count > 0; count--) {
            if (old != api.getDDC1Bit()) {
                return;
            }
        }
        throw new DDC1NoSignalException("No DDC1 signal detected");
    }

}
