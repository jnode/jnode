/*
 * $Id: GetPropertiesAction.java 4973 2009-02-02 07:52:47Z lsantha $
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
 
package gnu.java.security.action;

import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;


/**
 * Utility class for getting the map containing the environment variables in a privileged action.
 * 
 * @author crawley@jnode.org
 */
public class GetEnvAction implements PrivilegedAction<Map<String, String>> {

    /**
     * @see java.security.PrivilegedAction#run()
     */
    public Map<String, String> run() {
        return System.getenv();
    }
}
