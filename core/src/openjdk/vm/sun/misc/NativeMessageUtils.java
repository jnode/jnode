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
 
package sun.misc;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeMessageUtils {
    /**
     *  Print a message directly to stderr, bypassing all the
     *  character conversion methods.
     *  @param msg   message to print
     * @see sun.misc.MessageUtils#toStderr(String)
     */
    public static void toStderr(String msg){
        //todo improve it
        System.err.print(msg);
    }

    /**
     *  Print a message directly to stdout, bypassing all the
     *  character conversion methods.
     *  @param msg   message to print
     * @see sun.misc.MessageUtils#toStdout(String)
     */
    public static void toStdout(String msg){
        //todo improve it
        System.out.print(msg);
    }
}
