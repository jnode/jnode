/*
 * $Id $
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
package org.jnode.configure;

/**
 * This exception is thrown when we have diagnosed a problem while processing a
 * configure script.
 * 
 * @author crawley@jnode.org
 */
public class ConfigureException extends Exception {

    private static final long serialVersionUID = 349388505334474279L;

    private ScriptParser.ParseContext[] stack;

    public ConfigureException(String message, Throwable cause) {
        super(message + ": " + cause.getLocalizedMessage(), cause);
        this.stack = null;
    }

    public ConfigureException(String message) {
        super(message);
        this.stack = null;
    }

    public ConfigureException(String message, ScriptParser.ParseContext[] stack) {
        super(message);
        this.stack = stack;
    }

    public ScriptParser.ParseContext[] getStack() {
        return stack;
    }

    public void setStack(ScriptParser.ParseContext[] stack) {
        this.stack = stack;
    }
}
