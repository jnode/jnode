/*
 * $
 */
package org.jnode.awt.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jnode.awt.JNodeToolkit;
import org.jnode.driver.video.Surface;
import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;
import sun.java2d.pipe.TextPipe;

/**
 * Experiemntal text pipe implementation based on the current font support in JNode.
 *
 * @author Levente S\u00e1ntha
 */
public class DefaultTextPipe implements TextPipe {
    Map<Integer, Color> colorMap = new ConcurrentHashMap<Integer, Color>();

    public void drawString(final SunGraphics2D g2d, String s, double x, double y) {
        Color co = g2d.getColor();
        Font font = g2d.getFont();
        if (font != null) {
            JNodeToolkit tk = ((JNodeToolkit) Toolkit.getDefaultToolkit());
            if (tk == null) {
                System.err.println("Toolkit is null");
                return;
            }
            if (tk.getFontManager() == null) {
                System.err.println("FontManager is null");
                return;
            }
            tk.getFontManager().drawText(new Surface() {
                public void copyArea(int x, int y, int width, int height, int dx, int dy) {
                    org.jnode.vm.Unsafe.debug("DTP copyArea()\n");
                    g2d.copyArea(x, y, width, height, dx, dy);
                }

                public void draw(Shape shape, Shape clip, AffineTransform tx, Color color, int mode) {
                    org.jnode.vm.Unsafe.debug("DTP draw()\n");
                    Rectangle b = clip.getBounds();
                    g2d.setClip(b.x, b.y, b.width, b.height);
                    g2d.setTransform(tx);
                    g2d.setColor(color);
                    g2d.draw(shape);
                }

                public void fill(Shape shape, Shape clip, AffineTransform tx, Color color, int mode) {
                    org.jnode.vm.Unsafe.debug("DTP fill()\n");
                    Rectangle b = clip.getBounds();
                    g2d.setClip(b.x, b.y, b.width, b.height);
                    g2d.setTransform(tx);
                    g2d.setColor(color);
                    g2d.fill(shape);
                }

                ImageObserver io = new ImageObserver() {
                    public boolean imageUpdate(Image image, int flags, int x, int y, int width, int height) {
                        return false;
                    }
                };

                public void drawCompatibleRaster(Raster raster, int srcX, int srcY, int dstX, int dstY, int width,
                                                 int height, Color bgColor) {
                    org.jnode.vm.Unsafe.debug("DTP drawCompatibleRaster()\n");
                    BufferedImage bi = new BufferedImage(
                        new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000),
                        raster.createCompatibleWritableRaster(), false, new Hashtable());
                    g2d.copyImage(bi, dstX, dstY, srcX, srcY, width, height, bgColor, io);
                }

                public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX, int srcY, int dstX, int dstY,
                                            int width, int height, Color color) {
                    org.jnode.vm.Unsafe.debug("DTP drawAlphaRaster\n");
                    BufferedImage bi = new BufferedImage(
                        new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000),
                        raster.createCompatibleWritableRaster(), false, new Hashtable());
                    g2d.copyImage(bi, dstX, dstY, srcX, srcY, width, height, color, io);
                }

                public ColorModel getColorModel() {
                    org.jnode.vm.Unsafe.debug("DTP getColorModel\n");
                    return null;
                }

                public int getRGBPixel(int x, int y) {
                    org.jnode.vm.Unsafe.debug("DTP getRGBPixel\n");
                    return 0;
                }


                public void setRGBPixel(int x, int y, int color) {
                    //org.jnode.vm.Unsafe.debug("DTP setRGBPixel\n");
                    SurfaceData sd = g2d.getSurfaceData();
                    if (sd instanceof BufImgSurfaceData) {
                        BufImgSurfaceData surfaceData = (BufImgSurfaceData) sd;
                        BufferedImage bi = (BufferedImage) surfaceData.getDestination();
                        bi.setRGB(x, y, color);
                    } else {
                        Color c = colorMap.get(color);
                        if (c == null) {
                            c = new Color(color);
                            colorMap.put(color, c);
                        }
                        g2d.setColor(c);
                        g2d.drawLine(x, y, x, y);
                    }
                }

                public int[] getRGBPixels(Rectangle region) {
                    org.jnode.vm.Unsafe.debug("DTP getRGBPixel2\n");
                    return new int[0];
                }

                public void close() {
                    org.jnode.vm.Unsafe.debug("DTP close\n");
                }
            }, g2d.getClip(), g2d.getTransform(), s, font, (int) x, (int) y, g2d.getColor());

        }
        g2d.setColor(co);
    }

    public void drawGlyphVector(SunGraphics2D g2d, GlyphVector g, float x, float y) {
        throw new UnsupportedOperationException();
    }

    public void drawChars(SunGraphics2D g2d, char data[], int offset, int length, int x, int y) {
        drawString(g2d, new String(data, offset, length), x, y);
    }
}
