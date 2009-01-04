/*
 * $Id$
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
