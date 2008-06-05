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

import java.util.Map;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;

/**
 * This Argument base class is constructed with a Map that maps from String
 * values to instances of the parameter type V.  It then accepts tokens and
 * performs completion based on the Map.
 * 
 * @author crawley@jnode.org
 *
 * @param <V> the type of values recognized by this Argument class.
 */
public abstract class MappedArgument<V> extends Argument<V> {
    private final Map<String, V> valueMap;
    private final boolean caseInsensitive;

    /**
     * Construct an instance.  If caseInsensitive is <code>true</code>, the keySet of the valueMap
     * should consist of lower-cased Strings.  The constructor does not check this.  (Any map entry
     * containing an upper-case character will never match a token or supply a completion.)
     * 
     * @param label the argument's label (for Syntax binding)
     * @param flags the argument's flags
     * @param array the prototype value array
     * @param valueMap the Map that defines the set of acceptable tokens and the corresponding values.
     * @param caseInsensitive if <code>true</code>, token values will be converted to lower 
     *        case before they are checked against the valueMap.
     * @param description the argument's optional description, or <code>null</code>
     */
    public MappedArgument(String label, int flags, V[] array,
            Map<String, V> valueMap, boolean caseInsensitive, String description) {
        super(label, flags, array, description);
        this.valueMap = valueMap;
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Complete partial against the domain of the valueMap.
     */
    @Override
    public void complete(CompletionInfo completion, String partial) {
        if (caseInsensitive) {
            partial = partial.toLowerCase();
        }
        for (String str : valueMap.keySet()) {
            if (str.startsWith(partial)) {
                completion.addCompletion(str);
            }
        }
    }

    /**
     * Accept token if it is in the domain of the valueMap.
     */
    @Override
    protected V doAccept(Token token) throws CommandSyntaxException {
        String t = caseInsensitive ? token.token.toLowerCase() : token.token;
        V value = valueMap.get(t);
        if (value == null) {
            throw new CommandSyntaxException("'" + token.token + 
                    "' is not an acceptable " + argumentKind());
        } else {
            return value;
        }
    }
}
