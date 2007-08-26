/*
 * Copyright 1998-2001 Sun Microsystems, Inc.  All Rights Reserved.
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

class CommandNode extends AbstractCommandNode {

    void constrain(Context ctx) {
        if (components.size() == 3) {
            Node out = (Node)components.get(0);
            Node reply = (Node)components.get(1);
            Node error = (Node)components.get(2);
            if (!(out instanceof OutNode)) {
                error("Expected 'Out' item, got: " + out);
            }
            if (!(reply instanceof ReplyNode)) {
                error("Expected 'Reply' item, got: " + reply);
            }
            if (!(error instanceof ErrorSetNode)) {
                error("Expected 'ErrorSet' item, got: " + error);
            }
        } else if (components.size() == 1) {
            Node evt = (Node)components.get(0);
            if (!(evt instanceof EventNode)) {
                error("Expected 'Event' item, got: " + evt);
            }
        } else {
            error("Command must have Out and Reply items or ErrorSet item");
        }
        super.constrain(ctx);
    }

    void genJavaClassSpecifics(PrintWriter writer, int depth) {
        indent(writer, depth);
        writer.println("static final int COMMAND = " +
                       nameNode.value() + ";");
    }

    void genJava(PrintWriter writer, int depth) {
        genJavaClass(writer, depth);
    }
}
