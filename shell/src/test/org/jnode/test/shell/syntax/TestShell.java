/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.test.shell.syntax;

import org.jnode.shell.CommandShell;
import org.jnode.shell.syntax.Syntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * Fake shell implementation that provides services for testing CommandLine etc.
 *
 * @author crawley@jnode.org
 */
public class TestShell extends CommandShell {

    public TestShell() {
        super(new TestAliasManager(), new TestSyntaxManager());
    }

    public void addAlias(String alias, String className) {
        getAliasManager().add(alias, className);
    }

    public void addSyntax(String alias, Syntax syntax) {
        getSyntaxManager().add(new SyntaxBundle(alias, syntax));
    }
}
