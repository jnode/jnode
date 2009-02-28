/*
 * $Id$
 *
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
     * Initialize this instance.
     */
    public TargetedFileSet() {
    }

    /**
     * Initialize this instance.
     *
     * @param arg0
     */
    public TargetedFileSet(FileSet arg0) {
        super(arg0);
    }

    /**
     * Initialize this instance.
     *
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
