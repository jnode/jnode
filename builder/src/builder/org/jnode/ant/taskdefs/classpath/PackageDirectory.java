/*
 * $Id$
 */
package org.jnode.ant.taskdefs.classpath;

import java.io.File;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PackageDirectory implements Comparable {
    
    private final File baseDir;
    private final String packageFileName;
    
    public PackageDirectory(File baseDir, String directoryName) {
        this.baseDir = baseDir;
        this.packageFileName = directoryName;
    }
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        return packageFileName.compareTo(((PackageDirectory)obj).packageFileName);
    }
    
    public File getPackageDirectory() {
        return new File(baseDir, packageFileName);
    }

    public String getDirectoryName() {
        return packageFileName;
    }

    public String getPackageName() {
        return packageFileName.replace(File.separatorChar, '.');
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj instanceof PackageDirectory) {
            return ((PackageDirectory)obj).packageFileName.equals(packageFileName);
        } else {
            return packageFileName.equals(obj);
        }
    }
        
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return packageFileName.hashCode();
    }
    
    /**
     * @return Returns the baseDir.
     */
    public final File getBaseDir() {
        return this.baseDir;
    }
}
