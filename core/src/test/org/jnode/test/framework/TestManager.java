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
 
package org.jnode.test.framework;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginDescriptor;

/**
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
final class TestManager implements ExtensionPointListener {

	/** My logger */
	private static final Logger log = Logger.getLogger(TestManager.class);
	
	/** The org.jnode.tests extension point */
	private final ExtensionPoint typesEP;
	
	/** The classes for the tests */
	private final List<Class<? extends Test>> testClasses = new ArrayList<Class<? extends Test>>(); 

	/**
	 * Create a new instance
	 */
	protected TestManager(ExtensionPoint typesEP) {
		this.typesEP = typesEP;
		if (typesEP == null) {
			throw new IllegalArgumentException("The types extension-point cannot be null");
		}		
	}

	/**
	* Load all known tests.
	*/
	synchronized void runTests() {
		log.debug("runTests");
		
        TestSuite suite = new TestSuite();
        for (Class <? extends Test> testClass : testClasses) 
        {
			try
			{
				Test test = (Test) testClass.newInstance();
				suite.addTest(test);
				log.info("adding suite "+testClass.getName());
			} catch (InstantiationException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			}
		}
		
        log.debug("Running tests");
		junit.textui.TestRunner.run(suite);
	}

	/**
	* Refresh all known tests.
	*/
	synchronized void refreshTests() {
		log.debug("refreshTests");
		
		testClasses.clear();
		
		final Extension[] extensions = typesEP.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			final Extension ext = extensions[i];
			final PluginDescriptor desc = ext.getDeclaringPluginDescriptor();
			log.debug("plugin "+desc.getName()+" classloader="+desc.getPluginClassLoader());			
			final ConfigurationElement[] elements = ext.getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				ConfigurationElement e = elements[j];
				if("suite".equals(e.getName()))
				{
					String className = e.getAttribute("class");
					log.debug("className="+className);
					Class<? extends Test> clazz = (Class<? extends Test>) loadClass(className, desc);
					if(clazz != null)
					{
						testClasses.add(clazz);
						log.info("adding test class "+className);
					}
				}
			}
		}
	}
	
	private Class loadClass(String className, PluginDescriptor desc)
	{
		log.debug("searching class "+className);
		
        ClassLoader loader = Thread.currentThread().getContextClassLoader();		
		Class clazz = loadClass(className, loader);
		log.debug("ContextClassLoader:"+((clazz == null)?"not found":"FOUND"));
		
		loader = desc.getPluginClassLoader();
		if(clazz == null) 
		{
			clazz = loadClass(className, loader);
			log.debug("PluginClassLoader:"+((clazz == null)?"not found":"FOUND"));
		}
		
		return clazz;
	}
	
	private Class loadClass(String className, ClassLoader loader)
	{
		try {
			log.debug("loadClass: method 1");
			return Class.forName(className, true, loader);
		} catch (ClassNotFoundException e) {
			try {
				log.debug("loadClass: method 2");
				return loader.loadClass(className);
			} catch (ClassNotFoundException e1) {
				log.debug("loadClass: all methods failed");
				return null;
			}
		}
	}
	
	/**
	 * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
	 */
	public void extensionAdded(ExtensionPoint point, Extension extension) {
		log.debug("extensionAdded");
		refreshTests();
	}

	/**
	 * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
	 */
	public void extensionRemoved(ExtensionPoint point, Extension extension) {
		refreshTests();
	}
}
