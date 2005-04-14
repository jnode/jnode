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
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Andreas Haenel
 */
public class JIFSPlugin extends Plugin{

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
	/** Manager of Extensions */
    private final JIFSExtension jifsExtension;
	
	public JIFSPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.jifsExtension = new JIFSExtension(descriptor
                .getExtensionPoint("info"));
        log.debug("JIFSPlugin created.");
    }
	
	/**
     * Start this plugin
     */
    protected void startPlugin() throws PluginException {
//    	//create / mount JIFS
//    	try{
//    		createJIFS cJ = new createJIFS();
//    		cJ.execute(new CommandLine("start"),System.in,System.out,System.err);
//    		log.info("JIFSPlugin started.");
//    	} catch (Exception e){
//    		log.error(e);
//    	}
    }

    /**
     * Stop this plugin
     */
    protected void stopPlugin() {
////    	unmount / free JIFS
//    	try{
//    		createJIFS cJ = new createJIFS();
//    		cJ.execute(new CommandLine("stop"),System.in,System.out,System.err);
//    		log.info("JIFSPlugin stopped.");
//    	} catch (Exception e){
//    		log.error(e);
//    	}
    }
	
}