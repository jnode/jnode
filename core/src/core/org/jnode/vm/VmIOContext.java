/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class VmIOContext implements IOContext {
    private static InputStream globalInStream;
    private static PrintStream globalOutStream;
    private static PrintStream globalErrStream;

    public void setGlobalInStream(InputStream in) {
        globalInStream = in;
    }

    public void setGlobalOutStream(PrintStream out) {
        globalOutStream = out;
    }

    public PrintStream getGlobalOutStream() {
        return globalOutStream;
    }

    public void setGlobalErrStream(PrintStream err) {
        globalErrStream = err;
    }

    public PrintStream getGlobalErrStream() {
        return globalErrStream;
    }

    public InputStream getGlobalInStream() {
        return globalInStream;
    }

    public void setSystemIn(InputStream in) {
        globalInStream = in;
        VmSystem.setStaticField(System.class, "in", in);
    }

    public void setSystemOut(PrintStream out) {
        globalOutStream = out;
        VmSystem.setStaticField(System.class, "out", out);
    }

    public void setSystemErr(PrintStream err) {
        globalErrStream = err;
        VmSystem.setStaticField(System.class, "err", err);
    }

    public void enterContext() {
        // No-op
    }

    public void exitContext() {
        // No-op
    }

    public PrintStream getRealSystemErr() {
        return System.err;
    }

    public InputStream getRealSystemIn() {
        return System.in;
    }

    public PrintStream getRealSystemOut() {
        return System.out;
    }
}
