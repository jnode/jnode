/* Arc2D.java -- represents an arc in 2-D space
   Copyright (C) 2002 Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.awt.geom;

import java.util.NoSuchElementException;

/**
 * This class represents all arcs (segments of an ellipse in 2-D space). The
 * arcs are defined by starting angle and extent (arc length) in degrees, as
 * opposed to radians (like the rest of Java), and can be open, chorded, or
 * wedge shaped. The angles are skewed according to the ellipse, so that 45
 * degrees always points to the upper right corner (positive x, negative y)
 * of the bounding rectangle. A positive extent draws a counterclockwise arc,
 * and while the angle can be any value, the path iterator only traverses the
 * first 360 degrees. Storage is up to the subclasses.
 *
 * @author Eric Blake <ebb9@email.byu.edu>
 * @since 1.2
 * @status updated to 1.4, but still missing functionality
 */
public abstract class Arc2D extends RectangularShape {
	/**
	 * An open arc, with no segment connecting the endpoints. This type of
	 * arc still contains the same points as a chorded version.
	 */
	public static final int OPEN = 0;

	/**
	 * A closed arc with a single segment connecting the endpoints (a chord).
	 */
	public static final int CHORD = 1;

	/**
	 * A closed arc with two segments, one from each endpoint, meeting at the
	 * center of the ellipse.
	 */
	public static final int PIE = 2;

	/** The closure type of this arc. */
	private int type;

	/**
	 * Create a new arc, with the specified closure type.
	 *
	 * @param type one of {@link #OPEN}, {@link #CHORD}, or {@link #PIE}.
	 * @throws IllegalArgumentException if type is invalid
	 */
	protected Arc2D(int type) {
		if (type < OPEN || type > PIE)
			throw new IllegalArgumentException();
		this.type = type;
	}

	/**
	 * Get the starting angle of the arc in degrees.
	 *
	 * @return the starting angle
	 * @see #setAngleStart(double)
	 */
	public abstract double getAngleStart();

	/**
	 * Get the extent angle of the arc in degrees.
	 *
	 * @return the extent angle
	 * @see #setAngleExtent(double)
	 */
	public abstract double getAngleExtent();

	/**
	 * Return the closure type of the arc.
	 *
	 * @return the closure type
	 * @see #OPEN
	 * @see #CHORD
	 * @see #PIE
	 * @see #setArcType(int)
	 */
	public int getArcType() {
		return type;
	}

	/**
	 * Returns the starting point of the arc.
	 *
	 * @return the start point
	 */
	public Point2D getStartPoint() {
		double angle = getAngleStart() * (-180 / Math.PI);
		double x = (Math.cos(angle) * getWidth() + getX()) / 2;
		double y = (Math.sin(angle) * getHeight() + getY()) / 2;
		return new Point2D.Double(x, y);
	}

	/**
	 * Returns the ending point of the arc.
	 *
	 * @return the end point
	 */
	public Point2D getEndPoint() {
		double angle = (getAngleStart() + getAngleExtent()) * (-180 / Math.PI);
		double x = (Math.cos(angle) * getWidth() + getX()) / 2;
		double y = (Math.sin(angle) * getHeight() + getY()) / 2;
		return new Point2D.Double(x, y);
	}

	/**
	 * Set the parameters of the arc. The angles are in degrees, and a positive
	 * extent sweeps counterclockwise (from the positive x-axis to the negative
	 * y-axis).
	 *
	 * @param x the new x coordinate of the lower left of the bounding box
	 * @param y the new y coordinate of the lower left of the bounding box
	 * @param w the new width of the bounding box
	 * @param h the new height of the bounding box
	 * @param start the start angle, in degrees
	 * @param extent the arc extent, in degrees
	 * @param type one of {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
	 * @throws IllegalArgumentException if type is invalid
	 */
	public abstract void setArc(double x, double y, double w, double h, double start, double extent, int type);

	/**
	 * Set the parameters of the arc. The angles are in degrees, and a positive
	 * extent sweeps counterclockwise (from the positive x-axis to the negative
	 * y-axis).
	 *
	 * @param p the lower left point of the bounding box
	 * @param d the dimensions of the bounding box
	 * @param start the start angle, in degrees
	 * @param extent the arc extent, in degrees
	 * @param type one of {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
	 * @throws IllegalArgumentException if type is invalid
	 * @throws NullPointerException if p or d is null
	 */
	public void setArc(Point2D p, Dimension2D d, double start, double extent, int type) {
		setArc(p.getX(), p.getY(), d.getWidth(), d.getHeight(), start, extent, type);
	}

	/**
	 * Set the parameters of the arc. The angles are in degrees, and a positive
	 * extent sweeps counterclockwise (from the positive x-axis to the negative
	 * y-axis).
	 *
	 * @param r the new bounding box
	 * @param start the start angle, in degrees
	 * @param extent the arc extent, in degrees
	 * @param type one of {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
	 * @throws IllegalArgumentException if type is invalid
	 * @throws NullPointerException if r is null
	 */
	public void setArc(Rectangle2D r, double start, double extent, int type) {
		setArc(r.getX(), r.getY(), r.getWidth(), r.getHeight(), start, extent, type);
	}

	/**
	 * Set the parameters of the arc from the given one.
	 *
	 * @param a the arc to copy
	 * @throws NullPointerException if a is null
	 */
	public void setArc(Arc2D a) {
		setArc(a.getX(), a.getY(), a.getWidth(), a.getHeight(), a.getAngleStart(), a.getAngleExtent(), a.getArcType());
	}

	/**
	 * Set the parameters of the arc. The angles are in degrees, and a positive
	 * extent sweeps counterclockwise (from the positive x-axis to the negative
	 * y-axis). This controls the center point and radius, so the arc will be
	 * circular.
	 *
	 * @param x the x coordinate of the center of the circle
	 * @param y the y coordinate of the center of the circle
	 * @param r the radius of the circle
	 * @param start the start angle, in degrees
	 * @param extent the arc extent, in degrees
	 * @param type one of {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
	 * @throws IllegalArgumentException if type is invalid
	 */
	public void setArcByCenter(double x, double y, double r, double start, double extent, int type) {
		setArc(x - r, y - r, r + r, r + r, start, extent, type);
	}

	/**
	 * Sets the parameters of the arc by finding the tangents of two lines, and
	 * using the specified radius. The arc will be circular, will begin on the
	 * tangent point of the line extending from p1 to p2, and will end on the
	 * tangent point of the line extending from p2 to p3.
	 *
	 * XXX What happens if the points are colinear, or the radius negative?
	 *
	 * @param p1 the first point
	 * @param p2 the tangent line intersection point
	 * @param p3 the third point
	 * @param r the radius of the arc
	 * @throws NullPointerException if any point is null
	 */
	public void setArcByTangent(Point2D p1, Point2D p2, Point2D p3, double r) {
		// XXX Implement.
		throw new Error("not implemented");
	}

	/**
	 * Set the start, in degrees.
	 *
	 * @param start the new start angle
	 * @see #getAngleStart()
	 */
	public abstract void setAngleStart(double start);

	/**
	 * Set the extent, in degrees.
	 *
	 * @param extent the new extent angle
	 * @see #getAngleExtent()
	 */
	public abstract void setAngleExtent(double extent);

	/**
	 * Sets the starting angle to the angle of the given point relative to
	 * the center of the arc. The extent remains constant; in other words,
	 * this rotates the arc.
	 *
	 * @param p the new start point
	 * @throws NullPointerException if p is null
	 * @see #getStartPoint()
	 * @see #getAngleStart()
	 */
	public void setAngleStart(Point2D p) {
		double x = ((p.getX() * 2) - getX()) / getWidth();
		double y = ((p.getY() * 2) - getY()) / getHeight();
		setAngleStart(Math.atan2(y, x) * (-180 / Math.PI));
	}

	/**
	 * Sets the starting and extent angles to those of the given points
	 * relative to the center of the arc. The arc will be non-empty, and will
	 * extend counterclockwise.
	 *
	 * @param x1 the first x coordinate
	 * @param y1 the first y coordinate
	 * @param x2 the second x coordinate
	 * @param y2 the second y coordinate
	 * @see #setAngleStart(Point2D)
	 */
	public void setAngles(double x1, double y1, double x2, double y2) {
		// Normalize the points.
		double mx = getX();
		double my = getY();
		double mw = getWidth();
		double mh = getHeight();
		x1 = ((x1 * 2) - mx) / mw;
		y1 = ((y1 * 2) - my) / mh;
		x2 = ((x2 * 2) - mx) / mw;
		y2 = ((y2 * 2) - my) / mh;
		double start = Math.atan2(y1, x1) * (-180 / Math.PI);
		double extent = Math.atan2(y2, x2) * (-180 / Math.PI) - start;
		if (extent < 0)
			extent += 360;
		setAngleStart(start);
		setAngleExtent(extent);
	}

	/**
	 * Sets the starting and extent angles to those of the given points
	 * relative to the center of the arc. The arc will be non-empty, and will
	 * extend counterclockwise.
	 *
	 * @param p1 the first point
	 * @param p2 the second point
	 * @throws NullPointerException if either point is null
	 * @see #setAngleStart(Point2D)
	 */
	public void setAngles(Point2D p1, Point2D p2) {
		setAngles(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	/**
	 * Set the closure type of this arc.
	 *
	 * @param type one of {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
	 * @throws IllegalArgumentException if type is invalid
	 * @see #getArcType()
	 */
	public void setArcType(int type) {
		if (type < OPEN || type > PIE)
			throw new IllegalArgumentException();
		this.type = type;
	}

	/**
	 * Sets the location and bounds of the ellipse of which this arc is a part.
	 *
	 * @param x the new x coordinate
	 * @param y the new y coordinate
	 * @param w the new width
	 * @param h the new height
	 * @see #getFrame()
	 */
	public void setFrame(double x, double y, double w, double h) {
		setArc(x, y, w, h, getAngleStart(), getAngleExtent(), type);
	}

	/**
	 * Gets the bounds of the arc. This is much tighter than
	 * <code>getBounds</code>, as it takes into consideration the start and
	 * end angles, and the center point of a pie wedge, rather than just the
	 * overall ellipse.
	 *
	 * @return the bounds of the arc
	 * @see #getBounds()
	 */
	public Rectangle2D getBounds2D() {
		double extent = getAngleExtent();
		if (Math.abs(extent) >= 360)
			return makeBounds(getX(), getY(), getWidth(), getHeight());
		// XXX Finish implementing.
		throw new Error("not implemented");
	}

	/**
	 * Construct a bounding box in a precision appropriate for the subclass.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param w the width
	 * @param h the height
	 * @return the rectangle for use in getBounds2D
	 */
	protected abstract Rectangle2D makeBounds(double x, double y, double w, double h);

	/**
	 * Tests if the given angle, in degrees, is included in the arc.
	 *
	 * XXX Does this normalize all angles to -180 - 180 first?
	 *
	 * @param a the angle to test
	 * @return true if it is contained
	 */
	public boolean containsAngle(double a) {
		// XXX Implement.
		throw new Error("not implemented");
	}

	/**
	 * Determines if the arc contains the given point. If the bounding box
	 * is empty, then this will return false.
	 *
	 * @param x the x coordinate to test
	 * @param y the y coordinate to test
	 * @return true if the point is inside the arc
	 */
	public boolean contains(double x, double y) {
		double w = getWidth();
		double h = getHeight();
		if (w <= 0 || h <= 0)
			return false;
		// XXX Finish implementing.
		throw new Error("not implemented");
	}

	/**
	 * Tests if a given rectangle intersects the area of the arc.
	 *
	 * @param x the x coordinate of the rectangle
	 * @param y the y coordinate of the rectangle
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 * @return true if the two shapes share common points
	 */
	public boolean intersects(double x, double y, double w, double h) {
		double mw = getWidth();
		double mh = getHeight();
		if (mw <= 0 || mh <= 0 || w <= 0 || h <= 0)
			return false;
		// XXX Finish implementing.
		throw new Error("not implemented");
	}

	/**
	 * Tests if a given rectangle is contained in the area of the arc.
	 *
	 * @param x the x coordinate of the rectangle
	 * @param y the y coordinate of the rectangle
	 * @param w the width of the rectangle
	 * @param h the height of the rectangle
	 * @return true if the arc contains the rectangle
	 */
	public boolean contains(double x, double y, double w, double h) {
		double mw = getWidth();
		double mh = getHeight();
		if (mw <= 0 || mh <= 0 || w <= 0 || h <= 0)
			return false;
		// XXX Finish implementing.
		throw new Error("not implemented");
	}

	/**
	 * Tests if a given rectangle is contained in the area of the arc.
	 *
	 * @param r the rectangle
	 * @return true if the arc contains the rectangle
	 */
	public boolean contains(Rectangle2D r) {
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	/**
	 * Returns an iterator over this arc, with an optional transformation.
	 * This iterator is threadsafe, so future modifications to the arc do not
	 * affect the iteration.
	 *
	 * @param at the transformation, or null
	 * @return a path iterator
	 */
	public PathIterator getPathIterator(AffineTransform at) {
		return new ArcIterator(this, at);
	}

	/**
	 * This class is used to iterate over an arc. Since ellipses are a subclass
	 * of arcs, this is used by Ellipse2D as well.
	 *
	 * @author Eric Blake <ebb9@email.byu.edu>
	 */
	static final class ArcIterator implements PathIterator {
		/** The current iteration. */
		private int current;

		/** The last iteration. */
		private final int limit;

		/** The optional transformation. */
		private final AffineTransform xform;

		/** The x coordinate of the bounding box. */
		private final double x;

		/** The y coordinate of the bounding box. */
		private final double y;

		/** The width of the bounding box. */
		private final double w;

		/** The height of the bounding box. */
		private final double h;

		/** The start angle, in radians (not degrees). */
		private final double startAngRad;

		/** The extent angle, in radians (not degrees). */
		private final double extentAngRad;

		/** The arc closure type. */
		private final int type;

		private final double angExtDeg;
		private final double increment;

		/**
		 * Construct a new iterator over an arc.
		 *
		 * @param a the arc
		 * @param xform the transform
		 */
		ArcIterator(Arc2D a, AffineTransform xform) {
			this.xform = xform;
			x = a.getX();
			y = a.getY();
			w = a.getWidth();
			h = a.getHeight();
			angExtDeg = a.getAngleExtent();
			startAngRad = a.getAngleStart() * (Math.PI / 180);
			extentAngRad = angExtDeg * (Math.PI / 180);
			type = a.getArcType();
			double e = extentAngRad < 0 ? -extentAngRad : extentAngRad;
			if (w < 0 || h < 0)
				limit = -1;
			else if (e == 0)
				limit = type;
			else if (e <= 90)
				limit = type + 1;
			else if (e <= 180)
				limit = type + 2;
			else if (e <= 270)
				limit = type + 3;
			else
				limit = type + 4;

			double increment = angExtDeg;
			if (increment > 360.0) {
				increment = 360.0;
			} else if (increment < -360.0) {
				increment = -360.0;
			}
			increment /= limit;
			this.increment = Math.toRadians(increment);
		}

		/**
		 * Construct a new iterator over an ellipse.
		 *
		 * @param e the ellipse
		 * @param xform the transform
		 */
		ArcIterator(Ellipse2D e, AffineTransform xform) {
			this.xform = xform;
			x = e.getX();
			y = e.getY();
			w = e.getWidth();
			h = e.getHeight();
			startAngRad = 0;
			extentAngRad = -2 * Math.PI;
			angExtDeg = -360;
			type = CHORD;
			limit = (w < 0 || h < 0) ? -1 : 5;
			this.increment = Math.toRadians(angExtDeg / limit);
		}

		/**
		 * Return the winding rule.
		 *
		 * @return {@link PathIterator#WIND_NON_ZERO}
		 */
		public int getWindingRule() {
			return WIND_NON_ZERO;
		}

		/**
		 * Test if the iteration is complete.
		 *
		 * @return true if more segments exist
		 */
		public boolean isDone() {
			return current > limit;
		}

		/**
		 * Advance the iterator.
		 */
		public void next() {
			current++;
		}

		/**
		 * Put the current segment into the array, and return the segment type.
		 *
		 * @param coords an array of 6 elements
		 * @return the segment type
		 * @throws NullPointerException if coords is null
		 * @throws ArrayIndexOutOfBoundsException if coords is too small
		 */
		public int currentSegment(float[] coords) {
			if (current > limit) {
				throw new NoSuchElementException("arc iterator out of bounds");
			}
			if (current == 0) {
				coords[0] = (float) (Math.cos(startAngRad) * w + x) / 2;
				coords[1] = (float) (Math.sin(startAngRad) * h + y) / 2;
				if (xform != null) {
					xform.transform(coords, 0, coords, 0, 1);
				}
				return SEG_MOVETO;
			}
			if (type != OPEN && current == limit) {
				return SEG_CLOSE;
			}
			if (type == PIE && current == limit - 1) {
				coords[0] = (float) (x + w / 2);
				coords[1] = (float) (y + h / 2);
				if (xform != null) {
					xform.transform(coords, 0, coords, 0, 1);
				}
				return SEG_LINETO;
			}
			// XXX Fill coords with 2 control points and next quarter point

			double angle = startAngRad;
			angle += increment * (current - 1);
			double relx = Math.cos(angle);
			double rely = Math.sin(angle);
			double z = btan(increment);
			coords[0] = (float) (x + (relx - z * rely) * w);
			coords[1] = (float) (y + (rely + z * relx) * h);
			angle += increment;
			relx = Math.cos(angle);
			rely = Math.sin(angle);
			coords[2] = (float) (x + (relx + z * rely) * w);
			coords[3] = (float) (y + (rely - z * relx) * h);
			coords[4] = (float) (x + relx * w);
			coords[5] = (float) (y + rely * h);
			if (xform != null) {
				xform.transform(coords, 0, coords, 0, 3);
			}
			return SEG_CUBICTO;
		}

		/**
		 * Put the current segment into the array, and return the segment type.
		 *
		 * @param coords an array of 6 elements
		 * @return the segment type
		 * @throws NullPointerException if coords is null
		 * @throws ArrayIndexOutOfBoundsException if coords is too small
		 */
		public int currentSegment(double[] coords) {
			if (current > limit)
				throw new NoSuchElementException("arc iterator out of bounds");
			if (current == 0) {
				coords[0] = (Math.cos(startAngRad) * w + x) / 2;
				coords[1] = (Math.sin(startAngRad) * h + y) / 2;
				if (xform != null)
					xform.transform(coords, 0, coords, 0, 1);
				return SEG_MOVETO;
			}
			if (type != OPEN && current == limit)
				return SEG_CLOSE;
			if (type == PIE && current == limit - 1) {
				coords[0] = (float) (x + w / 2);
				coords[1] = (float) (y + h / 2);
				if (xform != null)
					xform.transform(coords, 0, coords, 0, 1);
				return SEG_LINETO;
			}
			double angle = startAngRad;
			angle += increment * (current - 1);
			double relx = Math.cos(angle);
			double rely = Math.sin(angle);
			double z = btan(increment);
			coords[0] = (x + (relx - z * rely) * w);
			coords[1] = (y + (rely + z * relx) * h);
			angle += increment;
			relx = Math.cos(angle);
			rely = Math.sin(angle);
			coords[2] = (x + (relx + z * rely) * w);
			coords[3] = (y + (rely - z * relx) * h);
			coords[4] = (x + relx * w);
			coords[5] = (y + rely * h);
			if (xform != null) {
				xform.transform(coords, 0, coords, 0, 3);
			}
			return SEG_CUBICTO;
		}
		
		private static double btan(double increment) {
			increment /= 2.0;
			double a = 1.0 - Math.cos(increment);
			double b = Math.tan(increment);
			double c = Math.sqrt(1.0 + b * b) - 1.0 + a;
			return 4.0 / 3.0 * a * b / c;
		}
	} // class ArcIterator

	/**
	 * This class implements an arc in double precision.
	 *
	 * @author Eric Blake <ebb9@email.byu.edu
	 * @since 1.2
	 */
	public static class Double extends Arc2D {
		/** The x coordinate of the box bounding the ellipse of this arc. */
		public double x;

		/** The y coordinate of the box bounding the ellipse of this arc. */
		public double y;

		/** The width of the box bounding the ellipse of this arc. */
		public double width;

		/** The height of the box bounding the ellipse of this arc. */
		public double height;

		/** The start angle of this arc, in degrees. */
		public double start;

		/** The extent angle of this arc, in degrees. */
		public double extent;

		/**
		 * Create a new, open arc at (0,0) with 0 extent.
		 */
		public Double() {
			super(OPEN);
		}

		/**
		 * Create a new arc of the given type at (0,0) with 0 extent.
		 *
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 */
		public Double(int type) {
			super(type);
		}

		/**
		 * Create a new arc with the given dimensions.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param w the width
		 * @param h the height
		 * @param start the start angle, in degrees
		 * @param extent the extent, in degrees
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 */
		public Double(double x, double y, double w, double h, double start, double extent, int type) {
			super(type);
			this.x = x;
			this.y = y;
			width = w;
			height = h;
			this.start = start;
			this.extent = extent;
		}

		/**
		 * Create a new arc with the given dimensions.
		 *
		 * @param r the bounding box
		 * @param start the start angle, in degrees
		 * @param extent the extent, in degrees
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 * @throws NullPointerException if r is null
		 */
		public Double(Rectangle2D r, double start, double extent, int type) {
			super(type);
			x = r.getX();
			y = r.getY();
			width = r.getWidth();
			height = r.getHeight();
			this.start = start;
			this.extent = extent;
		}

		/**
		 * Return the x coordinate of the bounding box.
		 *
		 * @return the value of x
		 */
		public double getX() {
			return x;
		}

		/**
		 * Return the y coordinate of the bounding box.
		 *
		 * @return the value of y
		 */
		public double getY() {
			return y;
		}

		/**
		 * Return the width of the bounding box.
		 *
		 * @return the value of width
		 */
		public double getWidth() {
			return width;
		}

		/**
		 * Return the height of the bounding box.
		 *
		 * @return the value of height
		 */
		public double getHeight() {
			return height;
		}

		/**
		 * Return the start angle of the arc, in degrees.
		 *
		 * @return the value of start
		 */
		public double getAngleStart() {
			return start;
		}

		/**
		 * Return the extent of the arc, in degrees.
		 *
		 * @return the value of extent
		 */
		public double getAngleExtent() {
			return extent;
		}

		/**
		 * Tests if the arc contains points.
		 *
		 * @return true if the arc has no interior
		 */
		public boolean isEmpty() {
			return width <= 0 || height <= 0;
		}

		/**
		 * Sets the arc to the given dimensions.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param w the width
		 * @param h the height
		 * @param start the start angle, in degrees
		 * @param extent the extent, in degrees
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 */
		public void setArc(double x, double y, double w, double h, double start, double extent, int type) {
			this.x = x;
			this.y = y;
			width = w;
			height = h;
			this.start = start;
			this.extent = extent;
			setArcType(type);
		}

		/**
		 * Sets the start angle of the arc.
		 *
		 * @param start the new start angle
		 */
		public void setAngleStart(double start) {
			this.start = start;
		}

		/**
		 * Sets the extent angle of the arc.
		 *
		 * @param start the new extent angle
		 */
		public void setAngleExtent(double extent) {
			this.extent = extent;
		}

		/**
		 * Creates a tight bounding box given dimensions that more precise than
		 * the bounding box of the ellipse.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param w the width
		 * @param h the height
		 */
		protected Rectangle2D makeBounds(double x, double y, double w, double h) {
			return new Rectangle2D.Double(x, y, w, h);
		}
	} // class Double

	/**
	 * This class implements an arc in float precision.
	 *
	 * @author Eric Blake <ebb9@email.byu.edu
	 * @since 1.2
	 */
	public static class Float extends Arc2D {
		/** The x coordinate of the box bounding the ellipse of this arc. */
		public float x;

		/** The y coordinate of the box bounding the ellipse of this arc. */
		public float y;

		/** The width of the box bounding the ellipse of this arc. */
		public float width;

		/** The height of the box bounding the ellipse of this arc. */
		public float height;

		/** The start angle of this arc, in degrees. */
		public float start;

		/** The extent angle of this arc, in degrees. */
		public float extent;

		/**
		 * Create a new, open arc at (0,0) with 0 extent.
		 */
		public Float() {
			super(OPEN);
		}

		/**
		 * Create a new arc of the given type at (0,0) with 0 extent.
		 *
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 */
		public Float(int type) {
			super(type);
		}

		/**
		 * Create a new arc with the given dimensions.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param w the width
		 * @param h the height
		 * @param start the start angle, in degrees
		 * @param extent the extent, in degrees
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 */
		public Float(float x, float y, float w, float h, float start, float extent, int type) {
			super(type);
			this.x = x;
			this.y = y;
			width = w;
			height = h;
			this.start = start;
			this.extent = extent;
		}

		/**
		 * Create a new arc with the given dimensions.
		 *
		 * @param r the bounding box
		 * @param start the start angle, in degrees
		 * @param extent the extent, in degrees
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 * @throws NullPointerException if r is null
		 */
		public Float(Rectangle2D r, float start, float extent, int type) {
			super(type);
			x = (float) r.getX();
			y = (float) r.getY();
			width = (float) r.getWidth();
			height = (float) r.getHeight();
			this.start = start;
			this.extent = extent;
		}

		/**
		 * Return the x coordinate of the bounding box.
		 *
		 * @return the value of x
		 */
		public double getX() {
			return x;
		}

		/**
		 * Return the y coordinate of the bounding box.
		 *
		 * @return the value of y
		 */
		public double getY() {
			return y;
		}

		/**
		 * Return the width of the bounding box.
		 *
		 * @return the value of width
		 */
		public double getWidth() {
			return width;
		}

		/**
		 * Return the height of the bounding box.
		 *
		 * @return the value of height
		 */
		public double getHeight() {
			return height;
		}

		/**
		 * Return the start angle of the arc, in degrees.
		 *
		 * @return the value of start
		 */
		public double getAngleStart() {
			return start;
		}

		/**
		 * Return the extent of the arc, in degrees.
		 *
		 * @return the value of extent
		 */
		public double getAngleExtent() {
			return extent;
		}

		/**
		 * Tests if the arc contains points.
		 *
		 * @return true if the arc has no interior
		 */
		public boolean isEmpty() {
			return width <= 0 || height <= 0;
		}

		/**
		 * Sets the arc to the given dimensions.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param w the width
		 * @param h the height
		 * @param start the start angle, in degrees
		 * @param extent the extent, in degrees
		 * @param type the arc type: {@link #OPEN}, {@link #CHORD}, or {@link #PIE}
		 * @throws IllegalArgumentException if type is invalid
		 */
		public void setArc(double x, double y, double w, double h, double start, double extent, int type) {
			this.x = (float) x;
			this.y = (float) y;
			width = (float) w;
			height = (float) h;
			this.start = (float) start;
			this.extent = (float) extent;
			setArcType(type);
		}

		/**
		 * Sets the start angle of the arc.
		 *
		 * @param start the new start angle
		 */
		public void setAngleStart(double start) {
			this.start = (float) start;
		}

		/**
		 * Sets the extent angle of the arc.
		 *
		 * @param start the new extent angle
		 */
		public void setAngleExtent(double extent) {
			this.extent = (float) extent;
		}

		/**
		 * Creates a tight bounding box given dimensions that more precise than
		 * the bounding box of the ellipse.
		 *
		 * @param x the x coordinate
		 * @param y the y coordinate
		 * @param w the width
		 * @param h the height
		 */
		protected Rectangle2D makeBounds(double x, double y, double w, double h) {
			return new Rectangle2D.Float((float) x, (float) y, (float) w, (float) h);
		}
	} // class Float
} // class Arc2D
