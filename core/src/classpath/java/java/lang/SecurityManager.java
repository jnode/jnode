// java.lang.SecurityManager
// (c) 1997 Peter Nagy
// API version: 1.0.2
// History:
//  1997-02-01 Initial version	Peter Nagy
//  1997-02-25 modified		Glynn Clements <glynn@sensei.co.uk>

package java.lang;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;
import java.security.SecurityPermission;

import org.jnode.vm.VmSystem;

public abstract class SecurityManager {
	protected boolean inCheck = false;

	// helper functions - protected

	protected Class[] getClassContext() {
		return VmSystem.getClassContext();
	}

	protected int classDepth(String name) {
		Class[] stack = getClassContext();
		for (int i = 0; i < stack.length; i++)
			if (stack[i].getName().equals(name))
				return i;
		return -1;
	}

	protected boolean inClass(String name) {
		return classDepth(name) >= 0;
	}

	protected int classLoaderDepth() {
		Class[] stack = getClassContext();
		for (int i = 0; i < stack.length; i++)
			if (stack[i].getClassLoader() != null)
				return i;
		return -1;
	}

	protected ClassLoader currentClassLoader() {
		Class[] stack = getClassContext();
		for (int i = 0; i < stack.length; i++) {
			ClassLoader loader = stack[i].getClassLoader();
			if (loader != null)
				return loader;
		}
		return null;
	}

	protected boolean inClassLoader() {
		return currentClassLoader() != null;
	}

	// miscellaneous functions

	public boolean getInCheck() {
		return inCheck;
	}

	public Object getSecurityContext() {
		return null;
	}

	// now for the paranoic default - subclass it!

	public boolean checkTopLevelWindow(Object window) {
		return false;
	}

	public void checkRead(String file, Object context) {
		throw new SecurityException();
	}

	public void checkRead(FileDescriptor fd) {
		throw new SecurityException();
	}

	public void checkRead(String file) {
		throw new SecurityException();
	}

	public void checkWrite(FileDescriptor fd) {
		throw new SecurityException();
	}

	public void checkWrite(String file) {
		throw new SecurityException();
	}

	public void checkListen(int port) {
		throw new SecurityException();
	}

	public void checkAccept(String host, int port) {
		throw new SecurityException();
	}

	public void checkConnect(String host, int port) {
		throw new SecurityException();
	}

	public void checkConnect(String host, int port, Object context) {
		throw new SecurityException();
	}

	public void checkDelete(String file) {
		throw new SecurityException();
	}

	public void checkExec(String cmd) {
		throw new SecurityException();
	}

	public void checkAccess(Thread t) {
		throw new SecurityException();
	}

	public void checkAccess(ThreadGroup g) {
		throw new SecurityException();
	}

	public void checkCreateClassLoader() {
		throw new SecurityException();
	}

	public void checkExit(int status) {
		throw new SecurityException();
	}

	public void checkLink(String lib) {
		throw new SecurityException();
	}

	public void checkMulticast(InetAddress addr) {
	}

	public void checkMulticast(InetAddress addr, byte ttl) {
	}

	public void checkPackageAccess(String pkg) {
		throw new SecurityException();
	}

	public void checkPackageDefinition(String pkg) {
		throw new SecurityException();
	}

	public void checkPropertyAccess(String key) {
		throw new SecurityException();
	}

	public void checkPropertyAccess(String key, String def) {
		throw new SecurityException();
	}

	public void checkPropertiesAccess() {
		throw new SecurityException();
	}

	public void checkSetFactory() {
		throw new SecurityException();
	}

	/**
	 * Check if the current thread is allowed to perform an operation that
	 * requires the specified <code>Permission</code>. This defaults to
	 * <code>AccessController.checkPermission</code>.
	 *
	 * @param perm the <code>Permission</code> required
	 * @throws SecurityException if permission is denied
	 * @throws NullPointerException if perm is null
	 * @since 1.2
	 */
	public void checkPermission(Permission perm) {
		// XXX Should be: AccessController.checkPermission(perm);
		throw new SecurityException("Operation not allowed");
	}

	/**
	 * Test whether a particular security action may be taken. The default
	 * implementation checks <code>SecurityPermission(action)</code>. If you
	 * override this, call <code>super.checkSecurityAccess</code> rather than
	 * throwing an exception.
	 *
	 * @param action the desired action to take
	 * @throws SecurityException if permission is denied
	 * @throws NullPointerException if action is null
	 * @throws IllegalArgumentException if action is ""
	 * @since 1.1
	 */
	public void checkSecurityAccess(String action) {
		checkPermission(new SecurityPermission(action));
	}

	protected SecurityManager() {
		if (System.getSecurityManager() != null)
			throw new SecurityException("SecurityManager already installed");
	}
}
