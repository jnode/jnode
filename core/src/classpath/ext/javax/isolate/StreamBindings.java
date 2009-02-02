/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
