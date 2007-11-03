package org.jnode.shell;

import java.io.Closeable;

/**
 * Instances of this class are used to denote distinguished streams; for example, when 
 * building a CommandLine.  They should be translated into real streams before the
 * command is actually invoked.
 * 
 * @author crawley@jnode.org
 */
public final class StreamMarker implements Closeable {
    private final String name;

    public StreamMarker(String name) {
        this.name = name;
    }

    public void close() {
        // Dummy operation
    }

    public String toString() {
        return name;
    }
}