/*
 * $Id$
 */
package org.jnode.driver.input.l10n;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import org.jnode.driver.input.DeadKeyException;
import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.UnsupportedKeyException;

/*
 * @author sauron
 */
public class KeyboardInterpreter_ES extends KeyboardInterpreter {

    protected int lastDeadVK = -1;

    protected int lastFlags = -1;

    public KeyboardInterpreter_ES() {
        super();
    }

    protected char interpretExtendedScanCode(int scancode, int vk,
            boolean released) throws UnsupportedKeyException, DeadKeyException {
        // TODO
        //throw new UnsupportedKeyException();
        boolean deadKey = false;

        switch (vk) {
        case KeyEvent.VK_DEAD_CIRCUMFLEX:
            lastDeadVK = KeyEvent.VK_DEAD_CIRCUMFLEX;
            lastFlags = getFlags();
            deadKey = true;
            break;
        case KeyEvent.VK_DEAD_DIAERESIS:
            lastDeadVK = KeyEvent.VK_DEAD_DIAERESIS;
            lastFlags = getFlags();
            deadKey = true;
            break;
        }

        if (deadKey) {
            throw new DeadKeyException();
        } else {
            try {
                switch (lcharMap[ scancode]) {
                case 'a':
                    switch (lastDeadVK) {
                    case KeyEvent.VK_DEAD_CIRCUMFLEX:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'â';
                            } else {
                                return 'à';
                            }
                        }
                    case KeyEvent.VK_DEAD_DIAERESIS:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'ä';
                            } else {
                                return 'á';
                            }
                        }
                    default:
                        throw new UnsupportedKeyException();
                    }
                case 'e':
                    switch (lastDeadVK) {
                    case KeyEvent.VK_DEAD_CIRCUMFLEX:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'ê';
                            } else {
                                return 'è';
                            }
                        }
                    case KeyEvent.VK_DEAD_DIAERESIS:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'ë';
                            } else {
                                return 'é';
                            }
                        }
                    default:
                        throw new UnsupportedKeyException();
                    }
                case 'i':
                    switch (lastDeadVK) {
                    case KeyEvent.VK_DEAD_CIRCUMFLEX:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'î';
                            } else {
                                return 'ì';
                            }
                        }
                    case KeyEvent.VK_DEAD_DIAERESIS:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'ï';
                            } else {
                                return 'í';
                            }
                        }
                    default:
                        throw new UnsupportedKeyException();
                    }
                case 'o':
                    switch (lastDeadVK) {
                    case KeyEvent.VK_DEAD_CIRCUMFLEX:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'ô';
                            } else {
                                return 'ò';
                            }
                        }
                    case KeyEvent.VK_DEAD_DIAERESIS:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'ö';
                            } else {
                                return 'ó';
                            }
                        }
                    default:
                        throw new UnsupportedKeyException();
                    }
                case 'u':
                    switch (lastDeadVK) {
                    case KeyEvent.VK_DEAD_CIRCUMFLEX:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'û';
                            } else {
                                return 'ù';
                            }
                        }
                    case KeyEvent.VK_DEAD_DIAERESIS:
                        {
                            if (lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                                return 'ü';
                            } else {
                                return 'ú';
                            }
                        }
                    default:
                        throw new UnsupportedKeyException();
                    }
                default:
                    throw new UnsupportedKeyException();
                }
            } finally {
                if (!released) {
                    lastDeadVK = -1;
                    lastFlags = -1;
                }
            }
        }

    }

    protected void initVkMap(int[] vkMap, char[] lcharMap, char[] ucharMap,
            char[] altGrCharMap) {
        vkMap[ 0] = KeyEvent.VK_UNDEFINED;
        vkMap[ 1] = KeyEvent.VK_ESCAPE;
        vkMap[ 2] = KeyEvent.VK_1;
        vkMap[ 3] = KeyEvent.VK_2;
        vkMap[ 4] = KeyEvent.VK_3;
        vkMap[ 5] = KeyEvent.VK_4;
        vkMap[ 6] = KeyEvent.VK_5;
        vkMap[ 7] = KeyEvent.VK_6;
        vkMap[ 8] = KeyEvent.VK_7;
        vkMap[ 9] = KeyEvent.VK_8;
        vkMap[ 10] = KeyEvent.VK_9;
        vkMap[ 11] = KeyEvent.VK_0;
        vkMap[ 12] = KeyEvent.VK_QUOTE;
        vkMap[ 13] = KeyEvent.VK_INVERTED_EXCLAMATION_MARK;
        vkMap[ 14] = KeyEvent.VK_BACK_SPACE;
        vkMap[ 15] = KeyEvent.VK_TAB;
        vkMap[ 16] = KeyEvent.VK_Q;
        vkMap[ 17] = KeyEvent.VK_W;
        vkMap[ 18] = KeyEvent.VK_E;
        vkMap[ 19] = KeyEvent.VK_R;
        vkMap[ 20] = KeyEvent.VK_T;
        vkMap[ 21] = KeyEvent.VK_Y;
        vkMap[ 22] = KeyEvent.VK_U;
        vkMap[ 23] = KeyEvent.VK_I;
        vkMap[ 24] = KeyEvent.VK_O;
        vkMap[ 25] = KeyEvent.VK_P;
        vkMap[ 26] = KeyEvent.VK_DEAD_CIRCUMFLEX;
        vkMap[ 27] = KeyEvent.VK_PLUS;
        vkMap[ 28] = KeyEvent.VK_ENTER;
        vkMap[ 29] = KeyEvent.VK_CONTROL;
        vkMap[ 30] = KeyEvent.VK_A;
        vkMap[ 31] = KeyEvent.VK_S;
        vkMap[ 32] = KeyEvent.VK_D;
        vkMap[ 33] = KeyEvent.VK_F;
        vkMap[ 34] = KeyEvent.VK_G;
        vkMap[ 35] = KeyEvent.VK_H;
        vkMap[ 36] = KeyEvent.VK_J;
        vkMap[ 37] = KeyEvent.VK_K;
        vkMap[ 38] = KeyEvent.VK_L;
        vkMap[ 39] = KeyEvent.VK_N; // remap to n tilde (ñ)
        vkMap[ 40] = KeyEvent.VK_DEAD_DIAERESIS; // dead acute
        vkMap[ 41] = KeyEvent.VK_DEAD_ABOVEDOT; // remaps to º
        vkMap[ 42] = KeyEvent.VK_SHIFT;
        vkMap[ 43] = KeyEvent.VK_DEAD_CEDILLA; //c cedilla
        vkMap[ 44] = KeyEvent.VK_Z;
        vkMap[ 45] = KeyEvent.VK_X;
        vkMap[ 46] = KeyEvent.VK_C;
        vkMap[ 47] = KeyEvent.VK_V;
        vkMap[ 48] = KeyEvent.VK_B;
        vkMap[ 49] = KeyEvent.VK_N;
        vkMap[ 50] = KeyEvent.VK_M;
        vkMap[ 51] = KeyEvent.VK_COMMA;
        vkMap[ 52] = KeyEvent.VK_PERIOD;
        vkMap[ 53] = KeyEvent.VK_MINUS;
        vkMap[ 54] = KeyEvent.VK_SHIFT;
        vkMap[ 55] = KeyEvent.VK_MULTIPLY;
        vkMap[ 56] = KeyEvent.VK_ALT;
        vkMap[ 57] = KeyEvent.VK_SPACE;
        vkMap[ 58] = KeyEvent.VK_CAPS_LOCK;
        vkMap[ 59] = KeyEvent.VK_F1;
        vkMap[ 60] = KeyEvent.VK_F2;
        vkMap[ 61] = KeyEvent.VK_F3;
        vkMap[ 62] = KeyEvent.VK_F4;
        vkMap[ 63] = KeyEvent.VK_F5;
        vkMap[ 64] = KeyEvent.VK_F6;
        vkMap[ 65] = KeyEvent.VK_F7;
        vkMap[ 66] = KeyEvent.VK_F8;
        vkMap[ 67] = KeyEvent.VK_F9;
        vkMap[ 68] = KeyEvent.VK_F10;
        vkMap[ 69] = KeyEvent.VK_NUM_LOCK;
        vkMap[ 70] = KeyEvent.VK_SCROLL_LOCK;
        vkMap[ 71] = KeyEvent.VK_NUMPAD7;
        vkMap[ 72] = KeyEvent.VK_NUMPAD8;
        vkMap[ 73] = KeyEvent.VK_NUMPAD9;
        vkMap[ 74] = KeyEvent.VK_SUBTRACT;
        vkMap[ 75] = KeyEvent.VK_NUMPAD4;
        vkMap[ 76] = KeyEvent.VK_NUMPAD5;
        vkMap[ 77] = KeyEvent.VK_NUMPAD6;
        vkMap[ 78] = KeyEvent.VK_ADD;
        vkMap[ 79] = KeyEvent.VK_NUMPAD1;
        vkMap[ 80] = KeyEvent.VK_NUMPAD2;
        vkMap[ 81] = KeyEvent.VK_NUMPAD3;
        vkMap[ 82] = KeyEvent.VK_NUMPAD0;
        vkMap[ 83] = KeyEvent.VK_DECIMAL;

        vkMap[ 86] = KeyEvent.VK_LESS;
        vkMap[ 87] = KeyEvent.VK_F11;
        vkMap[ 88] = KeyEvent.VK_F12;

        vkMap[ 96] = KeyEvent.VK_INSERT;
        vkMap[ 97] = KeyEvent.VK_HOME;
        vkMap[ 98] = KeyEvent.VK_PAGE_UP;
        vkMap[ 99] = KeyEvent.VK_DIVIDE;
        vkMap[ 100] = KeyEvent.VK_PRINTSCREEN;
        vkMap[ 101] = KeyEvent.VK_DELETE;
        vkMap[ 102] = KeyEvent.VK_END;
        vkMap[ 103] = KeyEvent.VK_PAGE_DOWN;
        vkMap[ 104] = KeyEvent.VK_UP;
        vkMap[ 105] = KeyEvent.VK_SEPARATOR;
        vkMap[ 111] = KeyEvent.VK_FINAL;
        vkMap[ 112] = KeyEvent.VK_CONTROL;
        vkMap[ 113] = KeyEvent.VK_LEFT;
        vkMap[ 114] = KeyEvent.VK_DOWN;
        vkMap[ 115] = KeyEvent.VK_RIGHT;
        vkMap[ 116] = KeyEvent.VK_PAUSE;

        lcharMap[ 2] = '1';
        lcharMap[ 3] = '2';
        lcharMap[ 4] = '3';
        lcharMap[ 5] = '4';
        lcharMap[ 6] = '5';
        lcharMap[ 7] = '6';
        lcharMap[ 8] = '7';
        lcharMap[ 9] = '8';
        lcharMap[ 10] = '9';
        lcharMap[ 11] = '0';
        lcharMap[ 12] = '\'';
        lcharMap[ 13] = '¡';
        lcharMap[ 14] = '\b';
        lcharMap[ 15] = '\t';
        lcharMap[ 16] = 'q';
        lcharMap[ 17] = 'w';
        lcharMap[ 18] = 'e';
        lcharMap[ 19] = 'r';
        lcharMap[ 20] = 't';
        lcharMap[ 21] = 'y';
        lcharMap[ 22] = 'u';
        lcharMap[ 23] = 'i';
        lcharMap[ 24] = 'o';
        lcharMap[ 25] = 'p';
        //lcharMap[26] = ' ';
        lcharMap[ 27] = '+';
        lcharMap[ 28] = '\n';
        //lcharMap[29] = ' ';
        lcharMap[ 30] = 'a';
        lcharMap[ 31] = 's';
        lcharMap[ 32] = 'd';
        lcharMap[ 33] = 'f';
        lcharMap[ 34] = 'g';
        lcharMap[ 35] = 'h';
        lcharMap[ 36] = 'j';
        lcharMap[ 37] = 'k';
        lcharMap[ 38] = 'l';
        lcharMap[ 39] = 'ñ';
        //lcharMap[40] = ' ';
        lcharMap[ 41] = 'º';
        lcharMap[ 43] = 'ç';
        lcharMap[ 44] = 'z';
        lcharMap[ 45] = 'x';
        lcharMap[ 46] = 'c';
        lcharMap[ 47] = 'v';
        lcharMap[ 48] = 'b';
        lcharMap[ 49] = 'n';
        lcharMap[ 50] = 'm';
        lcharMap[ 51] = ',';
        lcharMap[ 52] = '.';
        lcharMap[ 53] = '-';
        lcharMap[ 55] = '*';
        lcharMap[ 57] = ' ';
        lcharMap[ 71] = '7';
        lcharMap[ 72] = '8';
        lcharMap[ 73] = '9';
        lcharMap[ 74] = '-';
        lcharMap[ 75] = '4';
        lcharMap[ 76] = '5';
        lcharMap[ 77] = '6';
        lcharMap[ 78] = '+';
        lcharMap[ 79] = '1';
        lcharMap[ 80] = '2';
        lcharMap[ 81] = '3';
        lcharMap[ 82] = '0';
        lcharMap[ 83] = '.';
        lcharMap[ 86] = '<';
        lcharMap[ 99] = '/';

        ucharMap[ 2] = '!';
        ucharMap[ 3] = '"';
        ucharMap[ 4] = '·';
        ucharMap[ 5] = '$';
        ucharMap[ 6] = '%';
        ucharMap[ 7] = '&';
        ucharMap[ 8] = '/';
        ucharMap[ 9] = '(';
        ucharMap[ 10] = ')';
        ucharMap[ 11] = '=';
        ucharMap[ 12] = '?';
        ucharMap[ 13] = '¿';
        ucharMap[ 14] = '\b';
        ucharMap[ 15] = '\t';
        ucharMap[ 16] = 'Q';
        ucharMap[ 17] = 'W';
        ucharMap[ 18] = 'E';
        ucharMap[ 19] = 'R';
        ucharMap[ 20] = 'T';
        ucharMap[ 21] = 'Y';
        ucharMap[ 22] = 'U';
        ucharMap[ 23] = 'I';
        ucharMap[ 24] = 'O';
        ucharMap[ 25] = 'P';
        //ucharMap[26] = '^';
        ucharMap[ 27] = '*';
        ucharMap[ 28] = '\n';
        ucharMap[ 30] = 'A';
        ucharMap[ 31] = 'S';
        ucharMap[ 32] = 'D';
        ucharMap[ 33] = 'F';
        ucharMap[ 34] = 'G';
        ucharMap[ 35] = 'H';
        ucharMap[ 36] = 'J';
        ucharMap[ 37] = 'K';
        ucharMap[ 38] = 'L';
        ucharMap[ 39] = 'Ñ';
        //ucharMap[40] = ' ';
        ucharMap[ 41] = 'ª';
        ucharMap[ 43] = 'Ç';
        ucharMap[ 44] = 'Z';
        ucharMap[ 45] = 'X';
        ucharMap[ 46] = 'C';
        ucharMap[ 47] = 'V';
        ucharMap[ 48] = 'B';
        ucharMap[ 49] = 'N';
        ucharMap[ 50] = 'M';
        ucharMap[ 51] = ';';
        ucharMap[ 52] = ':';
        ucharMap[ 53] = '_';
        ucharMap[ 55] = '*';
        ucharMap[ 57] = ' ';
        ucharMap[ 71] = '7';
        ucharMap[ 72] = '8';
        ucharMap[ 73] = '9';
        ucharMap[ 74] = '-';
        ucharMap[ 75] = '4';
        ucharMap[ 76] = '5';
        ucharMap[ 77] = '6';
        ucharMap[ 78] = '+';
        ucharMap[ 79] = '1';
        ucharMap[ 80] = '2';
        ucharMap[ 81] = '3';
        ucharMap[ 82] = '0';
        ucharMap[ 83] = '.';
        ucharMap[ 86] = '>';
        ucharMap[ 99] = '/';

        altGrCharMap[ 2] = '|';
        altGrCharMap[ 3] = '@';
        altGrCharMap[ 4] = '#';
        altGrCharMap[ 5] = '~';
        //altGrCharMap[6] = '€'; //euro symbol doesn't show properly
        //altGrCharMap[18] = '€'; //euro symbol doesn't show properly
        altGrCharMap[ 26] = '[';
        altGrCharMap[ 27] = ']';
        altGrCharMap[ 40] = '{';
        altGrCharMap[ 41] = '\\';
        altGrCharMap[ 43] = '}';
    }

}

