package org.jnode.apps.telnetd;

import java.io.IOException;

import net.wimpi.telnetd.io.TelnetIO;
import net.wimpi.telnetd.io.TerminalIO;

import org.jnode.driver.textscreen.x86.AbstractPcBufferTextScreen;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class RemoteTextScreen extends AbstractPcBufferTextScreen {
    private final TerminalIO terminalIO;

    public RemoteTextScreen(TerminalIO terminalIO) {
        super(terminalIO.getColumns(), terminalIO.getRows());
        this.terminalIO = terminalIO;
    }

    @Override
    protected void sync(int offset, int length) {
        try {
            final int y = offset / getWidth();
            final int x = offset % getWidth();
            terminalIO.setCursor(y, x);

            final TelnetIO telnetIO = terminalIO.getTelnetIO();
            
            int offs = offset;
            for (int i = 0; i < length; i++) {
                //TODO is that proper way to manage colors ?
                terminalIO.setForegroundColor(getColor(offs++));
                
                telnetIO.write(getChar(offs++));
            }
            if (terminalIO.isAutoflushing()) {
                terminalIO.flush();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
