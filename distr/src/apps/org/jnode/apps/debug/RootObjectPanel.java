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
 
package org.jnode.apps.debug;

import java.util.Collection;
import java.util.Vector;
import javax.naming.NameNotFoundException;
import org.jnode.naming.InitialNaming;

/**
 * @author blind
 */
public abstract class RootObjectPanel extends ListPanel {
    /**
     * @param cols
     * @param rows
     */
    public RootObjectPanel(int cols, int rows) {
        super(cols, rows);
    }

    public void keyEntered(char c) {
        if (c == '+') {
            //get the currently selected element
            int selectedIndex = jlist.getSelectedIndex();
            if ((selectedIndex >= 0) && (selectedIndex < list.size())) {    //need to this check because
                //something is screwed: sometimes strange values are returned by getSelectedIndex()
                Object o = list.elementAt(jlist.getSelectedIndex());
                Object res = ((ListElement) o).getValue();
                if (res instanceof Collection) {
                    fill((Collection<?>) res);
                }
                //if(res instanceof Naming)
                //  fill(); //TODO: ...

                //if res is an array...
            }
        }
    }

    protected void fill() {
        final Vector<ListElement> list = new Vector<ListElement>();
        try {
            for (Class<?> key : InitialNaming.nameSet()) {
                Object namedObject = InitialNaming.lookup(key);
                list.addElement(new ListElement(namedObject, key.toString()));
            }
        } catch (NameNotFoundException nnfe) {
            nnfe.printStackTrace();
        }
        setList(list);
    }

    protected void fill(Collection<?> coll) {
        final Vector<ListElement> list = new Vector<ListElement>();
        for (Object o : coll) {
            final ListElement element = new ListElement(o, getElementLabel(o));
            list.addElement(element);
        }
        setList(list);
    }
}
