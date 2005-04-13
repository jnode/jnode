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
package org.jnode.fs.jifs.def;

import org.apache.log4j.Logger;

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
/**
 * @author Andreas Haenel
 */
final class JIFSExtension implements ExtensionPointListener {
	
	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** The org.jnode.fs.jifs.info extension point */
	private final ExtensionPoint infoEP;
	
	protected JIFSExtension(ExtensionPoint infoEP){
		if (infoEP == null) {
			throw new IllegalArgumentException("The info extension-point cannot be null.");
		}
		this.infoEP = infoEP;
	}
	
	/**
	 * An extension has been added to an extension point
	 * @param point
	 * @param extension
	 */
	public void extensionAdded(ExtensionPoint point, Extension extension){
		if (point.equals(infoEP)){
			log.info("Extension to JIFS added..");
		}
	}

	/**
	 * An extension has been removed from an extension point
	 * @param point
	 * @param extension
	 */
	public void extensionRemoved(ExtensionPoint point, Extension extension){
		if (point.equals(infoEP)){
			log.info("Extension from JIFS removed..");
		}
	}
}