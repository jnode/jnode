/*
 * Portions Copyright 2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.tools.internal.ws.wscompile;

import java.io.File;
import java.io.IOException;

import com.sun.codemodel.internal.JPackage;
import com.sun.codemodel.internal.writer.FileCodeWriter;
import com.sun.tools.internal.ws.processor.util.GeneratedFileInfo;
import com.sun.tools.internal.ws.processor.util.ProcessorEnvironment;

/**
 * {@link FileCodeWriter} implementation that notifies
 * JAX-WS about newly created files.
 *
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class WSCodeWriter extends FileCodeWriter {
    private final ProcessorEnvironment env;

    public WSCodeWriter( File outDir, ProcessorEnvironment _env ) throws IOException {
        super(outDir);
        this.env = _env;
    }

    protected File getFile(JPackage pkg, String fileName ) throws IOException {
        File f = super.getFile(pkg, fileName);

        // notify JAX-WS RI
        GeneratedFileInfo fi = new GeneratedFileInfo();
        fi.setType("JAXB"/*GeneratorConstants.FILE_TYPE_VALUETYPE*/);
        fi.setFile(f);
        env.addGeneratedFile(fi);
        // we can't really tell the file type, for we don't know
        // what this file is used for. Fortunately,
        // FILE_TYPE doesn't seem to be used, so it doesn't really
        // matter what we set.

        return f;
    }
}
