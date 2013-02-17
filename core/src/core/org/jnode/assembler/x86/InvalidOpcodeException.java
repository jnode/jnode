/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.assembler.x86;

/**
 * Exception used to signal an opcode with is invalid in the current
 * operating mode.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvalidOpcodeException extends RuntimeException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3761408616460268596L;

    /**
     *
     */
    public InvalidOpcodeException() {
        super();
    }

    /**
     * @param s
     */
    public InvalidOpcodeException(String s) {
        super(s);
    }

    /**
     * @param s
     * @param cause
     */
    public InvalidOpcodeException(String s, Throwable cause) {
        super(s, cause);
    }

    /**
     * @param cause
     */
    public InvalidOpcodeException(Throwable cause) {
        super(cause);
    }
}
