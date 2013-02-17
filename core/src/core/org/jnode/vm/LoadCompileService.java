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
 
package org.jnode.vm;

import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.jnode.assembler.ObjectResolver;
import org.jnode.annotation.Inline;
import org.jnode.annotation.Internal;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.MagicPermission;
import org.jnode.annotation.SharedStatics;
import org.jnode.vm.classmgr.ClassDecoder;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.NativeCodeCompiler;

/**
 * Service used to load classes and compile methods.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
@SharedStatics
public final class LoadCompileService {

    private static LoadCompileService service;

    private final ArrayList<Request> requestQueue = new ArrayList<Request>();

    private final ObjectResolver resolver;

    private final NativeCodeCompiler[] compilers;

    private final NativeCodeCompiler[] testCompilers;

    private static boolean started = false;

    private static final int threadCount = 2; //4

    /**
     * Default ctor
     */
    public LoadCompileService(ObjectResolver resolver) {
        this.resolver = resolver;
        final BaseVmArchitecture arch = VmMagic.currentProcessor()
            .getArchitecture();
        this.compilers = arch.getCompilers();
        this.testCompilers = arch.getTestCompilers();
    }

    /**
     * Recompile the given method with the given optimization level
     *
     * @param method
     * @param optLevel
     * @param enableTestCompilers
     */
    public static final void compile(VmMethod method, int optLevel,
                                     boolean enableTestCompilers) {
        if (service == null) {
            service = new LoadCompileService(new Unsafe.UnsafeObjectResolver());
        }

        if ((!started) || (Thread.currentThread() instanceof LoadCompileThread)) {
            // Compile now
            service.doCompile(method, optLevel, enableTestCompilers);
        } else {
            // Put request in queue
            service.enqueAndWait(new CompileRequest(method, optLevel,
                enableTestCompilers));
        }
    }

    /**
     * Get the highest supported optimization level for the regular or test compilers.
     */
    public static int getHighestOptimizationLevel(boolean test) {
        if (test) {
            return service.testCompilers == null ? -1 : service.testCompilers.length - 1;
        } else {
            return service.compilers == null ? -1 : service.compilers.length - 1;
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#defineClass(java.lang.String,
     *      ByteBuffer, java.security.ProtectionDomain)
     */
    public static final VmType<?> defineClass(String name, ByteBuffer data,
                                              ProtectionDomain protDomain, VmClassLoader loader) {
        initService();
        /* TODO review it: if(true || ... disables class definition on the load-compile thread.*/
        if (true || (!started) || (Thread.currentThread() instanceof LoadCompileThread)) {
            // Load now
            return service.doDefineClass(name, data, protDomain, loader);
        } else {
            // Put request in queue
            LoadRequest request = new LoadRequest(name, data, protDomain,
                loader);
            service.enqueAndWait(request);
            return request.getDefinedType();
        }
    }

    /**
     * Start this service.
     */
    static final void start() {
        initService();
        /*
        Disabled the startup of the LoadCompile thread service because multithreaded
        class loading creates deadlock in URLClassLoader and subclasses.
        Loading and compilation will happen onthe caller thread.
        - load-compile thread are enabled again but only used for compilation
        TODO review this: investigate class loading in dedicated threads (probably will not work)
        TODO and method compilation on dedicated threads (probably will work)
        */
        if (!started) {
            started = true;
            for (int i = 0; i < threadCount; i++) {
                LoadCompileThread thread = new LoadCompileThread(service,
                    "LoadCompile-" + i);
                thread.start();
            }
        }

    }

    @KernelSpace
    @Internal
    public static final void showInfo() {
        Unsafe.debug(" #loadcompile requests: ");
        Unsafe.debug((service != null) ? service.requestQueue.size() : 0);
    }

    /**
     * Initialize the service if needed.
     */
    @Inline
    private static void initService() {
        if (service == null) {
            service = new LoadCompileService(new Unsafe.UnsafeObjectResolver());
        }
    }

    /**
     * Put request in queue and wait for request to finish.
     *
     * @param request
     */
    private void enqueAndWait(Request request) {
        // Put request in queue
        synchronized (requestQueue) {
            requestQueue.add(request);
            requestQueue.notify();
        }
        // Wait for request to finish
        request.waitUntilFinished();
    }

    /**
     * Wait for a request in the queue and process it.
     */
    final void processNextRequest() {
        // Get the first request
        final Request request;
        synchronized (requestQueue) {
            while (requestQueue.isEmpty()) {
                try {
                    requestQueue.wait();
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
            request = requestQueue.get(0);
            requestQueue.remove(0);
        }
        try {
            // Process request
            request.execute();
        } finally {
            // Notify waiting threads
            request.setFinished();
        }
    }

    /**
     * Compile the given method
     *
     * @param vmMethod The method to compile
     * @param optLevel The optimization level
     */
    private void doCompile(VmMethod vmMethod, int optLevel,
                           boolean enableTestCompilers) {
        final NativeCodeCompiler cmps[];
        int index;
        if (enableTestCompilers) {
            index = optLevel;
            optLevel += compilers.length;
            cmps = testCompilers;
        } else {
            index = optLevel;
            cmps = compilers;
        }

        final NativeCodeCompiler cmp;
        if (index < 0) {
            index = 0;
        } else if (index >= cmps.length) {
            index = cmps.length - 1;
        }
        if (vmMethod.getNativeCodeOptLevel() < optLevel) {
            cmp = cmps[index];
            cmp.compileRuntime(vmMethod, resolver, optLevel, null);
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmClassLoader#defineClass(java.lang.String,
     *      ByteBuffer, java.security.ProtectionDomain)
     */
    final VmType<?> doDefineClass(String name, ByteBuffer data,
                                  ProtectionDomain protDomain, VmClassLoader loader) {
        return ClassDecoder.defineClass(name, data, true, loader, protDomain);
    }

    private abstract static class Request {

        private boolean finished = false;
        private Throwable exception;

        /**
         * Wait until this request is finished.
         */
        final synchronized void waitUntilFinished() {
            while (!finished) {
                try {
                    wait(5000);
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
            if (exception != null) {
                //todo debug
                exception.printStackTrace();
                throw new RuntimeException(errorMessage(), exception);
            }
        }

        final synchronized void setFinished() {
            finished = true;
            notifyAll();
        }

        final void execute() {
            try {
                doExecute();
            } catch (Throwable ex) {
                this.exception = ex;
            }
        }

        /**
         * Execute this request.
         */
        abstract void doExecute();

        /**
         * Gets request specific error message.
         *
         * @return
         */
        abstract String errorMessage();
    }

    static final class CompileRequest extends Request {

        private final VmMethod method;

        private final int optLevel;

        private final boolean enableTestCompilers;

        /**
         * @param method
         * @param optLevel
         * @param enableTestCompilers
         */
        CompileRequest(final VmMethod method, final int optLevel,
                       final boolean enableTestCompilers) {
            this.method = method;
            this.optLevel = optLevel;
            this.enableTestCompilers = enableTestCompilers;
        }

        /**
         * Execute this request.
         *
         * @see org.jnode.vm.LoadCompileService.Request#execute()
         */
        void doExecute() {
            service.doCompile(method, optLevel, enableTestCompilers);
        }

        /**
         * @see org.jnode.vm.LoadCompileService.Request#errorMessage()
         */
        @Override
        String errorMessage() {
            return "Error in compilation: ";
        }
    }

    static final class LoadRequest extends Request {
        private final String name;

        private final ByteBuffer data;

        private final ProtectionDomain protDomain;

        private final VmClassLoader loader;

        private VmType<?> definedType;

        /**
         * @param name
         * @param data
         * @param protDomain
         * @param loader
         */
        LoadRequest(final String name, final ByteBuffer data,
                    final ProtectionDomain protDomain, final VmClassLoader loader) {
            this.name = name;
            this.data = data;
            this.protDomain = protDomain;
            this.loader = loader;
        }

        /**
         * Execute this request.
         *
         * @see org.jnode.vm.LoadCompileService.Request#execute()
         */
        void doExecute() {
            definedType = service.doDefineClass(name, data, protDomain, loader);
        }

        /**
         * @return the definedType
         */
        final VmType<?> getDefinedType() {
            return definedType;
        }

        /**
         * @see org.jnode.vm.LoadCompileService.Request#errorMessage()
         */
        @Override
        String errorMessage() {
            return "Error in class loading: ";
        }
    }
}
