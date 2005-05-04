/*
 * $Id$
 */
package org.jnode.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class contains information needed for the assembly compilation
 * of the nano kernel sources.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class AsmSourceInfo {

    /** Include directories */
    private final List<File> includeDirs = new ArrayList<File>();
    
    /** Main source file */
    private File sourceFile;

    /**
     * Return a list of all configured include directories.
     * @return
     */
    public final List<File> includeDirs() {
        return Collections.unmodifiableList(includeDirs);
    }
    
    /**
     * Gets the main source file.
     * @return Returns the sourceFile.
     */
    public final File getSrcFile() {
        return sourceFile;
    }

    /**
     * Sets the main source file.
     * @param sourceFile The sourceFile to set.
     */
    public final void setSrcFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    /**
     * Create an includeDir sub element. 
     * @return
     */
    public IncludeDir createIncludeDir() {
        return new IncludeDir();
    }
    
    public final class IncludeDir {
        public void setDir(File dir) {
            includeDirs.add(dir);
        }
    }
}
