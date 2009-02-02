/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.textscreen.x86;

public class PcTextScreenUtils {
    private static final int COLOR_MASK = 0xFF00;
    private static final int COLOR_SHIFT = 8;

    private static final int CHARACTER_MASK = 0x00FF;

    public static final int encodeColor(int color) {
        return (color << COLOR_SHIFT) & COLOR_MASK;
    }

    public static final int decodeColor(char characterAndColor) {
        return (characterAndColor & COLOR_MASK) >> COLOR_SHIFT;
    }
    
    public static final int encodeCharacter(int character) {
        int ch = (character & CHARACTER_MASK);
        ch = ((ch == 0) ? ' ' : ch);
        return ch;
    }
        
    public static final int decodeCharacter(int characterAndColor) {
        return (characterAndColor & CHARACTER_MASK);
    }
        
    public static final char encodeCharacterAndColor(char character, int color) {
        return (char) (PcTextScreenUtils.encodeCharacter(character) | PcTextScreenUtils.encodeColor(color));
    }
    
    /**
     * Exchange the background and the foreground colors 
     * @param characterAndColor
     * @return
     */
    public static final char exchangeColors(char characterAndColor) {
        int color = PcTextScreenUtils.decodeColor(characterAndColor);
        color = ((color & 0xF0) >> 4) | ((color & 0x0F) << 4);
        //int color = 0x70;
        char character = (char) decodeCharacter(characterAndColor);
        
        return encodeCharacterAndColor(character, color);
    }
    
    private PcTextScreenUtils() {
    }
}
