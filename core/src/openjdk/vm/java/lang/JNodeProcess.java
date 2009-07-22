package java.lang;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 */
class JNodeProcess extends Process {

    static Process start(String[] cmdarray, java.util.Map<String, String> environment, String dir,
                                boolean redirectErrorStream) throws IOException {
        
        System.out.println("cmdarray: " + Arrays.asList(cmdarray));
        System.out.println("environment: " + environment);
        System.out.println("dir: " + dir);
        System.out.println("redirectErrorStream: " + redirectErrorStream);

        return new JNodeProcess();
    }
    
    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public InputStream getErrorStream() {
        return null;
    }

    @Override
    public int waitFor() throws InterruptedException {
        return 0;
    }

    @Override
    public int exitValue() {
        return 0;
    }

    @Override
    public void destroy() {

    }
}
