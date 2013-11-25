package org.jnode.fs;

/**
 * The interface for directories that are able to expose an ID that is unique across all instances in the file system.
 */
public interface FSDirectoryId {

    /**
     * Gets a unique ID for this directory in the context of its parent. This value should be the same every time the
     * directory is accessed.
     *
     * @return the unique ID for this directory.
     */
    public String getDirectoryId();
}
