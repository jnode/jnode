package java.util.jar;

import java.util.Enumeration;
import java.util.ArrayList;
import java.util.zip.ZipEntry;

/**
 * @see java.util.jar.JarFile
 */
class NativeJarFile {
    /**
     * @see java.util.jar.JarFile#getMetaInfEntryNames()
     */
    private static String[] getMetaInfEntryNames(JarFile instance) {
        ArrayList<String> ret = new ArrayList<String>();

        for (Enumeration e = instance.entries(); e.hasMoreElements(); ) {
            String name = ((ZipEntry) e.nextElement()).getName();
            if (name.startsWith("META-INF/"))
                ret.add(name);
        }

        return ret.isEmpty() ? null : ret.toArray(new String[ret.size()]);
    }
}
