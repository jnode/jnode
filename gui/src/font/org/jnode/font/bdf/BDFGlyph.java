/*
 Copyright (C) 2002-2006 Stephane Meslin-Weber <steph@tangency.co.uk>
 All rights reserved.
 
 This file is part of Odonata.
 
 Odonata is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Odonata is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Odonata; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
*/
package org.jnode.font.bdf;

import java.awt.Dimension;
import java.awt.Rectangle;


/**
 * Represents a single font Glyph.
 * @author Stephane Meslin-Weber
 */
public class BDFGlyph /* extends Glyph */ {
	public String name;
	public Rectangle bbx = new Rectangle();
	public int[] data;
    public StringBuffer rawData;
	private BDFFont font;
    
    /**
     * Decodes a BDF font BITMAP/ENDCHAR sequence of packed bits.
     * @param depth bits per pixel from parent font
     * @param data bit data encoded in text bytes
     * @return raw bit values
     */
    public int[] decode(int depth, StringBuffer data) {
        int[] result = new int[(data.length()/2)*(8 / depth)];

        for (int i = 0, offset=0; i < data.length(); i+=2,offset+=(8 / depth)) {
            String cut = data.substring(i, i+2);
            int value = Integer.parseInt(cut,16);

            int bits[] = null;
            
            //
            // Bit long, and not optimised...
            //
            switch(depth) {
            case 1:
                bits = new int[] {
                    (value & 0x80) >> 7,
                    (value & 0x40) >> 6,
                    (value & 0x20) >> 5,
                    (value & 0x10) >> 4,
                    (value & 0x08) >> 3,
                    (value & 0x04) >> 2,
                    (value & 0x02) >> 1,
                    (value & 0x01),                        
                };
                break;
            case 2:
                bits = new int[] {
                    (value & 0xC0) >> 6,
                    (value & 0x30) >> 4,
                    (value & 0x0C) >> 2,
                    (value & 0x03),
                };
                break;
            case 4:
                bits = new int[] {
                    (value & 0xF0) >> 4,
                    (value & 0x0F),
                };
                break;
            case 8:
                bits = new int[] {
                    value & 0xFF,
                };
                break;
            }           

            for(int k=0;k<bits.length;k++)
                result[offset+k] = bits[k];
        }

        return result;
    }

	public Rectangle getBbx() {
		return bbx;
	}
    
    void init(BDFFont font) {
        this.font = font;
        this.data = decode(getFont().getDepth(), rawData);
        rawData = null;
    }
    
	public void setBbx(Rectangle bbx) {
		this.bbx = bbx;
	}

	public int[] getData() {
		return data;
	}
    
    public void setRawData(StringBuffer rawData) {
        this.rawData = rawData;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BDFFont getFont() {
		return font;
	}

	public BDFGlyph(String name) {
		this.name = name;
	}
	
	public void setSWidth(int swx0, int swy0) {
//		System.err.println("SWidth: "+swx0+","+swy0);
	}

	private Dimension dsize = new Dimension();
	public void setDWidth(int dwx0, int dwy0) {
		dsize.setSize(dwx0,dwy0);
	}
    
    public Dimension getDWidth() {
        return dsize;
    }
	
	public void setBBX(int x, int y, int width, int height) {
		bbx.setBounds(x, y, width, height);
	}
    
    public String toString() {
        return "BDFGlyph[name="+name+", bbx="+bbx+", dsize="+dsize+"]";
    }
}