/* GeneralPath.java -- represents a shape built from subpaths
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

import java.awt.Rectangle;
import java.awt.Shape;

/**
 * STUBS ONLY
 * XXX Implement and document. Note that Sun's implementation only expects
 * float precision, not double.
 */
public final class GeneralPath implements Shape, Cloneable {

	public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;
	public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

	/** Initial size if not specified. */
	private static final int INIT_SIZE = 20;

	/** The winding rule. */
	int rule;

	private Segment firstSeg;
	private Segment curSeg;
	private float[] minMax = new float[] { Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE };

	/** The index of the most recent moveto point, or null. */
	private Point2D subpath = null;

	static final int nrPoints[] = { 2, 2, 4, 6, 2 };

	public GeneralPath() {
		this(WIND_NON_ZERO, INIT_SIZE);
	}
	public GeneralPath(int rule) {
		this(rule, INIT_SIZE);
	}
	public GeneralPath(int rule, int capacity) {
		if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
			throw new IllegalArgumentException();
		}
		this.rule = rule;
		if (capacity < INIT_SIZE) {
			capacity = INIT_SIZE;
		}
		firstSeg = new Segment(capacity);
		curSeg = firstSeg;
	}

	public GeneralPath(Shape s) {
		if (s instanceof GeneralPath) {
			final GeneralPath src = (GeneralPath)s;
			this.rule = src.rule;
			this.firstSeg = new Segment(src.firstSeg);
			this.curSeg = firstSeg;
			Segment srcP = src.firstSeg;
			Segment thisP = this.firstSeg;
			while (srcP.getNext() != null) {
				thisP.append(new Segment(srcP.getNext()));
				thisP = thisP.getNext();
				srcP = srcP.getNext();
				curSeg = thisP;
			} 
			this.minMax = new float[4];
			System.arraycopy(src.minMax, 0, this.minMax, 0, 4);
		} else {
			firstSeg = new Segment();
			curSeg = firstSeg;
			final PathIterator pi = s.getPathIterator(null);
			setWindingRule(pi.getWindingRule());
			append(pi, false);
		}
	}

	public void moveTo(float x, float y) {
		subpath = new Point2D.Float(x, y);
		add(PathIterator.SEG_MOVETO, x, y);
	}

	public void lineTo(float x, float y) {
		add(PathIterator.SEG_LINETO, x, y);
	}

	public void quadTo(float x1, float y1, float x2, float y2) {
		add(PathIterator.SEG_QUADTO, x1, y1);
		add(0, x2, y2);
	}

	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
		add(PathIterator.SEG_CUBICTO, x1, y1);
		add(0, x2, y2);
		add(0, x3, y3);
	}

	public void closePath() {
		add(PathIterator.SEG_CLOSE, (float) subpath.getX(), (float) subpath.getY());
	}

	public void append(Shape s, boolean connect) {
		append(s.getPathIterator(null), connect);
	}

	public void append(PathIterator pi, boolean connect) {
		final float[] f = new float[6];
		while (!pi.isDone()) {
			final int result = pi.currentSegment(f);
			pi.next();
			switch (result) {
				case PathIterator.SEG_MOVETO :
					if (!connect) {
						moveTo(f[0], f[1]);
						break;
					}
					if ((subpath != null) && (f[0] == subpath.getX()) && (f[1] == subpath.getY())) {
						break;
					}
					// Fallthrough.
				case PathIterator.SEG_LINETO :
					lineTo(f[0], f[1]);
					break;
				case PathIterator.SEG_QUADTO :
					quadTo(f[0], f[1], f[2], f[3]);
					break;
				case PathIterator.SEG_CUBICTO :
					curveTo(f[0], f[1], f[2], f[3], f[4], f[5]);
					break;
				default :
					closePath();
			}
			connect = false;
		}
	}

	public int getWindingRule() {
		return rule;
	}
	public void setWindingRule(int rule) {
		if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO)
			throw new IllegalArgumentException();
		this.rule = rule;
	}

	public Point2D getCurrentPoint() {
		return subpath;
	}

	public void reset() {
		subpath = null;
		firstSeg = new Segment();
		curSeg = firstSeg;
	}

	public void transform(AffineTransform xform) {
		Segment p = firstSeg;
		while (p != null) {
			p.transform(xform);
			p = p.getNext();
		}
		xform.transform(minMax, 0, minMax, 0, 2);
	}

	public Shape createTransformedShape(AffineTransform xform) {
		GeneralPath p = new GeneralPath(this);
		p.transform(xform);
		return p;
	}

	public Rectangle getBounds() {
		return getBounds2D().getBounds();
	}
	
	public Rectangle2D getBounds2D() {
		final float w = (minMax[2] - minMax[0]) + 1;
		final float h = (minMax[3] - minMax[1]) + 1;
		return new Rectangle2D.Float(minMax[0], minMax[1], w, h);
	}

	public boolean contains(double x, double y) {
		// XXX Implement.
		throw new Error("not implemented");
	}
	
	public boolean contains(Point2D p) {
		return contains(p.getX(), p.getY());
	}
	
	public boolean contains(double x, double y, double w, double h) {
		// XXX Implement.
		throw new Error("not implemented");
	}
	
	public boolean contains(Rectangle2D r) {
		return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public boolean intersects(double x, double y, double w, double h) {
		// XXX Implement.
		throw new Error("not implemented");
	}

	public boolean intersects(Rectangle2D r) {
		return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}

	public PathIterator getPathIterator(final AffineTransform at) {
		final Segment firstSeg = this.firstSeg;
		return new PathIterator() {
			private int current = 0;
			private Segment seg = firstSeg;

			public int getWindingRule() {
				return rule;
			}

			public boolean isDone() {
				return seg.isDone(current);
			}

			public void next() {
				if (!isDone()) {
					final int type = seg.getType(current);
					final int count = nrPoints[type];
					current += count;
					if ((current >= seg.size()) && (seg.getNext() != null)) {
						current -= seg.size();
						seg = seg.getNext();
					}
				}
			}

			public int currentSegment(float[] coords) {
				if (isDone()) {
					return SEG_CLOSE;
				}
				final int type = seg.getType(current);
				final int count = nrPoints[type];
				for (int i = 0; i < count; i++) {
					coords[i] = seg.get(current + i);
				}
				if (at != null) {
					at.transform(coords, 0, coords, 0, count / 2);
				}
				return type;
			}

			public int currentSegment(double[] coords) {
				if (isDone()) {
					return SEG_CLOSE;
				}
				final int type = seg.getType(current);
				final int count = nrPoints[type];
				for (int i = 0; i < count; i++) {
					coords[i] = seg.get(current + i);
				}
				if (at != null) {
					at.transform(coords, 0, coords, 0, count / 2);
				}
				return type;
			}
		};
	}

	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		return new FlatteningPathIterator(getPathIterator(at), flatness);
	}

	/**
	 * Create a new shape of the same run-time type with the same contents as
	 * this one.
	 *
	 * @return the clone
	 *
	 * @exception OutOfMemoryError If there is not enough memory available.
	 *
	 * @since 1.2
	 */
	public Object clone() {
		// This class is final; no need to use super.clone().
		return new GeneralPath(this);
	}

	private void add(int type, float x, float y) {
		if (!curSeg.add(type, x, y)) {
			final Segment newSeg = new Segment();
			curSeg.append(newSeg);
			curSeg = newSeg;
			newSeg.add(type, x, y);
		}
		minMax[0] = Math.min(minMax[0], x);
		minMax[1] = Math.min(minMax[1], y);
		minMax[2] = Math.max(minMax[2], x);
		minMax[3] = Math.max(minMax[3], y);
	}

	private static final class Segment {

		static final int DEFAULT_SIZE = 1024;

		private final byte[] types;
		private final float[] points;
		private final int size;
		private Segment next;
		private int index;

		public Segment() {
			this(DEFAULT_SIZE);
		}

		public Segment(int nrPoints) {
			this.types = new byte[nrPoints >> 1];
			this.points = new float[nrPoints];
			this.size = nrPoints;
		}

		/**
		 * Create a copy of the given segment.
		 * The next segment is not copied, next is set to null.
		 * @param src
		 */
		public Segment(Segment src) {
			this.types = new byte[src.types.length];
			this.points = new float[src.points.length];
			this.size = src.size;
			this.index = src.index;
			this.next = null;
			System.arraycopy(src.types, 0, this.types, 0, src.types.length);
			System.arraycopy(src.points, 0, this.points, 0, src.points.length);
		}

		/**
		 * Add a point
		 * @param index
		 * @param type
		 * @param x
		 * @param y
		 * @return true if added, false if index >= size
		 */
		public boolean add(int type, float x, float y) {
			if (index < size) {
				types[index >> 1] = (byte) type;
				points[index++] = x;
				points[index++] = y;
				return true;
			} else {
				return false;
			}
		}

		public int size() {
			return size;
		}

		public float get(int index) {
			if (index < size) {
				return points[index];
			} else {
				return next.get(index - size);
			}
		}

		public int getType(int index) {
			return types[index >> 1];
		}

		/**
		 * @return
		 */
		public Segment getNext() {
			return this.next;
		}

		/**
		 * Append the given segment to the end of the list
		 * @param s
		 */
		public void append(Segment s) {
			Segment p = this;
			while (p.next != null) {
				p = p.next;
			}
			p.next = s;
		}

		public void transform(AffineTransform xform) {
			xform.transform(points, 0, points, 0, index >> 1);
		}

		/**
		 * @return
		 */
		final int getIndex() {
			return this.index;
		}

		public boolean isDone(int current) {
			return (current >= index) && (next == null);
		}
	}
}
