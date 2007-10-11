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
 
package gnu.java.security.action;

import java.security.Policy;
import java.security.PrivilegedAction;

/**
 * Utility class for getting the current Policy in a privileged action.
 * 
 * @see java.security.Policy
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GetPolicyAction implements PrivilegedAction<Policy> {

    private static final GetPolicyAction instance = new GetPolicyAction();
    
    /**
     * Gets the single instance of this class.
     * @return
     */
    public static GetPolicyAction getInstance() {
        return instance;
    }
    
    /**
     * @see java.security.PrivilegedAction#run()
     */
    public Policy run() {
        return Policy.getPolicy();
    }
    
    /**
     * Singleton constructor.
     */
    private GetPolicyAction() {
        // Do nothing
    }
}
