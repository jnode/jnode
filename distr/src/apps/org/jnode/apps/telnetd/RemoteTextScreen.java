package org.jnode.apps.telnetd;

import java.io.IOException;

import net.wimpi.telnetd.io.TerminalIO;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;

/**
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class RemoteTextScreen extends AbstractPcTextScreen implements TextScreen {
	private final TerminalIO terminalIO;
	private final char[] buffer;
    private int cursorOffset;

	public RemoteTextScreen(TerminalIO terminalIO)
	{
		super(terminalIO.getColumns(), terminalIO.getRows());
		this.terminalIO = terminalIO;

        buffer = new char[terminalIO.getColumns() * terminalIO.getRows()];
        for (int i = 0; i < buffer.length; i ++) {
            buffer[i] = ' ';
        }
	}

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData
     * @param rawDataOffset
     */
	@Override
    public void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            //Unsafe.die("Screen:rawDataOffset = " + rawDataOffset);
        }
        char[] cha = new char[rawData.length];
        for (int i = 0; i < cha.length; i ++) {
            char c = (char) (rawData[i] & 0xFF);
            cha[i] = (c == 0) ? ' ' : c;
        }
        System.arraycopy(cha, rawDataOffset, buffer, 0, getWidth() * getHeight());
        sync();
    }

	public void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset * 2, buffer, destOffset * 2, length * 2);
        sync();
	}

	public void copyTo(TextScreen dst) {
		// TODO Auto-generated method stub

	}

    public char getChar(int offset) {
        return buffer[offset];
    }

    public int getColor(int offset) {
        return 0;
    }

    public void set(int offset, char ch, int count, int color) {
        char c = (char) (ch & 0xFF);
        buffer[offset] = c == 0 ? ' ' : c;
        sync();
    }

    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i ++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync();
    }

    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i ++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync();
    }

	public void setCursor(int x, int y) {
		try {
			terminalIO.setCursor(x, y);
	        cursorOffset = getOffset(x, y);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCursorVisible(boolean visible) {
		// ignore : cursor will allways be visible
	}

	public void sync() {
		int offset = 0;
		try {
			terminalIO.setCursor(0, 0);
			for(int y = 0 ; y < getHeight() ; y++)
			{
				for(int x = 0 ; x < getWidth() ; x++)
				{
					terminalIO.getTelnetIO().write(buffer[offset++]);
				}
			}
			if(terminalIO.isAutoflushing())
			{
				terminalIO.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
