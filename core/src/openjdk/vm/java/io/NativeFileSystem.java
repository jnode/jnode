package java.io;


/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class NativeFileSystem {
    /**
     * Return the FileSystem object representing this platform's local
     * filesystem.
     */
    public static Object getFileSystem()
    {
    	return new JNodeFileSystem();
    }
}
