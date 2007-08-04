/*
 * $Id$
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
 
package org.jnode.build;

import java.io.File;
import java.util.ArrayList;
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

    private File destDir;
    private final ArrayList<FileSet> fileSets = new ArrayList<FileSet>();
    private File pluginDir;
    private File systemPluginListFile;
    
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
     * @see org.apache.tools.ant.Task#execute()
     */
    public void execute() throws BuildException {
        for (FileSet fs : fileSets) {
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());

            final String[] listFiles = ds.getIncludedFiles();
            for (int j = 0; j < listFiles.length; j++) {
                final File listFile = new File(ds.getBasedir(), listFiles[j]);
                
                final InitJarBuilder builder = new InitJarBuilder();
                builder.setProject(getProject());
                builder.setTaskName(getTaskName());
                builder.setPluginDir(getPluginDir());
                builder.setSystemPluginList(systemPluginListFile);
                builder.setPluginList(listFile);
                builder.setDestDir(getDestDir());
                
                builder.execute();
            }
            
        }
    }

    /**
     * @return Returns the destDir.
     */
    public final File getDestDir() {
        return destDir;
    }
    
    /**
     * @return Returns the pluginDir.
     */
    public final File getPluginDir() {
        return pluginDir;
    }

    /**
     * @return Returns the systemPluginListFile.
     */
    public final File getSystemPluginList() {
        return systemPluginListFile;
    }

    /**
     * @param destDir The destDir to set.
     */
    public final void setDestDir(File destDir) {
        this.destDir = destDir;
    }

    /**
     * @param pluginDir The pluginDir to set.
     */
    public final void setPluginDir(File pluginDir) {
        this.pluginDir = pluginDir;
    }

    /**
     * @param systemPluginListFile The systemPluginListFile to set.
     */
    public final void setSystemPluginList(File systemPluginListFile) {
        this.systemPluginListFile = systemPluginListFile;
    }

}
