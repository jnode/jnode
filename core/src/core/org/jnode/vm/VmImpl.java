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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.annotation.Internal;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.SharedStatics;
import org.jnode.plugin.Extension;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginRegistry;
import org.jnode.system.resource.ResourceManager;
import org.jnode.vm.classmgr.CompiledCodeList;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.facade.VmProcessor;
import org.jnode.vm.facade.VmThreadVisitor;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.objects.BootableArrayList;
import org.jnode.vm.objects.Counter;
import org.jnode.vm.objects.CounterGroup;
import org.jnode.vm.objects.Statistic;
import org.jnode.vm.objects.Statistics;
import org.jnode.vm.objects.VmSystemObject;
import org.jnode.vm.scheduler.VmScheduler;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@SharedStatics
public
final class VmImpl extends VmSystemObject implements Statistics, org.jnode.vm.facade.Vm {

    /**
     * Are will in bootimage building phase?
     * FIXME it appears always set to true in constructor. but code depends on its (non-constant) value. remove it ?
     */
    private transient boolean bootstrap;

    /**
     * The current architecture
     */
    private final BaseVmArchitecture arch;

    /**
     * The heap manager
     */
    private final VmHeapManager heapManager;

    /** Set this boolean to turn the hot method manager on/off */
    // private final boolean runHotMethodManager = false;
    /**
     * Should this VM run in debug mode?
     */
    private final boolean debugMode;

    /**
     * Version of the OS and VM
     */
    private final String version;

    /**
     * The statics table
     */
    private final VmSharedStatics statics;

    /**
     * The list of all system processors
     */
    private final List<VmProcessor> processors;

    /**
     * All statistics
     */
    private transient Map<String, Statistic> statistics;

    /**
     * List of all compiled methods
     */
    private final CompiledCodeList compiledMethods;

    /**
     * For assertion checking things that should never happen.
     * TODO it appears unused. remove it ?
     */
    public static final boolean NOT_REACHED = false;

    private VmScheduler scheduler;

    /**
     * Initialize a new instance
     *
     * @param arch
     * @throws InstantiationException
     */
    public VmImpl(String version, BaseVmArchitecture arch, VmSharedStatics statics,
                  boolean debugMode, VmClassLoader loader, PluginRegistry pluginReg)
        throws InstantiationException {
        this.version = version;
        this.debugMode = debugMode;
        this.bootstrap = true;
        this.arch = arch;
        final HeapHelper helper = new HeapHelperImpl(arch);
        VmUtils.setVm(this);
        this.heapManager = createHeapManager(helper, arch, loader, pluginReg);
        this.statics = statics;
        this.processors = new BootableArrayList<VmProcessor>();
        this.compiledMethods = new CompiledCodeList();
    }

    /**
     * Find and instantiate the heap manager.
     *
     * @param arch
     * @param loader
     * @param pluginReg
     * @return
     * @throws InstantiationException
     */
    private static VmHeapManager createHeapManager(HeapHelper helper,
                                                   BaseVmArchitecture arch, VmClassLoader loader,
                                                   PluginRegistry pluginReg)
        throws InstantiationException {
        if (pluginReg == null) {
            // Use in tests and asm constant construction
            return null;
        }

        // Find and instantiate the heap manager.
        PluginDescriptor core = pluginReg
            .getPluginDescriptor("org.jnode.vm.core");
        Extension[] memMgrs = core.getExtensionPoint("memmgr").getExtensions();
        if (memMgrs.length != 1) {
            throw new InstantiationException(
                "memmgr extension point must have 1 extension");
        }
        Extension memMgr = memMgrs[0];
        if (memMgr.getConfigurationElements().length != 1) {
            throw new InstantiationException(
                "Expected 1 element in memmgr extension");
        }
        String memMgrClassName = memMgr.getConfigurationElements()[0]
            .getAttribute("class");
        Class[] consArgTypes = {VmClassLoader.class, HeapHelper.class};
        try {
            Class cls = Class.forName(memMgrClassName);
            Constructor cons = cls.getConstructor(consArgTypes);
            return (VmHeapManager) cons.newInstance(new Object[]{loader,
                helper});
        } catch (ClassNotFoundException ex) {
            throw new InstantiationException("Cannot find heap manager class "
                + memMgrClassName);
        } catch (IllegalAccessException ex) {
            throw new InstantiationException(
                "Cannot access heap manager class " + memMgrClassName);
        } catch (InvocationTargetException ex) {
            throw (InstantiationException) new InstantiationException(
                "Exception in creating heap manager" + ex.getMessage())
                .initCause(ex);
        } catch (NoSuchMethodException ex) {
            throw new InstantiationException(
                "Cannot find heap manager constructor");
        }
    }

    /**
     * @return Returns the bootstrap.
     */
    public final boolean isBootstrap() {
        return this.bootstrap;
    }

    /**
     * Causes JNode to stop working with a given message.
     *
     * @param msg
     */
    public static final void sysFail(String msg) {
        if (VmUtils.isRunningVm()) {
            Unsafe.die(msg);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final BaseVmArchitecture getArch() {
        return arch;
    }

    /**
     * {@inheritDoc}
     */
    public final VmHeapManager getHeapManager() {
        return heapManager;
    }

    /**
     * {@inheritDoc}
     */
    public final int availableProcessors() {
        return processors.size();
    }

    // The following code has been moved to org.jnode.shell.command.system.VmInfoCommand
//    /**
//     * Show VM info.
//     * 
//     * @param args
//     */
//    public static void main(String[] args) {
//        final Vm vm = getVm();
//        if ((vm != null) && !vm.isBootstrap()) {
//            final PrintStream out = System.out;
//            out.println("JNode VM " + vm.getVersion());
//            vm.dumpStatistics(out);
//            vm.getSharedStatics().dumpStatistics(out);
//            vm.heapManager.dumpStatistics(out);
//            final SecurityManager sm = System.getSecurityManager();
//            out.println("Security manager: " + sm);
//            for (VmProcessor cpu : vm.processors) {
//                out.println("Processor " + vm.processors.indexOf(cpu) + " (" + cpu.getIdString() + ")");
//                cpu.dumpStatistics(out);
//            }
//            if ((args.length > 0) && args[0].equals("reset")) {
//                vm.resetCounters();
//            }
//        }
//    }

    /**
     * {@inheritDoc}
     */
    public final boolean isDebugMode() {
        return this.debugMode;
    }

    /**
     * {@inheritDoc}
     */
    public final VmSharedStatics getSharedStatics() {
        return this.statics;
    }

    /**
     * {@inheritDoc}
     */
    public final String getVersion() {
        return this.version;
    }

    /**
     * Find all processors in the system and start them.
     */
    final void initializeProcessors(ResourceManager rm) {
        // Add the current (bootstrap) processor
        addProcessor(org.jnode.vm.scheduler.VmProcessor.current());
        // Let the architecture find the processors
        arch.initializeProcessors(rm);
        // Show some info
        final int cnt = processors.size();
        if (cnt == 1) {
            System.out.println("Detected 1 processor");
        } else {
            System.out.println("Detected " + cnt + " processors");
        }
    }

    /**
     * Add a discovered CPU.
     *
     * @param cpu
     */
    final void addProcessor(VmProcessor cpu) {
        processors.add(cpu);
    }

    /**
     * {@inheritDoc}
     */
    public final Counter getCounter(String name) {
        Counter cnt = (Counter) getStatistic(name);
        if (cnt == null) {
            synchronized (this) {
                cnt = (Counter) getStatistic(name);
                if (cnt == null) {
                    cnt = new Counter(name, name);
                    addStatistic(name, cnt);
                }
            }
        }
        return cnt;
    }

    /**
     * {@inheritDoc}
     */
    public final CounterGroup getCounterGroup(String name) {
        CounterGroup cnt = (CounterGroup) getStatistic(name);
        if (cnt == null) {
            synchronized (this) {
                cnt = (CounterGroup) getStatistic(name);
                if (cnt == null) {
                    cnt = new CounterGroup(name, name);
                    addStatistic(name, cnt);
                }
            }
        }
        return cnt;
    }

    private Statistic getStatistic(String name) {
        if (statistics != null) {
            return statistics.get(name);
        } else {
            return null;
        }
    }

    private void addStatistic(String name, Statistic stat) {
        if (statistics == null) {
            statistics = new TreeMap<String, Statistic>();
        }
        statistics.put(name, stat);
    }

    /**
     * @see org.jnode.vm.objects.Statistics#getStatistics()
     */
    public synchronized Statistic[] getStatistics() {
        if (statistics != null) {
            return statistics.values().toArray(
                new Statistic[statistics.size()]);
        } else {
            return new Statistic[0];
        }
    }

    /**
     * Gets the list of compiled methods.
     *
     * @return Returns the compiledMethods.
     */
    @KernelSpace
    public final CompiledCodeList getCompiledMethods() {
        return compiledMethods;
    }

    /**
     * @return the scheduler
     */
    @Internal
    public final VmScheduler getScheduler() {
        return scheduler;
    }

    /**
     * @param scheduler the scheduler to set
     */
    @Internal
    public final void setScheduler(VmScheduler scheduler) {
        if (this.scheduler == null) {
            this.scheduler = scheduler;
        }
    }

    /**
     * {@inheritDoc}
     */
    public final List<VmProcessor> getProcessors() {
        return new BootableArrayList<VmProcessor>(processors);
    }

    @Override
    public void accept(VmThreadVisitor vmThreadVisitor) {
        scheduler.visitAllThreads(vmThreadVisitor);
    }
}
