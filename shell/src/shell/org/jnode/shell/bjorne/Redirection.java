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
 
package org.jnode.shell.bjorne;

import java.io.File;

public class Redirection {
    private final File file;

    private final int fd;

    private final boolean output;

    private final boolean append;

    public Redirection(File file, boolean output, boolean append) {
        super();
        this.file = file;
        this.output = output;
        this.append = append;
        this.fd = -1;
    }

    public Redirection(int fd, boolean output) {
        super();
        this.file = null;
        this.output = output;
        this.append = true;
        this.fd = fd;
    }

    public boolean isFileRedirection() {
        return file != null;
    }

    public boolean isAppend() {
        return append;
    }

    public File getFile() {
        return file;
    }

    public boolean isOutput() {
        return output;
    }

    public int getFD() {
        return fd;
    }

}
