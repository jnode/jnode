/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.ant.taskdefs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.FileSet;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Native2AsciiTask extends Task {

    private final ArrayList fileSets = new ArrayList();

    private boolean update = false;

    public void addFileSet(FileSet fs) {
        fileSets.add(fs);
    }

    public void execute() throws BuildException {
        try {
            for (Iterator i = fileSets.iterator(); i.hasNext();) {
                final FileSet fs = (FileSet) i.next();

                final String[] files = fs.getDirectoryScanner(getProject())
                        .getIncludedFiles();
                final int fileCount = files.length;
                for (int j = 0; j < fileCount; j++) {
                    final String fname = files[j];
                    processFile(new File(fs.getDir(getProject()), fname));
                }

            }
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }

    public final boolean isUpdate() {
        return update;
    }

    private void processFile(File file) throws IOException {
        if (containsNonAscii(file)) {
            final File tmp = File.createTempFile("jnode", "n2a");
            try {
                final String[] cmd = { "native2ascii", file.getAbsolutePath(),
                        tmp.getAbsolutePath() };
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
