/*
 * $Id$
 */
package org.jnode.ant.taskdefs.classpath;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class TargetedFileSet extends ZipFileSet {

    static final String DEFAULT_TARGET = "1.4";
    private String target = DEFAULT_TARGET;
    private boolean ignoreMissing = false;
    
    /**
     * Initialize this instance 
     */
    public TargetedFileSet() {
    }

    /**
     * Initialize this instance 
     * @param arg0
     */
    public TargetedFileSet(FileSet arg0) {
        super(arg0);
    }

    /**
     * Initialize this instance 
     * @param arg0
     */
    public TargetedFileSet(ZipFileSet arg0) {
        super(arg0);
    }

    /**
     * @return Returns the target.
     */
    public final String getTarget() {
        return target;
    }

    /**
     * @param target The target to set.
     */
    public final void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return Returns the ignoreMissing.
     */
    public final boolean isIgnoremissing() {
        return ignoreMissing;
    }

    /**
     * @param ignoreMissing The ignoreMissing to set.
     */
    public final void setIgnoremissing(boolean ignoreMissing) {
        this.ignoreMissing = ignoreMissing;
    }
}
