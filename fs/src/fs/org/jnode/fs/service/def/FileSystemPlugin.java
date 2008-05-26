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

package org.jnode.fs.service.def;

import java.io.IOException;
import java.io.VMFile;
import java.io.VMFileSystemAPI;
import java.io.VMIOUtils;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public class FileSystemPlugin extends Plugin implements FileSystemService {

    /** My logger */
    private static final Logger log = Logger.getLogger(FileSystemPlugin.class);

    /** Manager of fs types */
    private final FileSystemTypeManager fsTypeManager;

    /** Manager of mounted filesystems */
    private final FileSystemManager fsm;

    /** The FS-API implementation */
    private final FileSystemAPIImpl api;

    /** The mounter */
    private FileSystemMounter mounter;

    /** The device of the VFS filesystem */
    private final VirtualFSDevice vfsDev;

    /** The virtual filesystem */
    private final VirtualFS vfs;

    /**
     * Create a new instance
     *
     */
    public FileSystemPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.fsTypeManager = new FileSystemTypeManager(descriptor
                .getExtensionPoint("types"));
        this.fsm = new FileSystemManager();
        this.vfsDev = new VirtualFSDevice();
        this.vfs = new VirtualFS(vfsDev);
        this.api = new FileSystemAPIImpl(fsm, vfs);
    }

    /**
     * Gets all registered file system types. All instances of the returned
     * collection are instanceof FileSystemType.
     */
    public Collection<FileSystemType<?>> fileSystemTypes() {
        return fsTypeManager.fileSystemTypes();
    }

    /**
     * Register a mounted filesystem
     *
     * @param fs
     */
    public void registerFileSystem(FileSystem fs) {
        fsm.registerFileSystem(fs);
    }

    /**
     * Unregister a mounted filesystem
     *
     * @param device
     */
    public FileSystem unregisterFileSystem(final Device device) {
        return AccessController
                .doPrivileged(new PrivilegedAction<FileSystem>() {

                    public FileSystem run() {
                        api.unregisterFileSystem(device);
                        return fsm.unregisterFileSystem(device);
                    }
                });
    }

    /**
     * Gets the filesystem registered on the given device.
     *
     * @param device
     * @return null if no filesystem was found.
     */
    public FileSystem getFileSystem(Device device) {
        return fsm.getFileSystem(device);
    }

    /**
     * Gets all registered filesystems. All instances of the returned collection
     * are instanceof FileSystem.
     */
    public Collection<FileSystem> fileSystems() {
        return fsm.fileSystems();
    }

    /**
     * Mount the given filesystem at the fullPath, using the fsPath as root of
     * the to be mounted filesystem.
     *
     * @param fullPath
     * @param fs
     * @param fsPath Null or empty to use the root of the filesystem.
     */
    public void mount(String fullPath, FileSystem fs, String fsPath)
    throws IOException {
        if (fsPath != null) {
            fsPath = VMFile.getNormalizedPath(fsPath);
        }
        api.mount(VMFile.getNormalizedPath(fullPath), fs, fsPath);
    }

    /**
     * Return a map (fullPath -> FileSystem) of mount points
     * @return a copy of the internal map, sorted by fullPath
     */
    public Map<String, FileSystem<?>> getMountPoints()
    {
    	return api.getMountPoints();
    }
    
    /*
    * (non-Javadoc)
    * @see org.jnode.fs.service.FileSystemService#getDeviceMountPoints()
    */
    public Map<String, String> getDeviceMountPoints(){
    	Map<String, FileSystem<?>> mounts = api.getMountPoints();
    	Map<String, String> result = new TreeMap<String, String>();
    	for(String path: mounts.keySet()){
    		FileSystem fs = (FileSystem)mounts.get(path);
    		result.put(fs.getDevice().getId(), path);
    	}
    	return result;
    } 
    
    /**
     * Is the given directory a mount.
     * @param fullPath
     * @return
     */
    public boolean isMount(String fullPath) {
        return api.isMount(VMFile.getNormalizedPath(fullPath));
    }

    /**
     * Gets the filesystem API.
     */
    public VMFileSystemAPI getApi() {
        return api;
    }

    /**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
        try {
            DeviceUtils.getDeviceManager().register(vfsDev);
            VMIOUtils.setAPI(getApi(), this);
            mounter = new FileSystemMounter(this);
            InitialNaming.bind(NAME, this);
            mounter.start();
        } catch (NamingException ex) {
            throw new PluginException(ex);
        } catch (DeviceAlreadyRegisteredException ex) {
            throw new PluginException(ex);
        } catch (DriverException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() {
        mounter.stop();
        InitialNaming.unbind(NAME);
        VMIOUtils.resetAPI(this);
        mounter = null;
        try {
            DeviceUtils.getDeviceManager().unregister(vfsDev);
        } catch (NameNotFoundException ex) {
            log.error("Cannot find devicemanager", ex);
        } catch (DriverException ex) {
            log.error("Cannot unregister vfs device", ex);
        }
    }

    /**
     * @see org.jnode.fs.service.FileSystemService#getFileSystemType(java.lang.String)
     */
	public <T extends FileSystemType<?>> T getFileSystemType(Class<T> name) throws FileSystemException
	{
        T result = fsTypeManager.getSystemType(name);
        if (result == null)
        {
        	throw new FileSystemException("FileSystemType " + name +
        			" doesn't exist");
        }
        return result;
    }
}
