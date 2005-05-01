/*
 * $Id$
 */
package org.jnode.fs.service.def;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FileSystem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VirtualFS implements FileSystem {
    
    final static Logger log = Logger.getLogger(VirtualFS.class);
    private final Device dev;
    private final VirtualDirEntry root;
    
    /**
     * Initialize this instance.
     * @throws IOException 
     */
    VirtualFS(Device dev) {
        this.dev = dev;
        try {
            this.root = new VirtualDirEntry(this, "/", null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }         
    }

    /**
     * @see org.jnode.fs.FileSystem#close()
     */
    public void close() throws IOException {
        // TODO Auto-generated method stub        
    }

    /**
     * @see org.jnode.fs.FileSystem#getDevice()
     */
    public Device getDevice() {
        return dev;
    }

    /**
     * @see org.jnode.fs.FileSystem#getRootEntry()
     */
    public FSEntry getRootEntry() {
        return root;
    }

    /**
     * @see org.jnode.fs.FileSystem#isReadOnly()
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * @see org.jnode.fs.FileSystem#isClosed()
     */
    public boolean isClosed() {
        return false;
    }
    
    /**
     * The filesystem on the given device will be removed.
     * @param dev
     */
    final void unregisterFileSystem(Device dev) {
        root.unregisterFileSystem(dev);
    }   
}
