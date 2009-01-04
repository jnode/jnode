package java.lang;

import org.jnode.vm.isolate.VmIsolate;
import org.jnode.vm.VmExit;
import javax.isolate.Isolate;

/**
 * @author Levente S\u00e1ntha
 * @see java.lang.Shutdown
 */
class NativeShutdown {
    /**
     * @see java.lang.Shutdown#halt0(int)
     */
    private static void halt0(int status) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 0) {
            StackTraceElement elem = trace[1];
            if (Shutdown.class.getName().equals(elem.getClassName()) &&
                "exit".equals(elem.getMethodName())) {
                vmExit(status);
                //end of execution
            }
        }

        vmHalt(status);
        //end of execution
    }

    /**
     * @see java.lang.Shutdown#runAllFinalizers()
     */
    private static void runAllFinalizers() {
        //todo implement it

    }

    /**
     * Native method that actually shuts down the virtual machine.
     *
     * @param status the status to end the process with
     */
    static void vmExit(int status) {
        if (VmIsolate.getRoot() == VmIsolate.currentIsolate()) {
            throw new VmExit(status);
        } else {
            VmIsolate.currentIsolate().systemExit(Isolate.currentIsolate(), status);
        }
    }

    /**
     * Native method that actually shuts down the virtual machine.
     *
     * @param status the status to end the process with
     */
    static void vmHalt(int status) {
        if (VmIsolate.getRoot() == VmIsolate.currentIsolate()) {
            throw new VmExit(status);
        } else {
            VmIsolate.currentIsolate().systemHalt(Isolate.currentIsolate(), status);
        }
    }
}
