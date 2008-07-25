package org.jnode.driver.textscreen.fb;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.jnode.driver.video.Surface;

class FbScreenPainter {
    private static final int margin = 5;
    private static final int w = 7;
    private static final int h = 18;
    
    private final Surface surface;
    private final int sw;
    private final int sh;
    private final BufferedImage buffer;
    private final Graphics graphics;
    private final Font font;
    private final FbTextScreen screen;
    private final Thread painterThread;

    private final Background background;
        
    private boolean update;

    public FbScreenPainter(FbTextScreen screen, Surface g) {
        this.screen = screen;
        this.surface = g;
        sh = h * screen.getHeight() + 2 * margin;
        sw = w * screen.getWidth() + 2 * margin;
        
        //background = new DefaultBackground(Color.BLACK);
        background = new GradientBackground(sw, sh);
        
        buffer = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
        graphics = buffer.getGraphics();
        font = new Font(
                "-FontForge-Bitstream Vera Sans Mono-Book-R-Normal-SansMono--12-120-75-75-P-69-ISO10646",
                Font.PLAIN, 12);

        painterThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        paintComponent();
                        synchronized (FbScreenPainter.this) {
                            if (!update) {
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
        
    protected void paintComponent() {
        background.paint(graphics);
        
        graphics.setColor(Color.WHITE);
        graphics.setFont(font);
        
        final char[] textBuffer = screen.getBuffer();
        final int length = screen.getWidth();
        int offset = 0;
        final int x = margin;
        int y = h;
        
        for (int i = 0; i < screen.getHeight(); i++) {
            graphics.drawChars(textBuffer, offset, length, x, y);
            
            offset += length;
            y += h;
        }
        surface.drawCompatibleRaster(buffer.getRaster(), 0, 0, 0, 0, sw, sh, Color.BLACK);
    }
    
    public synchronized void repaint() {
        //Unsafe.debug("repaint");
        if (!update) {
            update = true;
            notifyAll();
        }
    }
}
