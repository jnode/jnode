/* Rasterizer.java -- stub file.
   Copyright (C) 2007 Red Hat, Inc.

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by
the Free Software Foundation, version 2.

IcedTea is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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
exception statement from your version.
*/

package sun.dc.pr;

public class Rasterizer {

	public static final int BUTT = 0;
	public static final int ROUND = 0;
	public static final int SQUARE = 0;
	public static final int MITER = 0;
	public static final int BEVEL = 0;
	public static final int TILE_IS_GENERAL = 0;
	public static final String STROKE = null;
	public static final String EOFILL = null;
	public static final String NZFILL = null;
	public static int TILE_SIZE;
	public static int TILE_IS_ALL_0;
	
	public void getAlphaBox(int[] abox) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setOutputArea(int i, int j, int k, int l) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public int getTileState() {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub

	}

	public void nextTile() {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void reset() {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void writeAlpha(byte[] alpha, int xstride, int ystride, int offset) throws InterruptedException {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setPenDiameter(float minPenSizeAA) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setUsage(String stroke2) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setPenT4(float[] matrix) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setPenFitting(float penUnits, int minPenUnitsAA) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setCaps(int i) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setCorners(int i, float miterLimit) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setDash(float[] dashes, float dashPhase) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void setDashT4(float[] matrix) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void beginPath() {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void beginSubpath(float mx, float my) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void appendLine(float f, float g) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void appendQuadratic(float f, float g, float h, float i) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void appendCubic(float f, float g, float h, float i, float j, float k) {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void closedSubpath() {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

	public void endPath() throws PRException {
		throw new RuntimeException("Not implemented.");
		// TODO Auto-generated method stub
		
	}

}
