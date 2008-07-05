package org.jnode.driver.video.cursor;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;

import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.HardwareCursorImage;
import org.jnode.driver.video.Surface;
import org.jnode.vm.Unsafe;

public class SoftwareCursor extends BitmapGraphics implements HardwareCursorAPI {
    private BitmapGraphics graphics;
    private HardwareCursorImage cursorImage;
    private boolean cursorVisible = false;
    private int[] screenBackup;
    private Rectangle cursorArea = new Rectangle(0, 0, 0, 0);
    private Rectangle screenArea = new Rectangle();

    public SoftwareCursor(BitmapGraphics graphics) {
        setBitmapGraphics(graphics);
    }

    public void setBitmapGraphics(BitmapGraphics graphics) {
        if (this.graphics != graphics) {
            hideCursor();

            this.graphics = graphics;
            if (cursorImage != null) {
                int newHotspotX =
                        (int) Math.min(graphics.getWidth() - 1, cursorArea.getX() +
                                cursorImage.getHotSpotX());
                int newHotspotY =
                        (int) Math.min(graphics.getHeight() - 1, cursorArea.getY() +
                                cursorImage.getHotSpotY());
                cursorArea.setLocation(newHotspotX - cursorImage.getHotSpotX(), newHotspotY -
                        cursorImage.getHotSpotY());
            }

            // TODO test when screen resolution is changing
            showCursor();
        }
    }

    @Override
    public void copyArea(int srcX, int srcY, int w, int h, int dstX, int dstY) {

        // TODO don't take cursor pixels for the source

        final boolean intersects = intersectsCursor(dstX, dstY, w, h);
        if (intersects) {
            hideCursor();
        }

        graphics.copyArea(srcX, srcY, w, h, dstX, dstY);

        if (intersects) {
            showCursor();
        }
    }

    @Override
    public int doGetPixel(int x, int y) {
        // TODO don't take cursor pixels
        return graphics.doGetPixel(x, y);
    }

    @Override
    public int[] doGetPixels(Rectangle r) {
        // TODO don't take cursor pixels
        return graphics.doGetPixels(r);
    }

    @Override
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX, int srcY, int dstX,
            int dstY, int w, int h, int color) {

        final boolean intersects = intersectsCursor(dstX, dstY, w, h);
        if (intersects) {
            hideCursor();
        }

        graphics.drawAlphaRaster(raster, tx, srcX, srcY, dstX, dstY, w, h, color);

        if (intersects) {
            showCursor();
        }
    }

    @Override
    public void drawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int w, int h) {
        final boolean intersects = intersectsCursor(dstX, dstY, w, h);
        if (intersects) {
            hideCursor();
        }

        graphics.drawImage(src, srcX, srcY, dstX, dstY, w, h);

        if (intersects) {
            showCursor();
        }
    }

    @Override
    public void drawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int w, int h,
            int bgColor) {
        final boolean intersects = intersectsCursor(dstX, dstY, w, h);
        if (intersects) {
            hideCursor();
        }

        graphics.drawImage(src, srcX, srcY, dstX, dstY, w, h, bgColor);

        if (intersects) {
            showCursor();
        }
    }

    @Override
    public void drawLine(int x, int y, int w, int color, int mode) {
        final boolean intersects = intersectsCursor(x, y, w, 1);
        if (intersects) {
            hideCursor();
        }

        graphics.drawLine(x, y, w, color, mode);

        if (intersects) {
            showCursor();
        }
    }

    @Override
    public void drawPixels(int x, int y, int count, int color, int mode) {
        final boolean intersects = intersectsCursor(x, y, count, 1);
        if (intersects) {
            hideCursor();
        }

        graphics.drawPixels(x, y, count, color, mode);

        if (intersects) {
            showCursor();
        }
    }

    public int getWidth() {
        return graphics.getWidth();
    }

    public int getHeight() {
        return graphics.getHeight();
    }

    public void setCursorImage(HardwareCursor cursor) {
        if (cursor == null) {
            return;
        }

        try {
            final HardwareCursorImage cursImage = cursor.getImage(16, 16);

            if ((cursImage != null) && (this.cursorImage != cursImage)) {
                hideCursor();

                cursorArea.setSize(cursImage.getWidth(), cursImage.getHeight());
                if (cursorImage != null) {
                    int newX =
                            (int) (cursorArea.getX() + cursorImage.getHotSpotX() - cursImage
                                    .getHotSpotX());
                    int newY =
                            (int) (cursorArea.getY() + cursorImage.getHotSpotY() - cursImage
                                    .getHotSpotY());
                    cursorArea.setLocation(newX, newY);
                }
                this.cursorImage = cursImage;

                showCursor();
            }
        } catch (Throwable t) {
            Unsafe.debugStackTrace();
            Unsafe.debug("\nerror in setCursorImage (" + t.getClass().getName() + ") " +
                    t.getMessage() + "\n");
        }
    }

    public void setCursorPosition(int x, int y) {
        try {
            // x,y corresponds to the location of the cursor's hotspot on the
            // screen
            // it can be anywhere in the screen area (but some part of the
            // cursor might not be visible)
            x = Math.min(Math.max(x, 0), graphics.getWidth() - 1);
            y = Math.min(Math.max(y, 0), graphics.getHeight() - 1);

            if ((cursorArea.getX() != x) || (cursorArea.getY() != y)) {
                hideCursor();

                if (cursorImage != null) {
                    int newX = (int) (x - cursorImage.getHotSpotX());
                    int newY = (int) (y - cursorImage.getHotSpotY());
                    cursorArea.setLocation(newX, newY);
                } else {
                    cursorArea.setLocation(x, y);
                }

                showCursor();
            }
        } catch (Throwable t) {
            Unsafe.debugStackTrace();
            Unsafe.debug("\nerror in setCursorPosition (" + t.getClass().getName() + ") " +
                    t.getMessage() + "\n");
        }
    }

    public void setCursorVisible(boolean visible) {
        try {
            if (this.cursorVisible != visible) {
                this.cursorVisible = visible;

                if (visible) {
                    showCursor();
                } else {
                    hideCursor();
                }
            }
        } catch (Throwable t) {
            Unsafe.debugStackTrace();
            Unsafe.debug("\nerror in setCursorVisible (" + t.getClass().getName() + ") " +
                    t.getMessage() + "\n");
        }
    }

    private boolean intersectsCursor(int x, int y, int width, int height) {
        boolean intersects = false;

        if (cursorVisible && (width > 0) && (height > 0)) {
            screenArea.setBounds(x, y, width, height);
            intersects = cursorArea.intersects(screenArea);
        }

        return intersects;
    }

    private void showCursor() {
        if (cursorImage != null) {
            if (screenBackup == null) {
                screenBackup = new int[cursorImage.getWidth() * cursorImage.getHeight()];
            }

            // screenBackup = graphics.doGetPixels(cursorArea);
            final int cursorX = (int) cursorArea.getX();
            final int cursorY = (int) cursorArea.getY();
            final int maxY = Math.min(cursorY + cursorImage.getHeight(), graphics.getHeight());
            final int maxX = Math.min(cursorX + cursorImage.getWidth(), graphics.getWidth());
            final int width = cursorImage.getWidth();

            int index = 0;
            for (int y = cursorY; y < maxY; y++) {
                int lineIndex = index;
                for (int x = cursorX; x < maxX; x++) {
                    screenBackup[lineIndex] = graphics.doGetPixel(x, y);
                    lineIndex++;
                }

                index += width;
            }

            putPixels(cursorImage.getImage(), screenBackup);
        }
    }

    private void hideCursor() {
        if ((cursorImage != null) && (screenBackup != null)) {
            putPixels(screenBackup, null);
        }
    }

    private void putPixels(int[] pixels, int[] background) {
        final int cursorX = (int) cursorArea.getX();
        final int cursorY = (int) cursorArea.getY();
        final int maxY = Math.min(cursorY + cursorImage.getHeight(), graphics.getHeight());
        final int maxX = Math.min(cursorX + cursorImage.getWidth(), graphics.getWidth());
        final int width = cursorImage.getWidth();

        int index = 0;
        for (int y = cursorY; y < maxY; y++) {
            int lineIndex = index;
            for (int x = cursorX; x < maxX; x++) {
                int color;
                if (background == null) {
                    color = pixels[lineIndex];
                } else {
                    final int c = pixels[lineIndex];
                    final boolean isTransparent = (c == 0);
                    color = isTransparent ? background[lineIndex] : c;
                }

                graphics.drawPixels(x, y, 1, color, Surface.PAINT_MODE);
                lineIndex++;
            }

            index += width;
        }
    }
}
