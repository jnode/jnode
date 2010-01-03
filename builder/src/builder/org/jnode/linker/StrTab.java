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
 
package org.jnode.linker;

import java.util.HashMap;
import java.util.Map;

/**
 * @author epr
 */
public class StrTab {

    /**
     * (String, Index)*.
     */
    private final HashMap<String, Integer> str2addr = new HashMap<String, Integer>();
    /**
     * (Index, String)*.
     */
    private final HashMap<Integer, String> addr2str = new HashMap<Integer, String>();
    private int maxIndex;

    /**
     * Create a new instance.
     */
    public StrTab() {
        maxIndex = 1;
    }

    /**
     * Create a new instance and initialize from the given byte array.
     */
    public StrTab(byte[] data, int length) {
        final int cnt = length;
        int addr = 0;
        while (addr < cnt) {
            int size = 0;
            while ((addr + size < cnt) && (data[addr + size] != 0)) {
                size++;
            }
            final String s = new String(data, addr, size);
            final Integer index = new Integer(addr);
            str2addr.put(s, index);
            addr2str.put(index, s);
            addr += (size + 1);
        }
        maxIndex = cnt;
    }

    /**
     * Add a string and return its index.
     *
     * @param v
     */
    public int addString(String v) {
        int addr = findString(v);
        if (addr < 0) {
            addr = maxIndex;
            maxIndex += (v.length() + 1);
            final Integer index = new Integer(addr);
            str2addr.put(v, index);
            addr2str.put(index, v);
        }
        return addr;
    }

    /**
     * Gets the String at a given index.
     *
     * @param index
     */
    public String getString(int index) {
        return (String) addr2str.get(new Integer(index));
    }

    /**
     * Return the index of a given string, or -1 if not found.
     */
    public int findString(String v) {
        final Integer addr = (Integer) str2addr.get(v);
        if (addr != null) {
            return addr.intValue();
        } else {
            return -1;
        }
    }

    /**
     * Convert to a strtab formatted byte array.
     *
     * @return This strtab as elf strtab section.
     */
    public byte[] toByteArray() {
        final byte[] data = new byte[maxIndex];
        for (Map.Entry<Integer, String> entry : addr2str.entrySet()) {
            final String str = entry.getValue();
            final int addr = entry.getKey().intValue();
            for (int k = 0; k < str.length(); k++) {
                data[addr + k] = (byte) str.charAt(k);
            }
        }
        return data;
    }
}
