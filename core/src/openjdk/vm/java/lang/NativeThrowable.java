/*
 * $
 */
package java.lang;

import org.jnode.vm.scheduler.VmThread;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.VmStackFrame;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmInstanceField;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Levente S\u00e1ntha
 * @see Throwable
 */
class NativeThrowable {
    private static int BACKTRACE_OFFSET = ((VmInstanceField)Throwable.class.getVmClass().getField("backtrace")).getOffset();
    private static synchronized Throwable fillInStackTrace(Throwable instance){
        ObjectReference.fromObject(instance).toAddress().add(BACKTRACE_OFFSET).
                store(ObjectReference.fromObject(VmThread.getStackTrace(VmProcessor.current().getCurrentThread())));
        return instance;
    }

    private static int getStackTraceDepth(Throwable instance) {
        return ((Object[]) ObjectReference.fromObject(instance).toAddress().add(BACKTRACE_OFFSET).
                loadObjectReference().toObject()).length;
    }

    private static StackTraceElement getStackTraceElement(Throwable instance, int index) {
        final VmStackFrame frame = (VmStackFrame) ((Object[]) ObjectReference.fromObject(instance).toAddress().add(BACKTRACE_OFFSET).
                loadObjectReference().toObject())[index];
        final String location = frame.getLocationInfo();
        final int lineNumber = "?".equals(location) ? -1 : Integer.parseInt(location);
        final VmMethod method = frame.getMethod();
        final VmType<?> vmClass = (method == null) ? null : method.getDeclaringClass();
        final String fname = (vmClass == null) ? null : vmClass.getSourceFile();
        final String cname = (vmClass == null) ? "<unknown class>" : vmClass.getName();
        final String mname = (method == null) ? "<unknown method>" : method.getName();
        return new StackTraceElement(cname, mname, fname, method == null || method.isNative() ? -2 : lineNumber);
    }
}
