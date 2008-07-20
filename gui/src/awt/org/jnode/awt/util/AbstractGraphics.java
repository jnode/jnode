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

package org.jnode.awt.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * @deprecated 
 * @author epr
 */
public abstract class AbstractGraphics extends Graphics2D {

    private static final Stroke DEFAULT_STROKE = new BasicStroke();

    private Color bgColor = Color.WHITE;

    private Color fgColor = Color.BLACK;

    private Color xorColor = null;

    private Font font = new Font("Luxi Sans", Font.PLAIN, 10);

    Shape clip;

    private Stroke stroke = DEFAULT_STROKE;

    AffineTransform transform = new AffineTransform();

    private Paint paint = Color.WHITE;

    private Composite composite;

    /**
     * Initialize this instance
     *
     * @param width
     * @param height
     */
    public AbstractGraphics(int width, int height) {
        clip = new Rectangle(0, 0, width, height);
    }

    /**
     * Initialize this instance as a copy of the given instance
     *
     * @param src
     */
    public AbstractGraphics(AbstractGraphics src) {
        this.bgColor = src.bgColor;
        this.fgColor = src.fgColor;
        this.xorColor = src.xorColor;
        this.font = src.font;
        this.clip = src.clip;
        this.stroke = src.stroke;
        this.transform = new AffineTransform(src.transform);
        this.paint = src.paint;
        this.composite = src.composite;
    }

    /**
     * @param hints
     * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
     */
    public void addRenderingHints(Map hints) {
        // TODO Auto-generated method stub

    }

    /**
     * @param s
     * @see java.awt.Graphics2D#clip(java.awt.Shape)
     */
    public void clip(Shape s) {
        this.clip = s;
    }

    /**
     * @param shape
     * @see java.awt.Graphics2D#draw(java.awt.Shape)
     */
    public abstract void draw(Shape shape);

    /**
     * @param image
     * @param op
     * @param x
     * @param y
     * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage,
     *      java.awt.image.BufferedImageOp, int, int)
     */
    public void drawImage(BufferedImage image, BufferedImageOp op, int x, int y) {
        final Image img1 = op.filter(image, null);
        drawImage(img1, new AffineTransform(1f, 0f, 0f, 1f, x, y), null);
    }

    /**
     * @param image
     * @param xform
     * @param obs
     * @return boolean
     * @see java.awt.Graphics2D#drawImage(java.awt.Image,
     *      java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image image, AffineTransform xform,
                             ImageObserver obs) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param image
     * @param xform
     * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage,
     *      java.awt.geom.AffineTransform)
     */
    public void drawRenderableImage(RenderableImage image, AffineTransform xform) {
        // TODO Auto-generated method stub

    }

    /**
     * @param image
     * @param xform
     * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage,
     *      java.awt.geom.AffineTransform)
     */
    public void drawRenderedImage(RenderedImage image, AffineTransform xform) {
        // TODO Auto-generated method stub

    }

    /**
     * @param iterator
     * @param x
     * @param y
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator,
     *      float, float)
     */
    public void drawString(AttributedCharacterIterator iterator, float x,
                           float y) {
        // TODO Auto-generated method stub

    }

    /**
     * @param iterator
     * @param x
     * @param y
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator,
     *      int, int)
     */
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        drawString(iterator, (float) x, (float) y);
    }

    /**
     * @param text
     * @param x
     * @param y
     * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
     */
    public void drawString(String text, float x, float y) {
        this.drawString(text, Math.round(x), Math.round(y));
    }

    /**
     * @param shape
     * @see java.awt.Graphics2D#fill(java.awt.Shape)
     */
    public abstract void fill(Shape shape);

    /**
     * @return The background color
     * @see java.awt.Graphics2D#getBackground()
     */
    public Color getBackground() {
        return bgColor;
    }

    /**
     * @return Composite
     * @see java.awt.Graphics2D#getComposite()
     */
    public Composite getComposite() {
        return composite;
    }

    /**
     * @return The configuration
     * @see java.awt.Graphics2D#getDeviceConfiguration()
     */
    public abstract GraphicsConfiguration getDeviceConfiguration();

    /**
     * @return Paint
     * @see java.awt.Graphics2D#getPaint()
     */
    public Paint getPaint() {
        return paint;
    }

    /**
     * @param hintKey
     * @return Object
     * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
     */
    public Object getRenderingHint(Key hintKey) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return The hints
     * @see java.awt.Graphics2D#getRenderingHints()
     */
    public RenderingHints getRenderingHints() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return The stroke
     * @see java.awt.Graphics2D#getStroke()
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * @return The transform
     * @see java.awt.Graphics2D#getTransform()
     */
    public AffineTransform getTransform() {
        return new AffineTransform(transform);
    }

    /**
     * @param rect
     * @param text
     * @param onStroke
     * @return boolean
     * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
     */
    public boolean hit(Rectangle rect, Shape text, boolean onStroke) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param theta
     * @param x
     * @param y
     * @see java.awt.Graphics2D#rotate(double, double, double)
     */
    public void rotate(double theta, double x, double y) {
        transform(AffineTransform.getRotateInstance(theta, x, y));
    }

    /**
     * @param theta
     * @see java.awt.Graphics2D#rotate(double)
     */
    public void rotate(double theta) {
        transform(AffineTransform.getRotateInstance(theta));
    }

    /**
     * @param scaleX
     * @param scaleY
     * @see java.awt.Graphics2D#scale(double, double)
     */
    public void scale(double scaleX, double scaleY) {
        transform(AffineTransform.getScaleInstance(scaleX, scaleY));
    }

    /**
     * @param color
     * @see java.awt.Graphics2D#setBackground(java.awt.Color)
     */
    public void setBackground(Color color) {
        if (color != null)
            this.bgColor = color;
    }

    /**
     * @param comp
     * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
     */
    public void setComposite(Composite comp) {
        this.composite = comp;
    }

    /**
     * @param paint
     * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
     */
    public void setPaint(Paint paint) {
        if (paint != null) {
            this.paint = paint;
        }
    }

    /**
     * @param hintKey
     * @param hintValue
     * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key,
     *      java.lang.Object)
     */
    public void setRenderingHint(Key hintKey, Object hintValue) {
        // TODO Auto-generated method stub

    }

    /**
     * @param hints
     * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
     */
    public void setRenderingHints(Map hints) {
        // TODO Auto-generated method stub

    }

    /**
     * @param stroke
     * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
     */
    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
    }

    /**
     * @param Tx
     * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
     */
    public void setTransform(AffineTransform Tx) {
        this.transform = Tx;
    }

    /**
     * @param shearX
     * @param shearY
     * @see java.awt.Graphics2D#shear(double, double)
     */
    public void shear(double shearX, double shearY) {
        transform(AffineTransform.getScaleInstance(shearX, shearY));
    }

    /**
     * @param tx
     * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
     */
    public void transform(AffineTransform tx) {
        transform.concatenate(tx);
        // Adjust clip
        /*
        Rectangle2D r = clip.getBounds2D();
        double[] coords = new double[]{r.getX(), r.getY(),
            r.getX() + r.getWidth(), r.getY() + r.getHeight()};
        try {
            tx.createInverse().transform(coords, 0, coords, 0, 2);
            r.setRect(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1]);
            clip = r;
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        */
    }

    /**
     * @param tx
     * @param ty
     * @see java.awt.Graphics2D#translate(double, double)
     */
    public void translate(double tx, double ty) {
        //transform(AffineTransform.getTranslateInstance(tx, ty));
        //transform.translate(tx,ty);
        translate((int) tx, (int) ty);
    }

    /**
     * @param x
     * @param y
     * @see java.awt.Graphics#translate(int, int)
     */
    public void translate(int x, int y) {
        transform.translate(x, y);
        //adjust clip
        if (clip != null) {
            if (clip instanceof Rectangle) {
                Rectangle r = (Rectangle) clip;
                r.translate(x, y);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see java.awt.Graphics#clearRect(int, int, int, int)
     */
    public void clearRect(int x, int y, int width, int height) {
        final Color saveColor = getColor();
        setColor(bgColor);
        fillRect(x, y, width, height);
        setColor(saveColor);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see java.awt.Graphics#clipRect(int, int, int, int)
     */
    public void clipRect(int x, int y, int width, int height) {
        Rectangle r = new Rectangle(x, y, width, height);
        if (transform != null)
            r.translate((int) transform.getTranslateX(), (int) transform.getTranslateY());

        if (clip == null)
            clip = r;
        else if (clip instanceof Rectangle) {
            clip = ((Rectangle) clip).intersection(r);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param dx
     * @param dy
     * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
     */
    public abstract void copyArea(int x, int y, int width, int height, int dx,
                                  int dy);

    /**
     * @return The graphics
     * @see java.awt.Graphics#create()
     */
    public abstract Graphics create();

    /**
     * @see java.awt.Graphics#dispose()
     */
    public void dispose() {
        // Nothing to do here
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param startAngle
     * @param arcAngle
     * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
     */
    public void drawArc(int x, int y, int width, int height, int startAngle,
                        int arcAngle) {
        draw(new Arc2D.Float(x, y, width, height, startAngle, arcAngle,
            Arc2D.OPEN));
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param bgcolor
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int,
     *      java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image image, int x, int y, Color bgcolor,
                             ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int,
     *      java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image image, int x, int y, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     * @param bgcolor
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
     *      java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image image, int x, int y, int width, int height,
                             Color bgcolor, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
     *      java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image image, int x, int y, int width, int height,
                             ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param image
     * @param dx1
     * @param dy1
     * @param dx2
     * @param dy2
     * @param sx1
     * @param sy1
     * @param sx2
     * @param sy2
     * @param bgcolor
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int,
     *      int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2, Color bgcolor,
                             ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param image
     * @param dx1
     * @param dy1
     * @param dx2
     * @param dy2
     * @param sx1
     * @param sy1
     * @param sx2
     * @param sy2
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int,
     *      int, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2,
                             int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @see java.awt.Graphics#drawLine(int, int, int, int)
     */
    public final void drawLine(int x1, int y1, int x2, int y2) {
        draw(new Line2D.Float(x1, y1, x2, y2));
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see java.awt.Graphics#drawOval(int, int, int, int)
     */
    public final void drawOval(int x, int y, int width, int height) {
        draw(new Ellipse2D.Float(x, y, width, height));
    }

    /**
     * @param xPoints
     * @param yPoints
     * @param npoints
     * @see java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public final void drawPolygon(int[] xPoints, int[] yPoints, int npoints) {
        draw(new Polygon(xPoints, yPoints, npoints));
    }

    /**
     * @param xPoints
     * @param yPoints
     * @param npoints
     * @see java.awt.Graphics#drawPolyline(int[], int[], int)
     */
    public final void drawPolyline(int[] xPoints, int[] yPoints, int npoints) {
        final GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO,
            npoints * 2);
        path.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < npoints; i++) {
            path.lineTo(xPoints[i], yPoints[i]);
        }
        draw(path);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param arcWidth
     * @param arcHeight
     * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
     */
    public final void drawRoundRect(int x, int y, int width, int height,
                                    int arcWidth, int arcHeight) {
        draw(new RoundRectangle2D.Float(x, y, width, height, arcWidth,
            arcHeight));
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param startAngle
     * @param arcAngle
     * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
     */
    public final void fillArc(int x, int y, int width, int height,
                              int startAngle, int arcAngle) {
        fill(new Arc2D.Float(x, y, width, height, startAngle, arcAngle,
            Arc2D.OPEN));
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see java.awt.Graphics#fillOval(int, int, int, int)
     */
    public final void fillOval(int x, int y, int width, int height) {
        fill(new Ellipse2D.Float(x, y, width, height));
    }

    /**
     * @param xPoints
     * @param yPoints
     * @param npoints
     * @see java.awt.Graphics#fillPolygon(int[], int[], int)
     */
    public final void fillPolygon(int[] xPoints, int[] yPoints, int npoints) {
        fill(new Polygon(xPoints, yPoints, npoints));
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see java.awt.Graphics#fillRect(int, int, int, int)
     */
    public final void fillRect(int x, int y, int width, int height) {
        fill(new Rectangle2D.Float(x, y, width, height));
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param arcWidth
     * @param arcHeight
     * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
     */
    public final void fillRoundRect(int x, int y, int width, int height,
                                    int arcWidth, int arcHeight) {
        fill(new RoundRectangle2D.Float(x, y, width, height, arcWidth,
            arcHeight));
    }

    /**
     * @return Shape
     * @see java.awt.Graphics#getClip()
     */
    public Shape getClip() {
        return clip;
    }

    /**
     * @return The clip bounds
     * @see java.awt.Graphics#getClipBounds()
     */
    public Rectangle getClipBounds() {
        return clip.getBounds();
    }

    /**
     * @return The color
     * @see java.awt.Graphics#getColor()
     */
    public Color getColor() {
        return fgColor;
    }

    /**
     * Gets the color set by setXORColor, or null when this Graphics is in Paint
     * mode.
     *
     * @return The XOR mode color
     */
    public Color getXORColor() {
        return xorColor;
    }

    /**
     * @return The current font
     * @see java.awt.Graphics#getFont()
     */
    public Font getFont() {
        return font;
    }

    /**
     * @param font
     * @return The metrics
     * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
     */
    @SuppressWarnings("deprecation")
    public FontMetrics getFontMetrics(Font font) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @see java.awt.Graphics#setClip(int, int, int, int)
     */
    public void setClip(int x, int y, int width, int height) {
        Rectangle r = new Rectangle(x, y, width, height);
        if (transform != null)
            r.translate((int) transform.getTranslateX(), (int) transform.getTranslateY());
        clip = new Rectangle(x, y, width, height);
    }

    /**
     * @param clip
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     */
    public void setClip(Shape clip) {
        this.clip = clip;
    }

    /**
     * @param color
     * @see java.awt.Graphics#setColor(java.awt.Color)
     */
    public void setColor(Color color) {
        if (color != null)
            this.fgColor = color;
    }

    /**
     * @param font
     * @see java.awt.Graphics#setFont(java.awt.Font)
     */
    public void setFont(Font font) {
        if (font != null)
            this.font = font;
    }

    /**
     * @see java.awt.Graphics#setPaintMode()
     */
    public void setPaintMode() {
        this.xorColor = null;
    }

    /**
     * @param color
     * @see java.awt.Graphics#setXORMode(java.awt.Color)
     */
    public void setXORMode(Color color) {
        this.xorColor = color;
    }

    protected final void transform(double[] srcPts, int srcOff,
                                   double[] dstPts, int dstOff, int num) {
        transform.transform(srcPts, srcOff, dstPts, dstOff, num);
    }

    protected final void transform(float[] srcPts, int srcOff, float[] dstPts,
                                   int dstOff, int num) {
        transform.transform(srcPts, srcOff, dstPts, dstOff, num);
    }

    protected final void transform(double[] srcPts, int srcOff, float[] dstPts,
                                   int dstOff, int num) {
        transform.transform(srcPts, srcOff, dstPts, dstOff, num);
    }

    protected final void transform(float[] srcPts, int srcOff, double[] dstPts,
                                   int dstOff, int num) {
        transform.transform(srcPts, srcOff, dstPts, dstOff, num);
    }

    public FontRenderContext getFontRenderContext() {
        //TODO review this
        return new FontRenderContext(new AffineTransform(), false, false);
    }

    /**
     * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector,
     *      float, float)
     */
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        // TODO Auto-generated method stub

    }

}
