/* java.beans.PropertyEditorManager
   Copyright (C) 1998 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.beans;

import gnu.java.lang.ClassHelper;

/**
 ** PropertyEditorManager is used to find property editors
 ** for various types (not necessarily Beans).<P>
 **
 ** It first checks to see if the property editor is
 ** already registered; if it is, that property editor is
 ** used.  Next it takes the type's classname and appends
 ** "Editor" to it, and searches first in the class's
 ** package and then in the property editor search path.<P>
 **
 ** Default property editors are provided for:<P>
 ** <OL>
 ** <LI>boolean, byte, short, int, long, float, and double</LI>
 ** <LI>java.lang.String</LI>
 ** <LI>java.awt.Color</LI>
 ** <LI>java.awt.Font</LI>
 ** <OL>
 **
 ** <STRONG>Spec Suggestion:</STRONG> Perhaps an editor for
 ** Filename or something like it should be provided.  As well
 ** as char.
 **
 ** @author John Keiser
 ** @since JDK1.1
 ** @version 1.1.0, 29 Jul 1998
 **/

public class PropertyEditorManager {
	static java.util.Hashtable editors = new java.util.Hashtable();
	static String[] editorSearchPath = {"gnu.java.beans.editors","sun.beans.editors"};

	static {
		registerEditor(java.lang.Boolean.TYPE, gnu.java.beans.editors.NativeBooleanEditor.class);
		registerEditor(java.lang.Byte.TYPE,    gnu.java.beans.editors.NativeByteEditor.class);
		registerEditor(java.lang.Short.TYPE,   gnu.java.beans.editors.NativeShortEditor.class);
		registerEditor(java.lang.Integer.TYPE, gnu.java.beans.editors.NativeIntEditor.class);
		registerEditor(java.lang.Long.TYPE,    gnu.java.beans.editors.NativeLongEditor.class);
		registerEditor(java.lang.Float.TYPE,   gnu.java.beans.editors.NativeFloatEditor.class);
		registerEditor(java.lang.Double.TYPE,  gnu.java.beans.editors.NativeDoubleEditor.class);
		registerEditor(java.lang.String.class, gnu.java.beans.editors.StringEditor.class);
		registerEditor(java.awt.Color.class,   gnu.java.beans.editors.ColorEditor.class);
		registerEditor(java.awt.Font.class,    gnu.java.beans.editors.FontEditor.class);
	}

	/** Beats me why this class can be instantiated, but there
	 ** you have it.
	 **/
	public PropertyEditorManager() { }

	/** Register an editor for a class.  Replaces old editor
	 ** if there was one registered before.
	 ** @param editedClass the class that the property editor
	 **        will edit.
	 ** @param editorClass the PropertyEditor class.
	 **/
	public static void registerEditor(Class editedClass, Class editorClass) {
		editors.put(editedClass, editorClass);
	}

	/** Returns a new instance of the property editor for the
	 ** specified class.
	 ** @param editedClass the class that the property editor
	 **        will edit.
	 ** @return a PropertyEditor instance that can edit the
	 **         specified class.
	 **/
	public static PropertyEditor findEditor(Class editedClass) {
		try {

		Class found = (Class)editors.get(editedClass);
		if(found != null) {
			return (PropertyEditor)found.newInstance();
		}

		try {
			found = Class.forName(editedClass.getName()+"Editor");
			registerEditor(editedClass,found);
			return (PropertyEditor)found.newInstance();
		} catch(ClassNotFoundException E) {
		}

		String appendName = "." + ClassHelper.getTruncatedClassName(editedClass) + "Editor";
		synchronized(editorSearchPath) {
			for(int i=0;i<editorSearchPath.length;i++) {
				try {
					found = Class.forName(editorSearchPath[i] + appendName);
					registerEditor(editedClass,found);
					return (PropertyEditor)found.newInstance();
				} catch(ClassNotFoundException E) {
				}
			}
		}

		} catch(InstantiationException E) {
		} catch(IllegalAccessException E) {
		}
		return null;
	}

	/** Get the editor search path.
	 ** As a minor departure from the spec, the default value
	 ** for the editor search path is "gnu.java.beans.editors",
	 ** "sun.beans.editors".
	 ** @return the editor search path.
	 **/
	public static String[] getEditorSearchPath() {
		return editorSearchPath;
	}

	/** Set the editor search path.
	 ** @param editorSearchPath the new value for the editor
	 **        search path.
	 **/
	public static void setEditorSearchPath(String[] editorSearchPath) {
		synchronized(editorSearchPath) {
			PropertyEditorManager.editorSearchPath = editorSearchPath;
		}
	}
}
