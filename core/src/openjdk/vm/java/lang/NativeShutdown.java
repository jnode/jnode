package java.lang;

/**
 * @see java.lang.Shutdown
 *
 * @author Levente S\u00e1ntha
 */
class NativeShutdown {
    /**
     * @see java.lang.Shutdown#halt0(int)
     */
    private static void halt0(int status) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if(trace.length > 0) {
            StackTraceElement elem = trace[1];
            if(Shutdown.class.getName().equals(elem.getClassName()) &&
                "exit".equals(elem.getMethodName())) {
                VMRuntime.exit(status);
                //end of execution
            } 
        }

        VMRuntime.halt(status);
        //end of execution
    }
    /**
     * @see java.lang.Shutdown#runAllFinalizers()
     */
    private static void runAllFinalizers() {
        //todo implement it

    }
}
