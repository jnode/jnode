/*
 * $Id$
 */
package javax.isolate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

import org.jnode.vm.isolate.VmStreamBindings;

public final class StreamBindings {
    
    private final VmStreamBindings bindings = new VmStreamBindings();
    
    /**
     * Gets the bindings implementation
     * @return
     */
    final VmStreamBindings getBindings() {
        return bindings;
    }

    /**
     * Bind the stderr of a new isolate to the given file stream.
     * @param stream
     */
    public StreamBindings setErr(FileOutputStream stream) {
        bindings.setErr(stream);
        return this;
    }

    /**
     * Bind the stderr of a new isolate to the given socket.
     * @param socket
     */
    public StreamBindings setErr(Socket socket) {
        bindings.setErr(socket);
        return this;
    }
    
    /**
     * Bind the stdout of a new isolate to the given file stream.
     * @param stream
     */
    public StreamBindings setOut(FileOutputStream stream) {
        bindings.setOut(stream);
        return this;
    }

    /**
     * Bind the stdout of a new isolate to the given socket.
     * @param socket
     */
    public StreamBindings setOut(Socket socket) {
        bindings.setOut(socket);
        return this;
    }

    /**
     * Bind the stdin of a new isolate to the given stream.
     * @param stream
     */
    public StreamBindings setIn(FileInputStream stream) {
        bindings.setIn(stream);
        return this;
    }
    
    /**
     * Bind the stdin of a new isolate to the given stream.
     * @param socket
     */
    public StreamBindings setIn(Socket socket) {
        bindings.setIn(socket);
        return this;
    }
}
