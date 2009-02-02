/*
 * $Id$
 *
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
 
package org.jnode.vm;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.plugin.Extension;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginRegistry;
import org.jnode.system.ResourceManager;
import org.jnode.util.BootableArrayList;
import org.jnode.util.Counter;
import org.jnode.util.CounterGroup;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.NoInline;
import org.jnode.vm.annotation.SharedStatics;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.classmgr.CompiledCodeList;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmScheduler;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@SharedStatics
public final class Vm extends VmSystemObject implements Statistics {

    /**
     * The single instance
     */
    private static Vm instance;

    /**
     * Are will in bootimage building phase?
     */
    private transient boolean bootstrap;

    /**
     * The current architecture
     */
    private final VmArchitecture arch;

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
     * Should assertions be verified?
     */
    public static final boolean VerifyAssertions = true;

    /**
     * For assertion checking things that should never happen.
     */
    public static final boolean NOT_REACHED = false;

    private VmScheduler scheduler;

    /**
     * Initialize a new instance
     *
     * @param arch
     * @throws InstantiationException
     */
    public Vm(String version, VmArchitecture arch, VmSharedStatics statics,
              boolean debugMode, VmClassLoader loader, PluginRegistry pluginReg)
        throws InstantiationException {
        this.version = version;
        this.debugMode = debugMode;
        this.bootstrap = true;
        this.arch = arch;
        final HeapHelper helper = new HeapHelperImpl(arch);
        instance = this;
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
                                                   VmArchitecture arch, VmClassLoader loader, PluginRegistry pluginReg)
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
     * Is JNode currently running.
     *
     * @return true or false
     */
    public static final boolean isRunningVm() {
        return ((instance != null) && !instance.bootstrap);
    }

    /**
     * Is the bootimage being written?
     *
     * @return true or false.
     */
    public static final boolean isWritingImage() {
        return ((instance == null) || instance.bootstrap);
    }

    /**
     * Causes JNode to stop working with a given message.
     *
     * @param msg
     */
    public static final void sysFail(String msg) {
        if (isRunningVm()) {
            Unsafe.die(msg);
        }
    }

    /**
     * @return Returns the arch.
     */
    public static final VmArchitecture getArch() {
        return instance.arch;
    }

    /**
     * @return Returns the instance.
     */
    @KernelSpace
    @Uninterruptible
    public static final Vm getVm() {
        return instance;
    }

    /**
     * @return Returns the heapManager.
     */
    public static final VmHeapManager getHeapManager() {
        return instance.heapManager;
    }

    /**
     * Returns the number of available processors currently available to the
     * virtual machine. This number may change over time; so a multi-processor
     * program want to poll this to determine maximal resource usage.
     *
     * @return the number of processors available, at least 1
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
     * Does this VM run in debug mode.
     *
     * @return Returns the debugMode.
     */
    public final boolean isDebugMode() {
        return this.debugMode;
    }

    /**
     * @return Returns the statics.
     */
    public final VmSharedStatics getSharedStatics() {
        return this.statics;
    }

    /**
     * Gets the version of the current VM.
     *
     * @return Returns the version.
     */
    public final String getVersion() {
        return this.version;
    }

    /**
     * Find all processors in the system and start them.
     */
    final void initializeProcessors(ResourceManager rm) {
        // Add the current (bootstrap) processor
        addProcessor(VmProcessor.current());
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
     * Gets or creates a counter with a given name.
     *
     * @param name
     * @return The counter
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
     * Gets or creates a counter group with a given name.
     *
     * @param name
     * @return The counter group
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
     * @see org.jnode.util.Statistics#getStatistics()
     */
    public synchronized Statistic[] getStatistics() {
        if (statistics != null) {
            return statistics.values().toArray(
                new Statistic[statistics.size()]);
        } else {
            return new Statistic[0];
        }
    }

    public void dumpStatistics(PrintWriter out) {
        if (statistics != null) {
            final Statistic[] stats = getStatistics();
            for (int i = 0; i < stats.length; i++) {
                out.println(stats[i]);
            }
        }
    }

    public final void resetCounters() {
        if (statistics != null) {
            final Statistic[] stats = getStatistics();
            for (int i = 0; i < stats.length; i++) {
                final Statistic s = stats[i];
                if (s instanceof Counter) {
                    ((Counter) s).reset();
                }
            }
        }
    }

    /**
     * Assert the given value to be true.
     *
     * @param value
     */
    public static void _assert(boolean value) {
        if (!value) {
            assertionFailed(null, null);
        }
    }

    /**
     * Assert the given value to be true.
     *
     * @param value
     */
    public static void _assert(boolean value, String msg) {
        if (!value) {
            assertionFailed(msg, null);
        }
    }

    /**
     * Assert the given value to be true.
     *
     * @param value
     */
    public static void _assert(boolean value, String msg, String msg2) {
        if (!value) {
            assertionFailed(msg, msg2);
        }
    }

    /**
     * Throw an AssertionError with the given messages.
     *
     * @param msg
     * @param msg2
     * @throws NoInlinePragma
     */
    @NoInline
    private static void assertionFailed(String msg, String msg2) {
        if ((msg == null) && (msg2 == null)) {
            msg = "Assertion failed";
        } else if (msg2 != null) {
            msg = msg + ": " + msg2;
        }
        throw new AssertionError(msg);
    }

    /**
     * Gets the list of compiled methods.
     *
     * @return Returns the compiledMethods.
     */
    @KernelSpace
    public static final CompiledCodeList getCompiledMethods() {
        return instance.compiledMethods;
    }

    /**
     * A new type has been resolved by the VM. Create a new MM type to reflect
     * the VM type, and associate the MM type with the VM type.
     *
     * @param vmType The newly resolved type
     */
    @Inline
    public static void notifyClassResolved(VmType<?> vmType) {
        final Vm instance = Vm.instance;
        if (instance != null) {
            final VmHeapManager hm = instance.heapManager;
            if (hm != null) {
                hm.notifyClassResolved(vmType);
            }
        }
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
     * @return a copy of the processors list
     */
    public final List<VmProcessor> getProcessors() {
        return new BootableArrayList<VmProcessor>(processors);
    }
}
