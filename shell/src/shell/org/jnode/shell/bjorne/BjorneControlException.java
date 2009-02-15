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
 
package org.jnode.shell.bjorne;

import org.jnode.shell.ShellException;

/**
 * This exception is used to implement constructs that exit scopes
 * in the bjorne interpreter.  It should never be thrown by an application.
 * 
 * @author crawley@jnode.org
 */
class BjorneControlException extends ShellException {

    private static final long serialVersionUID = 1L;

    private final int control;

    private int count;

    BjorneControlException(final int control, final int count) {
        super();
        this.control = control;
        this.count = count;
    }

    final int getControl() {
        return control;
    }

    final int getCount() {
        return count;
    }

    final void decrementCount() {
        count--;
    }

}
