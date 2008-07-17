/*
 * $
 */
package org.jnode.awt.util;

import java.awt.AlphaComposite;
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
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class SurfaceGraphics2D extends Graphics2D {
    private static final BasicStroke DEFAULT_STROKE = new BasicStroke();
    private static final HashMap DEFAULT_HINTS;

    static {
        HashMap hints = new HashMap();
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
        DEFAULT_HINTS = hints;
    }

    private final BasicSurfaceGraphics simpleGraphics;
    private final AbstractSurface surface;
    private int mode = Surface.PAINT_MODE;
    private Color background = Color.WHITE;
    private AffineTransform transform = new AffineTransform();
    private Composite composite = AlphaComposite.SrcOver;
    private Stroke stroke = DEFAULT_STROKE;
    private Paint paint;
    private RenderingHints renderingHints = new RenderingHints(DEFAULT_HINTS);

    protected SurfaceGraphics2D(AbstractSurface surface) {
        this.simpleGraphics = new BasicSurfaceGraphics(surface);
        this.surface = surface;
    }

    protected SurfaceGraphics2D(SurfaceGraphics2D g) {
        this.simpleGraphics = new BasicSurfaceGraphics(g.simpleGraphics);
        this.surface = g.surface;
        this.mode = g.mode;
        this.background = g.background;
        this.transform = g.transform;
        this.composite = g.composite;
        this.stroke = g.stroke;
        this.paint = g.paint;
        this.renderingHints = g.renderingHints;
    }

    /**
     * Sets the values of an arbitrary number of preferences for the
     * rendering algorithms.
     * Only values for the rendering hints that are present in the
     * specified <code>Map</code> object are modified.
     * All other preferences not present in the specified
     * object are left unmodified.
     * Hint categories include controls for rendering quality and
     * overall time/quality trade-off in the rendering process.
     * Refer to the <code>RenderingHints</code> class for definitions of
     * some common keys and values.
     *
     * @param hints the rendering hints to be set
     * @see java.awt.RenderingHints
     */
    public void addRenderingHints(Map<?, ?> hints) {
        renderingHints.putAll(hints);
    }

    /**
     * Intersects the current <code>Clip</code> with the interior of the
     * specified <code>Shape</code> and sets the <code>Clip</code> to the
     * resulting intersection.  The specified <code>Shape</code> is
     * transformed with the current <code>Graphics2D</code>
     * <code>Transform</code> before being intersected with the current
     * <code>Clip</code>.  This method is used to make the current
     * <code>Clip</code> smaller.
     * To make the <code>Clip</code> larger, use <code>setClip</code>.
     * The <i>user clip</i> modified by this method is independent of the
     * clipping associated with device bounds and visibility.  If no clip has
     * previously been set, or if the clip has been cleared using
     * {@link java.awt.Graphics#setClip(java.awt.Shape) setClip} with a <code>null</code>
     * argument, the specified <code>Shape</code> becomes the new
     * user clip.
     *
     * @param s the <code>Shape</code> to be intersected with the current
     *          <code>Clip</code>.  If <code>s</code> is <code>null</code>,
     *          this method clears the current <code>Clip</code>.
     */
    public void clip(Shape s) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.clip() not implemented\n");
    }

    /**
     * Strokes the outline of a <code>Shape</code> using the settings of the
     * current <code>Graphics2D</code> context.  The rendering attributes
     * applied include the <code>Clip</code>, <code>Transform</code>,
     * <code>Paint</code>, <code>Composite</code> and
     * <code>Stroke</code> attributes.
     *
     * @param s the <code>Shape</code> to be rendered
     * @see #setStroke
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #clip
     * @see #setClip
     * @see #setComposite
     */
    public void draw(Shape s) {
        //todo G2D clip & transfom
        //org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.draw() not implemented\n");
        Point p = simpleGraphics.origin;
        AffineTransform t = AffineTransform.getTranslateInstance(p.x, p.y);
        surface.draw(s, simpleGraphics.getClip(), t, getColor(), mode);
    }

    /**
     * Renders the text of the specified
     * {@link java.awt.font.GlyphVector} using
     * the <code>Graphics2D</code> context's rendering attributes.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, <code>Paint</code>, and
     * <code>Composite</code> attributes.  The <code>GlyphVector</code>
     * specifies individual glyphs from a {@link java.awt.Font}.
     * The <code>GlyphVector</code> can also contain the glyph positions.
     * This is the fastest way to render a set of characters to the
     * screen.
     *
     * @param g the <code>GlyphVector</code> to be rendered
     * @param x the x position in User Space where the glyphs should
     *          be rendered
     * @param y the y position in User Space where the glyphs should
     *          be rendered
     * @throws NullPointerException if <code>g</code> is <code>null</code>.
     * @see java.awt.Font#createGlyphVector
     * @see java.awt.font.GlyphVector
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.drawGlyphVector() not implemented\n");
    }

    /**
     * Renders a <code>BufferedImage</code> that is
     * filtered with a
     * {@link java.awt.image.BufferedImageOp}.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>
     * and <code>Composite</code> attributes.  This is equivalent to:
     * <pre>
     * img1 = op.filter(img, null);
     * drawImage(img1, new AffineTransform(1f,0f,0f,1f,x,y), null);
     * </pre>
     *
     * @param op  the filter to be applied to the image before rendering
     * @param img the specified <code>BufferedImage</code> to be rendered.
     *            This method does nothing if <code>img</code> is null.
     * @param x   the x coordinate of the location in user space where
     *            the upper left corner of the image is rendered
     * @param y   the y coordinate of the location in user space where
     *            the upper left corner of the image is rendered
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.drawImage() not implemented\n");
    }

    /**
     * Renders an image, applying a transform from image space into user space
     * before drawing.
     * The transformation from user space into device space is done with
     * the current <code>Transform</code> in the <code>Graphics2D</code>.
     * The specified transformation is applied to the image before the
     * transform attribute in the <code>Graphics2D</code> context is applied.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, and <code>Composite</code> attributes.
     * Note that no rendering is done if the specified transform is
     * noninvertible.
     *
     * @param img   the specified image to be rendered.
     *              This method does nothing if <code>img</code> is null.
     * @param xform the transformation from image space into user space
     * @param obs   the {@link java.awt.image.ImageObserver}
     *              to be notified as more of the <code>Image</code>
     *              is converted
     * @return <code>true</code> if the <code>Image</code> is
     *         fully loaded and completely rendered, or if it's null;
     *         <code>false</code> if the <code>Image</code> is still being loaded.
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.drawImage2() not implemented\n");
        return false;
    }

    /**
     * Renders a
     * {@link java.awt.image.renderable.RenderableImage},
     * applying a transform from image space into user space before drawing.
     * The transformation from user space into device space is done with
     * the current <code>Transform</code> in the <code>Graphics2D</code>.
     * The specified transformation is applied to the image before the
     * transform attribute in the <code>Graphics2D</code> context is applied.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, and <code>Composite</code> attributes. Note
     * that no rendering is done if the specified transform is
     * noninvertible.
     * <p/>
     * Rendering hints set on the <code>Graphics2D</code> object might
     * be used in rendering the <code>RenderableImage</code>.
     * If explicit control is required over specific hints recognized by a
     * specific <code>RenderableImage</code>, or if knowledge of which hints
     * are used is required, then a <code>RenderedImage</code> should be
     * obtained directly from the <code>RenderableImage</code>
     * and rendered using
     * {@link #drawRenderedImage(java.awt.image.RenderedImage , java.awt.geom.AffineTransform) drawRenderedImage}.
     *
     * @param img   the image to be rendered. This method does
     *              nothing if <code>img</code> is null.
     * @param xform the transformation from image space into user space
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     * @see #drawRenderedImage
     */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.drawRenderableImage() not implemented\n");
    }

    /**
     * Renders a {@link java.awt.image.RenderedImage},
     * applying a transform from image
     * space into user space before drawing.
     * The transformation from user space into device space is done with
     * the current <code>Transform</code> in the <code>Graphics2D</code>.
     * The specified transformation is applied to the image before the
     * transform attribute in the <code>Graphics2D</code> context is applied.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, and <code>Composite</code> attributes. Note
     * that no rendering is done if the specified transform is
     * noninvertible.
     *
     * @param img   the image to be rendered. This method does
     *              nothing if <code>img</code> is null.
     * @param xform the transformation from image space into user space
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.drawRenderedImage() not implemented\n");
    }

    /**
     * Renders the text of the specified iterator applying its attributes
     * in accordance with the specification of the {@link java.awt.font.TextAttribute} class.
     * <p/>
     * The baseline of the first character is at position
     * (<i>x</i>,&nbsp;<i>y</i>) in User Space.
     * For characters in script systems such as Hebrew and Arabic,
     * the glyphs can be rendered from right to left, in which case the
     * coordinate supplied is the location of the leftmost character
     * on the baseline.
     *
     * @param iterator the iterator whose text is to be rendered
     * @param x        the x coordinate where the iterator's text is to be
     *                 rendered
     * @param y        the y coordinate where the iterator's text is to be
     *                 rendered
     * @throws NullPointerException if <code>iterator</code> is
     *                              <code>null</code>
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.drawString() not implemented\n");
    }

    /**
     * Renders the text specified by the specified <code>String</code>,
     * using the current text attribute state in the <code>Graphics2D</code> context.
     * The baseline of the first character is at position
     * (<i>x</i>,&nbsp;<i>y</i>) in the User Space.
     * The rendering attributes applied include the <code>Clip</code>,
     * <code>Transform</code>, <code>Paint</code>, <code>Font</code> and
     * <code>Composite</code> attributes. For characters in script systems
     * such as Hebrew and Arabic, the glyphs can be rendered from right to
     * left, in which case the coordinate supplied is the location of the
     * leftmost character on the baseline.
     *
     * @param str the <code>String</code> to be rendered
     * @param x   the x coordinate of the location where the
     *            <code>String</code> should be rendered
     * @param y   the y coordinate of the location where the
     *            <code>String</code> should be rendered
     * @throws NullPointerException if <code>str</code> is
     *                              <code>null</code>
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see java.awt.Graphics#setFont
     * @see #setTransform
     * @see #setComposite
     * @see #setClip
     */
    public void drawString(String str, float x, float y) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.drawString2() not implemented\n");
    }

    /**
     * Fills the interior of a <code>Shape</code> using the settings of the
     * <code>Graphics2D</code> context. The rendering attributes applied
     * include the <code>Clip</code>, <code>Transform</code>,
     * <code>Paint</code>, and <code>Composite</code>.
     *
     * @param s the <code>Shape</code> to be filled
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     * @see #transform
     * @see #setTransform
     * @see #setComposite
     * @see #clip
     * @see #setClip
     */
    public void fill(Shape s) {
        //org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.fill() todo G2D clip & transfom\n");
        //todo G2D clip & transfom
        Point p = simpleGraphics.origin;
        AffineTransform t = AffineTransform.getTranslateInstance(p.x, p.y);
        surface.fill(s, simpleGraphics.getClip(), t, getColor(), mode);
    }

    /**
     * Returns the background color used for clearing a region.
     *
     * @return the current <code>Graphics2D</code> <code>Color</code>,
     *         which defines the background color.
     * @see #setBackground
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Returns the current <code>Composite</code> in the
     * <code>Graphics2D</code> context.
     *
     * @return the current <code>Graphics2D</code> <code>Composite</code>,
     *         which defines a compositing style.
     * @see #setComposite
     */
    public Composite getComposite() {
        return composite;
    }

    /**
     * Returns the device configuration associated with this
     * <code>Graphics2D</code>.
     *
     * @return the device configuration of this <code>Graphics2D</code>.
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        //todo implement it
        return null;
    }

    /**
     * Get the rendering context of the <code>Font</code> within this
     * <code>Graphics2D</code> context.
     * The {@link java.awt.font.FontRenderContext}
     * encapsulates application hints such as anti-aliasing and
     * fractional metrics, as well as target device specific information
     * such as dots-per-inch.  This information should be provided by the
     * application when using objects that perform typographical
     * formatting, such as <code>Font</code> and
     * <code>TextLayout</code>.  This information should also be provided
     * by applications that perform their own layout and need accurate
     * measurements of various characteristics of glyphs such as advance
     * and line height when various rendering hints have been applied to
     * the text rendering.
     *
     * @return a reference to an instance of FontRenderContext.
     * @see java.awt.font.FontRenderContext
     * @see java.awt.Font#createGlyphVector
     * @see java.awt.font.TextLayout
     * @since 1.2
     */

    public FontRenderContext getFontRenderContext() {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.getFontRendererContext() not implemented\n");
        return null;
    }

    /**
     * Returns the current <code>Paint</code> of the
     * <code>Graphics2D</code> context.
     *
     * @return the current <code>Graphics2D</code> <code>Paint</code>,
     *         which defines a color or pattern.
     * @see #setPaint
     * @see java.awt.Graphics#setColor
     */
    public Paint getPaint() {
        if (paint == null)
            return simpleGraphics.getColor();
        else
            return paint;
    }

    /**
     * Returns the value of a single preference for the rendering algorithms.
     * Hint categories include controls for rendering quality and overall
     * time/quality trade-off in the rendering process.  Refer to the
     * <code>RenderingHints</code> class for definitions of some common
     * keys and values.
     *
     * @param hintKey the key corresponding to the hint to get.
     * @return an object representing the value for the specified hint key.
     *         Some of the keys and their associated values are defined in the
     *         <code>RenderingHints</code> class.
     * @see java.awt.RenderingHints
     * @see #setRenderingHint(java.awt.RenderingHints.Key, Object)
     */
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        return renderingHints.get(hintKey);
    }

    /**
     * Gets the preferences for the rendering algorithms.  Hint categories
     * include controls for rendering quality and overall time/quality
     * trade-off in the rendering process.
     * Returns all of the hint key/value pairs that were ever specified in
     * one operation.  Refer to the
     * <code>RenderingHints</code> class for definitions of some common
     * keys and values.
     *
     * @return a reference to an instance of <code>RenderingHints</code>
     *         that contains the current preferences.
     * @see java.awt.RenderingHints
     * @see #setRenderingHints(java.util.Map)
     */
    public RenderingHints getRenderingHints() {
        return renderingHints;
    }

    /**
     * Returns the current <code>Stroke</code> in the
     * <code>Graphics2D</code> context.
     *
     * @return the current <code>Graphics2D</code> <code>Stroke</code>,
     *         which defines the line style.
     * @see #setStroke
     */
    public Stroke getStroke() {
        return stroke;
    }

    /**
     * Returns a copy of the current <code>Transform</code> in the
     * <code>Graphics2D</code> context.
     *
     * @return the current <code>AffineTransform</code> in the
     *         <code>Graphics2D</code> context.
     * @see #transform
     * @see #setTransform
     */
    public AffineTransform getTransform() {
        return new AffineTransform(transform);
    }

    /**
     * Checks whether or not the specified <code>Shape</code> intersects
     * the specified {@link java.awt.Rectangle}, which is in device
     * space. If <code>onStroke</code> is false, this method checks
     * whether or not the interior of the specified <code>Shape</code>
     * intersects the specified <code>Rectangle</code>.  If
     * <code>onStroke</code> is <code>true</code>, this method checks
     * whether or not the <code>Stroke</code> of the specified
     * <code>Shape</code> outline intersects the specified
     * <code>Rectangle</code>.
     * The rendering attributes taken into account include the
     * <code>Clip</code>, <code>Transform</code>, and <code>Stroke</code>
     * attributes.
     *
     * @param rect     the area in device space to check for a hit
     * @param s        the <code>Shape</code> to check for a hit
     * @param onStroke flag used to choose between testing the
     *                 stroked or the filled shape.  If the flag is <code>true</code>, the
     *                 <code>Stroke</code> oultine is tested.  If the flag is
     *                 <code>false</code>, the filled <code>Shape</code> is tested.
     * @return <code>true</code> if there is a hit; <code>false</code>
     *         otherwise.
     * @see #setStroke
     * @see #fill
     * @see #draw
     * @see #transform
     * @see #setTransform
     * @see #clip
     * @see #setClip
     */
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.hit() not implemented\n");
        return false;
    }

    /**
     * Concatenates the current <code>Graphics2D</code>
     * <code>Transform</code> with a rotation transform.
     * Subsequent rendering is rotated by the specified radians relative
     * to the previous origin.
     * This is equivalent to calling <code>transform(R)</code>, where R is an
     * <code>AffineTransform</code> represented by the following matrix:
     * <pre>
     *           [   cos(theta)    -sin(theta)    0   ]
     *           [   sin(theta)     cos(theta)    0   ]
     *           [       0              0         1   ]
     * </pre>
     * Rotating with a positive angle theta rotates points on the positive
     * x axis toward the positive y axis.
     *
     * @param theta the angle of rotation in radians
     */
    public void rotate(double theta) {
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.rotate1() invoked\n");
        transform.concatenate(AffineTransform.getRotateInstance(theta));
    }

    /**
     * Concatenates the current <code>Graphics2D</code>
     * <code>Transform</code> with a translated rotation
     * transform.  Subsequent rendering is transformed by a transform
     * which is constructed by translating to the specified location,
     * rotating by the specified radians, and translating back by the same
     * amount as the original translation.  This is equivalent to the
     * following sequence of calls:
     * <pre>
     *           translate(x, y);
     *           rotate(theta);
     *           translate(-x, -y);
     * </pre>
     * Rotating with a positive angle theta rotates points on the positive
     * x axis toward the positive y axis.
     *
     * @param theta the angle of rotation in radians
     * @param x     the x coordinate of the origin of the rotation
     * @param y     the y coordinate of the origin of the rotation
     */
    public void rotate(double theta, double x, double y) {
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.rotate2() invoked\n");
        transform.concatenate(AffineTransform.getRotateInstance(theta, x, y));
    }

    /**
     * Concatenates the current <code>Graphics2D</code>
     * <code>Transform</code> with a scaling transformation
     * Subsequent rendering is resized according to the specified scaling
     * factors relative to the previous scaling.
     * This is equivalent to calling <code>transform(S)</code>, where S is an
     * <code>AffineTransform</code> represented by the following matrix:
     * <pre>
     *           [   sx   0    0   ]
     *           [   0    sy   0   ]
     *           [   0    0    1   ]
     * </pre>
     *
     * @param sx the amount by which X coordinates in subsequent
     *           rendering operations are multiplied relative to previous
     *           rendering operations.
     * @param sy the amount by which Y coordinates in subsequent
     *           rendering operations are multiplied relative to previous
     *           rendering operations.
     */
    public void scale(double sx, double sy) {
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.scale() invoked\n");
        transform.concatenate(AffineTransform.getScaleInstance(sx, sy));
    }

    /**
     * Sets the background color for the <code>Graphics2D</code> context.
     * The background color is used for clearing a region.
     * When a <code>Graphics2D</code> is constructed for a
     * <code>Component</code>, the background color is
     * inherited from the <code>Component</code>. Setting the background color
     * in the <code>Graphics2D</code> context only affects the subsequent
     * <code>clearRect</code> calls and not the background color of the
     * <code>Component</code>.  To change the background
     * of the <code>Component</code>, use appropriate methods of
     * the <code>Component</code>.
     *
     * @param color the background color that isused in
     *              subsequent calls to <code>clearRect</code>
     * @see #getBackground
     * @see java.awt.Graphics#clearRect
     */
    public void setBackground(Color color) {
        this.background = color;
    }

    /**
     * Sets the <code>Composite</code> for the <code>Graphics2D</code> context.
     * The <code>Composite</code> is used in all drawing methods such as
     * <code>drawImage</code>, <code>drawString</code>, <code>draw</code>,
     * and <code>fill</code>.  It specifies how new pixels are to be combined
     * with the existing pixels on the graphics device during the rendering
     * process.
     * <p>If this <code>Graphics2D</code> context is drawing to a
     * <code>Component</code> on the display screen and the
     * <code>Composite</code> is a custom object rather than an
     * instance of the <code>AlphaComposite</code> class, and if
     * there is a security manager, its <code>checkPermission</code>
     * method is called with an <code>AWTPermission("readDisplayPixels")</code>
     * permission.
     *
     * @param comp the <code>Composite</code> object to be used for rendering
     * @throws SecurityException if a custom <code>Composite</code> object is being
     *                           used to render to the screen and a security manager
     *                           is set and its <code>checkPermission</code> method
     *                           does not allow the operation.
     * @see java.awt.Graphics#setXORMode
     * @see java.awt.Graphics#setPaintMode
     * @see #getComposite
     * @see java.awt.AlphaComposite
     * @see SecurityManager#checkPermission
     * @see java.awt.AWTPermission
     */
    public void setComposite(Composite comp) {
        this.composite = comp;
    }

    /**
     * Sets the <code>Paint</code> attribute for the
     * <code>Graphics2D</code> context.  Calling this method
     * with a <code>null</code> <code>Paint</code> object does
     * not have any effect on the current <code>Paint</code> attribute
     * of this <code>Graphics2D</code>.
     *
     * @param paint the <code>Paint</code> object to be used to generate
     *              color during the rendering process, or <code>null</code>
     * @see java.awt.Graphics#setColor
     * @see #getPaint
     * @see java.awt.GradientPaint
     * @see java.awt.TexturePaint
     */
    public void setPaint(Paint paint) {
        if (paint == null)
            return;

        this.paint = paint;
        if (paint instanceof Color) {
            simpleGraphics.setColor((Color) paint);
        }
    }

    /**
     * Sets the value of a single preference for the rendering algorithms.
     * Hint categories include controls for rendering quality and overall
     * time/quality trade-off in the rendering process.  Refer to the
     * <code>RenderingHints</code> class for definitions of some common
     * keys and values.
     *
     * @param hintKey   the key of the hint to be set.
     * @param hintValue the value indicating preferences for the specified
     *                  hint category.
     * @see #getRenderingHint(java.awt.RenderingHints.Key)
     * @see java.awt.RenderingHints
     */
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        renderingHints.put(hintKey, hintValue);
    }

    /**
     * Replaces the values of all preferences for the rendering
     * algorithms with the specified <code>hints</code>.
     * The existing values for all rendering hints are discarded and
     * the new set of known hints and values are initialized from the
     * specified {@link java.util.Map} object.
     * Hint categories include controls for rendering quality and
     * overall time/quality trade-off in the rendering process.
     * Refer to the <code>RenderingHints</code> class for definitions of
     * some common keys and values.
     *
     * @param hints the rendering hints to be set
     * @see #getRenderingHints
     * @see java.awt.RenderingHints
     */
    public void setRenderingHints(Map<?, ?> hints) {
        renderingHints.clear();
        renderingHints.putAll(hints);
    }

    /**
     * Sets the <code>Stroke</code> for the <code>Graphics2D</code> context.
     *
     * @param s the <code>Stroke</code> object to be used to stroke a
     *          <code>Shape</code> during the rendering process
     * @see java.awt.BasicStroke
     * @see #getStroke
     */
    public void setStroke(Stroke s) {
        this.stroke = s;
    }

    /**
     * Overwrites the Transform in the <code>Graphics2D</code> context.
     * WARNING: This method should <b>never</b> be used to apply a new
     * coordinate transform on top of an existing transform because the
     * <code>Graphics2D</code> might already have a transform that is
     * needed for other purposes, such as rendering Swing
     * components or applying a scaling transformation to adjust for the
     * resolution of a printer.
     * <p>To add a coordinate transform, use the
     * <code>transform</code>, <code>rotate</code>, <code>scale</code>,
     * or <code>shear</code> methods.  The <code>setTransform</code>
     * method is intended only for restoring the original
     * <code>Graphics2D</code> transform after rendering, as shown in this
     * example:
     * <pre><blockquote>
     * // Get the current transform
     * AffineTransform saveAT = g2.getTransform();
     * // Perform transformation
     * g2d.transform(...);
     * // Render
     * g2d.draw(...);
     * // Restore original transform
     * g2d.setTransform(saveAT);
     * </blockquote></pre>
     *
     * @param Tx the <code>AffineTransform</code> that was retrieved
     *           from the <code>getTransform</code> method
     * @see #transform
     * @see #getTransform
     * @see java.awt.geom.AffineTransform
     */
    public void setTransform(AffineTransform Tx) {
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.setTransform() invoked\n");
        this.transform = new AffineTransform(Tx);
    }

    /**
     * Concatenates the current <code>Graphics2D</code>
     * <code>Transform</code> with a shearing transform.
     * Subsequent renderings are sheared by the specified
     * multiplier relative to the previous position.
     * This is equivalent to calling <code>transform(SH)</code>, where SH
     * is an <code>AffineTransform</code> represented by the following
     * matrix:
     * <pre>
     *           [   1   shx   0   ]
     *           [  shy   1    0   ]
     *           [   0    0    1   ]
     * </pre>
     *
     * @param shx the multiplier by which coordinates are shifted in
     *            the positive X axis direction as a function of their Y coordinate
     * @param shy the multiplier by which coordinates are shifted in
     *            the positive Y axis direction as a function of their X coordinate
     */
    public void shear(double shx, double shy) {
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.shear() invoked\n");
        transform.concatenate(AffineTransform.getShearInstance(shx, shy));
    }

    /**
     * Composes an <code>AffineTransform</code> object with the
     * <code>Transform</code> in this <code>Graphics2D</code> according
     * to the rule last-specified-first-applied.  If the current
     * <code>Transform</code> is Cx, the result of composition
     * with Tx is a new <code>Transform</code> Cx'.  Cx' becomes the
     * current <code>Transform</code> for this <code>Graphics2D</code>.
     * Transforming a point p by the updated <code>Transform</code> Cx' is
     * equivalent to first transforming p by Tx and then transforming
     * the result by the original <code>Transform</code> Cx.  In other
     * words, Cx'(p) = Cx(Tx(p)).  A copy of the Tx is made, if necessary,
     * so further modifications to Tx do not affect rendering.
     *
     * @param Tx the <code>AffineTransform</code> object to be composed with
     *           the current <code>Transform</code>
     * @see #setTransform
     * @see java.awt.geom.AffineTransform
     */
    public void transform(AffineTransform Tx) {
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.transform() invoked\n");
        transform.concatenate(Tx);
    }

    /**
     * Concatenates the current
     * <code>Graphics2D</code> <code>Transform</code>
     * with a translation transform.
     * Subsequent rendering is translated by the specified
     * distance relative to the previous position.
     * This is equivalent to calling transform(T), where T is an
     * <code>AffineTransform</code> represented by the following matrix:
     * <pre>
     *           [   1    0    tx  ]
     *           [   0    1    ty  ]
     *           [   0    0    1   ]
     * </pre>
     *
     * @param tx the distance to translate along the x-axis
     * @param ty the distance to translate along the y-axis
     */
    public void translate(double tx, double ty) {
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics2D.translate() invoked\n");
        transform.concatenate(AffineTransform.getTranslateInstance(tx, ty));
    }

    //Basic Graphics Related Methods

    public Graphics create() {
        return simpleGraphics.create();
    }

    public void translate(int x, int y) {
        simpleGraphics.translate(x, y);
    }

    public Color getColor() {
        return simpleGraphics.getColor();
    }

    public void setColor(Color c) {
        simpleGraphics.setColor(c);
    }

    public void setPaintMode() {
        simpleGraphics.setPaintMode();
    }

    public void setXORMode(Color c1) {
        simpleGraphics.setXORMode(c1);
    }

    public Font getFont() {
        return simpleGraphics.getFont();
    }

    public void setFont(Font font) {
        simpleGraphics.setFont(font);
    }

    public FontMetrics getFontMetrics(Font f) {
        return simpleGraphics.getFontMetrics(f);
    }

    public Rectangle getClipBounds() {
        return simpleGraphics.getClipBounds();
    }

    public void clipRect(int x, int y, int width, int height) {
        simpleGraphics.clipRect(x, y, width, height);
    }

    public void setClip(int x, int y, int width, int height) {
        simpleGraphics.setClip(x, y, width, height);
    }

    public Shape getClip() {
        return simpleGraphics.getClip();
    }

    public void setClip(Shape clip) {
        simpleGraphics.setClip(clip);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        simpleGraphics.copyArea(x, y, width, height, dx, dy);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        simpleGraphics.drawLine(x1, y1, x2, y2);
    }

    public void fillRect(int x, int y, int width, int height) {
        simpleGraphics.fillRect(x, y, width, height);
    }

    public void clearRect(int x, int y, int width, int height) {
        simpleGraphics.clearRect(x, y, width, height);
        /*
        Rectangle r = new Rectangle(x, y, width, height);
        simpleGraphics._transform(r);

        if(simpleGraphics.clip != null)
            r = simpleGraphics.clip.intersection(r);

        surface.fillRect(r.x, r.y, r.width, r.height, background.getRGB(), Surface.PAINT_MODE);
        surface.update(r.x, r.y, r.width, r.height);
          */
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        draw(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        fill(new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight));
    }

    public void drawOval(int x, int y, int width, int height) {
        draw(new Ellipse2D.Float(x, y, width, height));
    }

    public void fillOval(int x, int y, int width, int height) {
        fill(new Ellipse2D.Float(x, y, width, height));
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        draw(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN));
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        fill(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN));
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        final GeneralPath path = new GeneralPath(GeneralPath.WIND_NON_ZERO, nPoints * 2);
        path.moveTo(xPoints[0], yPoints[0]);
        for (int i = 1; i < nPoints; i++) {
            path.lineTo(xPoints[i], yPoints[i]);
        }
        draw(path);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        draw(new Polygon(xPoints, yPoints, nPoints));
    }

    public void drawPolygon(Polygon p) {
        draw(p);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        fill(new Polygon(xPoints, yPoints, nPoints));
    }

    public void fillPolygon(Polygon p) {
        fill(p);
    }

    public void drawString(String str, int x, int y) {
        simpleGraphics.drawString(str, x, y);
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        simpleGraphics.drawString(iterator, x, y);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return simpleGraphics.drawImage(img, x, y, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return simpleGraphics.drawImage(img, x, y, width, height, observer);
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return simpleGraphics.drawImage(img, x, y, bgcolor, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return simpleGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {
        return simpleGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor, ImageObserver observer) {
        return simpleGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    public void dispose() {
        simpleGraphics.dispose();
    }
}
