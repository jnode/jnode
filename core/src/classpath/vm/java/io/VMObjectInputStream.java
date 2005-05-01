/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package java.io;

import java.lang.reflect.Constructor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VMObjectInputStream {

    /**
     * This native method is used to get access to the protected method of the
     * same name in SecurityManger.
     * 
     * @param sm
     *            SecurityManager instance which should be called.
     * @return The current class loader in the calling stack.
     */
    static ClassLoader currentClassLoader() {
        // TODO implement me
        return null;
    }
    
    static Object allocateObject (Class clazzClass, Class constr_clazz, Constructor constructor)
    throws InstantiationException {
        // TODO implement me
        return null;
    }
    
    static void callConstructor (Class clazz, Object obj) {
        // TODO implement me
    }
    
}
