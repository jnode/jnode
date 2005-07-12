/*
 * $Id$
 */
package org.jnode.util;

import java.io.IOException;
import java.io.InputStream;

public class EmptyInputStream extends InputStream {

    /**
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {
        return -1;
    }

}
