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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;
import javax.isolate.IsolateStatus;
import javax.isolate.Link;
import javax.naming.NameNotFoundException;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginManager;
import org.jnode.util.BootableHashMap;
import org.jnode.vm.IOContext;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmIOContext;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmSystem;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.PrivilegedActionPragma;
import org.jnode.vm.annotation.SharedStatics;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmThread;

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

    private IsolateStatus.State isolateState;
    private IsolateStatus.ExitReason exitReason;
    private int exitCode;

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
    private BootableHashMap<VmType<?>, Class<?>> classesMap = new BootableHashMap<VmType<?>, Class<?>>();

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
    private VmLink[] dataLinks;

    /**
     * Status links created by newStatusLink()
     */
    private LinkedList<VmLink> statusLinks = new LinkedList<VmLink>();

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
     * Unique identifier.
     */
    private final int id;

    /**
     * Mapping between internal VmType and per isolate Class instance.
     */
    private BootableHashMap<VmIsolateLocal<?>, ?> isolateLocalMap = new BootableHashMap<VmIsolateLocal<?>, Object>();

    private final List<VmIsolate> children = new LinkedList<VmIsolate>();

    /**
     * Isolate states.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    @SharedStatics
    private enum State {
        CREATED(IsolateStatus.State.UNKNOWN),

        STARTING(IsolateStatus.State.STARTING),

        STARTED(IsolateStatus.State.STARTED),

        EXITING(IsolateStatus.State.EXITING),

        EXITED(IsolateStatus.State.EXITED),

        TERMINATING(IsolateStatus.State.EXITING),

        TERMINATED(IsolateStatus.State.EXITED),

        NEVERSTARTED(IsolateStatus.State.UNKNOWN),

        UNKNOWN(IsolateStatus.State.UNKNOWN);

        private final IsolateStatus.State isolateState;

        private State(IsolateStatus.State isolateState) {
            this.isolateState = isolateState;
        }

        IsolateStatus.State getIsolateState() {
            return isolateState;
        }
    }

    public static boolean walkIsolates(ObjectVisitor visitor) {
        for (int i = 0; i < StaticData.isolates.size(); i++) {
            VmIsolate isolate = StaticData.isolates.get(i);
            if (!isolate.isolatedStaticsTable.walk(visitor))
                return false;
        }
        return true;
    }

    public static Iterator<VmIsolatedStatics> staticsIterator() {
        ArrayList<VmIsolatedStatics> l = new ArrayList<VmIsolatedStatics>();
        VmIsolatedStatics ist = StaticData.rootIsolate.getIsolatedStaticsTable();
        if (ist != null)
            l.add(ist);

        for (VmIsolate is : StaticData.isolates.toArray(new VmIsolate[StaticData.isolates.size()])) {
            ist = is.getIsolatedStaticsTable();
            if (ist != null)
                l.add(ist);
        }

        return l.iterator();
    }

    /**
     * Static data of the VMIsolate class.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    @SharedStatics
    private static final class StaticData {
        /**
         * The root (aka system) isolate.
         */
        private static transient VmIsolate rootIsolate;
        /**
         * Non-root isolates.
         */
        private static final List<VmIsolate> isolates = new LinkedList<VmIsolate>();

        private static int nextId = 0;

        static final VmIsolate getRoot() {
            if (rootIsolate == null) {
                rootIsolate = new VmIsolate(null/*Thread.currentThread().getVmThread().getIsolatedStatics()*/);
//                org.jnode.vm.Unsafe.debug("getRoot() istatics: " + rootIsolate.isolatedStaticsTable + "\n");
//                org.jnode.vm.Unsafe.debugStackTrace();
            }
            return rootIsolate;
        }

        static synchronized int nextId() {
            return nextId++;
        }
    }

    /**
     * Isolate specific static data
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    private static class IsolatedStaticData {
        /**
         * The current isolate.
         */
        static VmIsolate current;

        /**
         * Types of the arguments of the main(String[]) method
         */
        static final Class[] mainTypes = new Class[]{String[].class};
    }

    /**
     * Constructor for the root isolate.
     */
    private VmIsolate(VmIsolatedStatics isolatedStatics) {
        this.id = StaticData.nextId();
        this.isolate = new Isolate(this);
        this.mainClass = null;
        this.args = null;
        this.bindings = new VmStreamBindings();
        this.state = State.STARTED;
        this.threadGroup = getRootThreadGroup();
        this.creator = null;
        this.isolatedStaticsTable = isolatedStatics;

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
        this.id = StaticData.nextId();
        this.initProperties = properties;
        this.isolate = isolate;
        this.mainClass = mainClass;
        this.args = args;
        this.bindings = bindings;
        final VmArchitecture arch = Vm.getArch();
        this.isolatedStaticsTable = new VmIsolatedStatics(VmMagic.currentProcessor().getIsolatedStatics(),
            arch, new Unsafe.UnsafeObjectResolver());
        this.creator = currentIsolate();
        if (getRoot().executor == null && isRoot()) {
            //initialize the root executor on the creation of the first child
            getRoot().invokeAndWait(new Runnable() {
                public void run() {
                    //org.jnode.vm.Unsafe.debug("Root executor ready\n");
                }
            });
        }
        StaticData.isolates.add(this);
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
     * @return the root isolate
     */
    public static VmIsolate getRoot() {
        //todo security
        return StaticData.getRoot();
    }

    /**
     * @return an array of isolates without the root isolate
     */
    public static VmIsolate[] getVmIsolates() {
        //todo security
        return StaticData.isolates.toArray(new VmIsolate[0]);
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
                    result = type.newClass();
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

    public final void isolateExit(int status) {
        changeState(State.EXITING);

        this.exitCode = status;
        if (currentIsolate() == this) {
            this.exitReason = IsolateStatus.ExitReason.SELF_EXIT;
        } else {
            this.exitReason = IsolateStatus.ExitReason.OTHER_EXIT;
        }

        disposeAppContext(this.exitReason == IsolateStatus.ExitReason.SELF_EXIT);

        //stopAllThreads();
    }

    public final void isolateHalt(int status) {
        changeState(State.EXITING);

        this.exitCode = status;
        if (currentIsolate() == this) {
            this.exitReason = IsolateStatus.ExitReason.SELF_HALT;
        } else {
            this.exitReason = IsolateStatus.ExitReason.OTHER_HALT;
        }

        disposeAppContext(this.exitReason == IsolateStatus.ExitReason.SELF_HALT);

        //stopAllThreads();
    }

    public final void systemExit(Isolate isolate, int status) {
        //only this isolate may call this method
        testIsolate(isolate);

        //todo add similar checks to other exit modes too
        synchronized (this) {
            if (!this.state.equals(State.STARTED))
                return;
        }

        changeState(State.EXITING);

        this.exitReason = IsolateStatus.ExitReason.SELF_EXIT;
        this.exitCode = status;

        disposeAppContext(true);

        //stopAllThreads();
    }

    public final void systemHalt(Isolate isolate, int status) {
        //only this isolate may call this method
        testIsolate(isolate);

        changeState(State.EXITING);

        this.exitReason = IsolateStatus.ExitReason.SELF_HALT;
        this.exitCode = status;

        disposeAppContext(true);

        //stopAllThreads();
    }

    private void stopAllThreads() {
        // TODO - investigate it
        // TODO - this is probably unsafe because any of the threads being killed could
        // be in the middle of updating a critical system data structure.  I'm also 
        // unsure of the order in which we are killing the threads here.  It might be
        // better to kill the isolate's main thread first to give it the chance to
        // do a graceful shutdown.  (Stephen Crawley - 2008-11-08)
        int ac = threadGroup.activeCount();
        if (ac > 0) {
            Thread[] ta = new Thread[ac];
            int rc = threadGroup.enumerate(ta);
            Thread current = Thread.currentThread();
            boolean found = false;
            for (int i = 0; i < rc; i++) {
                Thread thread = ta[i];
                if (current != thread) {
                    thread.getVmThread().stopForced(null);
                } else {
                    found = true;
                }
            }
            if (found) {
                current.getVmThread().stop(new ThreadDeath());
            } else {
                doExit();
            }
        } else {
            // TODO - analyze this case      
            doExit();
        }
    }

    /**
     * Request normal termination of this isolate.
     *
     * @param status
     */
    public final void implicitExit(VmThread vmThread, int status) {
        //on this isolate may call this method
        testIsolate(currentIsolate().isolate);

        // TODO - handle demon threads
        if (threadGroup.activeCount() > 0 || threadGroup.activeGroupCount() > 0)
            return;

        if (exitReason == null) {
            changeState(State.EXITING);
            exitReason = IsolateStatus.ExitReason.IMPLICIT_EXIT;
            this.exitCode = status;
        }

        if (vmThread.getName().indexOf("-AWT-stopper") > -1) {
            doExit();
        } else {
            disposeAppContext(true);
        }

        //doExit();
        ///stopAllThreads();
    }

    /**
     * Request normal termination of this isolate.
     */
    public final void uncaughtExceptionExit() {
        //on this isolate may call this method
        testIsolate(currentIsolate().isolate);

        // TODO - handle demon threads
        if (threadGroup.activeCount() > 0 || threadGroup.activeGroupCount() > 0)
            return;

        changeState(State.EXITING);

        exitReason = IsolateStatus.ExitReason.UNCAUGHT_EXCEPTION;
        this.exitCode = -1;

        disposeAppContext(true);

        //doExit();

        //stopAllThreads();
    }

    /**
     * Force termination of this isolate.
     *
     * @param status
     * @deprecated
     */
    @SuppressWarnings("deprecation")
    public final void halt(int status) {
        changeState(State.EXITING);
        switch (state) {
            case EXITING:
                threadGroup.stop();
                break;
        }

        if (currentIsolate() == this) {
            this.exitReason = IsolateStatus.ExitReason.SELF_HALT;
        } else {
            this.exitReason = IsolateStatus.ExitReason.OTHER_HALT;
        }

        this.exitCode = status;

        doExit();
    }

    private void doExit() {
        try {
            if (!threadGroup.isDestroyed())
                threadGroup.destroy();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        this.creator.removeChild(this);
        StaticData.isolates.remove(this);

        changeState(State.EXITED);
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
    public static Link[] getLinks() {
        final VmLink[] vmLinks = currentIsolate().dataLinks;

        if ((vmLinks == null) || (vmLinks.length == 0)) {
            return new Link[0];
        } else {
            Link[] links = new Link[vmLinks.length];
            int i = 0;
            for (VmLink vmLink : vmLinks) {
                links[i++] = vmLink.asLink();
            }
            return links;
        }
    }

    /**
     * Start this isolate.
     *
     * @param isolate the isolate to start
     * @param links   an array of links passed to the isolate on startup
     * @throws IsolateStartupException on startup failure
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
                throw new IllegalStateException("Isolate has already been started");
            }
            changeState(State.STARTING);
        }

        // Save starter
        this.starter = currentIsolate();

        // Save links
        this.dataLinks = null;
        if (links != null) {
            VmLink[] vmLinks = new VmLink[links.length];
            int i = 0;
            for (Link link : links) {
                if (!link.isOpen()) {
                    throw new IsolateStartupException("Link is closed");
                }
                vmLinks[i] = VmLink.fromLink(link);
            }
            this.dataLinks = vmLinks;
        }

        // Create a new ThreadGroup
        this.threadGroup = new ThreadGroup(StaticData.getRoot().threadGroup, mainClass);

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

        // Start the main thread.
        mainThread.start();
    }

    /*
    private Vector<Runnable> taskList = new Vector<Runnable>();
    private final Object taskSync = new Object();
    private Thread executorThread;
    */

//    public void invokeAndWait(final Runnable task) {
    //TODO implement VmIsolate.invokeAndWait(Runnable)
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
//    }
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


    private ExecutorService executor;

    /**
     * Execute a task within this isolate and wait for completion.
     *
     * @param task the task as a Runnable object
     */
    public synchronized void invokeAndWait(final Runnable task) {
        if (executor == null) {
            executor = AccessController.doPrivileged(new PrivilegedAction<ExecutorService>() {
                public ExecutorService run() {
                    return Executors.newSingleThreadExecutor(new IsolateThreadFactory2(VmIsolate.this));
                }
            });
        }
        if (task == null)
            return;

        try {
            if (executor.submit(task).get() != null) {
                throw new RuntimeException("Execution failed!");
            }
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Execute a task asynchronously within this isolate.
     *
     * @param task the task as a Runnable object
     */
    public synchronized void invokeLater(final Runnable task) {
        org.jnode.vm.Unsafe.debug("invokeLater Called - 0\n");
        if (executor == null) {
            executor = java.util.concurrent.Executors.newSingleThreadExecutor(new IsolateThreadFactory(this));
            org.jnode.vm.Unsafe.debug("invokeAndWait executor created - 0\n");
        }
        if (task == null)
            return;

        try {
            org.jnode.vm.Unsafe.debug("invokeAndWait submitting task - 0\n");
            executor.submit(task);
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    boolean isEDT() {
        if (appContext == null)
            return false;


        try {
            Object eq = appContext.getClass().getMethod("get", Object.class).
                invoke(appContext, appContext.getClass().getField("EVENT_QUEUE_KEY").get(null));
            if (eq == null)
                return false;

            org.jnode.vm.Unsafe.debug("isEDT - 1\n");

            Object t = eq.getClass().getField("dispatchThread").get(eq);
            if (t == null)
                return false;

            org.jnode.vm.Unsafe.debug("isEDT edt=" + t + "\n");
            org.jnode.vm.Unsafe.debug("isEDT currenThread=" + Thread.currentThread() + "\n");

            return t == Thread.currentThread();
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
        /*
                    try {
                        return (Boolean) Class.forName("java.awt.EventQueue").
                            getMethod("isDispatchThread").invoke(null);
                    } catch (Exception x) {
                        throw new RuntimeException(x);

                    }
                    */
        // return false;
    }

    private Object appContext;

    private void disposeAppContext(boolean intraIsolate) {
        if (appSupport != null) {
            appSupport.stop(intraIsolate);
        } else {
            stopAllThreads();
        }
    }

    /**
     * @param intraIsolate
     * @deprecated
     */
    private void disposeAppContext_old(boolean intraIsolate) {
        final Object appContext;
        final boolean is_edt;
        synchronized (this) {
            is_edt = isEDT();
            appContext = this.appContext;
            this.appContext = null;
        }
        org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 000\n");
        org.jnode.vm.Unsafe.debugStackTrace();
        org.jnode.vm.Unsafe.debug("disposeAppContextCalled  - 000 " + intraIsolate + "\n");
        if (appContext != null) {
            org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 0001\n");
            org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 0002\n");
            if (intraIsolate && is_edt) {
                org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 0003\n");
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 00\n");
                        getRoot().invokeAndWait(new Runnable() {
                            public void run() {
                                try {
                                    org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 01\n");
                                    appContext.getClass().getMethod("dispose").invoke(appContext);
                                    org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 02\n");
                                } catch (Exception x) {
                                    x.printStackTrace();
                                }
                            }
                        });
                        stopAllThreads();
                        doExit();
                    }
                }, "isolate-" + getId() + "-AWT-stopper");
                t.start();
            } else {
                org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 0004\n");
                org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 0\n");
                getRoot().invokeAndWait(new Runnable() {
                    public void run() {
                        try {
                            org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 1\n");
                            org.jnode.vm.Unsafe.debug("disposeAppContextCalled appcontext: " + appContext + "\n");
                            org.jnode.vm.Unsafe.debug(
                                "disposeAppContextCalled appcontext.getClass(): " + appContext.getClass() + "\n");
                            org.jnode.vm.Unsafe.debug("disposeAppContextCalled appcontext.getClass().dispose: " +
                                appContext.getClass().getMethod("dispose") + "\n");
                            appContext.getClass().getMethod("dispose").invoke(appContext);
                            org.jnode.vm.Unsafe.debug("disposeAppContextCalled - 2\n");
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                    }
                });
                stopAllThreads();
            }
        }
    }

    /**
     * Run this isolate. This method is called from IsolateThread.
     */
    @PrivilegedActionPragma
    final void run(IsolateThread thread) {
        try {
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
            final Class<?> cls = loader.loadClass(mainClass);

            //start executor
            //executorThread = new Thread(new TaskExecutor(), "isolate-executor");
            //executorThread.start();

            // Find main method
            final Method mainMethod = cls.getMethod("main",
                new Class[]{String[].class});
//                IsolatedStaticData.mainTypes);

            //inherit properties
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    Properties sys_porps = System.getProperties();
                    for (String prop : initProperties.stringPropertyNames()) {
                        sys_porps.setProperty(prop, initProperties.getProperty(prop));
                    }
                    return null;
                }
            });

            //create the appcontext for this isolate
            // TODO - improve this
            //appContext = Class.forName("sun.awt.SunToolkit").getMethod("createNewAppContext").invoke(null);
            this.appSupport = new AppSupport(this);
            this.appSupport.start();

            // Update the state of this isolate.
            changeState(State.STARTED);
            //add to parent
            this.creator.addChild(this);

            mainMethod.setAccessible(true);
            // Run main method.
            mainMethod.invoke(null, new Object[]{args});
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
            } finally {
                systemHalt(isolate, -1);
            }
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
     *
     * @return the main class name
     */
    public final String getMainClassName() {
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

    /**
     * Returns the identifier of this isolate.
     *
     * @return the unique identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the state of this isolate.
     *
     * @return the current state
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the VmIsolate instance which created this VmIsolate instance.
     *
     * @return
     */
    public VmIsolate getCreator() {
        return creator;
    }

    /**
     * Returns the map of isolate locals belonging to this isolate.
     *
     * @return the isolate local map
     */
    BootableHashMap getIsolateLocalMap() {
        return isolateLocalMap;
    }

    public IsolateStatus.State getIsolateState() {
        return isolateState;
    }

    /**
     * Create and return a new status link for this isolate and the supplied
     * receiver isolate.
     *
     * @param receiver the receiver for the link.
     * @return the link.
     */
    public synchronized Link newStatusLink(VmIsolate receiver) {
        Link link = VmLink.newLink(this, receiver);
        VmLink vmLink = VmLink.fromLink(link);
        statusLinks.add(vmLink);
        if (isolateState != null && isolateState.equals(IsolateStatus.State.EXITED)) {
            // The spec says that we should immediately send a link message
            // if the isolate is already 'EXITED'.
            sendStatus(vmLink, isolateState);
        }
        return link;
    }

    private synchronized boolean changeState(State newState) {
        this.state = newState;
        IsolateStatus.State newIsolateState = newState.getIsolateState();
        if (isolateState != newIsolateState) {
            this.isolateState = newIsolateState;
            for (VmLink link : statusLinks) {
                sendStatus(link, this.isolateState);
            }
        }
        return true;
    }

    private void sendStatus(VmLink link, IsolateStatus.State state) {
        if (state.equals(IsolateStatus.State.EXITED)) {
            link.sendStatus(new StatusLinkMessage(new IsolateStatusImpl(exitReason, exitCode)));
        } else {
            link.sendStatus(new StatusLinkMessage(new IsolateStatusImpl(state)));
        }
    }

    private void addChild(VmIsolate child) {
        synchronized (children) {
            children.add(child);
        }
    }

    private void removeChild(VmIsolate child) {
        synchronized (children) {
            children.remove(child);
        }
    }

    public VmIsolate[] getChildren() {
        synchronized (children) {
            return children.toArray(new VmIsolate[children.size()]);
        }
    }

    public ThreadGroup getThreadGroup() {
        if (threadGroup == null) {
            throw new IllegalStateException("Isolate not available");
        }
        return threadGroup;
    }

    private AppSupport appSupport;

    @SharedStatics
    private static class AppSupport {
        private static boolean awtSupport;

        static {
            try {
                Class.forName("java.awt.Toolkit");
                awtSupport = true;
            } catch (ClassNotFoundException x) {
                awtSupport = false;
            }
        }

        private final VmIsolate vmIsolate;
        private Object appContext;

        AppSupport(VmIsolate vmIsolate) {
            this.vmIsolate = vmIsolate;
        }

        boolean isAWTReady() {
            if (!awtSupport)
                return false;

            try {
                return Class.forName("java.awt.Toolkit").getField("toolkit").get(null) != null;
            } catch (Exception x) {
                return false;
            }
        }

        void start() throws Exception {
            if (isAWTReady()) {
                synchronized (this) {
                    appContext = Class.forName("sun.awt.SunToolkit").getMethod("createNewAppContext").invoke(null);
                }
            }
        }

        void stop(boolean intraIsolate) {
            boolean done = false;
            if (awtSupport) {
                synchronized (this) {
                    if (appContext != null) {
                        disposeAppContext(intraIsolate);
                        done = true;
                    }
                }
            }

            if (!done) {
                vmIsolate.stopAllThreads();
                vmIsolate.doExit();
            }
        }

        boolean isEDT() {
            if (appContext == null)
                return false;


            try {
                Object eq = appContext.getClass().getMethod("get", Object.class).
                    invoke(appContext, appContext.getClass().getField("EVENT_QUEUE_KEY").get(null));
                if (eq == null)
                    return false;

                Object t = eq.getClass().getField("dispatchThread").get(eq);
                if (t == null)
                    return false;

                return t == Thread.currentThread();
            } catch (Exception x) {
                throw new RuntimeException(x);
            }
            /*
            try {
                return (Boolean) Class.forName("java.awt.EventQueue").
                    getMethod("isDispatchThread").invoke(null);
            } catch (Exception x) {
                throw new RuntimeException(x);

            }
            */
            // return false;
        }

        private void disposeAppContext(boolean intraIsolate) {
            final Object appContext;
            final boolean is_edt;
            synchronized (this) {
                is_edt = isEDT();
                appContext = this.appContext;
                this.appContext = null;
            }
            if (appContext != null) {
                if (intraIsolate && is_edt) {
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            getRoot().invokeAndWait(new Runnable() {
                                public void run() {
                                    try {
                                        appContext.getClass().getMethod("dispose").invoke(appContext);
                                    } catch (Exception x) {
                                        x.printStackTrace();
                                    }
                                }
                            });
                            vmIsolate.stopAllThreads();
                            vmIsolate.doExit();
                        }
                    }, "isolate-" + vmIsolate.getId() + "-AWT-stopper");
                    t.start();
                } else {
                    getRoot().invokeAndWait(new Runnable() {
                        public void run() {
                            try {
                                appContext.getClass().getMethod("dispose").invoke(appContext);
                            } catch (Exception x) {
                                x.printStackTrace();
                            }
                        }
                    });
                    vmIsolate.stopAllThreads();
                }
            }
        }
    }
}
