package org.jnode.apps.jpartition;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Context {
    private final BufferedReader in;
    private final PrintStream out;
    private final ErrorReporter errorReporter;

    public Context(InputStream in, PrintStream out, ErrorReporter errorReporter) {
        InputStreamReader r = new InputStreamReader(in);
        this.in = new BufferedReader(r);

        this.out = out;
        this.errorReporter = errorReporter;
    }

    public final PrintStream getOut() {
        return out;
    }

    public final BufferedReader getIn() {
        return in;
    }

    public final ErrorReporter getErrorReporter() {
        return errorReporter;
    }
}
