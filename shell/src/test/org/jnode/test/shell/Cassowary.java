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

import javax.naming.NamingException;

import org.apache.log4j.BasicConfigurator;
import org.jnode.emu.naming.BasicNameSpace;
import org.jnode.emu.plugin.model.DummyExtensionPoint;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.def.DefaultShellManager;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.help.def.DefaultHelpFactory;

/**
 * This class assembles a minimal set of JNode services to support unit
 * testing some shell and syntax infrastructure.
 * <p>
 * A Cassowary is another large flightless bird ... like an Emu but different.
 *
 * @author crawley@jnode.org
 */
public class Cassowary {
    private static boolean initialized;

    protected static void initEnv() throws NamingException {
        if (initialized) {
            return;
        }
        InitialNaming.setNameSpace(new BasicNameSpace());
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
