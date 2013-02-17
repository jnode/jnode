/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.classmgr;

import java.util.StringTokenizer;

import org.jnode.vm.InternString;

/**
 * Class utility that mangle strings for the class manager.
 * 
 * @author Fabien DUMINY
 *
 */
class Mangler {
    /**
     * Mangle an identifier into a ASCII C name
     *
     * @param s
     * @return String
     */
    static String mangle(String s) {
        final char[] src = s.toCharArray();
        final int cnt = s.length();
        final StringBuilder res = new StringBuilder(cnt);
        for (int i = 0; i < cnt; i++) {
            final char ch = src[i];
            if (((ch >= 'a') && (ch <= 'z'))
                || ((ch >= 'A') && (ch <= 'Z'))
                || ((ch >= '0') && (ch <= '9'))) {
                res.append(ch);
            } else {
                res.append(Integer.toHexString(ch));
            }
        }
        return InternString.internString(res.toString());
    }

    /**
     * Mangle a classname into a ASCII C name
     *
     * @param s
     * @return String
     */
    static String mangleClassName(String s) {
        s = s.replace('/', '.');
        final StringTokenizer tok = new StringTokenizer(s, ".");
        final StringBuilder res = new StringBuilder(32);
        int q = tok.countTokens();
        res.append('Q');
        res.append(q);
        while (tok.hasMoreTokens()) {
            String v = tok.nextToken();
            res.append(v.length());
            res.append(v);
        }
        return InternString.internString(res.toString());
    }
}
