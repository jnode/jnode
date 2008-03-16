/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.syntax;

import java.util.Collections;
import java.util.Set;

public abstract class Syntax {
    
    protected final String label;
    private Syntax parent;
    
    
    public Syntax(String label) {
        this.label = label;
    }

    public final String getLabel() {
        return label;
    }

    public final boolean isLabelled() {
        return label != null;
    }
    
    public abstract String format(ArgumentBundle bundle);
    
    /**
     * @return the set of labels for this syntactic element's children, if any.
     */
    public Set<String> childLabels() {
        return Collections.emptySet();
    }
    
    public Syntax getParent() {
    	return parent;
    }
    
    public final Syntax getRoot() {
        Syntax p = parent;
        while (p.getParent() != null) {
        	p = p.getParent();
        }
        return p;
    }
    
    public void setParent(Syntax parent) {
        this.parent = parent;
    }

    /**
     * Prepare the MuSyntax for this Syntax.  In some cases, a 'prepare' method may
     * add extra synthetic arguments to the bundle.
     * 
     * @param bundle the bundle containing the command's arguments.
     * @return the prepared MuSyntax
     */
    public abstract MuSyntax prepare(ArgumentBundle bundle);
    
    /**
     * Determine if a formatted syntax would need to be wrapped in "()"s when embedded
     * in a larger format
     * 
     * @param formatted a formatted syntax
     * @return <code>true</code> if surrounding "()"s are required.
     */
    protected final boolean needsBracketting(String formatted) {
        if (formatted.startsWith("(") && formatted.endsWith(")")) {
            return false;
        }
        else if (formatted.startsWith("[") && formatted.endsWith("]")) {
            return false;
        }
        else {
            return formatted.contains(" ");
        }
    }
}
