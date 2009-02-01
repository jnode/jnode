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


import java.lang.reflect.Array;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;

/**
 * This Argument class accepts values corresponding to an enum type.  It should
 * be subclassed for each enum.
 * 
 * @author crawley@jnode.org
 *
 * @param <E> the enum type.  Note, an EnumArgument uses E's declared enum names
 * when mapping to and from command line tokens.  It is a bad idea for the E type
 * to have an overloaded toString() method.
 */
public abstract class EnumArgument<E extends Enum<E>> extends Argument<E> {
    
    private final Class<E> clazz;

    /**
     * Construct the enum argument.  The clazz argument is required so that we can
     * map between the enum values and their names.
     * 
     * @param label
     * @param flags
     * @param clazz
     * @param description
     */
    @SuppressWarnings("unchecked")
    public EnumArgument(String label, int flags, Class<E> clazz, String description) {
        super(label, flags, (E[]) Array.newInstance(clazz, 0), description);
        this.clazz = clazz;
    }

    public EnumArgument(String label, int flags, Class<E> clazz) {
        this(label, flags, clazz, null);
    }
    
    @Override
    protected E doAccept(Token token) throws CommandSyntaxException {
        try {
            return E.valueOf(clazz, token.text);
        } catch (IllegalArgumentException ex) {
            throw new CommandSyntaxException("not a valid <" + argumentKind() + ">");
        }
    }
    
    @Override
    public void complete(CompletionInfo completion, String partial) {
        for (E e : clazz.getEnumConstants()) {
            String eName = e.name();
            if (eName.startsWith(partial)) {
                completion.addCompletion(eName);
            }
        }
    }

    public String toString() {
        return "EnumArgument<" + clazz + ">{" + super.state() + "}";
    }

    @Override
    protected abstract String argumentKind();

}
