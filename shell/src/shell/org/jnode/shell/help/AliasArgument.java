/*
 * $Id$
 */

package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;

/**
 * @author qades
 */
public class AliasArgument extends Argument {

	public AliasArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public AliasArgument(String name, String description) {
		super(name, description);
	}


	public String complete(String partial) {
		List aliases = new ArrayList();
                try {
			// get the alias manager
			final AliasManager aliasMgr = ShellUtils.getShellManager().getCurrentShell().getAliasManager();

                        // collect matching aliases
			Iterator i = aliasMgr.aliasIterator();
			while( i.hasNext() ) {
				String alias = (String) i.next();
				if( alias.startsWith(partial) )
					aliases.add(alias);
			}

			return complete(partial, aliases);
		} catch( NameNotFoundException ex ) {
			// should not happen!
			return partial;
		}
	}
}
