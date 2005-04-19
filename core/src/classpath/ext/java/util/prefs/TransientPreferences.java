/*
 * $Id$
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
