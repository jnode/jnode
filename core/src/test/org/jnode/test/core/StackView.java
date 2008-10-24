/*
 * $
 */
package org.jnode.test.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Vm;
import org.jnode.vm.classmgr.Signature;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmThread;
import org.jnode.vm.x86.VmX86StackReader;
import org.jnode.vm.x86.VmX86Thread;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * @author Levente S\u00e1ntha
 */
public class StackView {
    public static void main(String[] argv) {
        String threadName = null;
        if (argv.length > 0) {
            threadName = argv[0];
        }

        Thread thread = null;
        if (threadName == null) {
            thread = Thread.currentThread();
        } else {
            // Find the root of the ThreadGroup tree
            ThreadGroup grp = Thread.currentThread().getThreadGroup();
            while (grp.getParent() != null) {
                grp = grp.getParent();
            }

            final int max = grp.activeCount() * 2;
            final Thread[] ts = new Thread[max];
            grp.enumerate(ts);

            for (int i = 0; i < max; i++) {
                final Thread t = ts[i];
                if (t != null) {
                    if (threadName.equals(t.getName())) {
                        thread = t;
                        break;
                    }
                }
            }

            if (thread == null) {
                System.out.println("Thread not found: " + threadName);
                return;
            }
        }

        VmThread vmThread = thread.getVmThread();

        final int stackSize = vmThread.getStackSize();
        final Object stack = invoke("getStack", VmThread.class, vmThread);
        if (stack != null) {
            final Address stackBottom = ObjectReference.fromObject(stack).toAddress();
            final Address stackTop = stackBottom.add(stackSize);
            final Address stackEnd;
            if (vmThread == VmThread.currentThread()) {
                stackEnd = stackBottom;
            } else {
                stackEnd = ((VmX86Thread) vmThread).getStackPointer();
            }
            final int slotSize = (Integer) invoke("getReferenceSize", VmX86Thread.class, vmThread);
            Address stackFrame = vmThread.getStackFrame();
            System.out.println("Stack start:    " + NumberUtils.hex(stackTop.toInt()));
            System.out.println("Stack end  :    " + NumberUtils.hex(stackEnd.toInt()));

            System.out.println("Raw stack:");
            Address ptr = stackTop;


            while (ptr.GE(stackEnd)) {
                final Address child = ptr.loadAddress();
                if (child != null) {
                    System.out.print(NumberUtils.hex(ptr.toInt()) + " ");
                    if (Vm.getHeapManager().isObject(child)) {
                        System.out.println(child);
                    } else {
                        System.out.println(NumberUtils.hex(child.toInt()));
                    }
                }
                ptr = ptr.sub(slotSize);
            }

            System.out.println("Stack frames:");
            VmX86StackReader sr = new VmX86StackReader(slotSize);
            Address currFrame = stackFrame;

            //Address prevFrame = null;
            do {
                ptr = currFrame;
                //ccid
                ptr = ptr.add(Offset.fromIntSignExtend(VmX86StackReader.METHOD_ID_OFFSET * slotSize));
                int ccid = ptr.loadInt();
                if (ccid == 0) {
                    //invalid farme, exit
                    break;
                }

                System.out.println("--------------------------------------------------------------------------");
                System.out.println("Stack frame:    " + NumberUtils.hex(stackFrame.toInt()));

                VmCompiledCode cc = Vm.getCompiledMethods().get(ccid);
                VmMethod vmMethod = cc.getMethod();
                int noArguments = vmMethod.getNoArguments();
                VmType[] args = new VmType[noArguments];
                for (int i = 0; i < noArguments; i++) {
                    args[i] = vmMethod.getArgumentType(i);
                }
                System.out.print(NumberUtils.hex(ptr.toInt()) + " ");
                System.out.println(vmMethod.getDeclaringClass().getName() + "." + vmMethod.getName() +
                    Signature.toSignature(vmMethod.getReturnType(), args));

                ptr = ptr.sub(Offset.fromIntSignExtend(VmX86StackReader.METHOD_ID_OFFSET * slotSize));

                //old EBP
                System.out.println("Previous frame");
                ptr = ptr.add(Offset.fromIntSignExtend(VmX86StackReader.PREVIOUS_OFFSET * slotSize));
                System.out.print(NumberUtils.hex(ptr.toInt()) + " ");
                System.out.println(NumberUtils.hex(ptr.loadInt()));

                ptr = ptr.sub(Offset.fromIntSignExtend(VmX86StackReader.PREVIOUS_OFFSET * slotSize));

                //return Address
                System.out.println("Return address");
                ptr = ptr.add(Offset.fromIntSignExtend(VmX86StackReader.RETURNADDRESS_OFFSET * slotSize));
                System.out.print(NumberUtils.hex(ptr.toInt()) + " ");
                System.out.println(NumberUtils.hex(ptr.loadInt()));

                //method argumants
                int sc = vmMethod.getArgSlotCount();
                System.out.println("Method arguments: " + sc);
                for (int i = 0; i < sc; i++) {
                    ptr = ptr.add(slotSize);
                    System.out.print(NumberUtils.hex(ptr.toInt()) + " Arg" + (i + 1) + " = ");
                    Address child = ptr.loadAddress();
                    if (Vm.getHeapManager().isObject(child)) {
                        System.out.println("Class: " + child.getClass() + " Value: " + child);
                    } else {
                        System.out.println(NumberUtils.hex(child.toInt()));
                    }
                    //System.out.println(NumberUtils.hex(ptr.loadInt()));
                }
            } while ((currFrame = sr.getPrevious(currFrame)) != null);

        } else {
            System.out.println("Stack is null");
        }
    }

    private static Object invoke(String methodName, Class clazz, Object instance) {
        try {
            Method met = clazz.getMethod(methodName);
            return met.invoke(instance);
        } catch (Exception x) {
            x.printStackTrace();
            throw new RuntimeException(x);
        }
    }

    private static Object get(String filedName, Class clazz, Object instance) {
        try {
            Field field = clazz.getField(filedName);
            return field.get(instance);
        } catch (Exception x) {
            x.printStackTrace();
            throw new RuntimeException(x);
        }
    }
}
