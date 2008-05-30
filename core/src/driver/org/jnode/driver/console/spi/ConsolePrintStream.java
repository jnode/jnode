package org.jnode.driver.console.spi;

import java.io.PrintStream;

public class ConsolePrintStream extends PrintStream {
    public ConsolePrintStream(ConsoleOutputStream out) {
        super(out);
    }

    public int getFgColor() {
        return ((ConsoleOutputStream) out).getFgColor();
    }

    public void setFgColor(int color) {
        ((ConsoleOutputStream) out).setFgColor(color);
    }
}
