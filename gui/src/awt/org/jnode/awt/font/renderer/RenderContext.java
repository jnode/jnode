/*
 * $Id$
 */
package org.jnode.awt.font.renderer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to hold objects often used in the font render process.
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RenderContext {

    private final Map<Object, WeakReference<?>> objects = new HashMap<Object, WeakReference<?>>();
    
    /**
     * Gets an object out of this context with a given key.
     * @param key
     * @return
     */
    public Object getObject(Object key) {
        final WeakReference<?> ref = objects.get(key);
        if (ref != null) {
            return ref.get();
        } else {
            return null;
        }
    }
    
    /**
     * Gets an object out of this context with a given key.
     * @param key
     * @return
     */
    public void setObject(Object key, Object value) {
        final WeakReference<?> ref = objects.get(key);
        if ((ref == null) || (ref.get() != value)) {
            objects.put(key, new WeakReference<Object>(value));
        }
    }    
}
