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
 
package gnu.classpath.jdwp;

import java.util.Iterator;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.lang.reflect.Method;
import gnu.classpath.jdwp.util.MethodResult;
import gnu.classpath.jdwp.event.EventRequest;

import org.jnode.annotation.NoInline;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.isolate.VmIsolate;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmStaticsIterator;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.ClassDecoder;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @see gnu.classpath.jdwp.VMVirtualMachine
 *
 * @author Levente S\u00e1ntha
 */
class NativeVMVirtualMachine {
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#suspendThread(java.lang.Thread)
     */
    private static void suspendThread(Thread arg1) {
        JDIVirtualMachine.suspendThread(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#resumeThread(java.lang.Thread)
     */
    private static void resumeThread(Thread arg1) {
        JDIVirtualMachine.resumeThread(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getSuspendCount(java.lang.Thread)
     */
    private static int getSuspendCount(Thread arg1) {
        return JDIVirtualMachine.getSuspendCount(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllLoadedClassesCount()
     */
    private static int getAllLoadedClassesCount() {
        return JDIVirtualMachine.getAllLoadedClassesCount();
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllLoadedClasses()
     */
    private static Iterator getAllLoadedClasses() {
        return JDIVirtualMachine.getAllLoadedClasses();
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getClassStatus(java.lang.Class)
     */
    private static int getClassStatus(Class arg1) {
        return JDIVirtualMachine.getClassStatus(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllClassMethods(java.lang.Class)
     */
    private static VMMethod[] getAllClassMethods(Class arg1) {
        return JDIVirtualMachine.getAllClassMethods(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getClassMethod(java.lang.Class, long)
     */
    private static VMMethod getClassMethod(Class arg1, long arg2) {
        return JDIVirtualMachine.getClassMethod(arg1, arg2);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrames(java.lang.Thread, int, int)
     */
    private static ArrayList getFrames(Thread arg1, int arg2, int arg3) {
        return JDIVirtualMachine.getFrames(arg1, arg2, arg3);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrame(java.lang.Thread, java.nio.ByteBuffer)
     */
    private static VMFrame getFrame(Thread arg1, ByteBuffer arg2) {
        return JDIVirtualMachine.getFrame(arg1, arg2);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrameCount(java.lang.Thread)
     */
    private static int getFrameCount(Thread arg1) {
        return JDIVirtualMachine.getFrameCount(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getThreadStatus(java.lang.Thread)
     */
    private static int getThreadStatus(Thread arg1) {
        return JDIVirtualMachine.getThreadStatus(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getLoadRequests(java.lang.ClassLoader)
     */
    private static ArrayList getLoadRequests(ClassLoader arg1) {
        return JDIVirtualMachine.getLoadRequests(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#executeMethod(java.lang.Object, java.lang.Thread, java.lang.Class, java.lang.reflect.Method, java.lang.Object[], boolean)
     */
    private static MethodResult executeMethod(Object arg1, Thread arg2, Class arg3, Method arg4, Object[] arg5, boolean arg6) {
        return JDIVirtualMachine.executeMethod(arg1, arg2, arg3, arg4, arg5, arg6);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getSourceFile(java.lang.Class)
     */
    private static String getSourceFile(Class arg1) {
        return JDIVirtualMachine.getSourceFile(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#registerEvent(gnu.classpath.jdwp.event.EventRequest)
     */
    private static void registerEvent(EventRequest arg1) {
        JDIVirtualMachine.registerEvent(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#unregisterEvent(gnu.classpath.jdwp.event.EventRequest)
     */
    private static void unregisterEvent(EventRequest arg1) {
        JDIVirtualMachine.unregisterEvent(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#clearEvents(byte)
     */
    private static void clearEvents(byte arg1) {
        JDIVirtualMachine.clearEvents(arg1);
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#redefineClass(Class, byte[])
     */
    public static void redefineClass(Class oldClass, byte[] classData){
        JDIVirtualMachine.redefineClass(oldClass, classData);
    }
}
