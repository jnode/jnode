/*
 * $Id$
 */
package org.jnode.fs.service.def;

import java.io.VMFileSystemAPI;
import java.io.VMIOUtils;
import java.util.Collection;

import javax.naming.NamingException;

import org.jnode.driver.Device;
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
public class FileSystemPlugin extends Plugin implements FileSystemService {

	/** Manager of fs types */
	private final FileSystemTypeManager tm;
	/** Manager of mounted filesystems */
	private final FileSystemManager fsm;
	/** The FS-API implementation */
	private final VMFileSystemAPI api;
	/** The mounter */
	private FileSystemMounter mounter;

	/**
	 * Create a new instance
	 *  
	 */
	public FileSystemPlugin(PluginDescriptor descriptor) {
		super(descriptor);
		this.tm = new FileSystemTypeManager(descriptor.getExtensionPoint("types"));
		this.fsm = new FileSystemManager();
		this.api = new FileSystemAPIImpl(fsm);
		fsm.initialize();
	}

	/**
	 * Gets all registered file system types. All instances of the returned
	 * collection are instanceof FileSystemType.
	 */
	public Collection fileSystemTypes() {
		return tm.fileSystemTypes();
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
	 * @param fs
	 */
	public void unregisterFileSystem(FileSystem fs) {
		fsm.unregisterFileSystem(fs);
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
	public Collection fileSystems() {
		return fsm.fileSystems();
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
			VMIOUtils.setAPI(getApi(), this);
			mounter = new FileSystemMounter();
			InitialNaming.bind(NAME, this);
			mounter.start();
		} catch (NamingException ex) {
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
	}

}
