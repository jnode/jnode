/* BasicComboBoxEditor.java --
   Copyright (C) 2004  Free Software Foundation, Inc.

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


package javax.swing.plaf.basic;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * This is a component that is responsible for displaying/editting  selected
 * item in comboBox. By default, the  JTextField is returned as
 * BasicComboBoxEditor.
 *
 * @author Olga Rodimina
 */
public class BasicComboBoxEditor extends Object implements ComboBoxEditor,
                                                           FocusListener
{
  protected JTextField editor;

  /**
   * Creates a new BasicComboBoxEditor object.
   */
  public BasicComboBoxEditor()
  {
    editor = new JTextField();
    editor.setBorder(new EmptyBorder(1, 1, 1, 1));
  }

  /**
   * This method returns textfield that will be used by the combo  box to
   * display/edit currently selected item in the combo box.
   *
   * @return textfield that will be used by the combo box to  display/edit
   *         currently selected item
   */
  public Component getEditorComponent()
  {
    return editor;
  }

  /**
   * Sets item that should be editted when any editting operation is performed
   * by the user. The value is always equal to the currently selected value
   * in the combo box. Thus whenever a different value is selected from the
   * combo box list then this method should be  called to change editting
   * item to the new selected item.
   *
   * @param selectedItem item that is currently selected in the combo box
   */
  public void setItem(Object item)
  {
    editor.setText(item.toString());
  }

  /**
   * This method returns item that is currently editable.
   *
   * @return item in the combo box that is currently editable
   */
  public Object getItem()
  {
    return editor.getText();
  }

  public void selectAll()
  {
    editor.selectAll();
  }

  /**
   * This method is called when textfield gains focus. This will enable
   * editing of the selected item.
   *
   * @param e the FocusEvent describing change in focus.
   */
  public void focusGained(FocusEvent e)
  {
    // FIXME: Need to implement
  }

  /**
   * This method is called when textfield loses focus. If during this time any
   * editting operation was performed by the user, then it will be cancelled
   * and selected item will not be changed.
   *
   * @param e the FocusEvent describing change in focus
   */
  public void focusLost(FocusEvent e)
  {
    // FIXME: Need to implement
  }

  /**
   * This method adds actionListener to the editor. If the user will edit
   * currently selected item in the textfield and pressEnter, then action
   * will be performed. The actionPerformed of this ActionListener should
   * change the selected item of the comboBox to the newly editted  selected
   * item.
   *
   * @param l the ActionListener responsible for changing selected item of the
   *        combo box when it is editted by the user.
   */
  public void addActionListener(ActionListener l)
  {
    // FIXME: Need to implement
  }

  /**
   * This method removes actionListener from the textfield.
   *
   * @param l the ActionListener to remove from the textfield.
   */
  public void removeActionListener(ActionListener l)
  {
    // FIXME: Need to implement
  }

  public static class UIResource extends BasicComboBoxEditor
    implements javax.swing.plaf.UIResource
  {
    /**
     * Creates a new UIResource object.
     */
    public UIResource()
    {
    }
  }
}
