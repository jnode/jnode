/* class Frame
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
 * A Frame is a top-level window with a title and a border.
 */
public class Frame
    extends Window
{
    public Frame() {
	this("");
    }

    public Frame(String title_) {
	super(null);
	_title = title_;
	_insets = new Insets(1,1,1,1);
    }

    public void setTitle(String title_) {
	_title = title_;
    }

    /** Return this Frame's title, or an empty string if the
     * frame does not have a tile. 
     */
    public String getTitle() {
	if (_title == null)
	    return null;
	else
	    return _title;
    }

    public void draw(Toolkit toolkit)
    {

	/* Draw the enclosing frame (but only if the insets are nonzero).
	 */
	int colorpair = super.getCursesColor();
	_term.blankBox(_origin, _size, colorpair);
	Insets insets = super.getInsets();
	if (insets.top != 0 && insets.bottom != 0)
	    _term.drawBox(_origin, _size, colorpair);

	/* Draw the title into the enclosing frame.
	 */
	if (_title.equals("") == false) {
	    _term.setCursor(_origin.addOffset(1,0));
	    _term.addChar(' ', 0, colorpair);
	    _term.addString(_title, 0, colorpair);
	    _term.addChar(' ', 0, colorpair);
	}

	/* Draw all the contained components
	 */
	super.draw(toolkit);
    }

    private String _title;
}
