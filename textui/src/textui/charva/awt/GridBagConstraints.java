/* class GridBagConstraints
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
 * This class defines constraints used for laying out components in the
 * GridBagLayout layout manager.
 */
public class GridBagConstraints
{
    /**
     * Creates a GridBagConstraints object with all of its fields set to
     * their default value.
     */
    public GridBagConstraints()
    {
    }

    public GridBagConstraints(int gridx_, int gridy_,
	    int gridwidth_, int gridheight_,
	    double weightx_, double weighty_,
	    int anchor_, int fill_, Insets insets_,
	    int ipadx_, int ipady_)
    {
	gridx = gridx_;
	gridy = gridy_;
	gridwidth = gridwidth_;
	gridheight = gridheight_;
	weightx = weightx_;
	weighty = weighty_;
	anchor = anchor_;
	fill = fill_;
	insets = insets_;
	ipadx = ipadx_;
	ipady = ipady_;
    }

    public String toString() {
	return "gridx=" + gridx + " gridy=" + gridy +
	    " gridwidth=" + gridwidth + " gridheight=" + gridheight;
    }

    //====================================================================
    // INSTANCE VARIABLES

    public int gridx = 0;
    public int gridy = 0;
    public int gridwidth = 1;
    public int gridheight = 1;
    public double weightx = 0.0;
    public double weighty = 0.0;
    public int anchor = CENTER;
    public int fill = NONE;	// Not used
    public Insets insets = new Insets(0,0,0,0);
    public int ipadx = 0;	// Not used
    public int ipady = 0;	// Not used

    public static final int CENTER = 100;
    public static final int NORTH = 101;
    public static final int NORTHEAST = 102;
    public static final int EAST = 103;
    public static final int SOUTHEAST = 104;
    public static final int SOUTH = 105;
    public static final int SOUTHWEST = 106;
    public static final int WEST = 107;
    public static final int NORTHWEST = 108;

    public static final int NONE = 200;
    public static final int HORIZONTAL = 201;
    public static final int VERTICAL = 202;
    public static final int BOTH = 203;
}
