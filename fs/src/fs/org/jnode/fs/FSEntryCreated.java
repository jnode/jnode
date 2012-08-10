package org.jnode.fs;

import java.io.IOException;

/**
 * Interface to stack on to declare implementation of a {@code getCreated()} method, until JNode adds it to {@link FSEntry}.
 */
public interface FSEntryCreated {
    public long getCreated() throws IOException;
}
