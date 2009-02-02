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
 
package org.jnode.driver.system.acpi.vm;

import java.io.PrintWriter;
import org.apache.log4j.Logger;

/**
 * AcpiObject.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public class AcpiObject {

    protected final Logger log = Logger.getLogger(getClass());
    private NameSpace nameSpace = null;

    public AcpiObject() {
        if (getName() != null)
            this.putInSameNameSpace(NameSpace.currentNameSpace);
    }

    public AcpiObject(NameSpace space) {
        nameSpace = space;
        if (getName() != null)
            this.putInSameNameSpace(space);
    }

    public NameSpace getNameSpace() {
        return nameSpace;
    }

    public String getName() {
        return Integer.toHexString(this.hashCode());
    }

    public void putInSameNameSpace(NameSpace space) {
        if (space == null)
            return;
        if (nameSpace != null)
            nameSpace.remove(getName());
        space.put(this);
    }

    public void putInSameNameSpace(AcpiObject obj) {
        if (obj == null)
            return;
        NameSpace newSpace = obj.getNameSpace();
        if (newSpace == null)
            return;
        if (nameSpace != null)
            nameSpace.remove(getName());
        newSpace.put(this);
    }

    public void dump(PrintWriter out, String prefix) {
        out.println(prefix + this.getClass().getName());
    }
}
