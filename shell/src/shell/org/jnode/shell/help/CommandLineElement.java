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
 
package org.jnode.shell.help;

import java.io.PrintStream;

import org.jnode.driver.console.CompletionInfo;

/**
 * @author qades
 */
abstract class CommandLineElement {
    private final String name;
    private final String description;

    public CommandLineElement(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract String format();
    public abstract void describe(Help help, PrintStream out);
    public abstract void complete(CompletionInfo completion, String partial);

    /** 
     * Indicates if the element is satisfied.
     * I.e. not taking any more values.
     * @return <code>false</code> if the element takes more argument values;
     * <code>false</code> otherwise
     */
    public abstract boolean isSatisfied();

}
