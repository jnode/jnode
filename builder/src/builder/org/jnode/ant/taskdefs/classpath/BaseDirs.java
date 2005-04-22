/*
 * $Id$
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
    
    public Map<String, JavaFile> scanJavaFiles(Project project) {
        final TreeMap<String, JavaFile> map = new TreeMap<String, JavaFile>();
        for (TargetedFileSet fs : fileSets) {
            final DirectoryScanner ds = fs.getDirectoryScanner(project);
            final String[] fNames = ds.getIncludedFiles();
            for (int j = 0; j < fNames.length; j++) {
                final String fName = fNames[j];
                final JavaFile existingFile = (JavaFile)map.get(fName);
                final JavaFile jf = new JavaFile(ds.getBasedir(), fName, fs.getTarget(), fs.isIgnoremissing());
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