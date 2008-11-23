/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.istack.internal;

import javax.activation.DataSource;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;

/**
 * {@link DataSource} backed by a byte buffer.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ByteArrayDataSource implements DataSource {

    private final String contentType;
    private final byte[] buf;
    private final int len;

    public ByteArrayDataSource(byte[] buf, String contentType) {
        this(buf,buf.length,contentType);
    }
    public ByteArrayDataSource(byte[] buf, int length, String contentType) {
        this.buf = buf;
        this.len = length;
        this.contentType = contentType;
    }

    public String getContentType() {
        if(contentType==null)
            return "application/octet-stream";
        return contentType;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(buf,0,len);
    }

    public String getName() {
        return null;
    }

    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException();
    }
}
