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
 
package org.jnode.apps.debug;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * @author blind
 */
public abstract class PropertiesPanel extends ListPanel {
    boolean showSuperFields = false;
    private Object rootObject;    //the object whose fields and methods are shown
    private boolean includeSuper;    //include superclass's fields and methods

    public PropertiesPanel(int cols, int rows) {
        super(cols, rows);
        includeSuper = false;
    }

    public void fillPanel(Object rootObject) {
        this.rootObject = rootObject;

        final Vector list = new Vector();

        Class parent = rootObject.getClass();
        //add the fields
        while (parent != null) {
            Field[] fields = parent.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                list.addElement(new ListElement(new ObjectFieldPair(rootObject, fields[i]),
                    //" "+fields[i].getType()+
                    " " + fields[i].getName()));
                fields[i].setAccessible(true);
            }
            if (includeSuper)
                parent = parent.getSuperclass();
            else
                break;
        }

        //add the methods
        parent = rootObject.getClass();
        while (parent != null) {
            Method[] methods = parent.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                String label = methods[i].getReturnType().getName() + " " + methods[i].getName() + "(";
                Object[] params = methods[i].getParameterTypes();
                for (int j = 0; j < params.length; j++) {
                    label += params[j].toString();
                    if (j != params.length - 1)
                        label += ',';
                }
                label += ")";
                list.addElement(new ListElement(new ObjectMethodPair(rootObject, methods[i]),
                    label));
                methods[i].setAccessible(true);
            }

            if (includeSuper)
                parent = parent.getSuperclass();
            else
                break;
        }

        setList(list);
    }

    /* (non-Javadoc)
      * @see org.jnode.apps.debug.ListPanel#elementSelected(java.lang.Object)
      */
    public abstract void elementSelected(Object o);

    /* (non-Javadoc)
      * @see org.jnode.apps.debug.ListPanel#keyEntered(charva.awt.event.KeyEvent)
      */
    public void keyEntered(char c) {
        try {
            if (c == 'p') {
                includeSuper = !includeSuper;
                fillPanel(rootObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
