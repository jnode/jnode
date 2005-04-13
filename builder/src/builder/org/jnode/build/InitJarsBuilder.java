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
 
package org.jnode.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Task used to build several initjars.
 * This task needs a "pluginDir" attribute containing the directory that contains
 * all the plugins.
 * This task also needs 1 or more filesets identifying the initjar plugin lists.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InitJarsBuilder extends Task {

    private File pluginDir;
    private File destDir;
    private final ArrayList fileSets = new ArrayList();
    
    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        for (Iterator i = fileSets.iterator(); i.hasNext(); ) {
            final FileSet fs = (FileSet)i.next();
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());

            final String[] listFiles = ds.getIncludedFiles();
            for (int j = 0; j < listFiles.length; j++) {
                final File listFile = new File(ds.getBasedir(), listFiles[j]);
                
                final InitJarBuilder builder = new InitJarBuilder();
                builder.setProject(getProject());
                builder.setTaskName(getTaskName());
                builder.setPluginDir(getPluginDir());
                builder.setPluginList(listFile);
                builder.setDestDir(getDestDir());
                
                builder.execute();
            }
            
        }
    }
    
    /**
     * Add a fileset to this task.
     * 
     * @return
     */
    public FileSet createFileset() {
        final FileSet fs = new FileSet();
        fileSets.add(fs);
        return fs;
    }

    /**
     * @return Returns the pluginDir.
     */
    public final File getPluginDir() {
        return pluginDir;
    }

    /**
     * @param pluginDir The pluginDir to set.
     */
    public final void setPluginDir(File pluginDir) {
        this.pluginDir = pluginDir;
    }

    /**
     * @return Returns the destDir.
     */
    public final File getDestDir() {
        return destDir;
    }

    /**
     * @param destDir The destDir to set.
     */
    public final void setDestDir(File destDir) {
        this.destDir = destDir;
    }

}
