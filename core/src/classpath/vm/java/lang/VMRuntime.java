/* VMRuntime.java -- VM interface to Runtime
   Copyright (C) 2003 Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.lang;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.jnode.vm.Vm;
import org.jnode.vm.VmProcess;
import org.jnode.vm.VmSystem;

/**
 * VMRuntime represents the interface to the Virtual Machine.
 *
 * @author Jeroen Frijters
 */
final class VMRuntime
{
    /**
     * No instance is ever created.
     */
    private VMRuntime() 
    {
    }

    /**
     * Returns the number of available processors currently available to the
     * virtual machine. This number may change over time; so a multi-processor
     * program want to poll this to determine maximal resource usage.
     *
     * @return the number of processors available, at least 1
     */
    static int availableProcessors() {
        return Vm.getVm().availableProcessors();
    }

    /**
     * Find out how much memory is still free for allocating Objects on the heap.
     *
     * @return the number of bytes of free memory for more Objects
     */
    static long freeMemory() {
		return VmSystem.freeMemory();        
    }

    /**
     * Find out how much memory total is available on the heap for allocating
     * Objects.
     *
     * @return the total number of bytes of memory for Objects
     */
    static long totalMemory() {
		return VmSystem.totalMemory();
    }

    /**
     * Returns the maximum amount of memory the virtual machine can attempt to
     * use. This may be <code>Long.MAX_VALUE</code> if there is no inherent
     * limit (or if you really do have a 8 exabyte memory!).
     *
     * @return the maximum number of bytes the virtual machine will attempt
     *         to allocate
     */
    static long maxMemory() {
		return VmSystem.totalMemory();
    }

    /**
     * Run the garbage collector. This method is more of a suggestion than
     * anything. All this method guarantees is that the garbage collector will
     * have "done its best" by the time it returns. Notice that garbage
     * collection takes place even without calling this method.
     */
    static void gc() {
		VmSystem.gc();
    }

    /**
     * Run finalization on all Objects that are waiting to be finalized. Again,
     * a suggestion, though a stronger one than {@link #gc()}. This calls the
     * <code>finalize</code> method of all objects waiting to be collected.
     *
     * @see #finalize()
     */
    static void runFinalization() {
		VmSystem.gc();
    }

    /**
     * Run finalization on all finalizable Objects (even live ones). This
     * should only be called immediately prior to VM termination.
     *
     * @see #finalize()
     */
    static void runFinalizationForExit() {
        
    }

    /**
     * Tell the VM to trace every bytecode instruction that executes (print out
     * a trace of it).  No guarantees are made as to where it will be printed,
     * and the VM is allowed to ignore this request.
     *
     * @param on whether to turn instruction tracing on
     */
    static void traceInstructions(boolean on) {
        
    }

    /**
     * Tell the VM to trace every method call that executes (print out a trace
     * of it).  No guarantees are made as to where it will be printed, and the
     * VM is allowed to ignore this request.
     *
     * @param on whether to turn method tracing on
     */
    static void traceMethodCalls(boolean on) {
        
    }

    /**
     * Native method that actually sets the finalizer setting.
     *
     * @param value whether to run finalizers on exit
     */
    static void runFinalizersOnExit(boolean value) {
        
    }

    /**
     * Native method that actually shuts down the virtual machine.
     *
     * @param status the status to end the process with
     */
    static void exit(int status) {
//LS        throw new Error("Cannot exit yet");
        System.err.println("Cannot exit yet with System.exit()");
    }

    /**
     * Load a file. If it has already been loaded, do nothing. The name has
     * already been mapped to a true filename.
     *
     * @param filename the file to load
     * @return 0 on failure, nonzero on success
     */
    static int nativeLoad(String filename) {
        return 0;
    }

    /**
     * Map a system-independent "short name" to the full file name, and append
     * it to the path.
     * XXX This method is being replaced by System.mapLibraryName.
     *
     * @param pathname the path
     * @param libname the short version of the library name
     * @return the full filename
     */
    static String nativeGetLibname(String pathname, String libname) {
        return null;
    }

    /**
     * Execute a process. The command line has already been tokenized, and
     * the environment should contain name=value mappings. If directory is null,
     * use the current working directory; otherwise start the process in that
     * directory.  If env is null, then the new process should inherit
     * the environment of this process.
     *
     * @param cmd the non-null command tokens
     * @param env the environment setup
     * @param dir the directory to use, may be null
     * @return the newly created process
     * @throws NullPointerException if cmd or env have null elements
     */
    static Process exec(String[] cmd, String[] env, File dir)
	throws IOException {
		if (env == null) {
			env = new String[0];
		}

		String mainClassName = cmd[0];
		String[] cmdArgs = new String[cmd.length - 1];
		System.arraycopy(cmd, 1, cmdArgs, 0, cmdArgs.length);
		try {
			final Process p =
				VmProcess.createProcess(mainClassName, cmdArgs, env);
			if (p == null) {
				throw new IOException("Exec error");
			} else {
				return p;
			}
		} catch (Exception ex) {
			final IOException ioe = new IOException("Exec error");
			ioe.initCause(ex);
			throw ioe;
		}
    }

    /**
     * Get the system properties. This is done here, instead of in System,
     * because of the bootstrap sequence. Note that the native code should
     * not try to use the Java I/O classes yet, as they rely on the properties
     * already existing. The only safe method to use to insert these default
     * system properties is {@link Properties#setProperty(String, String)}.
     *
     * <p>These properties MUST include:
     * <dl>
     * <dt>java.version         <dd>Java version number
     * <dt>java.vendor          <dd>Java vendor specific string
     * <dt>java.vendor.url      <dd>Java vendor URL
     * <dt>java.home            <dd>Java installation directory
     * <dt>java.vm.specification.version <dd>VM Spec version
     * <dt>java.vm.specification.vendor  <dd>VM Spec vendor
     * <dt>java.vm.specification.name    <dd>VM Spec name
     * <dt>java.vm.version      <dd>VM implementation version
     * <dt>java.vm.vendor       <dd>VM implementation vendor
     * <dt>java.vm.name         <dd>VM implementation name
     * <dt>java.specification.version    <dd>Java Runtime Environment version
     * <dt>java.specification.vendor     <dd>Java Runtime Environment vendor
     * <dt>java.specification.name       <dd>Java Runtime Environment name
     * <dt>java.class.version   <dd>Java class version number
     * <dt>java.class.path      <dd>Java classpath
     * <dt>java.library.path    <dd>Path for finding Java libraries
     * <dt>java.io.tmpdir       <dd>Default temp file path
     * <dt>java.compiler        <dd>Name of JIT to use
     * <dt>java.ext.dirs        <dd>Java extension path
     * <dt>os.name              <dd>Operating System Name
     * <dt>os.arch              <dd>Operating System Architecture
     * <dt>os.version           <dd>Operating System Version
     * <dt>file.separator       <dd>File separator ("/" on Unix)
     * <dt>file.encoding        <dd>Standard encoding for text (Default 8859_1)
     * <dt>path.separator       <dd>Path separator (":" on Unix)
     * <dt>line.separator       <dd>Line separator ("\n" on Unix)
     * <dt>user.name            <dd>User account name
     * <dt>user.home            <dd>User home directory
     * <dt>user.dir             <dd>User's current working directory
     * </dl>
     *
     * @param p the Properties object to insert the system properties into
     */
    static void insertSystemProperties(Properties p) {
        VmSystem.insertSystemProperties(p);
    }
} // class VMRuntime
