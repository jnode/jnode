/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.awt.image;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

/**
 * @author epr
 */
public class JNodeBufferedImage extends BufferedImage {

	/**
	 * @param colormodel
	 * @param writableraster
	 * @param premultiplied
	 * @param properties
	 */
	public JNodeBufferedImage(ColorModel colormodel, WritableRaster writableraster, boolean premultiplied, Hashtable properties) {
		super(colormodel, writableraster, premultiplied, properties);
	}

	/**
	 * @param w
	 * @param h
	 * @param type
	 */
	public JNodeBufferedImage(int w, int h, int type) {
		super(w, h, type);
	}

	/**
	 * @param w
	 * @param h
	 * @param type
	 * @param indexcolormodel
	 */
	public JNodeBufferedImage(int w, int h, int type, IndexColorModel indexcolormodel) {
		super(w, h, type, indexcolormodel);
	}

	/**
	 * @see java.awt.image.BufferedImage#createGraphics()
	 * @return The graphics
	 */
	public Graphics2D createGraphics() {
		return new JNodeBufferedImageGraphics(this);
	}
}
