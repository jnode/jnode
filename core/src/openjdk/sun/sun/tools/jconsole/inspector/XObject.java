/*
 * Copyright 2004-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package sun.tools.jconsole.inspector;

// java import
import javax.swing.*;

//

// java import
import java.io.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.net.*;
//


/**
 * This provides a wrapper to the Object class to allow it to be
 displayed/manipulated as a GUI object.
*/
@SuppressWarnings("serial")
public class XObject extends JLabel {
    private Object object;
    private static boolean useHashCodeRepresentation = true;
    public final static XObject NULL_OBJECT = new XObject("null");
    public XObject (Object object, Icon icon) {
        this(object);
        setIcon(icon);
    }

    public XObject (Object object) {
        setObject(object);
        setHorizontalAlignment(SwingConstants.LEFT);
    }

    public boolean equals(Object o) {
        try {
            if (o instanceof XObject) {
                return object.equals(((XObject)o).getObject());
            }
        }
        catch (Throwable t) {
            System.out.println("Error comparing XObjects"+
                               t.getMessage());
        }
        return false;
    }


    public Object getObject() {
        return object;
    }

    //if true the the object.hashcode is added to the label
    public static void
        useHashCodeRepresentation(boolean useHashCodeRepresentation) {
        XObject.useHashCodeRepresentation = useHashCodeRepresentation;
    }

    public static boolean hashCodeRepresentation() {
        return useHashCodeRepresentation;
    }

    public void setObject(Object object) {
        this.object = object;
        // if the object is not  a swing component,
        // use default icon
        try {
            String text = null;
            if (object instanceof JLabel) {
                setIcon(((JLabel)object).getIcon());
                if (getText() != null) {
                    text = ((JLabel)object).getText();

                }
            }
            else if (object instanceof JButton) {
                setIcon(((JButton)object).getIcon());
                if (getText() != null) {
                    text = ((JButton)object).getText();
                }
            }
            else if (getText() != null) {
                text = object.toString();
                setIcon(IconManager.DEFAULT_XOBJECT);
            }
            if (text != null) {
                if (useHashCodeRepresentation && (this != NULL_OBJECT)) {
                    text = text + "     ("+object.hashCode()+")";
                }
                setText(text);
            }
        }
        catch (Exception e) {
             System.out.println("Error setting XObject object :"+
                                e.getMessage());
        }
    }
}
