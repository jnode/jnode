package org.jnode.shell.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

public interface CommandOutput extends CommandIO {

    public OutputStream getOutputStream();
    
    public PrintStream getPrintStream();

    public Writer getWriter();
}
