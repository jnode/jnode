/*
 * $Id: UDPAppender.java 3076 2007-01-15 13:33:17Z hagar-wize $
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

package org.jnode.debug;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RemoteAppender extends WriterAppender {

    public static final String LAYOUT = "%-5p [%c{1}]: %m%n";

    /**
     * Create an appender for a given outputstream
     */
    public RemoteAppender(OutputStream out, Layout layout) {
        if (layout != null) {
            setLayout(layout);
        } else {
            setLayout(new PatternLayout(LAYOUT));
        }
        setWriter(new OutputStreamWriter(out));
    }

}
