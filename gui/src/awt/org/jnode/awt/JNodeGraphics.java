/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
	public JNodeGraphics(JNodeGenericPeer<?, ?> component) {
		super(component.getToolkitImpl().getGraphics(), ((Component)component.getTargetComponent()).getWidth(), ((Component)component.getTargetComponent()).getHeight());
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
