/* class Dialog
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

package charva.awt;


/**
 * The Dialog class is similar to a Frame but includes a blank area
 * around the border frame. This makes the dialog more clearly visible when
 * it is displayed on top of a cluttered screen.
 */
public class Dialog
    extends Window
{
    /** Creates a modal dialog without a title and with a Frame owner.
     */
    public Dialog(Frame owner_) {
	this(owner_, "");
    }

    public Dialog(Frame owner_, String title_) {
	super(owner_);
	_title = title_;
	_insets = new Insets(2,2,2,2);
    }

    public Dialog(Dialog owner_) {
	this(owner_, "");
    }

    public Dialog(Dialog owner_, String title_) {
	super(owner_);
	_title = title_;
	_insets = new Insets(2,2,2,2);
    }

    public void setTitle(String title_) {
	_title = title_;
    }

    /** This method does nothing because dialogs are
     * ALWAYS modal in the CHARVA package.
     */
    public void setModal(boolean modal_) {}
    public boolean isModal() { return true; }

    public void draw(Toolkit toolkit) {

	/* Draw the enclosing frame
	 */
	int colorpair = getCursesColor();
	_term.blankBox(_origin, _size, colorpair);
	int boxwidth = _size.width - 2;
	int boxheight = _size.height - 2;
	_term.drawBox(_origin.addOffset(1,1), 
		new Dimension(boxwidth, boxheight),
		colorpair);

	/* Draw the title into the enclosing frame.
	 */
	if (_title != null && _title.length() != 0) {
	    _term.setCursor(_origin.addOffset(2,1));
	    _term.addChar(' ', 0, colorpair);
	    _term.addString(_title, 0, colorpair);
	    _term.addChar(' ', 0, colorpair);
	}

	/* Draw all the contained components
	 */
	super.draw(toolkit);
    }

    public void debug(int level_) {
	System.err.println("Dialog origin=" + _origin + 
	    " size=" + _size );
    }

    private String _title = null;
}
