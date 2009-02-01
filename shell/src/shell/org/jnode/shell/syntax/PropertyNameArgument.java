/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.shell.syntax;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;


/**
 * This argument class accepts property names, with completion against the
 * names in the System properties object.
 * 
 * @author crawley@jnode.org
 */
public class PropertyNameArgument extends Argument<String> {
    
    public PropertyNameArgument(String label, int flags, String description) {
        super(label, flags, new String[0], description);
    }

    public PropertyNameArgument(String label, int flags) {
        this(label, flags, null);
    }

    public PropertyNameArgument(String label) {
        this(label, 0);
    }
    
    @Override
    protected String doAccept(Token token) throws CommandSyntaxException {
        return token.text;
    }
    
    @Override
    public void complete(CompletionInfo completion, String partial) {
        for (Object key : System.getProperties().keySet()) {
            String name = (String) key;
            if (name.startsWith(partial)) {
                completion.addCompletion(name);
            }
        }
    }

    @Override
    protected String argumentKind() {
        return "property";
    }

}
