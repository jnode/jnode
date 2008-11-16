/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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
package org.jnode.test.shell;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.log4j.BasicConfigurator;
import org.jnode.naming.AbstractNameSpace;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.def.DefaultShellManager;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.help.def.DefaultHelpFactory;

/**
 * A Cassowary is another large Australian bird ...
 *
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class Cassowary {
    private static boolean initialized;

    protected static void initEnv() throws NamingException {
        if (initialized) {
            return;
        }
        InitialNaming.setNameSpace(new AbstractNameSpace() {
            private Map<Class<?>, Object> space = new HashMap<Class<?>, Object>();

            public <T> void bind(Class<T> name, T service)
                throws NamingException, NameAlreadyBoundException {
                if (space.get(name) != null) {
                    throw new NameAlreadyBoundException();
                }
                space.put(name, service);
            }

            public void unbind(Class<?> name) {
                space.remove(name);
            }

            public <T> T lookup(Class<T> name) throws NameNotFoundException {
                T obj = (T) space.get(name);
                if (obj == null) {
                    throw new NameNotFoundException(name.getName());
                }
                return obj;
            }

            public Set<Class<?>> nameSet() {
                return space.keySet();
            }
        });
        InitialNaming.bind(DeviceManager.NAME, DeviceManager.INSTANCE);
        AliasManager alias_mgr =
            new DefaultAliasManager(new DummyExtensionPoint()).createAliasManager();
        InitialNaming.bind(AliasManager.NAME, alias_mgr);
        InitialNaming.bind(ShellManager.NAME, new DefaultShellManager());
        InitialNaming.bind(HelpFactory.NAME, new DefaultHelpFactory());

        BasicConfigurator.configure();
        initialized = true;
    }
}
