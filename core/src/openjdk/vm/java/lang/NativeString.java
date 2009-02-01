/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package java.lang;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
/**
 *
 */
public class NativeString {
    static final WeakHashMap<String, WeakReference<String>> internTable = new WeakHashMap<String, WeakReference<String>>();

    /**
     *
     * @param instance
     * @return
     * @see String#intern() 
     */
    private static String intern(String instance){
        synchronized (internTable) {
            final WeakReference<String> ref = internTable.get(instance);
            if (ref != null) {
                final String s = ref.get();
                // If s is null, then no strong references exist to the String;
                // the weak hash map will soon delete the key.
                if (s != null) {
                    return s;
                }
            }
            internTable.put(instance, new WeakReference<String>(instance));
        }
        return instance;
    }
}
