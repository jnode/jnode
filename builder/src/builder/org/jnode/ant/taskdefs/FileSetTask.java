/*
 * $Id: HeaderTask.java 3379 2007-08-04 10:19:57Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Abstract class for ant task that process one or more FileSet
 * The concrete classes only have to implement the <i>process</i> method
 * for doing the concrete work on a file.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
abstract public class FileSetTask extends Task {
    protected boolean trace = false;
    protected boolean failOnError = true;

    private final ArrayList<FileSet> fileSets = new ArrayList<FileSet>();

    public final void setTrace(boolean trace) {
        this.trace = trace;
    }

    public final void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public void addFileSet(FileSet fs) {
        fileSets.add(fs);
    }

    final public void execute() throws BuildException {
        try {
            doExecute();
        } catch (BuildException be) {
            if (failOnError) {
                throw be;
            } else {
                be.printStackTrace();
            }
        } catch (Throwable t) {
            if (failOnError) {
                throw new BuildException(t);
            } else {
                t.printStackTrace();
            }
        }
    }

    protected void doExecute() throws BuildException {
        // default implementation : simply iterate on all files
        processFiles();
    }

    final protected void processFiles() throws BuildException {
        final Project project = getProject();
        try {
            for (FileSet fs : fileSets) {
                final String[] files = fs.getDirectoryScanner(project)
                    .getIncludedFiles();
                final File projectDir = fs.getDir(project);
                for (String fname : files) {
                    processFile(new File(projectDir, fname));
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    abstract protected void processFile(File file) throws IOException;
}
