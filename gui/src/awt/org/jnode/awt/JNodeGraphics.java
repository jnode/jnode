/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;

import org.jnode.awt.util.AbstractSurfaceGraphics;

/**
 * @author epr
 */
public class JNodeGraphics extends AbstractSurfaceGraphics {

	private final JNodeGenericPeer component;
	private final JNodeToolkit toolkit;

	/**
	 * Initialize a graphics for the given component
	 * @param component
	 */
	public JNodeGraphics(JNodeGenericPeer component) {
		super(component.getToolkitImpl().getGraphics(), ((Component)component.getAwtObject()).getWidth(), ((Component)component.getAwtObject()).getHeight());
		this.component = component;
		this.toolkit = component.getToolkitImpl();
	}

	/**
	 * Initialize a graphics base on the given source.
	 * @param src
	 */
	public JNodeGraphics(JNodeGraphics src) {
		super(src);
		this.component = src.component;
		this.toolkit = src.toolkit;
	}

	/**
	 * @see java.awt.Graphics#create()
	 * @return The graphics
	 */
	public Graphics create() {
		return new JNodeGraphics(this);
	}

	/**
	 * @param font
	 * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
	 * @return The metrics
	 */
	public FontMetrics getFontMetrics(Font font) {
		return toolkit.getFontMetrics(font);
	}

	/**
	 * @see java.awt.Graphics2D#getDeviceConfiguration()
	 * @return The configuration
	 */
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
