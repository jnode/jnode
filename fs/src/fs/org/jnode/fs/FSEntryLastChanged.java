package org.jnode.fs;

import java.io.IOException;

/**
 * Interface to stack on to declare implementation of a {@code getLastChanged()} method, until JNode adds it to {@link FSEntry}.
 */
public interface FSEntryLastChanged {
    public long getLastChanged() throws IOException;
}
