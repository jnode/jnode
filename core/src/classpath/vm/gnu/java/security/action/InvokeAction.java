/*
 * $Id$
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
 
package gnu.java.security.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;


/**
 * Utility class for invoking a method in a privileged action.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvokeAction implements PrivilegedExceptionAction<Object> {

    private final Method method;
    private final Object object;
    private final Object[] args;
    
    /**
     * Initialize this instance.
     * @param method
     * @param object
     * @param args
     */
    public InvokeAction(Method method, Object object, Object[] args) {
        this.method = method;
        this.object = object;
        this.args = args;
    }
    
    /**
     * @see java.security.PrivilegedExceptionAction#run()
     */
    public Object run() throws IllegalAccessException, InvocationTargetException {
        return method.invoke(object, args);
    }
}
