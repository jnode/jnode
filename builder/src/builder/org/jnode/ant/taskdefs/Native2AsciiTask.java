/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.ant.taskdefs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Execute;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Native2AsciiTask extends FileSetTask {

    private boolean update = false;

    public final boolean isUpdate() {
        return update;
    }

    @Override
    protected void processFile(File file) throws IOException {
        if (containsNonAscii(file)) {
            final File tmp = File.createTempFile("jnode", "n2a");
            try {
                final String[] cmd = {"native2ascii", file.getAbsolutePath(),
                    tmp.getAbsolutePath()};
                final Execute exe = new Execute();
                exe.setCommandline(cmd);
                final int rc = exe.execute();
                if (rc != 0) {
                    throw new BuildException("native2ascii failed");
                }
                if (file.length() != tmp.length()) {
                    // We have a changed file
                    if (update) {
                        getProject().copyFile(tmp, file);
                        log("Updated " + file);
                    } else {
                        log("Update needed " + file);
                    }
                }
            } finally {
                tmp.delete();
            }
        }
    }

    private boolean containsNonAscii(File file) throws IOException {
        final FileInputStream is = new FileInputStream(file);
        try {
            final BufferedInputStream bis = new BufferedInputStream(is);
            int v;
            while ((v = bis.read()) >= 0) {
                if ((v & 0xFF) > 128) {
                    return true;
                }
            }
            return false;
        } finally {
            is.close();
        }
    }

    public final void setUpdate(boolean update) {
        this.update = update;
    }
}
