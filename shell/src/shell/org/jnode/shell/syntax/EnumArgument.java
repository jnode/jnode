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


import java.lang.reflect.Array;

import org.jnode.shell.CommandLine.Token;

/**
 * This Argument class accepts values corresponding to an enum type.
 * 
 * @author crawley@jnode.org
 *
 * @param <E> the enum type.
 */
public class EnumArgument<E extends Enum<E>> extends Argument<E> {
    
    private final Class<E> clazz;

    @SuppressWarnings("unchecked")
    public EnumArgument(String label, int flags, Class<E> clazz, String description) {
        super(label, flags, (E[]) Array.newInstance(clazz, 0), description);
        this.clazz = clazz;
    }

    public EnumArgument(String label, int flags, Class<E> clazz) {
        this(label, flags, clazz, null);
    }
    
    @Override
    protected void doAccept(Token token) throws CommandSyntaxException {
        try {
            addValue(E.valueOf(clazz, token.token));
        }
        catch (IllegalArgumentException ex) {
            throw new CommandSyntaxException("invalid value for enum");
        }
    }
    
    public String toString() {
        return "EnumArgument<" + clazz + "{" + super.toString() + "}";
    }

    @Override
    protected String argumentKind() {
        return clazz.getSimpleName();
    }

}
