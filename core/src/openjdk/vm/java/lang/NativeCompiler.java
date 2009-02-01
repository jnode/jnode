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
 
package java.lang;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeCompiler {
    /**
     * @see Compiler#initialize()
     */
    private static void initialize(){}

    /**
     * @see Compiler#registerNatives()
     */
    private static void registerNatives(){}

    /**
     * @see Compiler@compileClass
     */
    public static boolean compileClass(Class<?> clazz){
        //todo implement it
        return false;
    }

    /**
     * @see Compiler#compileClasses(String)
     */
    public static boolean compileClasses(String string){
        //todo implement it
        return false;
    }

    /**
     * @see Compiler#command(Object)
     */
    public static Object command(Object any) {
        return any;
    }

    /**
     * @see Compiler#enable()
     */
    public static void enable(){}

    /**
     * @see Compiler#disable()
     */
    public static void disable(){}
}
