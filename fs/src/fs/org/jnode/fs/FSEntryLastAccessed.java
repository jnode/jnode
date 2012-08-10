package org.jnode.fs;

import java.io.IOException;

/**
 * Interface to stack on to declare implementation of a {@code getLastAccessed()} method, until JNode adds it to {@link FSEntry}.
 */
public interface FSEntryLastAccessed {
    public long getLastAccessed() throws IOException;
}
