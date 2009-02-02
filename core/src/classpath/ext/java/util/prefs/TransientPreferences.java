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
 
package java.util.prefs;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TransientPreferences extends AbstractPreferences {

    /**
     * Initialize this instance.
     */
    public TransientPreferences() {
        super(null, "name");        
    }

    /**
     * @see java.util.prefs.AbstractPreferences#childrenNamesSpi()
     */
    protected String[] childrenNamesSpi() throws BackingStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.util.prefs.AbstractPreferences#childSpi(java.lang.String)
     */
    protected AbstractPreferences childSpi(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.util.prefs.AbstractPreferences#flushSpi()
     */
    protected void flushSpi() throws BackingStoreException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see java.util.prefs.AbstractPreferences#getSpi(java.lang.String)
     */
    protected String getSpi(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.util.prefs.AbstractPreferences#keysSpi()
     */
    protected String[] keysSpi() throws BackingStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.util.prefs.AbstractPreferences#putSpi(java.lang.String, java.lang.String)
     */
    protected void putSpi(String key, String value) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see java.util.prefs.AbstractPreferences#removeNodeSpi()
     */
    protected void removeNodeSpi() throws BackingStoreException {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see java.util.prefs.AbstractPreferences#removeSpi(java.lang.String)
     */
    protected void removeSpi(String key) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see java.util.prefs.AbstractPreferences#syncSpi()
     */
    protected void syncSpi() throws BackingStoreException {
        // TODO Auto-generated method stub
        
    }
    
    
}
