/*
 * $
 */
package org.jnode.awt.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class BasicGraphics extends Graphics {
    protected Rectangle clip;
    protected Point origin = new Point();
    protected Color color = Color.BLACK;
    protected Font font = new Font("Luxi Sans", Font.PLAIN, 10);

    protected BasicGraphics() {

    }

    protected BasicGraphics(BasicGraphics g) {
        this.clip = g.clip.getBounds();
        this.origin = g.origin.getLocation();
        this.color = g.color;
        this.font = g.font;
    }

    /**
     * Intersects the current clip with the specified rectangle.
     * The resulting clipping area is the intersection of the current
     * clipping area and the specified rectangle.  If there is no
     * current clipping area, either because the clip has never been
     * set, or the clip has been cleared using <code>setClip(null)</code>,
     * the specified rectangle becomes the new clip.
     * This method sets the user clip, which is independent of the
     * clipping associated with device bounds and window visibility.
     * This method can only be used to make the current clip smaller.
     * To set the current clip larger, use any of the setClip methods.
     * Rendering operations have no effect outside of the clipping area.
     *
     * @param x      the x coordinate of the rectangle to intersect the clip with
     * @param y      the y coordinate of the rectangle to intersect the clip with
     * @param width  the width of the rectangle to intersect the clip with
     * @param height the height of the rectangle to intersect the clip with
     * @see #setClip(int, int, int, int)
     * @see #setClip(java.awt.Shape)
     */
    public void clipRect(int x, int y, int width, int height) {
        Rectangle r = new Rectangle(x, y, width, height);
        _transform(r);
        if (clip == null)
            clip = r;
        else
            clip = clip.intersection(r);

        if (this.clip.width == 0 && this.clip.height == 0) {
            org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics: zero clip " + clip + "\n");
            org.jnode.vm.Unsafe.debugStackTrace();
        }
    }

    /**
     * Disposes of this graphics context and releases
     * any system resources that it is using.
     * A <code>Graphics</code> object cannot be used after
     * <code>dispose</code>has been called.
     * <p/>
     * When a Java program runs, a large number of <code>Graphics</code>
     * objects can be created within a short time frame.
     * Although the finalization process of the garbage collector
     * also disposes of the same system resources, it is preferable
     * to manually free the associated resources by calling this
     * method rather than to rely on a finalization process which
     * may not run to completion for a long period of time.
     * <p/>
     * Graphics objects which are provided as arguments to the
     * <code>paint</code> and <code>update</code> methods
     * of components are automatically released by the system when
     * those methods return. For efficiency, programmers should
     * call <code>dispose</code> when finished using
     * a <code>Graphics</code> object only if it was created
     * directly from a component or another <code>Graphics</code> object.
     *
     * @see java.awt.Graphics#finalize
     * @see java.awt.Component#paint
     * @see java.awt.Component#update
     * @see java.awt.Component#getGraphics
     * @see java.awt.Graphics#create
     */
    public void dispose() {

    }

    /**
     * Gets the current clipping area.
     * This method returns the user clip, which is independent of the
     * clipping associated with device bounds and window visibility.
     * If no clip has previously been set, or if the clip has been
     * cleared using <code>setClip(null)</code>, this method returns
     * <code>null</code>.
     *
     * @return a <code>Shape</code> object representing the
     *         current clipping area, or <code>null</code> if
     *         no clip is set.
     * @see java.awt.Graphics#getClipBounds
     * @see java.awt.Graphics#clipRect
     * @see java.awt.Graphics#setClip(int, int, int, int)
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     * @since JDK1.1
     */
    public Shape getClip() {
        if (clip == null)
            return null;

        Rectangle c = new Rectangle(clip);
        i_transform(c);
        return c;
    }

    /**
     * Returns the bounding rectangle of the current clipping area.
     * This method refers to the user clip, which is independent of the
     * clipping associated with device bounds and window visibility.
     * If no clip has previously been set, or if the clip has been
     * cleared using <code>setClip(null)</code>, this method returns
     * <code>null</code>.
     * The coordinates in the rectangle are relative to the coordinate
     * system origin of this graphics context.
     *
     * @return the bounding rectangle of the current clipping area,
     *         or <code>null</code> if no clip is set.
     * @see java.awt.Graphics#getClip
     * @see java.awt.Graphics#clipRect
     * @see java.awt.Graphics#setClip(int, int, int, int)
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     * @since JDK1.1
     */
    public Rectangle getClipBounds() {
        if (clip == null)
            return null;

        Rectangle c = new Rectangle(clip);
        i_transform(c);
        return c;
    }

    /**
     * Gets this graphics context's current color.
     *
     * @return this graphics context's current color.
     * @see java.awt.Color
     * @see java.awt.Graphics#setColor(java.awt.Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the current font.
     *
     * @return this graphics context's current font.
     * @see java.awt.Font
     * @see java.awt.Graphics#setFont(java.awt.Font)
     */
    public Font getFont() {
        return font;
    }

    /**
     * Gets the font metrics for the specified font.
     *
     * @param f the specified font
     * @return the font metrics for the specified font.
     * @see java.awt.Graphics#getFont
     * @see java.awt.FontMetrics
     * @see java.awt.Graphics#getFontMetrics()
     */
    public FontMetrics getFontMetrics(Font f) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }

    /**
     * Sets the current clipping area to an arbitrary clip shape.
     * Not all objects that implement the <code>Shape</code>
     * interface can be used to set the clip.  The only
     * <code>Shape</code> objects that are guaranteed to be
     * supported are <code>Shape</code> objects that are
     * obtained via the <code>getClip</code> method and via
     * <code>Rectangle</code> objects.  This method sets the
     * user clip, which is independent of the clipping associated
     * with device bounds and window visibility.
     *
     * @param clip the <code>Shape</code> to use to set the clip
     * @see java.awt.Graphics#getClip()
     * @see java.awt.Graphics#clipRect
     * @see java.awt.Graphics#setClip(int, int, int, int)
     * @since JDK1.1
     */
    public void setClip(Shape clip) {
        if (clip instanceof Rectangle) {
            this.clip = new Rectangle((Rectangle) clip);
            _transform(this.clip);
        } else if (clip instanceof Rectangle2D) {
            this.clip = clip.getBounds();
            _transform(this.clip);
        }
        if (this.clip.width == 0 && this.clip.height == 0) {
            org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics: zero clip " + clip + "\n");
            org.jnode.vm.Unsafe.debugStackTrace();
        }
    }

    /**
     * Sets the current clip to the rectangle specified by the given
     * coordinates.  This method sets the user clip, which is
     * independent of the clipping associated with device bounds
     * and window visibility.
     * Rendering operations have no effect outside of the clipping area.
     *
     * @param x      the <i>x</i> coordinate of the new clip rectangle.
     * @param y      the <i>y</i> coordinate of the new clip rectangle.
     * @param width  the width of the new clip rectangle.
     * @param height the height of the new clip rectangle.
     * @see java.awt.Graphics#getClip
     * @see java.awt.Graphics#clipRect
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     * @since JDK1.1
     */
    public void setClip(int x, int y, int width, int height) {
        this.clip = new Rectangle(x, y, width, height);
        _transform(this.clip);
        if (clip.width == 0 && clip.height == 0) {
            org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics: zero clip " + clip + "\n");
            org.jnode.vm.Unsafe.debugStackTrace();
        }
    }

    /**
     * Sets this graphics context's current color to the specified
     * color. All subsequent graphics operations using this graphics
     * context use this specified color.
     *
     * @param c the new rendering color.
     * @see java.awt.Color
     * @see java.awt.Graphics#getColor
     */
    public void setColor(Color c) {
        if (c != null)
            this.color = c;
    }

    /**
     * Sets this graphics context's font to the specified font.
     * All subsequent text operations using this graphics context
     * use this font. A null argument is silently ignored.
     *
     * @param font the font.
     * @see java.awt.Graphics#getFont
     * @see java.awt.Graphics#drawString(String, int, int)
     * @see java.awt.Graphics#drawBytes(byte[], int, int, int, int)
     * @see java.awt.Graphics#drawChars(char[], int, int, int, int)
     */
    public void setFont(Font font) {
        if (font != null)
            this.font = font;
    }

    /**
     * Sets the paint mode of this graphics context to overwrite the
     * destination with this graphics context's current color.
     * This sets the logical pixel operation function to the paint or
     * overwrite mode.  All subsequent rendering operations will
     * overwrite the destination with the current color.
     */
    public void setPaintMode() {
        //todo
    }

    /**
     * Sets the paint mode of this graphics context to alternate between
     * this graphics context's current color and the new specified color.
     * This specifies that logical pixel operations are performed in the
     * XOR mode, which alternates pixels between the current color and
     * a specified XOR color.
     * <p/>
     * When drawing operations are performed, pixels which are the
     * current color are changed to the specified color, and vice versa.
     * <p/>
     * Pixels that are of colors other than those two colors are changed
     * in an unpredictable but reversible manner; if the same figure is
     * drawn twice, then all pixels are restored to their original values.
     *
     * @param c1 the XOR alternation color
     */
    public void setXORMode(Color c1) {
        //todo
    }

    /**
     * Translates the origin of the graphics context to the point
     * (<i>x</i>,&nbsp;<i>y</i>) in the current coordinate system.
     * Modifies this graphics context so that its new origin corresponds
     * to the point (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's
     * original coordinate system.  All coordinates used in subsequent
     * rendering operations on this graphics context will be relative
     * to this new origin.
     *
     * @param x the <i>x</i> coordinate.
     * @param y the <i>y</i> coordinate.
     */
    public void translate(int x, int y) {
        origin.translate(x, y);
    }

    protected void _transform(Rectangle r) {
        r.x = r.x + origin.x;
        r.y = r.y + origin.y;
    }

    protected void i_transform(Rectangle r) {
        r.x = r.x - origin.x;
        r.y = r.y - origin.y;
    }
}
