/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.VMImageAPI;

import org.jnode.awt.image.JNodeBufferedImageGraphics;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VMImageAPIImpl implements VMImageAPI {
	
	public Graphics2D createGraphics(BufferedImage image) {
		return new JNodeBufferedImageGraphics(image);
	}
	
}
