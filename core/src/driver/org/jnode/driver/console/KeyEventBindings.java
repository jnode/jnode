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
 
package org.jnode.driver.console;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Set;

import org.jnode.driver.input.KeyboardEvent;

/**
 * This class implements a "soft" mapping from KeyEvents to actions to be performed
 * by (for example) the KeyboardReader.
 * 
 * @author crawley@jnode.org
 */
public abstract class KeyEventBindings<T extends Enum<?>> {
    
    private final T defaultCharAction;
    private final T defaultVKAction;
    private CodeMap<T> charMap;
    private CodeMap<T> vkMap;
    
    /**
     * Create empty bindings.
     * @param defaultCharAction the default action for a character-valued event
     * @param defaultVKAction the default action for a VK-valued event.
     */
    public KeyEventBindings(T defaultCharAction, T defaultVKAction) {
        this.defaultCharAction = defaultCharAction;
        this.defaultVKAction = defaultVKAction;
        this.charMap = new CodeMap<T>(defaultCharAction);
        this.vkMap = new CodeMap<T>(defaultVKAction);
    }
    
    /**
     * Create a copy of an existing KeyEventBindings object.
     * @param bindings the bindings to be copied.
     */
    public KeyEventBindings(KeyEventBindings<T> bindings) {
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
    public T getKeyboardEventAction(KeyboardEvent event) {
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
    public T getCharAction(char ch) {
        return charMap.get(ch);
    }

    /**
     * Lookup the action for a given virtual key code and modifier set
     * 
     * @param vk the virtual key code
     * @param modifiers the modifier set
     * @return the corresponding action.
     */
    public T getVKAction(int vk, int modifiers) {
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
    
    public T getVKAction(VirtualKey vk) {
        return vkMap.get(vk.value);
    }

    /**
     * Set the action for a given character
     * 
     * @param ch the character
     * @param action the action
     */
    public void setCharAction(char ch, T action) {
        charMap.put(ch, action);
    }

    /**
     * Unset the action for a given character.
     * This is equivalent to setting the action to the default action.
     * 
     * @param ch the character
     */
    public void unsetCharAction(char ch) {
        charMap.put(ch, defaultCharAction);
    }

    /**
     * Set the action for a given virtual key code and modifier set
     * 
     * @param vk the virtual key code
     * @param modifiers the modifier set
     * @param action the action
     */
    public void setVKAction(int vk, int modifiers, T action) {
        checkVK(vk, modifiers);
        vkMap.put(vk | (modifiers << 16), action);
    }

    /**
     * Unset the action for a given virtual key code and modifier set.
     * This is equivalent to setting the action to the default action.
     * 
     * @param vk the virtual key code
     * @param modifiers the modifier set
     */
    public void unsetVKAction(int vk, int modifiers) {
        setVKAction(vk, modifiers, defaultVKAction);
    }

    /**
     * Set the action for a given VirtualKey
     * 
     * @param vk the VirtualKey
     * @param action the action
     */
    public void setVKAction(VirtualKey vk, T action) {
        setVKAction(vk.getVKCode(), vk.getModifiers(), action);
    }

    /**
     * Unset the action for a given VirtualKey.
     * This is equivalent to setting the action to the default action.
     * 
     * @param vk the VirtualKey
     */
    public void unsetVKAction(VirtualKey vk) {
        setVKAction(vk, defaultVKAction);
    }

    /**
     * Set the action for a given range of characters
     * 
     * @param chLo the first character in the range
     * @param chHi the last character in the range
     * @param action the action
     */
    public void setCharAction(char chLo, char chHi, T action) {
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
    public void setVKAction(int vkLo, int vkHi, int modifiers, T action) {
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
     * Get all characters that are currently bound to an action.
     * @return the bound characters.
     */
    public char[] getBoundChars() {
        Set<Integer> charSet = charMap.getKeys();
        char[] res = new char[charSet.size()];
        int i = 0;
        for (int ch : charSet) {
            res[i++] = (char) ch;
        }
        return res;
    }

    /**
     * Get all virtual keys that are currently bound to an action.
     * @return the bound virtual keys.
     */
    public VirtualKey[] getBoundVKs() {
        Set<Integer> vkSet = vkMap.getKeys();
        VirtualKey[] res = new VirtualKey[vkSet.size()];
        int i = 0;
        for (int vk : vkSet) {
            res[i++] = new VirtualKey(vk);
        }
        return res;
    }

    /**
     * This class implements a sparse representation of an int to int
     * mapping using a HashMap and a default value.
     */
    private static class CodeMap<T extends Enum<?>> {
        private final HashMap<Integer, T> map;
        private final T defaultValue;

        public CodeMap(T defaultValue) {
            this.defaultValue = defaultValue;
            this.map = new HashMap<Integer, T>();
        }

        public CodeMap(CodeMap<T> codeMap) {
            this.defaultValue = codeMap.defaultValue;
            this.map = new HashMap<Integer, T>(codeMap.map);
        }

        public T get(int key) {
            T value = this.map.get(key);
            if (value != null) {
                return value;
            } else {
                return this.defaultValue;
            }
        }

        public void put(int key, T value) {
            if (value == this.defaultValue) {
                this.map.remove(key);
            } else {
                this.map.put(key, value);
            }
        }

        public void put(int keyLo, int keyHi, T value) {
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
        
        public Set<Integer> getKeys() {
            return map.keySet();
        }
    }
}
