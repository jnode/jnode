/*
 * $Id$
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
