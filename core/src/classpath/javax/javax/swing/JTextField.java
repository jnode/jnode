/* JTextField.java -- 
   Copyright (C) 2002 Free Software Foundation, Inc.

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


package javax.swing;

import java.awt.event.ActionListener;
import java.util.Vector;
import javax.accessibility.AccessibleStateSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public class JTextField extends JEditorPane
{

	/**
	 * AccessibleJTextField
	 */
	protected class AccessibleJTextField extends AccessibleJTextComponent {

		//-------------------------------------------------------------
		// Initialization ---------------------------------------------
		//-------------------------------------------------------------

		/**
		 * Constructor AccessibleJTextField
		 * @param component TODO
		 */
		protected AccessibleJTextField(JTextField component) {
			super(component);
			// TODO
		} // AccessibleJTextField()


		//-------------------------------------------------------------
		// Methods ----------------------------------------------------
		//-------------------------------------------------------------

		/**
		 * getAccessibleStateSet
		 * @returns AccessibleStateSet
		 */
		public AccessibleStateSet getAccessibleStateSet() {
			return null; // TODO
		} // getAccessibleStateSet()


	} // AccessibleJTextField


    Vector actions = new Vector();

  public JTextField()
  {
  }

    public JTextField(int a)
    {
    }

    public void addActionListener(ActionListener l)
    {
	actions.addElement(l);
    }

    public void removeActionListener(ActionListener l)
    {
	actions.removeElement(l);
    }

    public void selectAll()
    {
    }
}
