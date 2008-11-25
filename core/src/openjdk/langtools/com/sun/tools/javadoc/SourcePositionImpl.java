/*
 * Copyright 2001-2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.javadoc;

import com.sun.javadoc.SourcePosition;
import com.sun.tools.javac.util.Position;

import java.io.File;

/**
 * A source position: filename, line number, and column number.
 *
 * @since J2SE1.4
 * @author Neal M Gafter
 * @author Michael Van De Vanter (position representation changed to char offsets)
 */
class SourcePositionImpl implements SourcePosition {
    String filename;
    int position;
    Position.LineMap lineMap;

    /** The source file. Returns null if no file information is 
     *  available. */
    public File file() {
	return (filename == null) ? null : new File(filename);
    }

    /** The line in the source file. The first line is numbered 1;
     *  0 means no line number information is available. */
    public int line() {
	if (lineMap == null) {
	    return 0;
	} else {
	    return lineMap.getLineNumber(position);
	}
    }

    /** The column in the source file. The first column is
     *  numbered 1; 0 means no column information is available.
     *  Columns count characters in the input stream; a tab
     *  advances the column number to the next 8-column tab stop.
     */
    public int column() {
	if (lineMap == null) {
	    return 0;
	}else {
	    return lineMap.getColumnNumber(position);
	}
    }

    private SourcePositionImpl(String file, int position,
			       Position.LineMap lineMap) {
	super();
	this.filename = file;
	this.position = position;
	this.lineMap = lineMap;
    }

    public static SourcePosition make(String file, int pos,
				      Position.LineMap lineMap) {
	if (file == null) return null;
	return new SourcePositionImpl(file, pos, lineMap);
    }

    public String toString() {
	if (position == Position.NOPOS)
	    return filename;
	else
	    return filename + ":" + line();
    }
}
