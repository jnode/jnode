package org.jnode.shell;

import java.io.IOException;
import java.io.InputStream;

/**
 * A NullInputStream instance is the logical equivalent of "/dev/null".  Calling a read method
 * returns -1 to indicate EOF.
 * 
 * @author Stephen Crawley
 */
public class NullInputStream extends InputStream {

	@Override
	public int read() throws IOException {
		// Return the EOF indication
		return -1;
	}
}
