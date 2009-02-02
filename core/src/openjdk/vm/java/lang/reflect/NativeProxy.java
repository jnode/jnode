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
 
package java.lang.reflect;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeProxy {
    /**
     * @see java.lang.reflect.Proxy#defineClass0(ClassLoader, String, byte[], int, int)
     */
    private static Class defineClass0(ClassLoader loader, String name,
					     byte[] b, int off, int len){
        if(loader == null)
            loader = Thread.currentThread().getContextClassLoader();
        return loader.getVmClassLoader().defineClass(name, b, off, len, Object.class.getProtectionDomain()).asClass();
    }
}
