/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.vm.isolate;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jnode.vm.classmgr.VmIsolatedStatics;

class IsolateThreadFactory implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final VmIsolatedStatics isolatedStatics;
    final String namePrefix;

    IsolateThreadFactory(final VmIsolate isolate) {
        group = isolate.getThreadGroup();
        namePrefix = "isolate-" + isolate.getId() + "-executor-";
        isolatedStatics = isolate.getIsolatedStaticsTable();
    }

    public Thread newThread(final Runnable r) {
        class IsolateFactoryThread extends Thread {
            IsolateFactoryThread(ThreadGroup group, String name, VmIsolatedStatics isolatedStatics) {
                super(group, r, name, isolatedStatics);
            }
        }

        Thread t = new IsolateFactoryThread(group, namePrefix + threadNumber.getAndIncrement(), isolatedStatics) {
            public void start() {
//                org.jnode.vm.Unsafe.debug("factory 1 thread start() " + this.getName() + "\n");
//                getVmThread().switchToIsolate(isolatedStatics);
                super.start();
            }
        };
        /*
        PluginManager piManager;
        try {
            piManager = InitialNaming.lookup(PluginManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new RuntimeException("Cannot find PluginManager", ex);
        }
        */
        //t.setContextClassLoader(piManager.getRegistry().getPluginsClassLoader());
//        if (t.isDaemon())
        //          t.setDaemon(false);
        //    if (t.getPriority() != Thread.NORM_PRIORITY)
        //      t.setPriority(Thread.NORM_PRIORITY + 2);
        return t;
    }
}


class IsolateThreadFactory2 implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final VmIsolatedStatics isolatedStatics;
    final String namePrefix;
    private final Thread factoryThread;
    private Thread newThread;
    private final Object lock = new Object();
    private Runnable runnable;

    IsolateThreadFactory2(final VmIsolate isolate) {
        group = isolate.getThreadGroup();
        namePrefix = "isolate-" + isolate.getId() + "-executor-";
        isolatedStatics = isolate.getIsolatedStaticsTable();
        factoryThread = new Thread(group, new Runnable() {
            public void run() {
                while (true) {
                    synchronized (lock) {
                        try {
                            while (runnable == null) {
                                lock.wait();
                            }

                            newThread = new Thread(group, runnable, namePrefix + threadNumber.getAndIncrement()) {
                                public void start() {
//                                    org.jnode.vm.Unsafe.debug("factory thread start() " + this.getName() + "\n");
                                    super.start();
                                }
                            };
                            runnable = null;
                            lock.notifyAll();
                        } catch (InterruptedException x) {
                            break;
                        }
                    }
                }
            }
        }, "isolate-" + isolate.getId() + "-thread-factory-");
        factoryThread.start();
    }

    public synchronized Thread newThread(final Runnable r) {
        Thread ret;
//        org.jnode.vm.Unsafe.debug("IsolateThreadFactory2.newThread() called\n");
//        org.jnode.vm.Unsafe.debugStackTrace();
        synchronized (lock) {
            newThread = null;
            runnable = r;
            lock.notifyAll();
            while (newThread == null) {
                try {
                    lock.wait();
                } catch (InterruptedException x) {
                    break;
                }
            }
            ret = newThread;
            newThread = null;
            lock.notifyAll();
        }
//        org.jnode.vm.Unsafe.debug("IsolateThreadFactory2.newThread() returned\n");

        return ret;
    }
}

/*         `
package org.jnode.vm.isolate;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jnode.vm.classmgr.VmIsolatedStatics;

class IsolateThreadFactory implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final VmIsolatedStatics isolatedStatics;
    final String namePrefix;

    IsolateThreadFactory(final VmIsolate isolate) {
        group = isolate.getThreadGroup();
        namePrefix = "isolate-" + isolate.getId() + "-executor-";
        isolatedStatics = isolate.getIsolatedStaticsTable();
    }

    public Thread newThread(final Runnable r) {

        org.jnode.vm.Unsafe.debug("newThread Called - 0\n");
        Thread t = new IsolateFactoryThread(group, r, namePrefix + threadNumber.getAndIncrement(), null);
        org.jnode.vm.Unsafe.debug("newThread thread created - 0\n");
//        if (t.isDaemon())
  //          t.setDaemon(false);
    //    if (t.getPriority() != Thread.NORM_PRIORITY)
      //      t.setPriority(Thread.NORM_PRIORITY + 2);
        return t;
    }
}

class IsolateThreadFactory2 implements ThreadFactory {
    final ThreadGroup group;
    final AtomicInteger threadNumber = new AtomicInteger(1);
    final VmIsolatedStatics isolatedStatics;
    final String namePrefix;
    private final Thread factoryThread;
    private Thread newThread;
    private final Object lock = new Object();
    private boolean flag = false;

    IsolateThreadFactory2(final VmIsolate isolate) {
        group = isolate.getThreadGroup();
        namePrefix = "isolate-" + isolate.getId() + "-executor-";
        isolatedStatics = isolate.getIsolatedStaticsTable();
        factoryThread = new Thread(new Runnable(){
            public void run() {
                while(true) {
                    synchronized (lock) {
                        try {
                            while(!flag) {
                                lock.wait();

                            }

                            newThread = new IsolateFactoryThread(group, null, namePrefix +
                            threadNumber.getAndIncrement(), null);
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                    }
                }
            }
        },"isolate-" + isolate.getId() + "-thread-factory-");
        factoryThread.start();
    }

    public Thread newThread(final Runnable r) {

        org.jnode.vm.Unsafe.debug("newThread Called - 0\n");
        Thread t = new IsolateFactoryThread(group, r, namePrefix + threadNumber.getAndIncrement(), null);
        org.jnode.vm.Unsafe.debug("newThread thread created - 0\n");
//        if (t.isDaemon())
  //          t.setDaemon(false);
    //    if (t.getPriority() != Thread.NORM_PRIORITY)
      //      t.setPriority(Thread.NORM_PRIORITY + 2);
        return t;
    }
}


class IsolateFactoryThread extends Thread {
            IsolateFactoryThread(ThreadGroup group, Runnable r, String name, VmIsolatedStatics isolatedStatics) {
                super(group, r, name, isolatedStatics);
            }
        }

*/
