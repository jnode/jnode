/*
 * Copyright 2001-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.jdwpgen;

import java.util.*;
import java.io.*;

class ErrorSetNode extends AbstractSimpleNode {

    void constrainComponent(Context ctx, Node node) {
        if (node instanceof ErrorNode) {
            node.constrain(ctx);
        } else {
            error("Expected 'Error' item, got: " + node);
        }
    }

    void document(PrintWriter writer) {
        
        writer.println("<dt>" + "Error Data");
        if (components.size() == 0) {
            writer.println("<dd>(None)");
        } else {
            writer.println("<dd><table border=1 cellpadding=3 cellspacing=0 width=\"90%\" summary=\"\">");
        for (Iterator it = components.iterator(); it.hasNext();) {
            ((Node)it.next()).document(writer);
        }
        writer.println("</table>");
        }
    }

    void genJavaComment(PrintWriter writer, int depth) {}

    void genJava(PrintWriter writer, int depth) {}

    void genCInclude(PrintWriter writer) {}

    void genJavaDebugWrite(PrintWriter writer, int depth,
                           String writeLabel) {}

    void genJavaDebugWrite(PrintWriter writer, int depth,
                           String writeLabel, String displayValue) {}

    public void genJavaRead(PrintWriter writer, int depth,
                            String readLabel) {}
 
    void genJavaDebugRead(PrintWriter writer, int depth,
                          String readLabel, String displayValue) {}

    void genJavaPreDef(PrintWriter writer, int depth) {}

}
