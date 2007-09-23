/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.isolate;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Vector;
import java.util.ArrayList;

import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;
import javax.isolate.Link;
import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginManager;
import org.jnode.util.BootableHashMap;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;
import org.jnode.vm.*;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.PrivilegedActionPragma;
import org.jnode.vm.annotation.SharedStatics;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.isolate.link.VmDataLink;

/**
 * VM specific implementation of the Isolate class.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmIsolate {

    /**
     * The Isolate object that this object implements.
     */
    private final Isolate isolate;

    /**
     * The classname of the main class.
     */
    private final String mainClass;

    /**
     * The arguments to the main method.
     */
    private final String[] args;

    /**
     * The isolate the started this isolate.
     */
    private VmIsolate starter;

    /**
     * The isolate that created this isolate.
     */
    private final VmIsolate creator;

    /**
     * The state of this isolate.
     */
    private State state = State.CREATED;

    /**
     * The root of the threadgroups for this isolate.
     */
    private ThreadGroup threadGroup;

    /**
     * IO bindings
     */
    private final VmStreamBindings bindings;

    /**
     * Mapping between internal VmType and per isolate Class instance.
     */
    private BootableHashMap<VmType< ? >, Class< ? >> classesMap = new BootableHashMap<VmType< ? >, Class< ? >>();

    /**
     * Isolated Statics table for this isolate
     */
    private final VmIsolatedStatics isolatedStaticsTable;

    /**
     * System classloader for this isolate
     */
    private ClassLoader systemClassLoader;
    
    /**
     * Links passed to the start of this isolate
     */
    private VmDataLink[] dataLinks;
    
    /**
     * The isolate-specific default IO context
     */
    private final IOContext vmIoContext = new VmIOContext();
    
    /**
     * The isolate-specific switchable IO context
     */
    private IOContext ioContext = vmIoContext;

    private Properties initProperties;

    /**
     * Isolate states.
     * 
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    @SharedStatics
    private enum State {
        CREATED, STARTING, STARTED, EXITED, TERMINATED
    }

    public static boolean walkIsolates(ObjectVisitor visitor){
        for(int i = 0; i < StaticData.isolates.size(); i ++){
            VmIsolate isolate = StaticData.isolates.get(i);
            if(!isolate.isolatedStaticsTable.walk(visitor))
                return false;
        }
        return true;
    }
    /**
     * Static data of the VMIsolate class.
     * 
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    @SharedStatics
    private static final class StaticData {
        /** The root (aka system) isolate. */
        private static transient VmIsolate rootIsolate;
        /** Non-root isolates. */
        private static final ArrayList<VmIsolate> isolates = new ArrayList<VmIsolate>();

        static final VmIsolate getRoot() {
            if (rootIsolate == null) {
                rootIsolate = new VmIsolate();
            }
            return rootIsolate;
        }
    }

    /**
     * Isolate specific static data
     * 
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    private static class IsolatedStaticData {
        /** The current isolate. */
        static VmIsolate current;

        /** Types of the arguments of the main(String[]) method */
        static final Class[] mainTypes = new Class[] { String[].class };
    }

    /**
     * Constructor for the root isolate.
     */
    private VmIsolate() {
        this.isolate = new Isolate(this);
        this.mainClass = null;
        this.args = null;
        this.bindings = new VmStreamBindings();
        this.state = State.STARTED;
        this.threadGroup = getRootThreadGroup();
        this.creator = null;
        this.isolatedStaticsTable = null;

        // Initialize currentHolder
        IsolatedStaticData.current = this;
    }

    /**
     * Initialize this instance.
     * 
     * @param isolate
     * @param mainClass
     * @param args
     * @param bindings
     * @param properties
     */
    public VmIsolate(Isolate isolate, VmStreamBindings bindings,
            Properties properties, String mainClass, String[] args) {
        StaticData.isolates.add(this);
        this.initProperties = properties;
        this.isolate = isolate;
        this.mainClass = mainClass;
        this.args = args;
        this.bindings = bindings;
        final VmArchitecture arch = Vm.getArch();
        this.isolatedStaticsTable = new VmIsolatedStatics(VmMagic
                .currentProcessor().getIsolatedStatics(), arch,
                new Unsafe.UnsafeObjectResolver());
        this.creator = currentIsolate();
    }

    /**
     * Is the current thread running in the root isolate
     */
    public static boolean isRoot() {
        VmIsolate result = IsolatedStaticData.current;
        if (result != null) {
            return (result == StaticData.getRoot());
        }
        return true;
    }

    /**
     * Is the current thread running in the root isolate
     */
    public static VmIsolate getRoot() {
        return StaticData.getRoot();
    }

    /**
     * Gets the current isolate.
     * 
     * @return
     */
    public static VmIsolate currentIsolate() {
        VmIsolate result = IsolatedStaticData.current;
        if (result == null) {
            result = StaticData.getRoot();
        }
        return result;
    }
    
    /**
     * Gets the isolate specific Class for the given type.
     */
    @SuppressWarnings("unchecked")
    public final <T> Class<T> getClassForType(VmType<T> type) {
        Class<T> result = (Class<T>) classesMap.get(type);
        if (result == null) {
            synchronized (classesMap) {
                result = (Class<T>) classesMap.get(type);
                if (result == null) {
                    result = new Class<T>(type);
                    classesMap.put(type, result);
                }
            }
        }
        return result;
    }

    /**
     * Gets the isolate object that this object implements.
     * 
     * @return
     */
    public final Isolate getIsolate() {
        return isolate;
    }

    /**
     * Request normal termination of this isolate.
     * 
     * @param status
     */
    public final void exit(Isolate isolate, int status) {
        testIsolate(isolate);
        state = State.EXITED;
        StaticData.isolates.remove(this);
    }

    /**
     * Force termination of this isolate.
     * 
     * @param status
     */
    @SuppressWarnings("deprecation")
    public final void halt(Isolate isolate, int status) {
        testIsolate(isolate);
        switch (state) {
        case STARTED:
            threadGroup.stop();
            break;
        }

        this.state = State.TERMINATED;
        StaticData.isolates.remove(this);
    }

    /**
     * Has this isolate reached the exited state.
     * 
     * @return
     */
    public final boolean hasExited() {
        switch (state) {
        case EXITED:
        case TERMINATED:
            return true;
        default:
            return false;
        }
    }

    /**
     * Has this isolate reached the terminated state.
     * 
     * @return
     */
    public final boolean hasTerminated() {
        switch (state) {
        case TERMINATED:
            return true;
        default:
            return false;
        }
    }

    /**
     * Has this isolate reached the started state.
     * 
     * @return
     */
    public final boolean hasStarted() {
        switch (state) {
        case STARTED:
        case EXITED:
        case TERMINATED:
            return true;
        default:
            return false;
        }
    }
    
    /**
     * Gets the links passed to the start of the current isolate.
     */
    public final static Link[] getLinks() {
        final VmDataLink[] vmLinks = currentIsolate().dataLinks;
        
        if ((vmLinks == null) || (vmLinks.length == 0)) {
            return new Link[0];
        } else {
            Link[] links = new Link[vmLinks.length];
            int i = 0;
            for (VmDataLink vmLink : vmLinks) {
                links[i++] = vmLink.asLink();
            }
            return links;
        }
    }

    /**
     * Start this isolate.
     * 
     * @param messages
     * @throws IsolateStartupException
     */
    @PrivilegedActionPragma
    public final void start(Isolate isolate, Link[] links)
            throws IsolateStartupException {
        testIsolate(isolate);
        // The creator of this isolate must be the same as the current isolate
        if (creator != currentIsolate()) {
            throw new IllegalStateException(
                    "Creator is different from current isolate");
        }

        synchronized (this) {
            // The state must be CREATED
            if (state != State.CREATED) {
                throw new IllegalStateException(
                        "Isolate has already been started");
            }
            this.state = State.STARTING;
        }

        // Save starter
        this.starter = currentIsolate();
        
        // Save links
        this.dataLinks = null;
        if (links != null) {
            VmDataLink[] vmLinks = new VmDataLink[links.length];
            int i = 0;
            for (Link link : links) {
                if (!link.isOpen()) {
                    throw new IsolateStartupException("Link is closed");
                }
                vmLinks[i] = VmDataLink.fromLink(link);
            }
            this.dataLinks = vmLinks;
        }

        // Create a new ThreadGroup
        this.threadGroup = new ThreadGroup(StaticData.getRoot().threadGroup,
                mainClass);

        // Find plugin manager
        PluginManager piManager;
        try {
            piManager = InitialNaming.lookup(PluginManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new IsolateStartupException("Cannot find PluginManager", ex);
        }

        // Create I/O channels
        final PrintStream stdout;
        final PrintStream stderr;
        final InputStream stdin;
        try {
            stdout = bindings.createIsolatedOut();
            stderr = bindings.createIsolatedErr();
            stdin = bindings.createIsolatedIn();
        } catch (IOException ex) {
            throw new IsolateStartupException("Failed to create I/O streams",
                    ex);
        }

        // Create the main thread
        final IsolateThread mainThread = new IsolateThread(threadGroup, this,
                piManager, stdout, stderr, stdin);

        // Update the state of this isolate.
        this.state = State.STARTED;

        // Start the main thread.
        mainThread.start();
    }

    private Vector<Runnable> taskList = new Vector<Runnable>();
    private final Object taskSync = new Object();
    private Thread executorThread;

    public void invokeAndWait(final Runnable task){
        /*
        if(this == StaticData.rootIsolate){
            task.run();
            return;
        }
        
        synchronized(taskSync){
            taskList.add(task);
            taskSync.notifyAll();
        }

        synchronized(task){
            while(taskList.contains(task)){
                try {
                    task.wait();
                }catch(InterruptedException e){
                    //
                }
            }
        }
        */
    }
    /*
    private class TaskExecutor implements Runnable{
        public void run() {
            //while(!VmIsolate.this.hasTerminated()){
            do {
                Runnable task = null;
                synchronized(taskSync){
                    try {
                        while(taskList.isEmpty()){
                            taskSync.wait();
                        }
                        try {
                            task = taskList.get(0);
                            task.run();
                            taskList.remove(0);
                        } catch(Throwable t){
                            System.err.println("Error during task execution, dropping task");
                            t.printStackTrace();
                            taskList.remove(0);
                        }
                    }catch(InterruptedException ie){
                        //
                    }
                }
                if(task != null)
                    synchronized(task){
                        task.notifyAll();
                    }
            } while(!hasExited());
            //} while(true);
        }
    }
    */

    /**
     * Run this isolate. This method is called from IsolateThread.
     */
    @PrivilegedActionPragma
    final void run(IsolateThread thread) {
        VmIsolate o_current = IsolatedStaticData.current;
        try {
            Unsafe.debug("isolated run ");
            // Set current
            IsolatedStaticData.current = VmIsolate.this;

            // Set I/O
            VmSystem.setOut(thread.getStdout());
            VmSystem.setErr(thread.getStderr());
            VmSystem.setIn(thread.getStdin());

            // Set context classloader
            final ClassLoader loader = thread.getPluginManager().getRegistry()
                    .getPluginsClassLoader();
            Thread.currentThread().setContextClassLoader(loader);

            // Fire started events.
            // TODO implement me

            // Load the main class
            final Class< ? > cls = loader.loadClass(mainClass);

            //start executor
            //executorThread = new Thread(new TaskExecutor(), "isolate-executor");
            //executorThread.start();

            // Find main method
            final Method mainMethod = cls.getMethod("main",
                    IsolatedStaticData.mainTypes);

            //inherit properties
            Properties sys_porps = System.getProperties();
            for( String prop : initProperties.stringPropertyNames()){
                sys_porps.setProperty(prop, initProperties.getProperty(prop));
            }

            // Run main method.
            mainMethod.invoke(null, new Object[] { args });
        } catch (Throwable ex) {
            try {
                Unsafe.debug(" exception in isolate.run");
                Unsafe.debug(ex.getMessage());
                StackTraceElement[] trace = ex.getStackTrace();
                if (trace != null) {
                    Unsafe.debug("getStackTrace() != null\n");
                    for (int i = 0; i < trace.length; i++) {
                        StackTraceElement element = trace[i];
                        Unsafe.debug(element.getClassName());
                        Unsafe.debug('#');
                        Unsafe.debug(element.getMethodName());
                        Unsafe.debug(element.getLineNumber());
                        Unsafe.debug('\n');
                    }
                } else {
                    Unsafe.debug("getStackTrace() == null\n");
                }
                ex.printStackTrace();
            } catch (Throwable ex2) {
                Unsafe.debug("Exception in catch block.. giving up: ");
                Unsafe.debug(ex2.getMessage());
            }
        } finally {
            IsolatedStaticData.current = o_current;
        }
    }

    /**
     * Gets the root thread group of the current thread.
     * 
     * @return
     */
    private static final ThreadGroup getRootThreadGroup() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }
        return tg;
    }

    /**
     * @return the isolatedStaticsTable
     */
    final VmIsolatedStatics getIsolatedStaticsTable() {
        return isolatedStaticsTable;
    }

    /**
     * Gets the classname of the main class.
     * @return the main class name
     */
    final String getMainClassName() {
        return mainClass;
    }

    /**
     * Gets the system classloader for this isolate.
     */
    public final ClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }

    /**
     * Sets the system classloader for this isolate.
     */
    public final void setSystemClassLoader(ClassLoader loader) {
        if (this.systemClassLoader == null) {
            this.systemClassLoader = loader;
        }
    }

    private void testIsolate(Isolate isolate) {
        if (this.isolate != isolate) {
            throw new SecurityException("Method called by invalid isolate");
        }
    }

	public IOContext getIOContext() {
		return ioContext;
	}

	public void setIOContext(IOContext context) {
		ioContext = context;
	}

	public void resetIOContext() {
		ioContext = vmIoContext;
	}
}
