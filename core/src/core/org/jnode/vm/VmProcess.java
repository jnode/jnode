/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.jnode.vm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author epr
 */
public class VmProcess extends Process {

	/** Identifier of this process */
	private final int id;
	/** Last used process identifier */
	private static int lastId = 1;
	/** Root thread group for this process */
	private final ThreadGroup threadGroup;
	/** Exit code */	
	private int exitValue;
	/** Is this process still running */
	private boolean running;
	final String mainClassName;
	final String[] args;
	private InputStream in;
	private InputStream err;
	private OutputStream out;
	private static Process rootProcess;

	/**
	 * Create a new process
	 *
	 * @param mainClassName
	 * @param args
	 * @param in
	 * @param out
	 * @param err
	 */	
	public VmProcess(String mainClassName, String[] args, InputStream in, PrintStream out, PrintStream err) {
		synchronized (getClass()) {
			this.id = lastId++;
		}
		this.running = true;
		this.threadGroup = new ThreadGroup("Process-" + id, this);
		this.mainClassName = mainClassName;
		if (args == null) {
			this.args = new String[0]; 
		} else {
			this.args = new String[args.length];
			System.arraycopy(args, 0, this.args, 0, args.length);
		}
		
		if (System.in == null) {
			Unsafe.debug("Set System.in.");
			System.setIn(in);
		}
		if (System.out == null) {
			Unsafe.debug("Set System.out.");
			System.setOut(out);
		}
		if (System.err == null) {
			Unsafe.debug("Set System.err.");
			System.setErr(err);
		}
		
		final Thread mainThread = new Thread(threadGroup, new ProcessRunner());
		mainThread.start();	
	}
	
	private VmProcess(ThreadGroup rootGroup) {
		synchronized (getClass()) {
			this.id = lastId++;
		}
		this.running = true;
		this.threadGroup = rootGroup;
		this.mainClassName = "system";
		this.args = new String[0];
	}
	
	/**
	 * Create and run a new process in its own classloader.
	 * @param mainClassName
	 * @param args
	 * @param envp
	 * @return The created process
	 * @throws Exception
	 */
	public static Process createProcess(String mainClassName, String[] args, String[] envp) 
	throws Exception {
		final ClassLoader cl = new VmProcessClassLoader();
		final Class processClass = cl.loadClass(VmProcess.class.getName());
		final Class[] argTypes = new Class[] { 
			String.class,
			String[].class,
			InputStream.class,
			PrintStream.class,
			PrintStream.class
		};
		final Constructor cons = processClass.getConstructor(argTypes);
		final Object[] consArgs = new Object[] {
			mainClassName, 
			args,
			System.in,
			System.out,
			System.err
		};
		final Process proc = (Process)cons.newInstance(consArgs);
		return proc;
	}
	
	public static Process getRootProcess(ThreadGroup group) {
		if (rootProcess == null) {
			rootProcess = new VmProcess(group);
		}
		return rootProcess;
	}
	
	/**
	 * @see java.lang.Process#destroy()
	 */
	public void destroy() {
		exit(1);
	}

	/**
	 * @see java.lang.Process#exitValue()
	 * @return The exit value
	 * @throws IllegalThreadStateException
	 */
	public int exitValue() throws IllegalThreadStateException {
		return exitValue;
	}

	/**
	 * @see java.lang.Process#getErrorStream()
	 * @return The error stream
	 */
	public InputStream getErrorStream() {
		return err;
	}

	/**
	 * @see java.lang.Process#getInputStream()
	 * @return The input stream
	 */
	public InputStream getInputStream() {
		return in;
	}

	/**
	 * @see java.lang.Process#getOutputStream()
	 * @return The output stream
	 */
	public OutputStream getOutputStream() {
		return out;
	}
	
	/**
	 * Stop this process
	 * @param exitValue
	 */
	protected synchronized void exit(int exitValue) {
		this.exitValue = exitValue;
		this.running = false;
		notifyAll();
	}

	/**
	 * @see java.lang.Process#waitFor()
	 * @return The exit value
	 * @throws InterruptedException
	 */
	public synchronized int waitFor() throws InterruptedException {
		while (running) {
			wait();
		}
		return exitValue;
	}
	
	/**
	 * Class used as new process thread.
	 * @author epr
	 */
	class ProcessRunner
	implements Runnable {
		
		/**
		 * Run the process
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				final Class mainClass = Class.forName(mainClassName);
				final Method mainMethod = mainClass.getMethod("main", new Class[] { String[].class });
		
				try {		
					mainMethod.invoke(null, new Object[] { args });
				} catch (InvocationTargetException ex) {
					ex.getTargetException().printStackTrace();		
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}

}
