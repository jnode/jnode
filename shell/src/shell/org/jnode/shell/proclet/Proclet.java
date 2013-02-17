/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import gnu.java.security.action.GetPropertiesAction;

import java.io.Closeable;
import java.security.AccessController;
import java.util.Map;
import java.util.Properties;

import org.jnode.shell.CommandRunnable;
import org.jnode.shell.CommandThread;
import org.jnode.shell.CommandThreadImpl;
import org.jnode.util.ProxyStream;
import org.jnode.vm.VmExit;
import org.jnode.vm.VmIOContext;
import org.jnode.vm.VmSystem;
import org.jnode.vm.isolate.VmIsolate;

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
public class Proclet extends ThreadGroup {
    public static final int NO_SUCH_PID = 0;
    
    private Properties properties;
    private Map<String, String> environment;
    private int threadCount;
    private final int pid;

    private int exitStatus = 0;
    private Throwable uncaughtException;

    private static int nextPid = 1;

    private Proclet(ThreadGroup parent, Properties properties,
        Map<String, String> environment, Object[] streams) throws ProcletException {
        super(parent, nextProcletName());
        Proclet parentContext = getParentContext(parent);
        if (streams == null) {
            streams = new Object[] {
                    // FIXME ... the deproxy calls are probably redundant
                    deproxy(System.in), deproxy(System.out), deproxy(System.err)
            };
        }
        if (properties == null) {
            if (parentContext != null) {
                properties = parentContext.properties;
            } else {
                // FIXME ... temporary
                properties = AccessController.doPrivileged(new GetPropertiesAction());
            }
            properties = (Properties) properties.clone();
        }
        if (environment == null) {
            if (parentContext != null) {
                environment = parentContext.environment;
            } else {
                environment = VmIOContext.getGlobalEnv();
            }
        }
        this.environment = environment;
        this.properties = properties;
        this.pid = extractPid(getName());
        ((ProcletIOContext) VmIsolate.getRoot().getIOContext()).setStreamsForNewProclet(pid, streams);
        setDaemon(true);
    }

    private Closeable deproxy(Closeable stream) {
        if (stream instanceof ProxyStream) {
            return ((ProxyStream<?>) stream).getProxiedStream();
        } else {
            return stream;
        }
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

    public synchronized void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    public synchronized void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * Get the ProcletContext for the current thread.
     * 
     * @return the context, or <code>null</code> if the current thread is not
     *         a member of a proclet.
     */
    public static Proclet currentProcletContext() {
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
    private static Proclet getParentContext(ThreadGroup threadGroup) {
        while (threadGroup != null) {
            if (threadGroup instanceof Proclet) {
                return (Proclet) threadGroup;
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
    public static CommandThread createProclet(CommandRunnable target) {
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
    public static CommandThread createProclet(CommandRunnable target, Properties properties, 
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
    public static CommandThreadImpl createProclet(CommandRunnable target,
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
    public static CommandThreadImpl createProclet(CommandRunnable target,
            Properties properties, Map<String, String> environment,
            Object[] streams, String name, long size) {
        Proclet procletContext = new Proclet(Thread
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
