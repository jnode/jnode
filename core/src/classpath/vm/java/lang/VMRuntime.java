/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package java.lang;

import java.io.File;
import java.io.IOException;

import org.jnode.vm.Vm;
import org.jnode.vm.VmProcess;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmExit;
import org.jnode.vm.isolate.VmIsolate;
import javax.isolate.Isolate;

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
        if(VmIsolate.getRoot() == VmIsolate.currentIsolate()){
            throw new VmExit(status);
        } else {
            VmIsolate.currentIsolate().systemExit(Isolate.currentIsolate(), status);
        }
    }

     /**
     * Native method that actually shuts down the virtual machine.
     *
     * @param status the status to end the process with
     */
    static void halt(int status) {
        if(VmIsolate.getRoot() == VmIsolate.currentIsolate()){
            throw new VmExit(status);
        } else {
            VmIsolate.currentIsolate().systemHalt(Isolate.currentIsolate(), status);
        }
    }

    /**
     * Load a file. If it has already been loaded, do nothing. The name has
     * already been mapped to a true filename.
     *
     * @param filename the file to load
     * @param loader class loader, or <code>null</code> for the boot loader
     * @return 0 on failure, nonzero on success
     */
    static int nativeLoad(String filename, ClassLoader loader) {
        //todo review
        //for new we assume success since there are no native libs on jnode
        return 1;
    }

    /**
     * Map a system-independent "short name" to the full file name, and append
     * it to the path.
     * XXX This method is being replaced by System.mapLibraryName.
     *
     * @param libname the path
     * @param libname the short version of the library name
     * @return the full filename
     */
    static String mapLibraryName(String libname) {
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
     * This method is called by Runtime.addShutdownHook() when it is
     * called for the first time. It enables the VM to lazily setup
     * an exit handler, should it so desire.
     */
    static void enableShutdownHooks()
    {
    }
} // class VMRuntime
