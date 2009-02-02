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
 
package com.sun.java.util.jar.pack;

/**
 * @author Levente S\u00e1ntha
 */
public class Pack200Command {
    public static void main(String[] argv) throws Exception {
        System.setProperty(Utils.DEBUG_DISABLE_NATIVE, "true");
        String[] args = new String[argv.length + 1];
        args[0] = "--pack";
        for(int i = 0; i < argv.length; i++){
            args[i + 1] = argv[i];
        }
        Driver.main(args);
    }
}
