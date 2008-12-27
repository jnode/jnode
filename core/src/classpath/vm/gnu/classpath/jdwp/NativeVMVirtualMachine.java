package gnu.classpath.jdwp;

import java.util.Iterator;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.lang.reflect.Method;
import gnu.classpath.jdwp.util.MethodResult;
import gnu.classpath.jdwp.event.EventRequest;
import org.jnode.vm.Vm;
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
        //todo implement it
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#resumeThread(java.lang.Thread)
     */
    private static void resumeThread(Thread arg1) {
        //todo implement it
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getSuspendCount(java.lang.Thread)
     */
    private static int getSuspendCount(Thread arg1) {
        //todo implement it
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllLoadedClassesCount()
     */
    private static int getAllLoadedClassesCount() {
        //todo implement it
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllLoadedClasses()
     */
    private static Iterator getAllLoadedClasses() {
        return new Iterator() {
            private VmStaticsIterator iter = new VmStaticsIterator(Vm.getVm().getSharedStatics());
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
                return iter.next().newClass();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getClassStatus(java.lang.Class)
     */
    private static int getClassStatus(Class arg1) {
        //todo implement it
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getAllClassMethods(java.lang.Class)
     */
    private static VMMethod[] getAllClassMethods(Class arg1) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getClassMethod(java.lang.Class, long)
     */
    private static VMMethod getClassMethod(Class arg1, long arg2) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrames(java.lang.Thread, int, int)
     */
    private static ArrayList getFrames(Thread arg1, int arg2, int arg3) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrame(java.lang.Thread, java.nio.ByteBuffer)
     */
    private static VMFrame getFrame(Thread arg1, ByteBuffer arg2) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getFrameCount(java.lang.Thread)
     */
    private static int getFrameCount(Thread arg1) {
        //todo implement it
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getThreadStatus(java.lang.Thread)
     */
    private static int getThreadStatus(Thread arg1) {
        //todo implement it
        return 0;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getLoadRequests(java.lang.ClassLoader)
     */
    private static ArrayList getLoadRequests(ClassLoader arg1) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#executeMethod(java.lang.Object, java.lang.Thread, java.lang.Class, java.lang.reflect.Method, java.lang.Object[], boolean)
     */
    private static MethodResult executeMethod(Object arg1, Thread arg2, Class arg3, Method arg4, Object[] arg5, boolean arg6) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#getSourceFile(java.lang.Class)
     */
    private static String getSourceFile(Class arg1) {
        //todo implement it
        return null;
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#registerEvent(gnu.classpath.jdwp.event.EventRequest)
     */
    private static void registerEvent(EventRequest arg1) {
        //todo implement it
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#unregisterEvent(gnu.classpath.jdwp.event.EventRequest)
     */
    private static void unregisterEvent(EventRequest arg1) {
        //todo implement it
    }
    /**
     * @see gnu.classpath.jdwp.VMVirtualMachine#clearEvents(byte)
     */
    private static void clearEvents(byte arg1) {
        //todo implement it
    }

    public static void redefineClass(Class oldClass, byte[] classData){
        VmType old_type = VmType.fromClass(oldClass);
        VmType new_type = ClassDecoder.defineClass(oldClass.getName(),
                ByteBuffer.wrap(classData), false,
                VmType.fromClass(oldClass).getLoader(),
                oldClass.getProtectionDomain());
        for(int i = 0; i < old_type.getNoDeclaredMethods(); i++){
            VmMethod old_method = old_type.getDeclaredMethod(i);
            if(!old_method.isNative()){
                VmMethod new_method = new_type.getDeclaredMethod(old_method.getName(), old_method.getSignature());
                if(new_method == null) continue;
                old_method.setBytecode(new_method.getBytecode());
                old_method.resetOptLevel();
                old_method.recompile();
                System.out.println("Redefined: " + old_method);
            }
        }
    }
}
