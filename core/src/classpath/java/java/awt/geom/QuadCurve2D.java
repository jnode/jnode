/*
 * QuadCurve2D.java -- represents a parameterized quadratic curve in 2-D space Copyright (C) 2002 Free Software Foundation
 * 
 * This file is part of GNU Classpath.
 * 
 * GNU Classpath is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2, or (at
 * your option) any later version.
 * 
 * GNU Classpath is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with GNU Classpath; see the file COPYING. If not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions of the GNU General Public License cover the whole
 * combination.
 * 
 * As a special exception, the copyright holders of this library give you permission to link this library with independent modules to produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and conditions of
 * the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to do so, delete this
 */

package java.awt.geom;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.NoSuchElementException;

/**
 * STUBS ONLY XXX Implement and document.
 */
public abstract class QuadCurve2D implements Shape, Cloneable {
	protected QuadCurve2D() {
	}

	public abstract double getX1();
	public abstract double getY1();
	public abstract Point2D getP1();
	public abstract double getCtrlX();
	public abstract double getCtrlY();
	public abstract Point2D getCtrlPt();
	public abstract double getX2();
	public abstract double getY2();
	public abstract Point2D getP2();

	public abstract void setCurve(double x1, double y1, double cx, double cy, double x2, double y2);
	public void setCurve(double[] coords, int offset) {
		setCurve(coords[offset++], coords[offset++], coords[offset++], coords[offset++], coords[offset++], coords[offset++]);
	}
	public void setCurve(Point2D p1, Point2D c, Point2D p2) {
		setCurve(p1.getX(), p1.getY(), c.getX(), c.getY(), p2.getX(), p2.getY());
	}
	public void setCurve(Point2D[] pts, int offset) {
		setCurve(pts[offset].getX(), pts[offset++].getY(), pts[offset].getX(), pts[offset++].getY(), pts[offset].getX(), pts[offset++].getY());
	}
	public void setCurve(QuadCurve2D c) {
		setCurve(c.getX1(), c.getY1(), c.getCtrlX(), c.getCtrlY(), c.getX2(), c.getY2());
	}
	public static double getFlatnessSq(double x1, double y1, double cx, double cy, double x2, double y2) {
		return Line2D.ptSegDistSq(x1, y1, x2, y2, cx, cy);
	}
	public static double getFlatness(double x1, double y1, double cx, double cy, double x2, double y2) {
		return Math.sqrt(getFlatnessSq(x1, y1, cx, cy, x2, y2));
	}
	public static double getFlatnessSq(double[] coords, int offset) {
		return getFlatnessSq(coords[offset++], coords[offset++], coords[offset++], coords[offset++], coords[offset++], coords[offset++]);
	}
	public static double getFlatness(double[] coords, int offset) {
		return Math.sqrt(getFlatnessSq(coords[offset++], coords[offset++], coords[offset++], coords[offset++], coords[offset++], coords[offset++]));
	}
	public double getFlatnessSq() {
		return getFlatnessSq(getX1(), getY1(), getCtrlX(), getCtrlY(), getX2(), getY2());
	}
	public double getFlatness() {
		return Math.sqrt(getFlatnessSq(getX1(), getY1(), getCtrlX(), getCtrlY(), getX2(), getY2()));
	}

	public void subdivide(QuadCurve2D l, QuadCurve2D r) {
		if (l == null)
			l = new QuadCurve2D.Double();
		if (r == null)
			r = new QuadCurve2D.Double();
		// Use empty slots at end to share single array.
		double[] d = new double[] { getX1(), getY1(), getCtrlX(), getCtrlY(), getX2(), getY2(), 0, 0, 0, 0 };
		subdivide(d, 0, d, 0, d, 4);
		l.setCurve(d, 0);
		r.setCurve(d, 4);
	}

	public static void subdivide(QuadCurve2D src, QuadCurve2D l, QuadCurve2D r) {
		src.subdivide(l, r);
	}

	public static void subdivide(double[] src, int srcoff, double[] left, int leftoff, double[] right, int rightoff) {
		double x1 = src[srcoff + 0];
		double y1 = src[srcoff + 1];
		double ctrlx = src[srcoff + 2];
		double ctrly = src[srcoff + 3];
		double x2 = src[srcoff + 4];
		double y2 = src[srcoff + 5];
		if (left != null) {
			left[leftoff + 0] = x1;
			left[leftoff + 1] = y1;
		}
		if (right != null) {
			right[rightoff + 4] = x2;
			right[rightoff + 5] = y2;
		}
		x1 = (x1 + ctrlx) / 2.0;
		y1 = (y1 + ctrly) / 2.0;
		x2 = (x2 + ctrlx) / 2.0;
		y2 = (y2 + ctrly) / 2.0;
		ctrlx = (x1 + x2) / 2.0;
		ctrly = (y1 + y2) / 2.0;
		if (left != null) {
			left[leftoff + 2] = x1;
			left[leftoff + 3] = y1;
			left[leftoff + 4] = ctrlx;
			left[leftoff + 5] = ctrly;
		}
		if (right != null) {
			right[rightoff + 0] = ctrlx;
			right[rightoff + 1] = ctrly;
			right[rightoff + 2] = x2;
			right[rightoff + 3] = y2;
		}
	}
	public static int solveQuadratic(double[] eqn) {
		return solveQuadratic(eqn, eqn);
	}

	public static int solveQuadratic(double[] eqn, double[] res) {
		double c = eqn[0];
		double b = eqn[1];
		double a = eqn[2];
		if (a == 0) {
			if (b == 0)
				return -1;
			res[0] = -c / b;
			return 1;
		}
		c /= a;
		b /= a * 2;
		double det = Math.sqrt(b * b - c);
		if (det != det)
			return 0;
		// For fewer rounding errors, we calculate the two roots differently.
		if (b > 0) {
			res[0] = -b - det;
			res[1] = -c / (b + det);
		} else {
			res[0] = -c / (b - det);
			res[1] = -b + det;
		}
		return 2;
	}

	public boolean contains(double x, double y) {
		// We count the "Y" crossings to determine if the point is
		// inside the curve bounded by its closing line.
		int crossings = 0;
		double x1 = getX1();
		double y1 = getY1();
		double x2 = getX2();
		double y2 = getY2();
		// First check for a crossing of the line connecting the endpoints
		double dy = y2 - y1;
		if ((dy > 0.0 && y >= y1 && y <= y2) || (dy < 0.0 && y <= y1 && y >= y2)) {
			if (x <= x1 + (y - y1) * (x2 - x1) / dy) {
				crossings++;
			}
		}
		// Solve the Y parametric equation for intersections with y
		double ctrlx = getCtrlX();
		double ctrly = getCtrlY();
		boolean include0 = ((y2 - y1) * (ctrly - y1) >= 0);
		boolean include1 = ((y1 - y2) * (ctrly - y2) >= 0);
		double eqn[] = new double[3];
		double res[] = new double[3];
		fillEqn(eqn, y, y1, ctrly, y2);
		int roots = solveQuadratic(eqn, res);
		roots = evalQuadratic(res, roots, include0, include1, eqn, x1, ctrlx, x2);
		while (--roots >= 0) {
			if (x < res[roots]) {
				crossings++;
			}
		}
		return ((crossings & 1) == 1);
	}

	/**
	 * Fill an array with the coefficients of the parametric equation in t, ready for solving against val with solveQuadratic. We currently have: val = Py(t) = C1*(1-t)^2 + 2*CP*t*(1-t) + C2*t^2 = C1 -
	 * 2*C1*t + C1*t^2 + 2*CP*t - 2*CP*t^2 + C2*t^2 = C1 + (2*CP - 2*C1)*t + (C1 - 2*CP + C2)*t^2 0 = (C1 - val) + (2*CP - 2*C1)*t + (C1 - 2*CP + C2)*t^2 0 = C + Bt + At^2 C = C1 - val B = 2*CP -
	 * 2*C1 A = C1 - 2*CP + C2
	 */
	private static void fillEqn(double eqn[], double val, double c1, double cp, double c2) {
		eqn[0] = c1 - val;
		eqn[1] = cp + cp - c1 - c1;
		eqn[2] = c1 - cp - cp + c2;
		return;
	}

	/**
	 * Evaluate the t values in the first num slots of the vals[] array and place the evaluated values back into the same array. Only evaluate t values that are within the range <0, 1>, including
	 * the 0 and 1 ends of the range iff the include0 or include1 booleans are true. If an "inflection" equation is handed in, then any points which represent a point of inflection for that quadratic
	 * equation are also ignored.
	 */
	private static int evalQuadratic(double vals[], int num, boolean include0, boolean include1, double inflect[], double c1, double ctrl, double c2) {
		int j = 0;
		for (int i = 0; i < num; i++) {
			double t = vals[i];
			if ((include0 ? t >= 0 : t > 0) && (include1 ? t <= 1 : t < 1) && (inflect == null || inflect[1] + 2 * inflect[2] * t != 0)) {
				double u = 1 - t;
				vals[j++] = c1 * u * u + 2 * ctrl * t * u + c2 * t * t;
			}
		}
		return j;
	}

	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}
	public boolean intersects(double x, double y, double w, double h) {
		// Trivially reject non-existant rectangles
		if (w < 0 || h < 0) {
			return false;
		}

		// Trivially accept if either endpoint is inside the rectangle
		// (not on its border since it may end there and not go inside)
		// Record where they lie with respect to the rectangle.
		//     -1 => left, 0 => inside, 1 => right
		double x1 = getX1();
		double y1 = getY1();
		int x1tag = getTag(x1, x, x + w);
		int y1tag = getTag(y1, y, y + h);
		if (x1tag == INSIDE && y1tag == INSIDE) {
			return true;
		}
		double x2 = getX2();
		double y2 = getY2();
		int x2tag = getTag(x2, x, x + w);
		int y2tag = getTag(y2, y, y + h);
		if (x2tag == INSIDE && y2tag == INSIDE) {
			return true;
		}
		double ctrlx = getCtrlX();
		double ctrly = getCtrlY();
		int ctrlxtag = getTag(ctrlx, x, x + w);
		int ctrlytag = getTag(ctrly, y, y + h);

		// Trivially reject if all points are entirely to one side of
		// the rectangle.
		if (x1tag < INSIDE && x2tag < INSIDE && ctrlxtag < INSIDE) {
			return false; // All points left
		}
		if (y1tag < INSIDE && y2tag < INSIDE && ctrlytag < INSIDE) {
			return false; // All points above
		}
		if (x1tag > INSIDE && x2tag > INSIDE && ctrlxtag > INSIDE) {
			return false; // All points right
		}
		if (y1tag > INSIDE && y2tag > INSIDE && ctrlytag > INSIDE) {
			return false; // All points below
		}

		// Test for endpoints on the edge where either the segment
		// or the curve is headed "inwards" from them
		// Note: These tests are a superset of the fast endpoint tests
		//       above and thus repeat those tests, but take more time
		//       and cover more cases
		if (inwards(x1tag, x2tag, ctrlxtag) && inwards(y1tag, y2tag, ctrlytag)) {
			// First endpoint on border with either edge moving inside
			return true;
		}
		if (inwards(x2tag, x1tag, ctrlxtag) && inwards(y2tag, y1tag, ctrlytag)) {
			// Second endpoint on border with either edge moving inside
			return true;
		}

		// Trivially accept if endpoints span directly across the rectangle
		boolean xoverlap = (x1tag * x2tag <= 0);
		boolean yoverlap = (y1tag * y2tag <= 0);
		if (x1tag == INSIDE && x2tag == INSIDE && yoverlap) {
			return true;
		}
		if (y1tag == INSIDE && y2tag == INSIDE && xoverlap) {
			return true;
		}

		// We now know that both endpoints are outside the rectangle
		// but the 3 points are not all on one side of the rectangle.
		// Therefore the curve cannot be contained inside the rectangle,
		// but the rectangle might be contained inside the curve, or
		// the curve might intersect the boundary of the rectangle.

		double[] eqn = new double[3];
		double[] res = new double[3];
		if (!yoverlap) {
			// Both y coordinates for the closing segment are above or
			// below the rectangle which means that we can only intersect
			// if the curve crosses the top (or bottom) of the rectangle
			// in more than one place and if those crossing locations
			// span the horizontal range of the rectangle.
			fillEqn(eqn, (y1tag < INSIDE ? y : y + h), y1, ctrly, y2);
			return (solveQuadratic(eqn, res) == 2 && evalQuadratic(res, 2, true, true, null, x1, ctrlx, x2) == 2 && getTag(res[0], x, x + w) * getTag(res[1], x, x + w) <= 0);
		}

		// Y ranges overlap. Now we examine the X ranges
		if (!xoverlap) {
			// Both x coordinates for the closing segment are left of
			// or right of the rectangle which means that we can only
			// intersect if the curve crosses the left (or right) edge
			// of the rectangle in more than one place and if those
			// crossing locations span the vertical range of the rectangle.
			fillEqn(eqn, (x1tag < INSIDE ? x : x + w), x1, ctrlx, x2);
			return (solveQuadratic(eqn, res) == 2 && evalQuadratic(res, 2, true, true, null, y1, ctrly, y2) == 2 && getTag(res[0], y, y + h) * getTag(res[1], y, y + h) <= 0);
		}

		// The X and Y ranges of the endpoints overlap the X and Y
		// ranges of the rectangle, now find out how the endpoint
		// line segment intersects the Y range of the rectangle
		double dx = x2 - x1;
		double dy = y2 - y1;
		double k = y2 * x1 - x2 * y1;
		int c1tag, c2tag;
		if (y1tag == INSIDE) {
			c1tag = x1tag;
		} else {
			c1tag = getTag((k + dx * (y1tag < INSIDE ? y : y + h)) / dy, x, x + w);
		}
		if (y2tag == INSIDE) {
			c2tag = x2tag;
		} else {
			c2tag = getTag((k + dx * (y2tag < INSIDE ? y : y + h)) / dy, x, x + w);
		}
		// If the part of the line segment that intersects the Y range
		// of the rectangle crosses it horizontally - trivially accept
		if (c1tag * c2tag <= 0) {
			return true;
		}

		// Now we know that both the X and Y ranges intersect and that
		// the endpoint line segment does not directly cross the rectangle.
		//
		// We can almost treat this case like one of the cases above
		// where both endpoints are to one side, except that we will
		// only get one intersection of the curve with the vertical
		// side of the rectangle. This is because the endpoint segment
		// accounts for the other intersection.
		//
		// (Remember there is overlap in both the X and Y ranges which
		//  means that the segment must cross at least one vertical edge
		//  of the rectangle - in particular, the "near vertical side" -
		//  leaving only one intersection for the curve.)
		//
		// Now we calculate the y tags of the two intersections on the
		// "near vertical side" of the rectangle. We will have one with
		// the endpoint segment, and one with the curve. If those two
		// vertical intersections overlap the Y range of the rectangle,
		// we have an intersection. Otherwise, we don't.

		// c1tag = vertical intersection class of the endpoint segment
		//
		// Choose the y tag of the endpoint that was not on the same
		// side of the rectangle as the subsegment calculated above.
		// Note that we can "steal" the existing Y tag of that endpoint
		// since it will be provably the same as the vertical intersection.
		c1tag = ((c1tag * x1tag <= 0) ? y1tag : y2tag);

		// c2tag = vertical intersection class of the curve
		//
		// We have to calculate this one the straightforward way.
		// Note that the c2tag can still tell us which vertical edge
		// to test against.
		fillEqn(eqn, (c2tag < INSIDE ? x : x + w), x1, ctrlx, x2);
		int num = solveQuadratic(eqn, res);

		// Note: We should be able to assert(num == 2); since the
		// X range "crosses" (not touches) the vertical boundary,
		// but we pass num to evalQuadratic for completeness.
		evalQuadratic(res, num, true, true, null, y1, ctrly, y2);

		// Note: We can assert(num evals == 1); since one of the
		// 2 crossings will be out of the [0,1] range.
		c2tag = getTag(res[0], y, y + h);

		// Finally, we have an intersection if the two crossings
		// overlap the Y range of the rectangle.
		return (c1tag * c2tag <= 0);
	}

	private static final int BELOW = -2;
	private static final int LOWEDGE = -1;
	private static final int INSIDE = 0;
	private static final int HIGHEDGE = 1;
	private static final int ABOVE = 2;

	/*
	 * Determine where coord lies with respect to the range from low to high. It is assumed that low <= high.  The return value is one of the 5 values BELOW, LOWEDGE, INSIDE, HIGHEDGE, or ABOVE.
	 */
	private static int getTag(double coord, double low, double high) {
		if (coord <= low) {
			return (coord < low ? BELOW : LOWEDGE);
		}
		if (coord >= high) {
			return (coord > high ? ABOVE : HIGHEDGE);
		}
		return INSIDE;
	}

	/*
	 * Determine if the pttag represents a coordinate that is already in its test range, or is on the border with either of the two opttags representing another coordinate that is "towards the
	 * inside" of that test range. In other words, are either of the two "opt" points "drawing the pt inward"?
	 */
	private static boolean inwards(int pttag, int opt1tag, int opt2tag) {
		switch (pttag) {
			case BELOW :
			case ABOVE :
			default :
				return false;
			case LOWEDGE :
				return (opt1tag >= INSIDE || opt2tag >= INSIDE);
			case INSIDE :
				return true;
			case HIGHEDGE :
				return (opt1tag <= INSIDE || opt2tag <= INSIDE);
		}
	}

	public boolean intersects(Rectangle2D r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public boolean contains(double x, double y, double w, double h) {
		return (contains(x, y) && contains(x + w, y) && contains(x + w, y + h) && contains(x, y + h));
	}

	public boolean contains(Rectangle2D r) {
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public Rectangle getBounds() {
		return getBounds2D().getBounds();
	}

	public PathIterator getPathIterator(final AffineTransform at) {
		return new PathIterator() {
			/** Current coordinate. */
			private int current;

			public int getWindingRule() {
				return WIND_NON_ZERO;
			}

			public boolean isDone() {
				return current < 2;
			}

			public void next() {
				current++;
			}

			public int currentSegment(float[] coords) {
				if (current == 0) {
					coords[0] = (float) getX1();
					coords[1] = (float) getY1();
					if (at != null)
						at.transform(coords, 0, coords, 0, 1);
					return SEG_MOVETO;
				}
				if (current == 1) {
					coords[0] = (float) getCtrlX();
					coords[1] = (float) getCtrlY();
					coords[2] = (float) getX2();
					coords[3] = (float) getY2();
					if (at != null)
						at.transform(coords, 0, coords, 0, 2);
					return SEG_QUADTO;
				}
				throw new NoSuchElementException("quad iterator out of bounds");
			}

			public int currentSegment(double[] coords) {
				if (current == 0) {
					coords[0] = getX1();
					coords[1] = getY1();
					if (at != null)
						at.transform(coords, 0, coords, 0, 1);
					return SEG_MOVETO;
				}
				if (current == 1) {
					coords[0] = getCtrlX();
					coords[1] = getCtrlY();
					coords[2] = getX2();
					coords[3] = getY2();
					if (at != null)
						at.transform(coords, 0, coords, 0, 2);
					return SEG_QUADTO;
				}
				throw new NoSuchElementException("quad iterator out of bounds");
			}
		};
	}
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return new FlatteningPathIterator(getPathIterator(at), flatness);
	}

	/**
	 * Create a new curve of the same run-time type with the same contents as this one.
	 * 
	 * @return the clone
	 * 
	 * @exception OutOfMemoryError
	 *                If there is not enough memory available.
	 * 
	 * @since 1.2
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw (Error) new InternalError().initCause(e); // Impossible
		}
	}

	/**
	 * STUBS ONLY
	 */
	public static class Double extends QuadCurve2D {
		public double x1;
		public double y1;
		public double ctrlx;
		public double ctrly;
		public double x2;
		public double y2;

		public Double() {
		}

		public Double(double x1, double y1, double cx, double cy, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			ctrlx = cx;
			ctrly = cy;
			this.x2 = x2;
			this.y2 = y2;
		}

		public double getX1() {
			return x1;
		}
		public double getY1() {
			return y1;
		}
		public Point2D getP1() {
			return new Point2D.Double(x1, y1);
		}

		public double getCtrlX() {
			return ctrlx;
		}
		public double getCtrlY() {
			return ctrly;
		}
		public Point2D getCtrlPt() {
			return new Point2D.Double(ctrlx, ctrly);
		}

		public double getX2() {
			return x2;
		}
		public double getY2() {
			return y2;
		}
		public Point2D getP2() {
			return new Point2D.Double(x2, y2);
		}

		public void setCurve(double x1, double y1, double cx, double cy, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			ctrlx = cx;
			ctrly = cy;
			this.x2 = x2;
			this.y2 = y2;
		}
		public Rectangle2D getBounds2D() {
			double nx1 = Math.min(Math.min(x1, ctrlx), x2);
			double ny1 = Math.min(Math.min(y1, ctrly), y2);
			double nx2 = Math.max(Math.max(x1, ctrlx), x2);
			double ny2 = Math.max(Math.max(y1, ctrly), y2);
			return new Rectangle2D.Double(nx1, ny1, nx2 - nx1, ny2 - ny1);
		}
	} // class Double

	/**
	 * STUBS ONLY
	 */
	public static class Float extends QuadCurve2D {
		public float x1;
		public float y1;
		public float ctrlx;
		public float ctrly;
		public float x2;
		public float y2;

		public Float() {
		}

		public Float(float x1, float y1, float cx, float cy, float x2, float y2) {
			this.x1 = x1;
			this.y1 = y1;
			ctrlx = cx;
			ctrly = cy;
			this.x2 = x2;
			this.y2 = y2;
		}

		public double getX1() {
			return x1;
		}
		public double getY1() {
			return y1;
		}
		public Point2D getP1() {
			return new Point2D.Float(x1, y1);
		}

		public double getCtrlX() {
			return ctrlx;
		}
		public double getCtrlY() {
			return ctrly;
		}
		public Point2D getCtrlPt() {
			return new Point2D.Float(ctrlx, ctrly);
		}

		public double getX2() {
			return x2;
		}
		public double getY2() {
			return y2;
		}
		public Point2D getP2() {
			return new Point2D.Float(x2, y2);
		}

		public void setCurve(double x1, double y1, double cx, double cy, double x2, double y2) {
			this.x1 = (float) x1;
			this.y1 = (float) y1;
			ctrlx = (float) cx;
			ctrly = (float) cy;
			this.x2 = (float) x2;
			this.y2 = (float) y2;
		}
		public void setCurve(float x1, float y1, float cx, float cy, float x2, float y2) {
			this.x1 = x1;
			this.y1 = y1;
			ctrlx = cx;
			ctrly = cy;
			this.x2 = x2;
			this.y2 = y2;
		}
		public Rectangle2D getBounds2D() {
			float nx1 = Math.min(Math.min(x1, ctrlx), x2);
			float ny1 = Math.min(Math.min(y1, ctrly), y2);
			float nx2 = Math.max(Math.max(x1, ctrlx), x2);
			float ny2 = Math.max(Math.max(y1, ctrly), y2);
			return new Rectangle2D.Float(nx1, ny1, nx2 - nx1, ny2 - ny1);
		}
	} // class Float
} // class CubicCurve2D
