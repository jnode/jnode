/*
 * $Id$
 */
package java.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.jnode.vm.HeapManager;
import org.jnode.vm.VmProcess;

/**
 * Runtime
 * @author epr
 */
public class Runtime {
	
	/** The single instance */
	private static final Runtime runtime = new Runtime();

	/**
	 * Gets the current runtime
	 * @return
	 */
	public static Runtime getRuntime() {
		return runtime;
	}

	/**
	 * Exit the current process.
	 * @param status
	 */
	public void exit(int status) {
		SecurityManager s = System.getSecurityManager();
		if (s != null) {
			s.checkExit(status);
		}
		Thread.currentThread().getThreadGroup().getProcess().exit(status);
	}

	/**
	 * Create and start a new process.
	 * @param cmdarray
	 * @param envp
	 * @return
	 * @throws IOException
	 */
	public Process exec(String[] cmdarray, String[] envp) throws IOException {
		String command = cmdarray[0];

		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkExec(command);

		if (envp == null) {
			envp = new String[0];
		}

		String mainClassName = cmdarray[0];
		String[] cmdArgs = new String[cmdarray.length - 1];
		System.arraycopy(cmdarray, 1, cmdArgs, 0, cmdArgs.length);
		try {
			final Process p =
				VmProcess.createProcess(mainClassName, cmdArgs, envp);
			if (p == null) {
				throw new IOException("Exec error");
			} else {
				return p;
			}
		} catch (Exception ex) {
			throw new IOException("Exec error", ex);
		}
	}

	public Process exec(String[] cmdarray) throws IOException {
		return exec(cmdarray, null);
	}

	public Process exec(String command, String[] envp) throws IOException {
		StringTokenizer tokenizer = new StringTokenizer(command);
		String[] cmdarray = new String[tokenizer.countTokens()];
		for (int n = 0; tokenizer.hasMoreTokens(); n++)
			cmdarray[n] = tokenizer.nextToken();
		return exec(cmdarray, envp);
	}

	public Process exec(String command) throws IOException {
		return exec(command, null);
	}

	/**
	 * Load a native library.
	 * @throw UnsatisfiedLinkError Always thrown on JNode.
	 * @param filename
	 */
	public void load(String filename) {
		final SecurityManager s = System.getSecurityManager();
		if (s != null) {
			s.checkLink(filename);
		}

		throw new UnsatisfiedLinkError(filename);
	}

	/**
	 * Load a native library.
	 * @throw UnsatisfiedLinkError Always thrown on JNode.
	 * @param filename
	 */
	public void loadLibrary(String libname) {
		final SecurityManager s = System.getSecurityManager();
		if (s != null) {
			s.checkLink(libname);
		}

		throw new UnsatisfiedLinkError(libname);
	}

	public long freeMemory() {
		return HeapManager.freeMemory();
	}

	public long totalMemory() {
		return HeapManager.totalMemory();
	}

	public void gc() {
		HeapManager.gc();
	}

	public void runFinalization() {
		HeapManager.gc();
	}

	/**
	 * Enables/Disables tracing of instructions. 
	 * If the boolean argument is true, this method suggests that the 
	 * Java virtual machine emit debugging information for each 
	 * instruction in the virtual machine as it is executed. 
	 * The format of this information, and the file or other output
	 * stream to which it is emitted, depends on the host environment. 
	 * The virtual machine may ignore this request if it does not support 
	 * this feature. The destination of the trace output is system dependent.
	 * 
	 * If the boolean argument is false, this method causes the virtual 
	 * machine to stop performing the detailed instruction trace it is 
	 * performing. 
	 * @param on
	 */
	public void traceInstructions(boolean on) {
		// Not implemented
	}

	/**
	 * Enables/Disables tracing of method calls. 
	 * If the boolean argument is true, this method suggests that the Java 
	 * virtual machine emit debugging information for each method in the 
	 * virtual machine as it is called. The format of this information, 
	 * and the file or other output stream to which it is emitted, depends 
	 * on the host environment. The virtual machine may ignore this request 
	 * if it does not support this feature.
	 * 
	 * Calling this method with argument false suggests that the virtual
	 * machine cease emitting per-call debugging information. 
	 * @param on
	 */
	public void traceMethodCalls(boolean on) {
		// Not implemented
	}

	public InputStream getLocalizedInputStream(InputStream in) {
		return in; // TODO: implement this properly
	}

	public OutputStream getLocalizedOutputStream(OutputStream out) {
		return out; // TODO: implement this properly
	}

	private Runtime() {
	}
}
