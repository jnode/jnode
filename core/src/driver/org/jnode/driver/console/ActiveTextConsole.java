package org.jnode.driver.console;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.jnode.driver.console.textscreen.ConsoleKeyEventBindings;



/**
 * This virtual console class operates on the console that has the current focus.
 * The {@link #getOut()} and {@link #getErr()} return Writers that track changes 
 * to the active console.
 * 
 * @author crawley@jnode.org
 */
public class ActiveTextConsole extends VirtualTextConsole {
    // FIXME this implementation has various race conditions to do with
    // changes to the active console.  The most harmful of these is that
    // the ATCWriter classes may end up writing to an inactive console if
    // the console switch happens at just the wrong time.
    
    private TextConsole activeConsole;
    private final ConsoleManager mgr;
    private ATCWriter out;
    private ATCWriter err;
    
    public ActiveTextConsole(ConsoleManager mgr) {
        this.mgr = mgr;
        this.activeConsole = (TextConsole) mgr.getFocus();
    }

    @Override
    public String getConsoleName() {
        return "active";
    }

    /**
     * This operation returns a Writer that writes to the 'err' stream for
     * the text console that currently has 'focus'.  As the focus changes
     * (e.g. as a result of user console switching), this Writer tracks the
     * changes.
     */
    @Override
    public synchronized Writer getErr() {
        if (err == null) {
            err = new ATCWriter(true);
        }
        return err;
    }

    @Override
    public int getHeight() {
        return getActiveTextConsole().getHeight();
    }

    /**
     * This operation is not supported because the resulting Reader would
     * behave in a way that breaks JNode's conceptual model of virtual consoles.
     */
    @Override
    public Reader getIn() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConsoleManager getManager() {
        return mgr;
    }

    /**
     * This operation returns a Writer that writes to the 'out' stream for
     * the text console that currently has 'focus'.  As the focus changes
     * (e.g. as a result of user console switching), this Writer tracks the
     * changes.
     */
    @Override
    public synchronized Writer getOut() {
        if (out == null) {
            out = new ATCWriter(false);
        }
        return out;
    }

    @Override
    public int getTabSize() {
        return getActiveTextConsole().getTabSize();
    }

    @Override
    public int getWidth() {
        return getActiveTextConsole().getWidth();
    }
    
    @Override
    public ConsoleKeyEventBindings getKeyEventBindings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setKeyEventBindings(ConsoleKeyEventBindings bindings) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the current active console; i.e. the one that currently has focus.
     * Note that this may change at any instant.
     * 
     * @return the currently active TextConsole, or the most recent one if
     *         the currently active Console is not a TextConsole.
     */
    public synchronized TextConsole getActiveTextConsole() {
        if (!activeConsole.isFocused()) {
            Console tmp = mgr.getFocus();
            if (tmp instanceof TextConsole) {
                activeConsole = (TextConsole) tmp;
            }
        }
        return activeConsole;
    }
    
    /**
     * This internal Writer writes to the current active console.
     */
    private class ATCWriter extends Writer {
        private final boolean isErr;
        
        public ATCWriter(boolean isErr) {
            super();
            this.isErr = isErr;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void flush() throws IOException {
            getWriter().flush();
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            getWriter().write(cbuf, off, len);
        }

        @Override
        public void write(int c) throws IOException {
            getWriter().write(c);
        }
        
        private Writer getWriter() {
            return isErr ? getActiveTextConsole().getErr() :
                getActiveTextConsole().getOut();
        }
    }
}
