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
package org.jnode.shell.syntax;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.driver.input.KeyboardLayoutManager;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandLine.Token;


/**
 * This argument accepts any string as a potential keyboard layout id, and completes
 * against the ids registered with the keyboard layout manager.
 * 
 * @author crawley@jnode.org
 */
public class KeyboardLayoutArgument extends StringArgument {

    public KeyboardLayoutArgument(String label, int flags, String description) {
        super(label, flags, description);
    }

    @Override
    protected String argumentKind() {
        return "keyboard layout";
    }
    
    @Override
    protected String doAccept(Token token) throws CommandSyntaxException {
        return token.text; 
    }

    @Override
    public void complete(CompletionInfo completion, String partial) {
        try {
            KeyboardLayoutManager mgr = InitialNaming.lookup(KeyboardLayoutManager.NAME);
            // collect matching devices
            for (String layout : mgr.layouts()) {
                if (layout.startsWith(partial)) {
                    completion.addCompletion(layout);
                }
            }
        } catch (NameNotFoundException ex) {
            throw new SyntaxFailureException("KeyboardLayoutManager not found. Check your system setup");
        }
    }
}
