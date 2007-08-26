/*
 * Copyright 1998-2002 Sun Microsystems, Inc.  All Rights Reserved.
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

abstract class AbstractTypeListNode extends AbstractNamedNode {

    void constrainComponent(Context ctx, Node node) {
        if (node instanceof TypeNode) {
            node.constrain(ctx);
        } else {
            error("Expected type descriptor item, got: " + node);
        }
    }

    void document(PrintWriter writer) {
        writer.println("<dt>" + name() + " Data");
        if (components.size() == 0) {
            writer.println("<dd>(None)");
        } else {
            writer.println("<dd><table border=1 cellpadding=3 cellspacing=0 width=\"90%\" summary=\"\"><tr>");
            for (int i = maxStructIndent; i > 0; --i) {
                writer.print("<th width=\"4%\">");
            }
            writer.println("<th width=\"15%\"><th width=\"65%\">");
            writer.println("");
            for (Iterator it = components.iterator(); it.hasNext();) {
                ((Node)it.next()).document(writer);
            }
            writer.println("</table>");
        }
    }

    void genJavaClassBodyComponents(PrintWriter writer, int depth) {
        for (Iterator it = components.iterator(); it.hasNext();) {
            TypeNode tn = (TypeNode)it.next();

            tn.genJavaDeclaration(writer, depth);
        }
    }
  
    void genJavaReads(PrintWriter writer, int depth) {
        for (Iterator it = components.iterator(); it.hasNext();) {
            TypeNode tn = (TypeNode)it.next();
            tn.genJavaRead(writer, depth, tn.name());
        }
    }

    void genJavaReadingClassBody(PrintWriter writer, int depth, 
                                 String className) {
        genJavaClassBodyComponents(writer, depth);
        writer.println();
        indent(writer, depth);
        if (!context.inEvent()) {
            writer.print("private ");
        }
        writer.println(className + 
                       "(VirtualMachineImpl vm, PacketStream ps) {");
        genJavaReads(writer, depth+1);
        indent(writer, depth);
        writer.println("}");
    }

    String javaParams() {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = components.iterator(); it.hasNext();) {
            TypeNode tn = (TypeNode)it.next();
            sb.append(tn.javaParam());
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
  
    void genJavaWrites(PrintWriter writer, int depth) {
        for (Iterator it = components.iterator(); it.hasNext();) {
            TypeNode tn = (TypeNode)it.next();
            tn.genJavaWrite(writer, depth, tn.name());
        }
    }

    void genJavaWritingClassBody(PrintWriter writer, int depth, 
                                 String className) {
        genJavaClassBodyComponents(writer, depth);
        writer.println();
        indent(writer, depth);
        writer.println(className + "(" + javaParams() + ") {");
        for (Iterator it = components.iterator(); it.hasNext();) {
            TypeNode tn = (TypeNode)it.next();
            indent(writer, depth+1);
            writer.println("this." + tn.name() + " = " + tn.name() + ";");
        }
        indent(writer, depth);
        writer.println("}");
    }
}
