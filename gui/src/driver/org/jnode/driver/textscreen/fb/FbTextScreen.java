/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.jnode.driver.textscreen.TextScreen;
import org.jnode.driver.textscreen.x86.AbstractPcTextScreen;
import org.jnode.driver.video.Surface;


class FbTextScreen extends AbstractPcTextScreen {
    private final Font font;
    private final char[] buffer;

    private int cursorOffset;
    private boolean cursorVisible = true;

    private FbScreenPainter painter;
    private final Surface surface;
    private final Background background;
    private final BufferedImage bufferedImage;
    private final Graphics graphics;
    private final int margin;
    
    /**
     * 
     * @param g
     * @param width in pixels
     * @param height in pixels
     */
    public FbTextScreen(Surface g, BufferedImage bufferedImage, Graphics graphics, Font font, int nbColumns, 
            int nbRows, int margin) {
        super(nbColumns, nbRows);
        buffer = new char[getWidth() * getHeight()];        
        Arrays.fill(buffer, ' ');
        
        //this.background = new DefaultBackground(Color.BLACK);
        this.background = new GradientBackground(bufferedImage.getWidth(), bufferedImage.getHeight());
        
        this.surface = g;
        this.bufferedImage = bufferedImage;
        this.graphics = graphics;
        this.font = font;
        this.margin = margin;
        
        open();
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
        sync(offset, count);
    }

    public void set(int offset, char[] ch, int chOfs, int length, int color) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync(offset, length);
    }

    public void set(int offset, char[] ch, int chOfs, int length, int[] colors, int colorsOfs) {
        char[] cha = new char[ch.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (ch[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }
        System.arraycopy(cha, chOfs, buffer, offset, length);
        sync(offset, length);
    }

    public void copyContent(int srcOffset, int destOffset, int length) {
        System.arraycopy(buffer, srcOffset * 2, buffer, destOffset * 2, length * 2);
        sync(destOffset, length);
    }

    public void copyTo(TextScreen dst, int offset, int length) {

    }

    public void sync(int offset, int length) {
        if (painter != null) {
            painter.repaint();
        }
    }

    public int setCursor(int x, int y) {
        cursorOffset = getOffset(x, y);
        return cursorOffset;
    }

    public int setCursorVisible(boolean visible) {
        cursorVisible = visible;
        return cursorOffset;
    }

    /**
     * Copy the content of the given rawData into this screen.
     *
     * @param rawData the data as a char array
     * @param rawDataOffset the offset in the data array
     */
    @Override
    public void copyFrom(char[] rawData, int rawDataOffset) {
        if (rawDataOffset < 0) {
            // Unsafe.die("Screen:rawDataOffset = " + rawDataOffset);
        }
        char[] cha = new char[rawData.length];
        for (int i = 0; i < cha.length; i++) {
            char c = (char) (rawData[i] & 0xFF);
            cha[i] = c == 0 ? ' ' : c;
        }

        final int length = getWidth() * getHeight();
        System.arraycopy(cha, rawDataOffset, buffer, 0, length);
        sync(0, length);
    }

    char[] getBuffer() {
        return buffer;
    }

    class FbScreenPainter {
        private final Thread painterThread;

        private boolean stop = false;
        private boolean update = true;

        public FbScreenPainter() {
            painterThread = new Thread(new Runnable() {
                public void run() {
                    while (!stop) {
                        try {
                            paintComponent();
                            synchronized (FbScreenPainter.this) {
                                if (update) {
                                    update = false;
                                    FbScreenPainter.this.wait();
                                }
                            }
                        } catch (InterruptedException x) {
                            break;
                        }
                    }
                }
            }, "FbScreenPainter");
            painterThread.start();
        }
        
        private void stop() {
            this.stop = true; 
        }
            
        protected void paintComponent() {
            background.paint(graphics);
            
            graphics.setColor(Color.WHITE);
            graphics.setFont(font);
            
            final int fontHeight = graphics.getFontMetrics().getHeight();
            
            final char[] textBuffer = getBuffer();
            final int length = getWidth();
            int offset = 0;
            final int x = margin;
            int y = fontHeight + margin;            
            
            for (int i = 0; i < getHeight(); i++) {
                graphics.drawChars(textBuffer, offset, length, x, y);
                
                offset += length;
                y += fontHeight;
            }
            surface.drawCompatibleRaster(bufferedImage.getRaster(), 0, 0, 0, 0, bufferedImage.getWidth(), 
                    bufferedImage.getHeight(), Color.BLACK);
        }
        
        public synchronized void repaint() {
            if (!update) {
                update = true;
                notifyAll();
            }
        }
    }

    void close() {
        if (painter != null) {
            painter.stop();
            painter = null;
        }
    }
    
    void open() {
        if (painter == null) {
            painter = new FbScreenPainter();
        }
    }    
}
