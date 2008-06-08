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
 
package org.jnode.test.net;

import java.io.InputStream;
import java.net.URL;

/**
 * @author epr
 */
public class URLTest {

    public static void main(String[] args) throws Exception {

        final URL url = new URL((args.length > 0) ? args[0] : "http://192.168.200.1");
        final InputStream is = url.openConnection().getInputStream();
        try {
            int ch;
            final StringBuffer buf = new StringBuffer();
            while ((ch = is.read()) >= 0) {
                buf.append((char) ch);
            }
            System.out.println("Result:\n" + buf);
        } finally {
            is.close();
        }
    }
}
