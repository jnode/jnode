/* class JTextComponent
 *
 * Copyright (C) 2001  R M Pitman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package charvax.swing.text;

import charvax.swing.JComponent;

/**
 * JTextComponent is a much-simplified version of the Swing
 * java.swing.text.JTextComponent class, and is the base class 
 * for JTextArea.
 */
public abstract class JTextComponent
    extends JComponent
{
    public JTextComponent()
    {
    }

    public String getDocument() 
    { 
	return _document.toString(); 
    }

    public void setDocument(String document_) 
    { 
	_document = new StringBuffer(document_); 
    }

    public synchronized String getText() {
	return _document.toString();
    }

    public synchronized void setText(String text_)
    {
	_document = new StringBuffer(text_);
    }

    public int getCaretPosition()
    {
	return _caretPosition;
    }

    public void setCaretPosition(int caret_)
    {
	_caretPosition = caret_;
    }

    /** Returns the boolean flag indicating whether this TextComponent
     * is editable or not.
     */
    public boolean isEditable() { return _editable; }

    /**
     * Sets the boolean that indicates whether this TextComponent should be
     * editable or not.
     */
    public void setEditable(boolean editable_)
    {
	_editable = editable_;
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** Index (from the start of the string) where next character will 
     * be inserted.
     */
    protected int _caretPosition;

    protected StringBuffer _document;

    protected boolean _editable = true;
}
