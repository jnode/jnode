/* class JPasswordField
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

package charvax.swing;

import charva.awt.Container;
import charva.awt.Insets;
import charva.awt.Point;
import charva.awt.Toolkit;

/**
 * JPassword allows the editing of a single line of text; it indicates
 * that something was typed, but does not display the characters that 
 * were typed.
 * The JPasswordField class, being a subclass of JComponent, has a setBorder()
 * method which allows an optional Border to be set.
 */
public class JPasswordField
    extends JTextField
{
    public JPasswordField() {
	super("");
    }

    /** Use this constructor when you want to initialize the value.
     */
    public JPasswordField(String text_) {
	super(text_);
    }

    /** 
     * Use this constructor when you want to leave the text field empty
     * but set its length.
     */
    public JPasswordField(int length_) {
	super("", length_);
    }

    /**
     * Use this constructor when you want to set both the initial value and the
     * length.
     */
    public JPasswordField(String text_, int length_) {
	super(text_, length_);
    }

    /** Set the echo character for this password field.
     */
    public void setEchoChar(char echochar_) {
	_echoChar = echochar_;
    }

    /** Get the echo character for this text field.
     */
    public char getEchoChar() { return _echoChar; }

    /** Get the flag which indicates whether the echo character
     * has been set.
     */
    public boolean echoCharIsSet() { return (_echoChar != 0); }

    /** @deprecated Replaced by getpassword()
     */
    public String getText() { return super.getText(); }

    /** Returns the password value as an array of chars.
     */
    public char[] getPassword() { return super.getText().toCharArray(); }

    /**
     * Called by this JPasswordField's parent container.
     * @param toolkit
     */
    public void draw(Toolkit toolkit) {

	/* Get the absolute origin of this component.
	 */
	Point origin = getLocationOnScreen();

	/* Blank out the area of this component, but only if this
	 * component's color-pair is different than that of the
	 * parent container.
	 */
	int colorpair = getCursesColor();
	Container parent = getParent();
	if (parent != null && colorpair != parent.getCursesColor())
	    toolkit.blankBox(origin, this.getSize(), colorpair);

	// Draw the border if there is one.
	if (_border != null) {
	    _border.paintBorder(this, colorpair,
		    origin.x, origin.y,
		    this.getWidth(), this.getHeight(), toolkit);
	}

	/* Now draw the JPasswordField itself.
	 */
	Insets insets = super.getInsets();
	origin.translate(insets.left, insets.top);

	/* If the field is enabled, it is drawn with the UNDERLINE
	 * attribute.  If it is disabled, it is drawn without the
	 * UNDERLINE attribute.
	 */
	int attrib = 0;
	if (super._enabled)
	    attrib |= Toolkit.A_UNDERLINE;

	toolkit.setCursor(origin);
	toolkit.addString(_padding, attrib, colorpair);
	toolkit.setCursor(origin);

	// Get the displayable portion of the string
	int end;
	if (super._document.length() > (_offset + _columns))
	    end = _offset + _columns;
	else
	    end = super._document.length();

	/* If the echo character is set, display echo characters instead
	 * of the actual string.
	 */
	StringBuffer displaybuf = new StringBuffer();
	if (_echoChar != 0) {
	    for (int i=0; i<super._document.length(); i++)
		displaybuf.append(_echoChar);
	}
	else  {
	    for (int i=0; i<super._document.length(); i++)
		displaybuf.append(' ');
	}

	toolkit.addString(
		displaybuf.substring(_offset, end).toString(), 
		attrib, colorpair);
	toolkit.setCursor(origin.addOffset(super._caretPosition - _offset, 0));
    }

    /** Returns a String representation of this component.
     */
    public String toString() {
	return "JPasswordField location=" + getLocation() + 
	    " text=\"" + super._document + "\"" +
	    " actionCommand=\"" + getActionCommand() + "\"";
    }

    public void debug(int level_) {
	for (int i=0; i<level_; i++)
	    System.err.print("    ");
	System.err.println("JPasswordField origin=" + _origin + 
	    " size=" + getSize() + " text=" + super._document);
    }

    //====================================================================
    // INSTANCE VARIABLES

    /** The character that will be set by setEchoChar, and echoed
     * thereafter. Setting this value to 0 indicates that there is no
     * echochar set.
     */
    private char _echoChar = '*';
}
