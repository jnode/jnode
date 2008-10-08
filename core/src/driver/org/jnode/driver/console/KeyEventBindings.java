/*
 * $Id: CommandLine.java 4611 2008-10-07 12:55:32Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
package org.jnode.driver.console;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import org.jnode.driver.input.KeyboardEvent;

/**
 * This class implements a "soft" mapping from KeyEvents to actions to be performed
 * by (for example) the KeyboardReader.
 * 
 * @author crawley@jnode.org
 */
public class KeyEventBindings {
    
    private final int defaultCharAction;
    private final int defaultVKAction;
    private CodeMap charMap;
    private CodeMap vkMap;
    
    /**
     * Create empty bindings.
     * @param defaultCharAction the default action for a character-valued event
     * @param defaultVKAction the default action for a VK-valued event.
     */
    public KeyEventBindings(int defaultCharAction, int defaultVKAction) {
        this.defaultCharAction = defaultCharAction;
        this.defaultVKAction = defaultVKAction;
        this.charMap = new CodeMap(defaultCharAction);
        this.vkMap = new CodeMap(defaultVKAction);
    }
    
    /**
     * Create a copy of an existing KeyEventBindings object.
     * @param bindings the bindings to be copied.
     */
    public KeyEventBindings(KeyEventBindings bindings) {
        this.defaultCharAction = bindings.defaultCharAction;
        this.defaultVKAction = bindings.defaultVKAction;
        this.charMap = new CodeMap(bindings.charMap);
        this.vkMap = new CodeMap(bindings.vkMap);
    }

    /**
     * Reset the bindings to the state created by the constructor {@link #KeyEventBindings()}.
     */
    public void clear() {
        charMap = new CodeMap(this.defaultCharAction);
        vkMap = new CodeMap(this.defaultVKAction);
    }
    
    /**
     * Lookup the action for a given KeyboardEvent.
     * 
     * @param ch the character
     * @return the corresponding action.
     */
    public int getKeyboardEventAction(KeyboardEvent event) {
        char ch = event.getKeyChar();
        if (ch == KeyEvent.CHAR_UNDEFINED) {
            return getVKAction(event.getKeyCode(), event.getModifiers());
        } else {
            return getCharAction(ch);
        }
    }

    /**
     * Lookup the action for a given character
     * 
     * @param ch the character
     * @return the corresponding action.
     */
    public int getCharAction(char ch) {
        return charMap.get(ch);
    }

    /**
     * Lookup the action for a given virtual key code and modifier set
     * 
     * @param vk the virtual key code
     * @param modifiers the modifier set
     * @return the corresponding action.
     */
    public int getVKAction(int vk, int modifiers) {
        checkVK(vk, modifiers);
        return vkMap.get(vk | (modifiers << 16));
    }

    private void checkVK(int vk, int modifiers) {
        if (vk < 0 || vk > 65535) {
            throw new IllegalArgumentException("virtual key code range error");
        }
        if (modifiers < 0 || modifiers > 65535) {
            throw new IllegalArgumentException("modifiers range error");
        }
    }

    /**
     * Set the action for a given character
     * 
     * @param ch the character
     * @param action the action
     */
    public void setCharAction(char ch, int action) {
        charMap.put(ch, action);
    }

    /**
     * Set the action for a given virtual key code and no modifiers
     * 
     * @param vk the virtual key code
     * @param action the action
     */
    public void setVKAction(int vk, int action) {
        checkVK(vk, 0);
        vkMap.put(vk, action);
    }

    /**
     * Set the action for a given virtual key code and modifier set
     * 
     * @param vk the virtual key code
     * @param modifiers the modifier set
     * @param action the action
     */
    public void setVKAction(int vk, int modifiers, int action) {
        checkVK(vk, modifiers);
        vkMap.put(vk | (modifiers << 16), action);
    }

    /**
     * Set the action for a given range of characters
     * 
     * @param chLo the first character in the range
     * @param chHi the last character in the range
     * @param action the action
     */
    public void setCharAction(char chLo, char chHi, int action) {
        charMap.put(chLo, chHi, action);
    }

    /**
     * Set the action for a given range of virtual key codes with a given modifier set.
     * 
     * @param vkLo the first virtual key code in the range
     * @param vkHi the last virtual key code in the range
     * @param modifiers the modifier set
     * @param action the action
     */
    public void setVKAction(int vkLo, int vkHi, int modifiers, int action) {
        checkVK(vkLo, modifiers);
        checkVK(vkHi, modifiers);
        if (modifiers == 0) {
            vkMap.put(vkLo, vkHi, action);
        } else {
            if (vkLo > vkHi) {
                throw new IllegalArgumentException("vkLo > vkHi");
            }
            for (int vk = vkLo; vk <= vkHi; vk++) {
                vkMap.put(vk | (modifiers << 16), action);
            }
        }
    }

    /**
     * This class implements a sparse representation of an int to int
     * mapping using a HashMap and a default value.
     */
    private static class CodeMap {
        private final HashMap<Integer, Integer> map;
        private final int defaultValue;

        public CodeMap(int defaultValue) {
            this.defaultValue = defaultValue;
            this.map = new HashMap<Integer, Integer>();
        }

        public CodeMap(CodeMap codeMap) {
            this.defaultValue = codeMap.defaultValue;
            this.map = new HashMap<Integer, Integer>(codeMap.map);
        }

        public int get(int key) {
            Integer value = this.map.get(key);
            if (value != null) {
                return value;
            } else {
                return this.defaultValue;
            }
        }

        public void put(int key, int value) {
            if (value == this.defaultValue) {
                this.map.remove(key);
            } else {
                this.map.put(key, value);
            }
        }

        public void put(int keyLo, int keyHi, int value) {
            if (keyLo > keyHi) {
                throw new IllegalArgumentException("keyLo > keyHi");
            }
            if (value == this.defaultValue) {
                for (int key = keyLo; key <= keyHi; key++) {
                    this.map.remove(key);
                }
            } else {
                for (int key = keyLo; key <= keyHi; key++) {
                    this.map.put(key, value);
                }
            }
        }
    }
}
