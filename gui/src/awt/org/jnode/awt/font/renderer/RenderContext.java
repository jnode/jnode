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

package org.jnode.awt.font.renderer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Class used to hold objects often used in the font render process.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RenderContext {

    private final Map<Object, WeakReference<?>> objects = new HashMap<Object, WeakReference<?>>();

    /**
     * Gets an object out of this context with a given key.
     *
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
     *
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
