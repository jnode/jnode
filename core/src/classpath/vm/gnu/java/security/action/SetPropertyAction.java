/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package gnu.java.security.action;

import java.security.PrivilegedAction;


/**
 * Utility class for setting properties in a privileged action.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SetPropertyAction implements PrivilegedAction<Void> {

    private final String key;
    private final String value;
    
    /**
     * Initialize this instance.
     * @param key
     */
    public SetPropertyAction(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    /**
     * Set the property
     * @see java.security.PrivilegedAction#run()
     */
    public Void run() {
        System.setProperty(key, value);
        return null;
    }
}
