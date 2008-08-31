package org.jnode.shell.io;

import java.io.InputStream;
import java.io.Reader;

public interface CommandInput extends CommandIO {

    public InputStream getInputStream();

    public Reader getReader() throws CommandIOException;
}
