
// / / (C) 1996 Glynn Clements <glynn@sensei.co.uk> - Freely Redistributable

package java.lang;

import java.util.Enumeration;
import java.util.Vector;

import org.jnode.vm.VmProcess;

public class ThreadGroup {

	private static ThreadGroup rootGroup;
	private boolean daemon;
	private boolean destroyed = false;
	private Vector groups = new Vector();
	private int maxPriority;
	private String name;
	private ThreadGroup parent;
	private Vector threads = new Vector();
	private final Process process;

	/**
	 * A private noarg-constructor used only by the VM to avoid chicken-and-the-egg problems when
	 * trying to start up the first Thread and ThreadGroup.
	 */
	private ThreadGroup() {
		this.parent = null;
		this.name = "Root Thread Group";
		this.maxPriority = java.lang.Thread.MAX_PRIORITY;
		this.daemon = false;
		this.process = VmProcess.getRootProcess(this);
	}

	protected static ThreadGroup getRootGroup() {
		if (rootGroup == null) {
			rootGroup = new ThreadGroup();
		}
		return rootGroup;
	}

	public ThreadGroup(String name) {
		this(Thread.currentThread().getThreadGroup(), name);
	}

	public ThreadGroup(String name, Process newProcess) {
		this(Thread.currentThread().getThreadGroup(), name, newProcess);
	}

	public ThreadGroup(ThreadGroup parent, String name) {
		this(parent, name, parent.getProcess());
	}

	public ThreadGroup(ThreadGroup parent, String name, Process newProcess) {
		ThreadGroup current = Thread.currentThread().getThreadGroup();

		current.checkAccess();

		if (parent == null) {
			throw new NullPointerException("parent ThreadGroup not specified");
		}

		if (parent.destroyed) {
			throw new IllegalThreadStateException("parent ThreadGroup has been destroyed");
		}

		this.parent = parent;
		this.name = name;
		this.maxPriority = parent.maxPriority;
		this.daemon = parent.daemon;
		this.process = newProcess;
	}

	public int activeCount() {
		return allThreadsCount();
	}

	public int activeGroupCount() {
		return allGroupsCount();
	}

	/**
	 * Add a thread to this group.
	 * 
	 * @param thread
	 */
	void add(Thread thread) {
		if (destroyed) {
			throw new IllegalThreadStateException("ThreadGroup destroyed");
		}
		threads.addElement(thread);
	}

	public ThreadGroup[] allGroups() {
		ThreadGroup[] result = new ThreadGroup[allGroupsCount()];

		ThreadGroup[] g = groups();
		System.arraycopy(g, 0, result, 0, g.length);

		Enumeration e = groups.elements();
		for (int n = g.length; e.hasMoreElements(); n++) {
			ThreadGroup group = (ThreadGroup) e.nextElement();
			g = group.groups();
			System.arraycopy(g, 0, result, n, g.length);
			n += g.length;
		}

		return result;
	}

	public int allGroupsCount() {
		int n = groupsCount();

		Enumeration e = groups.elements();
		while (e.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) e.nextElement();
			n += g.allGroupsCount();
		}

		return n;
	}

	public Thread[] allThreads() {
		Thread[] result = new Thread[allThreadsCount()];

		Thread[] t = threads();
		System.arraycopy(t, 0, result, 0, t.length);

		Enumeration e = groups.elements();
		for (int n = t.length; e.hasMoreElements(); n++) {
			ThreadGroup group = (ThreadGroup) e.nextElement();
			t = group.threads();
			System.arraycopy(t, 0, result, n, t.length);
			n += t.length;
		}

		return result;
	}

	public int allThreadsCount() {
		int n = threadsCount();

		Enumeration e = groups.elements();
		while (e.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) e.nextElement();
			n += g.allThreadsCount();
		}

		return n;
	}

	public final void checkAccess() {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null)
			sm.checkAccess(this);
	}

	public final void destroy() {
		checkAccess();

		if (destroyed)
			throw new IllegalThreadStateException("ThreadGroup has already been destroyed");

		if (threads.size() > 0)
			throw new IllegalThreadStateException("ThreadGroup has Threads remaining");

		Enumeration e = groups.elements();
		while (e.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) e.nextElement();
			g.destroy();
		}

		parent.remove(this);
		destroyed = true;
	}
	
	/**
	 * Is this object destroyed?
	 * @return
	 */
	public boolean isDestroyed() {
		return destroyed;
	}

	public int enumerate(Thread[] list) {
		return enumerate(list, false);
	}

	public int enumerate(Thread[] list, boolean recurse) {
		Thread[] threads = recurse ? allThreads() : threads();
		int n = Math.min(threads.length, list.length);
		System.arraycopy(threads, 0, list, 0, n);
		return n;
	}

	public int enumerate(ThreadGroup[] list) {
		return enumerate(list, false);
	}

	public int enumerate(ThreadGroup[] list, boolean recurse) {
		ThreadGroup[] groups = recurse ? allGroups() : groups();
		int n = Math.min(groups.length, list.length);
		System.arraycopy(groups, 0, list, 0, n);
		return n;
	}

	public final int getMaxPriority() {
		return maxPriority;
	}

	public final String getName() {
		return name;
	}

	public final ThreadGroup getParent() {
		return parent;
	}

	public ThreadGroup[] groups() {
		ThreadGroup[] result = new ThreadGroup[groupsCount()];

		Enumeration e = groups.elements();
		for (int n = 0; e.hasMoreElements(); n++)
			result[n] = (ThreadGroup) e.nextElement();

		return result;
	}

	public int groupsCount() {
		return groups.size();
	}

	public final boolean isDaemon() {
		return daemon;
	}

	public void list() {
		list("");
	}

	void list(String pad) {
		System.out.println(pad + this);
		pad += "    ";

		Enumeration et = threads.elements();
		while (et.hasMoreElements()) {
			Thread t = (Thread) et.nextElement();
			System.out.println(pad + t);
		}

		Enumeration eg = groups.elements();
		while (eg.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) eg.nextElement();
			g.list(pad);
		}
	}

	public final boolean parentOf(ThreadGroup g) {
		return (this == g) || parentOf(g.getParent());
	}

	void remove(Thread thread) {
		if (destroyed) {
			throw new IllegalThreadStateException("ThreadGroup destroyed");
		}
		threads.removeElement(thread);
		if (daemon && (threadsCount() == 0) && (groupsCount() == 0)) {
			destroy();
		}
	}

	void remove(ThreadGroup group) {
		if (destroyed)
			throw new IllegalThreadStateException("ThreadGroup destroyed");
		groups.removeElement(group);
		if (daemon && (threadsCount() == 0) && (groupsCount() == 0))
			destroy();
	}

	/**
	 * Resume this group of threads
	 * @deprecated
	 */
	public final void resume() {
		checkAccess();

		Enumeration et = threads.elements();
		while (et.hasMoreElements()) {
			Thread t = (Thread) et.nextElement();
			t.resume();
		}

		Enumeration eg = groups.elements();
		while (eg.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) et.nextElement();
			g.resume();
		}
	}

	public final void setDaemon(boolean daemon) {
		checkAccess();
		this.daemon = daemon;
	}

	public final void setMaxPriority(int n) {
		checkAccess();

		if (n < Thread.MIN_PRIORITY || n > Thread.MAX_PRIORITY)
			throw new IllegalArgumentException("Invalid priority: " + n);

		if (parent != null && n > parent.maxPriority)
			n = parent.maxPriority;

		maxPriority = n;

		Enumeration e = groups.elements();
		while (e.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) e.nextElement();
			g.setMaxPriority(n);
		}
	}

	/**
	 * Stop this group of threads.
	 * @deprecated
	 */
	public final void stop() {
		checkAccess();

		Enumeration et = threads.elements();
		while (et.hasMoreElements()) {
			Thread t = (Thread) et.nextElement();
			t.stop();
		}

		Enumeration eg = groups.elements();
		while (eg.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) eg.nextElement();
			g.stop();
		}
	}

	/**
	 * Used by the VM, ignored here.
	 * @param b
	 * @return
	 * @deprecated
	 */
	public boolean allowThreadSuspension(boolean b) {
		// Ignore
		return true;
	}
	
	/**
	 * Suspend this group of threads.
	 * @deprecated
	 */
	public final void suspend() {
		checkAccess();

		Enumeration et = threads.elements();
		while (et.hasMoreElements()) {
			Thread t = (Thread) et.nextElement();
			t.suspend();
		}

		Enumeration eg = groups.elements();
		while (eg.hasMoreElements()) {
			ThreadGroup g = (ThreadGroup) eg.nextElement();
			g.suspend();
		}
	}

	public Thread[] threads() {
		Thread[] result = new Thread[threadsCount()];

		Enumeration e = threads.elements();
		for (int n = 0; e.hasMoreElements(); n++)
			result[n] = (Thread) e.nextElement();

		return result;
	}

	public int threadsCount() {
		return threads.size();
	}

	public String toString() {
		return getClass().getName() + "[name=" + name + ",maxpri=" + maxPriority + "]";
	}

	public void uncaughtException(Thread t, Throwable e) {
		if (parent != null)
			parent.uncaughtException(t, e);
		else if (!(e instanceof ThreadDeath))
			e.printStackTrace();
	}

	/**
	 * @return
	 */
	public final Process getProcess() {
		return process;
	}
}
