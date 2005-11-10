package org.jnode.fs.ftpfs;

import org.jnode.fs.FSEntry;
import org.jnode.fs.FSAccessRights;

import java.io.IOException;
import java.util.Date;

import com.enterprisedt.net.ftp.FTPFile;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPFSEntry implements FSEntry {
    FTPFileSystem fileSystem;
    FTPFile ftpFile;
    FTPFSDirectory parent;

    public FTPFSEntry(FTPFileSystem fileSystem, FTPFile ftpFile) {
        this.fileSystem = fileSystem;
        this.ftpFile = ftpFile;
    }


    public void setParent(FTPFSDirectory parent) {
        this.parent = parent;
    }

    /**
     * Gets the accessrights for this entry.
     *
     * @throws java.io.IOException
     */
    public FSAccessRights getAccessRights() throws IOException {
        return null;
    }

    /**
     * Gets the directory this entry refers to. This method can only be called
     * if <code>isDirectory</code> returns true.
     *
     * @return The directory described by this entry
     */
    public FTPFSDirectory getDirectory() throws IOException {
        return (FTPFSDirectory) this;
    }

    /**
     * Gets the file this entry refers to. This method can only be called
     * if <code>isFile</code> returns true.
     *
     * @return The file described by this entry
     */
    public FTPFSFile getFile() throws IOException {
        return (FTPFSFile) this;
    }

    /**
     * Gets the last modification time of this entry.
     *
     * @throws java.io.IOException
     */

    public long getLastModified() throws IOException {
        return ftpFile.lastModified().getTime();
    }

    /**
     * Gets the name of this entry.
     */
    public String getName() {
        return ftpFile.getName();
    }

    /**
     * Gets the directory this entry is a part of.
     */
    public FTPFSDirectory getParent() {
        return null;
    }

    /**
     * Is this entry refering to a (sub-)directory?
     */
    public boolean isDirectory() {
        return ftpFile.isDir();
    }

    /**
     * Indicate if the entry has been modified in memory (ie need to be saved)
     *
     * @return true if the entry need to be saved
     * @throws java.io.IOException
     */
    public boolean isDirty() throws IOException {
        return false;
    }

    /**
     * Is this entry refering to a file?
     */
    public boolean isFile() {
        return !ftpFile.isDir();
    }

    /**
     * Gets the last modification time of this entry.
     *
     * @throws java.io.IOException
     */
    public void setLastModified(long lastModified) throws IOException {

    }

    /**
     * Sets the name of this entry.
     */
    public void setName(String newName) throws IOException {

    }

    /**
     * Gets the filesystem to which this object belongs.
     */
    public FTPFileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Is this object still valid.
     * <p/>
     * An object is not valid anymore if it has been removed from the filesystem.
     * All invocations on methods (exception this method) of invalid objects
     * must throw an IOException.
     */
    public boolean isValid() {
        return true;
    }
}
