/*
 * $Id$
 */
package org.jnode.shell.alias.def;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;


/**
 * @author epr
 */
public class DefaultAliasManager implements AliasManager, ExtensionPointListener {

	private final DefaultAliasManager parent;
	private final HashMap aliases = new HashMap();
	private final ExtensionPoint aliasesEP;
	
	/**
	 * Create a new instance
	 */
	public DefaultAliasManager(ExtensionPoint aliasesEP) {
		this.parent = null;
		this.aliasesEP = aliasesEP;
		refreshAliases();
	}
	
	/**
	 * Create a new instance
	 */
	public DefaultAliasManager(DefaultAliasManager parent) {
		this.parent = parent;
		this.aliasesEP = null;
		if (parent == null) {
			throw new IllegalArgumentException("parent cannot be null");
		}
	}
	
	/**
	 * Add an alias
	 * @param alias
	 * @param className
	 */
	public void add(String alias, String className) {
		if (parent == null) {
			throw new UnsupportedOperationException("Cannot modify the system alias manager");
		} else {
			aliases.put(alias, new Alias(alias, className));
		}
	}
	
	/**
	 * Remove an alias
	 * @param alias
	 */
	public void remove(String alias) {
		if (parent == null) {
			throw new UnsupportedOperationException("Cannot modify the system alias manager");
		} else {
			aliases.remove(alias);
		}
	}
	
	/**
	 * Gets the class of a given alias
	 * @param alias
	 * @return the class of the given alias
	 * @throws ClassNotFoundException
	 */
	public Class getAliasClass(String alias) 
	throws ClassNotFoundException, NoSuchAliasException {
		return getAlias(alias).getAliasClass();		
	}

	/**
	 * Gets the classname of a given alias
	 * @param alias
	 * @return the classname of the given alias
	 */
	public String getAliasClassName(String alias) 
	throws NoSuchAliasException {
		return getAlias(alias).getClassName();		
	}
	
	
	/**
	 * Create a new alias manager that has this alias manager as parent.
	 */
	public AliasManager createAliasManager() {
		return new DefaultAliasManager(this);		
	}
	
	
	/**
	 * Gets an iterator to iterator over all aliases.
	 * @return An iterator the returns instances of String.
	 */
	public Iterator aliasIterator() {
		if (parent == null) {
			return aliases.keySet().iterator();
		} else {
			final HashSet all = new HashSet();
			for (Iterator i = parent.aliasIterator(); i.hasNext(); ) {
				all.add(i.next());		
			}
			all.addAll(aliases.keySet());
			return all.iterator();
		}
	}
	
	/**
	 * Gets the alias with the given name
	 * @param alias
	 */
	protected Alias getAlias(String alias) 
	throws NoSuchAliasException {
		final Alias a = (Alias)aliases.get(alias);
		if (a != null) {
			return a;
		} else if (parent != null) {
			return parent.getAlias(alias);
		} else {
			throw new NoSuchAliasException(alias);
		}
	}

	/**
	 * Reload the alias list from the extension-point
	 *
	 */
	protected void refreshAliases() {
		if (aliasesEP != null) {
			aliases.clear();
			final Extension[] extensions = aliasesEP.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				final Extension ext = extensions[i];
				final ConfigurationElement[] elements = ext.getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					createAlias(aliases, elements[j]);
				}
			}
		}
	}
	
	private void createAlias(Map aliases, ConfigurationElement element) {
		final String name = element.getAttribute("name");
		final String className = element.getAttribute("class");
		if ((name != null) && (className != null)) {
			aliases.put(name, new Alias(name, className));			
		}
	}
	
	/**
	 * @see org.jnode.plugin.ExtensionPointListener#extensionAdded(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
	 */
	public void extensionAdded(ExtensionPoint point, Extension extension) {
		refreshAliases();
	}

	/**
	 * @see org.jnode.plugin.ExtensionPointListener#extensionRemoved(org.jnode.plugin.ExtensionPoint, org.jnode.plugin.Extension)
	 */
	public void extensionRemoved(ExtensionPoint point, Extension extension) {
		refreshAliases();
	}

	static class Alias {
		private final String alias;
		private final String className;
		private Class aliasClass;
		
		public Alias(String alias, String className) {
			this.alias = alias;
			this.className = className;
		}
		
		/**
		 * Gets the name of this alias
		 */
		public String getAlias() {
			return alias;
		}

		/**
		 * Gets the name of the class of this alias
		 */
		public String getClassName() {
			return className;
		}
		
		/**
		 * Gets the class of this alias
		 */
		public Class getAliasClass() 
		throws ClassNotFoundException {
			if (aliasClass == null) {
				aliasClass = Thread.currentThread().getContextClassLoader().loadClass(className);
			}
			return aliasClass;
		}
	}	
}
