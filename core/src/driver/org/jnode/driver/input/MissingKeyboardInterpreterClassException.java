/*
 * $Id: KeyboardInterpreter.java 4604 2008-10-05 02:25:18Z crawley $
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
package org.jnode.driver.input;

/**
 * This exception is thrown to indicate that a keyboard layout identifier maps
 * to a non-existent {@link KeyboardInterpreter} class.
 * 
 * @author crawley@jnode.org
 */
public class MissingKeyboardInterpreterClassException extends KeyboardInterpreterException {

    public MissingKeyboardInterpreterClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingKeyboardInterpreterClassException(String message) {
        super(message);
    }

}
