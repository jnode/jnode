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
 
package org.jnode.ant.taskdefs.classpath;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;

public final class BaseDirs {

    private final ArrayList<TargetedFileSet> fileSets = new ArrayList<TargetedFileSet>();

    public TargetedFileSet createFileset() {
        final TargetedFileSet fs = new TargetedFileSet();
        fileSets.add(fs);
        return fs;
    }

    public Map<String, SourceFile> scanJavaFiles(Project project) {
        final TreeMap<String, SourceFile> map = new TreeMap<String, SourceFile>();
        for (TargetedFileSet fs : fileSets) {
            final DirectoryScanner ds = fs.getDirectoryScanner(project);
            final String[] fNames = ds.getIncludedFiles();
            for (int j = 0; j < fNames.length; j++) {
                final String fName = fNames[j];
                final SourceFile existingFile = map.get(fName);
                final SourceFile jf = new SourceFile(ds.getBasedir(), fName, fs.getTarget(), fs.isIgnoremissing());
                if (existingFile != null) {
                    if (existingFile.getTarget().equals(fs.getTarget())) {
                        throw new BuildException("File " + fName + " already exists");
                    }
                    existingFile.append(jf);
                } else {
                    map.put(fName, jf);
                }
            }
        }
        return map;
    }
}
