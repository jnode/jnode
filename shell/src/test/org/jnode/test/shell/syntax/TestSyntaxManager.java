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
 
package org.jnode.test.shell.syntax;
 
import java.util.Collection;
import java.util.HashMap;

import org.jnode.shell.syntax.Syntax;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;

/**
 * @author crawley@jnode.org
 */
public class TestSyntaxManager implements SyntaxManager {

    private final HashMap<String, SyntaxBundle> syntaxes = new HashMap<String, SyntaxBundle>();

    public void add(SyntaxBundle bundle) {
        syntaxes.put(bundle.getAlias(), bundle);
    }

    public SyntaxBundle remove(String alias) {
        return syntaxes.remove(alias);
    }

    public SyntaxBundle getSyntaxBundle(String alias) {
        return syntaxes.get(alias);
    }

    public SyntaxManager createSyntaxManager() {
        return new TestSyntaxManager();
    }

    @Override
    public Collection<String> getKeys() {
        throw new UnsupportedOperationException("go away");
    }
    
    
}