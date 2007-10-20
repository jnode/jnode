package org.jnode.shell;

import java.io.IOException;
import java.io.InputStream;

public class NullInputStream extends InputStream {

	@Override
	public int read() throws IOException {
		return 0;
	}
}
