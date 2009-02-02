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
 
package org.jnode.shell.syntax;

/**
 * This exception is thrown when an application tries to use a single-valued Argument as
 * multi-valued, or vice-versa.  This is deemed to be an application error, and hence is
 * defined as an unchecked exception.
 * 
 * @author crawley@jnode
 */
public class SyntaxMultiplicityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SyntaxMultiplicityException(String message) {
        super(message);
    }

}
