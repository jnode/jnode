/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.proclet;

import java.io.Closeable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.jnode.shell.CommandThread;
import org.jnode.shell.CommandThreadImpl;
import org.jnode.vm.VmExit;
import org.jnode.vm.VmSystem;

/**
 * This class implements the proclet-specific state used in the JNode proclet
 * mechanism.
 * <p>
 * A 'proclet' is group of threads that has its own version of the system
 * properties, streams and environment and superficially behaves as if it was a
 * self-contained application.
 * 
 * @author crawley@jnode.org
 */
public class ProcletContext extends ThreadGroup {
    private Properties properties;
    private Object[] streams;
    private Map<String, String> environment;
    private int threadCount;
    private final int pid;

    private int exitStatus = 0;
    private Throwable uncaughtException;

    private static int nextPid = 1;

    private ProcletContext(ThreadGroup parent, Properties properties,
        Map<String, String> environment, Object[] streams) throws ProcletException {
        super(parent, nextProcletName());
        ProcletContext parentContext = getParentContext(parent);
        if (properties == null) {
            if (parentContext != null) {
                properties = parentContext.properties;
            }
            if (properties == null) {
                properties = AccessController
                        .doPrivileged(new PrivilegedAction<Properties>() {
                            public Properties run() {
                                return System.getProperties();
                            }
                        });
            }
            properties = (Properties) properties.clone();
        }
        if (streams == null) {
            if (parentContext != null) {
                streams = parentContext.streams;
            }
            if (streams == null) {
                try {
                    streams = new Object[] {
                        resolve(System.in), resolve(System.out), resolve(System.err)};
                } catch (ProxyStreamException ex) {
                    throw new ProcletException("Broken streams", ex);
                }
            } else {
                streams = (Object[]) streams.clone();
            }
        }
        if (environment == null) {
            if (parentContext != null) {
                environment = new HashMap<String, String>(
                        parentContext.environment);
            } else {
                environment = new HashMap<String, String>();
            }
        }
        this.environment = environment;
        this.properties = properties;
        this.streams = streams;
        this.pid = extractPid(getName());
        setDaemon(true);
    }

    private Closeable resolve(Closeable stream) throws ProxyStreamException {
        return (stream instanceof ProxyStream) ? ((ProxyStream<?>) stream)
                .getProxiedStream() : stream;
    }

    /**
     * Get the proclet's unique PID. This value uniquely identifies the proclet.
     * 
     * @return the pid
     */
    public int getPid() {
        return pid;
    }

    public synchronized Map<String, String> getEnvironment() {
        return environment;
    }

    public synchronized Properties getProperties() {
        return properties;
    }

    /**
     * Set the stream object that corresponds to a given 'fd'.
     * 
     * @param fd a non-negative index into the streams vector.
     * @param stream the stream object to set.
     */
    synchronized void setStream(int fd, Object stream) {
        if (stream instanceof ProcletProxyStream) {
            throw new IllegalArgumentException(
                    "stream is a proclet proxy stream");
        }
        if (fd < 0) {
            throw new IllegalArgumentException("fd is negative");
        }
        if (fd >= streams.length) {
            Object[] tmp = new Object[fd + 1];
            System.arraycopy(streams, 0, tmp, 0, streams.length);
            streams = tmp;
        }
        streams[fd] = stream;
    }

    /**
     * Get the stream object that corresponds to a given 'fd'.
     * 
     * @param fd a non-negative index into the streams vector.
     * @return the stream object, or <code>null</code>
     */
    public synchronized Object getStream(int fd) {
        if (fd < 0) {
            throw new IllegalArgumentException("fd is negative");
        } else if (fd > streams.length) {
            return null;
        } else {
            return streams[fd];
        }
    }

    public synchronized void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Get the ProcletContext for the current thread.
     * 
     * @return the context, or <code>null</code> if the current thread is not
     *         a member of a proclet.
     */
    public static ProcletContext currentProcletContext() {
        if (!VmSystem.hasVmIOContext()) {
            return getParentContext(Thread.currentThread().getThreadGroup());
        } else {
            return null;
        }
    }

    /**
     * Get the ProcletContext for a thread group. This is the thread group
     * itself, or the innermost enclosing parent thread group that is a
     * ProcletContect instance.
     * 
     * @param threadGroup the starting thread group
     * @return the context, or <code>null</code> if the thread group does not
     *         have an ancestor that is a ProcletContex.
     */
    private static ProcletContext getParentContext(ThreadGroup threadGroup) {
        while (threadGroup != null) {
            if (threadGroup instanceof ProcletContext) {
                return (ProcletContext) threadGroup;
            }
            threadGroup = threadGroup.getParent();
        }
        return null;
    }

    /**
     * Create a new Thread as the initial thread for a new proclet. The proclet
     * context will be initialised from the current thread's proclet context (if
     * it exists) or from {@link java.lang.System#in},
     * {@link java.lang.System#out}, {@link java.lang.System#err}, and
     * {@link java.lang.System#getProperties()}.
     * 
     * @param target the new Thread's Runnable object.
     * @return the new Thread
     */
    public static CommandThread createProclet(Runnable target) {
        return createProclet(target, null, null, null, null, 0);
    }

    /**
     * Create a new Thread as the initial thread for a new proclet, using a
     * supplied set of system properties, and environment and streams vector. If
     * any of these is null, the corresponding proclet context will be
     * initialised from the current thread's proclet context (if it exists) or
     * from {@link java.lang.System#in}, {@link java.lang.System#out},
     * {@link java.lang.System#err}, and
     * {@link java.lang.System#getProperties()}.
     * 
     * @param properties the proclet's system properties, or <code>null</code>.
     * @param environment the proclet's environment, or <code>null</code>.
     * @param streams the proclet's streams vector, or <code>null</code>.
     * @param target the new Thread's Runnable object.
     * @return the new Thread
     */
    public static CommandThread createProclet(Runnable target, Properties properties, 
            Map<String, String> environment, Object[] streams) {
        return createProclet(target, properties, environment, streams, null, 0);
    }

    /**
     * Create a new Thread as the initial thread for a new proclet, using a
     * supplied set of system properties, and environment and streams vector. If
     * any of these is null, the corresponding proclet context will be
     * initialised from the current thread's proclet context (if it exists) or
     * from {@link java.lang.System#in}, {@link java.lang.System#out},
     * {@link java.lang.System#err}, and
     * {@link java.lang.System#getProperties()}. This overload also supplies an
     * optional thread name.
     * 
     * @param properties the proclet's system properties, or <code>null</code>.
     * @param environment the proclet's environment, or <code>null</code>.
     * @param streams the proclet's streams vector, or <code>null</code>.
     * @param target the new Thread's Runnable object.
     * @param name an optional Thread name.
     * @return the new Thread
     */
    public static CommandThreadImpl createProclet(Runnable target,
            Properties properties, Map<String, String> environment,
            Object[] streams, String name) {
        return createProclet(target, properties, environment, streams, name, 0);
    }

    /**
     * Create a new Thread as the initial thread for a new proclet, using a
     * supplied set of system properties, and environment and streams vector. If
     * any of these is null, the corresponding proclet context will be
     * initialised from the current thread's proclet context (if it exists) or
     * from {@link java.lang.System#in}, {@link java.lang.System#out},
     * {@link java.lang.System#err}, and
     * {@link java.lang.System#getProperties()}. This overload also allows the
     * caller to specify a thread stack size.
     * 
     * @param properties the proclet's system properties, or <code>null</code>.
     * @param environment the proclet's environment, or <code>null</code>.
     * @param streams the proclet's streams vector, or <code>null</code>.
     * @param target the new Thread's Runnable object.
     * @param name the new Thread's name.
     * @param size the new Thread's stack size; zero denotes the default thread
     *        stack size.
     * @return the new Thread
     */
    public static CommandThreadImpl createProclet(Runnable target,
            Properties properties, Map<String, String> environment,
            Object[] streams, String name, long size) {
        ProcletContext procletContext = new ProcletContext(Thread
                .currentThread().getThreadGroup(), properties, environment,
                streams);
        if (name == null) {
            name = procletContext.autoThreadName();
        }
        return new CommandThreadImpl(procletContext, target, name, size);
    }

    /**
     * Generate a "unique" default name for a Proclet thread.
     */
    private synchronized String autoThreadName() {
        return getName() + "-" + (++threadCount);
    }

    private static synchronized String nextProcletName() {
        return "Proclet-" + nextPid++;
    }

    private static int extractPid(String name) {
        return Integer.parseInt(name.substring("Proclet-".length()));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable t) {
        if (t instanceof VmExit) {
            exitStatus = ((VmExit) t).getStatus();
        } else {
            uncaughtException = t;
        }
        super.uncaughtException(thread, t);
    }

    /**
     * Retrieves a proclet's exit status.
     * 
     * @return the status value set in System.exit(int), or zero.
     */
    public int getExitStatus() {
        return exitStatus;
    }

    /**
     * If the proclet dies with an uncaught exception, this method returns that
     * exception.
     * 
     * @return the exception, or <code>null</code>.
     */
    public Throwable getUncaughtException() {
        return uncaughtException;
    }

    /**
     * Return a human-readable String representing this ProcletContext.
     * 
     * @return a human-readable String representing this ProcletContext
     */
    public String toString() {
        return getClass().getName() + "[name=" + getName() + ",maxpri="
                + getMaxPriority() + ",pid=" + getPid() + ']';
    }

}
