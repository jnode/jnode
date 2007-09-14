/*
 * $Id$
 */
package java.util.prefs;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeFileSystemPreferences {
    /**
     * @see java.util.prefs.FileSystemPreferences#lockFile0(String, int, boolean)
     */
    private static int[] lockFile0(String fileName, int permission, boolean shared){
        //todo implement it
        return new int[]{1, 0};
    }

    /**
     *
     * @see java.util.prefs.FileSystemPreferences#unlockFile0(int)
     */
    private static int unlockFile0(int lockHandle){
        //todo implement it
        return 0;
    }

    /**
     * @see java.util.prefs.FileSystemPreferences#chmod(String, int)
     */
    private static int chmod(String fileName, int permission){
        //todo implement it
        return 0;
    }
}
