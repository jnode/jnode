/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.java2d.pipe;

import java.awt.Shape;
import java.awt.BasicStroke;
import java.awt.geom.PathIterator;
import java.awt.geom.AffineTransform;

import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.ServiceLoader;
import sun.security.action.GetPropertyAction;

import sun.awt.geom.PathConsumer2D;

/**
 * This class abstracts a number of features for which the Java 2D
 * implementation relies on proprietary licensed software libraries.
 * Access to those features is now achieved by retrieving the singleton
 * instance of this class and calling the appropriate methods on it.
 * The 3 primary features abstracted here include:
 * <dl>
 * <dt>Shape createStrokedShape(Shape, [BasicStroke attributes]);
 * <dd>This method implements the functionality of the method of the
 * same name on the {@link BasicStroke} class.
 * <dt>void strokeTo(Shape, [rendering parameters], PathConsumer2D);
 * <dd>This method performs widening of the source path on the fly
 * and sends the results to the given {@link PathConsumer2D} object.
 * This procedure avoids having to create an intermediate Shape
 * object to hold the results of the {@code createStrokedShape} method.
 * The main user of this method is the Java 2D non-antialiasing renderer.
 * <dt>AATileGenerator getAATileGenerator(Shape, [rendering parameters]);
 * <dd>This method returns an object which can iterate over the
 * specified bounding box and produce tiles of coverage values for
 * antialiased rendering.  The details of the operation of the
 * {@link AATileGenerator} object are explained in its class comments.
 * </dl>
 * Additionally, the following informational method supplies important
 * data about the implementation.
 * <dl>
 * <dt>float getMinimumAAPenSize()
 * <dd>This method provides information on how small the BasicStroke
 * line width can get before dropouts occur.  Rendering with a BasicStroke
 * is defined to never allow the line to have breaks, gaps, or dropouts
 * even if the width is set to 0.0f, so this information allows the
 * {@link SunGraphics2D} class to detect the "thin line" case and set
 * the rendering attributes accordingly.
 * </dl>
 * At startup the runtime will load a single instance of this class.
 * It searches the classpath for a registered provider of this API
 * and returns either the last one it finds, or the instance whose
 * class name matches the value supplied in the System property
 * {@code sun.java2d.renderer}.
 * Additionally, a runtime System property flag can be set to trace
 * all calls to methods on the {@code RenderingEngine} in use by
 * setting the sun.java2d.renderer.trace property to any non-null value.
 * <p>
 * Parts of the system that need to use any of the above features should
 * call {@code RenderingEngine.getInstance()} to obtain the properly
 * registered (and possibly trace-enabled) version of the RenderingEngine.
 */
public abstract class RenderingEngine {
    private static RenderingEngine reImpl;

    /**
     * Returns an instance of {@code RenderingEngine} as determined
     * by the installation environment and runtime flags.
     * <p>
     * A specific instance of the {@code RenderingEngine} can be
     * chosen by specifying the runtime flag:
     * <pre>
     *     java -Dsun.java2d.renderer=&lt;classname&gt;
     * </pre>
     * If no specific {@code RenderingEngine} is specified on the command
     * line then the last one returned by enumerating all subclasses of
     * {@code RenderingEngine} known to the ServiceLoader is used.
     * <p>
     * Runtime tracing of the actions of the {@code RenderingEngine}
     * can be enabled by specifying the runtime flag:
     * <pre>
     *     java -Dsun.java2d.renderer.trace=&lt;any string&gt;
     * </pre>
     * @return an instance of {@code RenderingEngine}
     * @since 1.7
     */
    public static synchronized RenderingEngine getInstance() {
        if (reImpl != null) {
            return reImpl;
        }

        reImpl = (RenderingEngine)
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    String reClass =
                        System.getProperty("sun.java2d.renderer",
                                           "sun.dc.DuctusRenderingEngine");

                    ServiceLoader<RenderingEngine> reLoader =
                        ServiceLoader.loadInstalled(RenderingEngine.class);

                    RenderingEngine service = null;

                    for (RenderingEngine re : reLoader) {
                        service = re;
                        if (re.getClass().getName().equals(reClass)) {
                            break;
                        }
                    }
                    return service;
                }
            });

        if (reImpl == null) {
            throw new InternalError("No RenderingEngine module found");
        }

        GetPropertyAction gpa =
            new GetPropertyAction("sun.java2d.renderer.trace");
        String reTrace = (String) AccessController.doPrivileged(gpa);
        if (reTrace != null) {
            reImpl = new Tracer(reImpl);
        }

        return reImpl;
    }

    /**
     * Create a widened path as specified by the parameters.
     * <p>
     * The specified {@code src} {@link Shape} is widened according
     * to the specified attribute parameters as per the
     * {@link BasicStroke} specification.
     *
     * @param src the source path to be widened
     * @param width the width of the widened path as per {@code BasicStroke}
     * @param caps the end cap decorations as per {@code BasicStroke}
     * @param join the segment join decorations as per {@code BasicStroke}
     * @param miterlimit the miter limit as per {@code BasicStroke}
     * @param dashes the dash length array as per {@code BasicStroke}
     * @param dashphase the initial dash phase as per {@code BasicStroke}
     * @return the widened path stored in a new {@code Shape} object
     * @since 1.7
     */
    public abstract Shape createStrokedShape(Shape src,
                                             float width,
                                             int caps,
                                             int join,
                                             float miterlimit,
                                             float dashes[],
                                             float dashphase);

    /**
     * Sends the geometry for a widened path as specified by the parameters
     * to the specified consumer.
     * <p>
     * The specified {@code src} {@link Shape} is widened according
     * to the parameters specified by the {@link BasicStroke} object.
     * Adjustments are made to the path as appropriate for the
     * {@link VALUE_STROKE_NORMALIZE} hint if the {@code normalize}
     * boolean parameter is true.
     * Adjustments are made to the path as appropriate for the
     * {@link VALUE_ANTIALIAS_ON} hint if the {@code antialias}
     * boolean parameter is true.
     * <p>
     * The geometry of the widened path is forwarded to the indicated
     * {@link PathConsumer2D} object as it is calculated.
     *
     * @param src the source path to be widened
     * @param bs the {@code BasicSroke} object specifying the
     *           decorations to be applied to the widened path
     * @param normalize indicates whether stroke normalization should
     *                  be applied
     * @param antialias indicates whether or not adjustments appropriate
     *                  to antialiased rendering should be applied
     * @param consumer the {@code PathConsumer2D} instance to forward
     *                 the widened geometry to
     * @since 1.7
     */
    public abstract void strokeTo(Shape src,
                                  AffineTransform at,
                                  BasicStroke bs,
                                  boolean thin,
                                  boolean normalize,
                                  boolean antialias,
                                  PathConsumer2D consumer);

    /**
     * Construct an antialiased tile generator for the given shape with
     * the given rendering attributes and store the bounds of the tile
     * iteration in the bbox parameter.
     * The {@code at} parameter specifies a transform that should affect
     * both the shape and the {@code BasicStroke} attributes.
     * The {@code clip} parameter specifies the current clip in effect
     * in device coordinates and can be used to prune the data for the
     * operation, but the renderer is not required to perform any
     * clipping.
     * If the {@code BasicStroke} parameter is null then the shape
     * should be filled as is, otherwise the attributes of the
     * {@code BasicStroke} should be used to specify a draw operation.
     * The {@code thin} parameter indicates whether or not the
     * transformed {@code BasicStroke} represents coordinates smaller
     * than the minimum resolution of the antialiasing rasterizer as
     * specified by the {@code getMinimumAAPenWidth()} method.
     * <p>
     * Upon returning, this method will fill the {@code bbox} parameter
     * with 4 values indicating the bounds of the iteration of the
     * tile generator.
     * The iteration order of the tiles will be as specified by the
     * pseudo-code:
     * <pre>
     *     for (y = bbox[1]; y < bbox[3]; y += tileheight) {
     *         for (x = bbox[0]; x < bbox[2]; x += tilewidth) {
     *         }
     *     }
     * </pre>
     * If there is no output to be rendered, this method may return
     * null.
     *
     * @param s the shape to be rendered (fill or draw)
     * @param at the transform to be applied to the shape and the
     *           stroke attributes
     * @param clip the current clip in effect in device coordinates
     * @param bs if non-null, a {@code BasicStroke} whose attributes
     *           should be applied to this operation
     * @param thin true if the transformed stroke attributes are smaller
     *             than the minimum dropout pen width
     * @param normalize true if the {@code VALUE_STROKE_NORMALIZE}
     *                  {@code RenderingHint} is in effect
     * @param bbox returns the bounds of the iteration
     * @return the {@code AATileGenerator} instance to be consulted
     *         for tile coverages, or null if there is no output to render
     * @since 1.7
     */
    public abstract AATileGenerator getAATileGenerator(Shape s,
                                                       AffineTransform at,
                                                       Region clip,
                                                       BasicStroke bs,
                                                       boolean thin,
                                                       boolean normalize,
                                                       int bbox[]);

    /**
     * Returns the minimum pen width that the antialiasing rasterizer
     * can represent without dropouts occuring.
     * @since 1.7
     */
    public abstract float getMinimumAAPenSize();

    /**
     * Utility method to feed a {@link PathConsumer2D} object from a
     * given {@link PathIterator}.
     * This method deals with the details of running the iterator and
     * feeding the consumer a segment at a time.
     */
    public static void feedConsumer(PathIterator pi, PathConsumer2D consumer) {
        float coords[] = new float[6];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                consumer.moveTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                consumer.lineTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                consumer.quadTo(coords[0], coords[1],
                                coords[2], coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                consumer.curveTo(coords[0], coords[1],
                                 coords[2], coords[3],
                                 coords[4], coords[5]);
                break;
            case PathIterator.SEG_CLOSE:
                consumer.closePath();
                break;
            }
            pi.next();
        }
    }

    static class Tracer extends RenderingEngine {
        RenderingEngine target;
        String name;

        public Tracer(RenderingEngine target) {
            this.target = target;
            name = target.getClass().getName();
        }

        public Shape createStrokedShape(Shape src,
                                        float width,
                                        int caps,
                                        int join,
                                        float miterlimit,
                                        float dashes[],
                                        float dashphase)
        {
            System.out.println(name+".createStrokedShape("+
                               src.getClass().getName()+", "+
                               "width = "+width+", "+
                               "caps = "+caps+", "+
                               "join = "+join+", "+
                               "miter = "+miterlimit+", "+
                               "dashes = "+dashes+", "+
                               "dashphase = "+dashphase+")");
            return target.createStrokedShape(src,
                                             width, caps, join, miterlimit,
                                             dashes, dashphase);
        }

        public void strokeTo(Shape src,
                             AffineTransform at,
                             BasicStroke bs,
                             boolean thin,
                             boolean normalize,
                             boolean antialias,
                             PathConsumer2D consumer)
        {
            System.out.println(name+".strokeTo("+
                               src.getClass().getName()+", "+
                               at+", "+
                               bs+", "+
                               (thin ? "thin" : "wide")+", "+
                               (normalize ? "normalized" : "pure")+", "+
                               (antialias ? "AA" : "non-AA")+", "+
                               consumer.getClass().getName()+")");
            target.strokeTo(src, at, bs, thin, normalize, antialias, consumer);
        }

        public float getMinimumAAPenSize() {
            System.out.println(name+".getMinimumAAPenSize()");
            return target.getMinimumAAPenSize();
        }

        public AATileGenerator getAATileGenerator(Shape s,
                                                  AffineTransform at,
                                                  Region clip,
                                                  BasicStroke bs,
                                                  boolean thin,
                                                  boolean normalize,
                                                  int bbox[])
        {
            System.out.println(name+".getAATileGenerator("+
                               s.getClass().getName()+", "+
                               at+", "+
                               clip+", "+
                               bs+", "+
                               (thin ? "thin" : "wide")+", "+
                               (normalize ? "normalized" : "pure")+")");
            return target.getAATileGenerator(s, at, clip,
                                             bs, thin, normalize,
                                             bbox);
        }
    }
}
