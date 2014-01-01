/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package gnu.classpath.jdwp;

import gnu.classpath.jdwp.event.EventRequest;
import gnu.classpath.jdwp.util.MethodResult;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import org.jnode.annotation.NoInline;
import org.jnode.vm.BaseVmArchitecture;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.classmgr.ClassDecoder;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticsIterator;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.isolate.VmIsolate;

/**
 * User: lsantha
 * Date: 6/26/11 10:53 AM
 */
public class JDIVirtualMachine {
    @NoInline
    static boolean debug() {
        return false;
    }

    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#suspendThread(java.lang.Thread)
     */
    @NoInline
    static void suspendThread(Thread arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.suspendThread()");
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#resumeThread(java.lang.Thread)
     */
    @NoInline
    static void resumeThread(Thread arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.resumeThread()");
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getSuspendCount(java.lang.Thread)
     */
    @NoInline
    static int getSuspendCount(Thread arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getSuspendCount()");
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllLoadedClassesCount()
     */
    @NoInline
    static int getAllLoadedClassesCount() {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getAllLoadedClassesCount()");
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllLoadedClasses()
     */
    @NoInline
    static Iterator getAllLoadedClasses() {
        if(debug())
            System.out.println("NativeVMVirtualMachine.getAllLoadedClasses()");
        return new Iterator() {
            private VmStaticsIterator iter = new VmStaticsIterator(VmUtils.getVm().getSharedStatics());
            private Iterator<VmIsolatedStatics> isolated = VmIsolate.staticsIterator();

            public boolean hasNext() {
                if (iter.hasNext())
                    return true;
                else {
                    while (isolated.hasNext()) {
                        iter = new VmStaticsIterator(isolated.next());
                        if (iter.hasNext())
                            return true;
                    }
                }
                return false;
            }

            public Object next() {
                return iter.next().asClass();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getClassStatus(java.lang.Class)
     */
    @NoInline
    static int getClassStatus(Class arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getClassStatus()");
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllClassMethods(java.lang.Class)
     */
    @NoInline
    static VMMethod[] getAllClassMethods(Class arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getAllClassMethods()");
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getClassMethod(java.lang.Class, long)
     */
    @NoInline
    static VMMethod getClassMethod(Class arg1, long arg2) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getClassMethod()");
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrames(java.lang.Thread, int, int)
     */
    @NoInline
    static ArrayList getFrames(Thread arg1, int arg2, int arg3) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getFrame()");
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrame(java.lang.Thread, java.nio.ByteBuffer)
     */
    @NoInline
    static VMFrame getFrame(Thread arg1, ByteBuffer arg2) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getFrame()");
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrameCount(java.lang.Thread)
     */
    @NoInline
    static int getFrameCount(Thread arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getFrameCount()");
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getThreadStatus(java.lang.Thread)
     */
    @NoInline
    static int getThreadStatus(Thread thread) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getThreadStatus()");

//        public static final int ZOMBIE = 0;
//        public static final int RUNNING = 1;
//        public static final int SLEEPING = 2;
//        public static final int MONITOR = 3;
//        public static final int WAIT = 4;

        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getLoadRequests(java.lang.ClassLoader)
     */
    @NoInline
    static ArrayList getLoadRequests(ClassLoader arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getLoadRequest()");
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#executeMethod(java.lang.Object, java.lang.Thread, java.lang.Class, java.lang.reflect.Method, java.lang.Object[], boolean)
     */
    @NoInline
    static MethodResult executeMethod(Object arg1, Thread arg2, Class arg3, Method arg4, Object[] arg5, boolean arg6) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.executeMethod()");
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getSourceFile(java.lang.Class)
     */
    @NoInline
    static String getSourceFile(Class arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.getSourceFile()");
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#registerEvent(gnu.classpath.jdwp.event.EventRequest)
     */
    @NoInline
    static void registerEvent(EventRequest arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.registerEvent() " + arg1.getId() + " " + arg1.getEventKind() +
                " " + arg1.getSuspendPolicy() +  " " + arg1.getFilters());
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#unregisterEvent(gnu.classpath.jdwp.event.EventRequest)
     */
    @NoInline
    static void unregisterEvent(EventRequest arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.unregisterEvent()");
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#clearEvents(byte)
     */
    @NoInline
    static void clearEvents(byte arg1) {
        //todo implement it
        if(debug())
            System.out.println("NativeVMVirtualMachine.clearEvents()");
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#redefineClass(Class, byte[])
     */
    @NoInline
    static void redefineClass(Class oldClass, byte[] classData){
        if(debug())
            System.out.println("NativeVMVirtualMachine.redefineClass()");

        String name = oldClass.getName();
        VmType old_type = VmType.fromClass(oldClass);
        VmClassLoader loader = VmType.fromClass(oldClass).getLoader();
        ProtectionDomain pd = oldClass.getProtectionDomain();
        VmType new_type = ClassDecoder.defineClass(name, ByteBuffer.wrap(classData), false, loader, pd);
        for(int i = 0; i < old_type.getNoDeclaredMethods(); i++){
            VmMethod old_method = old_type.getDeclaredMethod(i);
            if (!old_method.isNative()) {
                VmMethod new_method = new_type.getDeclaredMethod(old_method.getName(), old_method.getSignature());
                if(new_method == null) continue;

                old_method.setBytecode(new_method.getBytecode());
                old_method.resetOptLevel();
                old_method.recompile();
                System.out.println("Redefined method: " + old_method.getFullName());
            }
        }
    }
}
