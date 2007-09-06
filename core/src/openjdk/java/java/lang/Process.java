/*
 * Copyright 1995-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.lang;

import java.io.*;

/**
 * The {@link ProcessBuilder#start()} and
 * {@link Runtime#exec(String[],String[],File) Runtime.exec}
 * methods create a native process and
 * return an instance of a subclass of <code>Process</code> that can
 * be used to control the process and obtain information about it.
 * The class <code>Process</code> provides methods for performing
 * input from the process, performing output to the process, waiting
 * for the process to complete, checking the exit status of the process,
 * and destroying (killing) the process.
 *
 * <p>
 * The methods that create processes may not work well for special
 * processes on certain native platforms, such as native windowing
 * processes, daemon processes, Win16/DOS processes on Microsoft Windows, or shell
 * scripts. The created subprocess does not have its own terminal or
 * console. All its standard io (i.e. stdin, stdout, stderr)  operations
 * will be redirected to the parent process through three streams
 * ({@link #getOutputStream()},
 * {@link #getInputStream()},
 * {@link #getErrorStream()}).
 * The parent process uses these streams to feed input to and get output
 * from the subprocess. Because some native platforms only provide
 * limited buffer size for standard input and output streams, failure
 * to promptly write the input stream or read the output stream of
 * the subprocess may cause the subprocess to block, and even deadlock.
 *
 * <p>
 * The subprocess is not killed when there are no more references to 
 * the <code>Process</code> object, but rather the subprocess 
 * continues executing asynchronously.
 *
 * <p>
 * There is no requirement that a process represented by a <code>Process</code> 
 * object execute asynchronously or concurrently with respect to the Java 
 * process that owns the <code>Process</code> object.
 *
 * @author  unascribed
 * @version 1.31, 05/05/07
 * @see     ProcessBuilder
 * @see     Runtime#exec(String[], String[], File)
 * @since   JDK1.0
 */
public abstract class Process
{
    /**
     * Gets the output stream of the subprocess.
     * Output to the stream is piped into the standard input stream of 
     * the process represented by this <code>Process</code> object. 
     * <p>
     * Implementation note: It is a good idea for the output stream to 
     * be buffered.
     *
     * @return  the output stream connected to the normal input of the
     *          subprocess.
     */
    abstract public OutputStream getOutputStream();

    /**
     * Gets the input stream of the subprocess.
     * The stream obtains data piped from the standard output stream 
     * of the process represented by this <code>Process</code> object. 
     * <p>
     * Implementation note: It is a good idea for the input stream to 
     * be buffered.
     *
     * @return  the input stream connected to the normal output of the
     *          subprocess.
     * @see ProcessBuilder#redirectErrorStream()
     */
    abstract public InputStream getInputStream();

    /**
     * Gets the error stream of the subprocess.
     * The stream obtains data piped from the error output stream of the 
     * process represented by this <code>Process</code> object. 
     * <p>
     * Implementation note: It is a good idea for the input stream to be 
     * buffered.
     *
     * @return  the input stream connected to the error stream of the
     *          subprocess.
     * @see ProcessBuilder#redirectErrorStream()
     */
    abstract public InputStream getErrorStream();

    /**
     * causes the current thread to wait, if necessary, until the 
     * process represented by this <code>Process</code> object has 
     * terminated. This method returns 
     * immediately if the subprocess has already terminated. If the
     * subprocess has not yet terminated, the calling thread will be
     * blocked until the subprocess exits.
     *
     * @return     the exit value of the process. By convention, 
     *             <code>0</code> indicates normal termination.
     * @exception  InterruptedException  if the current thread is 
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    abstract public int waitFor() throws InterruptedException;

    /**
     * Returns the exit value for the subprocess.
     *
     * @return  the exit value of the subprocess represented by this 
     *          <code>Process</code> object. by convention, the value 
     *          <code>0</code> indicates normal termination.
     * @exception  IllegalThreadStateException  if the subprocess represented 
     *             by this <code>Process</code> object has not yet terminated.
     */
    abstract public int exitValue();

    /**
     * Kills the subprocess. The subprocess represented by this 
     * <code>Process</code> object is forcibly terminated.
     */
    abstract public void destroy();
}
