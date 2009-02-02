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
 
package java.lang;

import org.jnode.vm.Vm;
import org.jnode.vm.VmSystem;

/**
 * @author Levente S\u00e1ntha
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
