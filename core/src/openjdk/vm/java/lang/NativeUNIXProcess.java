package java.lang;

import java.io.FileDescriptor;

/**
 * @see java.lang.UNIXProcess
 * @author Levente S\u00e1ntha
 */
class NativeUNIXProcess {
    /**
     * @see java.lang.UNIXProcess#waitForProcessExit(int)
     */
    private static int waitForProcessExit(UNIXProcess instance, int arg1) {
        //todo implement it
        //throw new UnsupportedOperationException();
        return -1;
    }
    /**
     * @see java.lang.UNIXProcess#forkAndExec(byte[], byte[], int, byte[], int, byte[], boolean, java.io.FileDescriptor, java.io.FileDescriptor, java.io.FileDescriptor)
     */
    private static int forkAndExec(UNIXProcess instance, byte[] arg1, byte[] arg2, int arg3, byte[] arg4, int arg5, byte[] arg6, boolean arg7, FileDescriptor arg8, FileDescriptor arg9, FileDescriptor arg10) {
        //todo implement it
        //throw new UnsupportedOperationException();
        return -1;
    }
    /**
     * @see java.lang.UNIXProcess#destroyProcess(int)
     */
    private static void destroyProcess(int arg1) {
        //todo implement it
        //throw new UnsupportedOperationException();
    }
    /**
     * @see java.lang.UNIXProcess#initIDs()
     */
    private static void initIDs() {
        
    }
}
