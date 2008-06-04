/*
 * $Id$
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

package org.jnode.driver.input;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * A KeyboardInterpreter translate scancodes into KeyboardEvent's.
 *
 * @author epr
 * @author Martin Husted Hartvig
 */
public abstract class KeyboardInterpreter {
    // FIXME We are currently using the character zero (0x0000) to indicate that no
    // (Unicode) character corresponds to a keycode.  This means that we cannot represent
    // the NUL control character.  Instead we should really be using 0xffff which Unicode
    // defines to be a non-character.

    private int flags;

    protected int lastDeadVK = -1;

    protected final Keys keys;
    private final UnsupportedKeyException unsupportedKeyException = new UnsupportedKeyException();

    private boolean extendedMode;
    private int capsLock = 0;

    private final char enter = '\n';
    private final char divide = '/';

    public static final int XT_RELEASE = 0x80;
    public static final int XT_EXTENDED = 0xE0;

    public KeyboardInterpreter() {
        keys = new Keys();

        initKeys(keys);
    }

    /**
     * Interpret a given scancode into a keyevent.
     *
     * @param scancode
     */
    public final KeyboardEvent interpretScancode(int scancode) {
        final boolean extendedMode = this.extendedMode;

        if (scancode == XT_EXTENDED) {
            this.extendedMode = true;
            return null;
        } else {
            this.extendedMode = false;
        }

        final boolean released = ((scancode & XT_RELEASE) != 0);
        final long time = System.currentTimeMillis();

        scancode &= 0x7f;

        int vk = deriveKeyCode(scancode, extendedMode);

        // debug output to find new keycodes
//    System.out.println("[" + (extendedMode ? "E" : "N") + scancode + "," + vk + "] " /*+ KeyEvent.getKeyText(vk)*/);

        if (!extendedMode) {
            if ((flags & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
                vk = keys.getKey(scancode).getAltGrVirtuelKey();
            } else if ((flags & InputEvent.SHIFT_DOWN_MASK) != 0) {
                vk = keys.getKey(scancode).getUpperVirtuelKey();
            } else {
                vk = keys.getKey(scancode).getLowerVirtuelKey();
            }
        }


        adjustFlags(vk, released);

        // debug output to find new keycodes
//    System.out.println("[" + (extendedMode ? "E" : "N") + scancode + "," + vk + "] " /*+ KeyEvent.getKeyText(vk)*/);

        try {
            final char ch;
            ch = interpretExtendedScanCode(scancode, vk, released, extendedMode);
            return new KeyboardEvent(released ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED, time, flags, vk, ch);
        } catch (UnsupportedKeyException e) {
            final char ch;

            if ((flags & InputEvent.CTRL_DOWN_MASK) != 0) {
                ch = keys.getKey(scancode).getControlChar();
            } else if ((flags & InputEvent.SHIFT_DOWN_MASK) != 0) {
                ch = keys.getKey(scancode).getUpperChar();

                if (!extendedMode)
                    vk = keys.getKey(scancode).getUpperVirtuelKey();
            } else if ((flags & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
                ch = keys.getKey(scancode).getAltGrChar();

                if (!extendedMode)
                    vk = keys.getKey(scancode).getAltGrVirtuelKey();
            } else {
                ch = keys.getKey(scancode).getLowerChar();
            }
            return new KeyboardEvent(released ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED, time, flags, vk, ch);
        } catch (DeadKeyException e) {
            return null;
        }
    }

    private void adjustFlags(int vk, boolean released) {
        final int mask;
        switch (vk) {
            case KeyEvent.VK_ALT:
                mask = InputEvent.ALT_DOWN_MASK;
                break;
            case KeyEvent.VK_ALT_GRAPH:
                mask = InputEvent.ALT_GRAPH_DOWN_MASK;
                break;
            case KeyEvent.VK_CONTROL:
                mask = InputEvent.CTRL_DOWN_MASK;
                break;
            case KeyEvent.VK_SHIFT:
                mask = InputEvent.SHIFT_DOWN_MASK;
                break;
            case KeyEvent.VK_CAPS_LOCK:
                if (capsLock == 0 || capsLock == 3)
                    mask = InputEvent.SHIFT_DOWN_MASK;
                else
                    mask = 0;

                capsLock++;
                capsLock %= 4;

                break;
            default:
                mask = 0;
        }

        if (mask != 0) {
            if (released) {
                this.flags &= ~mask;
            } else {
                this.flags |= mask;
            }
        }
    }

    protected int deriveKeyCode(int scancode, boolean extended) {
        int vk = 0;

        if (extended) {
            switch (scancode) {
                case 82:
                    vk = KeyEvent.VK_INSERT;
                    break;
                case 71:
                    vk = KeyEvent.VK_HOME;
                    break;
                case 73:
                    vk = KeyEvent.VK_PAGE_UP;
                    break;
                case 83:
                    vk = KeyEvent.VK_DELETE;
                    break;
                case 79:
                    vk = KeyEvent.VK_END;
                    break;
                case 81:
                    vk = KeyEvent.VK_PAGE_DOWN;
                    break;
                case 72:
                    vk = KeyEvent.VK_UP;
                    break;
                case 75:
                    vk = KeyEvent.VK_LEFT;
                    break;
                case 80:
                    vk = KeyEvent.VK_DOWN;
                    break;
                case 77:
                    vk = KeyEvent.VK_RIGHT;
                    break;
                case 28:
                    vk = KeyEvent.VK_ENTER;
                    break;
                case 55:
                    vk = KeyEvent.VK_PRINTSCREEN;
                    break;
                case 56:
                    vk = KeyEvent.VK_ALT_GRAPH;
                    break;
                case 29:
                    vk = KeyEvent.VK_CONTROL;
                    break;
                case 93:
                    vk = KeyEvent.VK_PROPS;
                    break;
                case 53:
                    vk = KeyEvent.VK_DIVIDE;
                    break;
                default:
                    vk = 0;
            }
        }

        return vk;
    }


    /**
     * Method interpretExtendedScanCode this method sould be used to handle the dead keys and other special keys
     *
     * @param scancode an int
     * @param vk       an int
     * @param released a  boolean
     * @return the char to use or throws an Exception
     * @throws UnsupportedKeyException is thrown if the current key is not handled by this method
     * @throws DeadKeyException        is thrown if the current key is a dead key
     * @since 0.15
     */
    protected char interpretExtendedScanCode(int scancode, int vk, boolean released, boolean extended)
        throws UnsupportedKeyException, DeadKeyException {
        boolean deadKey = false;

        switch (vk) {
            case KeyEvent.VK_DEAD_ABOVEDOT:
            case KeyEvent.VK_DEAD_ABOVERING:
            case KeyEvent.VK_DEAD_ACUTE:
            case KeyEvent.VK_DEAD_BREVE:
            case KeyEvent.VK_DEAD_CARON:
            case KeyEvent.VK_DEAD_CEDILLA:
            case KeyEvent.VK_DEAD_CIRCUMFLEX:
            case KeyEvent.VK_DEAD_DIAERESIS:
            case KeyEvent.VK_DEAD_DOUBLEACUTE:
            case KeyEvent.VK_DEAD_GRAVE:
            case KeyEvent.VK_DEAD_IOTA:
            case KeyEvent.VK_DEAD_MACRON:
            case KeyEvent.VK_DEAD_OGONEK:
            case KeyEvent.VK_DEAD_SEMIVOICED_SOUND:
            case KeyEvent.VK_DEAD_TILDE:
            case KeyEvent.VK_DEAD_VOICED_SOUND:
                lastDeadVK = vk;
                deadKey = true;
                break;

            case KeyEvent.VK_INSERT:
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_PAGE_UP:
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_END:
            case KeyEvent.VK_PAGE_DOWN:
            case KeyEvent.VK_UP:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_PRINTSCREEN:
            case KeyEvent.VK_ALT_GRAPH:
            case KeyEvent.VK_PROPS:
            case KeyEvent.VK_CONTROL:  // both normal and extend should return 0
                if (extended)
                    return 0;
                break;

            case KeyEvent.VK_ENTER:
                if (extended)
                    return enter;
                break;
            case KeyEvent.VK_DIVIDE:
                if (extended)
                    return divide;
                break;
        }

        if (deadKey) {
            throw new DeadKeyException();
        } else if (lastDeadVK != -1) {
            try {
                Key key = keys.getKey(scancode);

                char[] deadChars = key.getDeadKeyChar(lastDeadVK);

                if (flags == InputEvent.SHIFT_DOWN_MASK) {
                    if (deadChars.length > 1) {
                        return deadChars[1];
                    } else
                        throw new UnsupportedKeyException();
                } else if (deadChars.length > 0)
                    return deadChars[0];
                else
                    return 0;
            } finally {
                if (!released) {
                    lastDeadVK = -1;
                }
            }
        }

        throw unsupportedKeyException;
    }

    /**
     * @param keycode
     * @return
     * @throws UnsupportedKeyException
     */
    public KeyboardEvent interpretKeycode(int keycode) {
        final int scancode = keys.getScanCode(keycode);
        if (scancode < 0) return null;
        return interpretScancode(scancode);
    }

    /**
     * @return Returns the flags.
     */
    protected final int getFlags() {
        return this.flags;
    }


    protected abstract void initKeys(Keys keys);
}


