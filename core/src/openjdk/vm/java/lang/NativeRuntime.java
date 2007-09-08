/*
 * $Id$
 */
package java.lang;

import org.jnode.vm.Vm;
import org.jnode.vm.VmSystem;

/**
 * @author Levente Sántha
 */
public class NativeRuntime {
    /**
     * @see Runtime#availableProcessors()
     */
    public static int availableProcessors(Runtime instance){
        return Vm.getVm().availableProcessors();
    }

    /**
     * @see Runtime#freeMemory()
     */
    public static long freeMemory(Runtime instance){
        return VmSystem.freeMemory();
    }

    /**
     * @see Runtime#totalMemory()
     */
    public static long totalMemory(Runtime instance){
        return VmSystem.totalMemory();
    }

    /**
     *  @see Runtime#maxMemory()
     */
    public static long maxMemory(Runtime instance){
        return VmSystem.totalMemory();
    }

    /**
     * @see Runtime#gc()
     */
    public static void gc(Runtime instance){
        VmSystem.gc();
    }

    /* Wormhole for calling java.lang.ref.Finalizer.runFinalization */
    private static void runFinalization0(){
        VmSystem.gc();
    }

    /**
     * @see Runtime#traceInstructions(boolean)
     */
    public static void traceInstructions(Runtime instance, boolean on){

    }

    /**
     * @see Runtime#traceMethodCalls(boolean)
     */
    public static void traceMethodCalls(Runtime instance, boolean on){

    }
}
