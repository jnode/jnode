/* class FileFilter
 */

package charvax.swing.filechooser;

import java.io.File;

/**
 * FileFilter is an abstract class that has no default implementation.
 * An implementation of this class can be set on a JFileChooser to prevent
 * unwanted files from appearing in the directory listing.
 */
public abstract class FileFilter
{
    public FileFilter() {}

    /**
     * Determines whether the specified file is accepted by this filter.
     */
    public abstract boolean accept(File file_);
}
