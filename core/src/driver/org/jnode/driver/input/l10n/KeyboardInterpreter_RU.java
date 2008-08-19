/*
 * $Id: KeyboardInterpreter_RU.java 4153 2008-08-19 $
 *
 * JNode.org
 * Copyright (C) 2003-2008 JNode.org
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

package org.jnode.driver.input.l10n;

import org.jnode.driver.input.KeyboardInterpreter;
import org.jnode.driver.input.Keys;
import org.jnode.driver.input.Key;

import java.awt.event.KeyEvent;

/**
 * @author Sergey Mashkov (cy6ergn0m)
 * @since 0.2.8
 */

public class KeyboardInterpreter_RU extends KeyboardInterpreter {

    protected void initKeys(Keys keys) {
        keys.setKey(1, new Key(KeyEvent.VK_ESCAPE));
        keys.setKey(2, new Key('1', KeyEvent.VK_1, '!', KeyEvent.VK_EXCLAMATION_MARK));
        keys.setKey(3, new Key('2', KeyEvent.VK_2, '\"', KeyEvent.VK_QUOTEDBL));
        keys.setKey(4, new Key('3', KeyEvent.VK_3, '\u2116', KeyEvent.VK_NUMBER_SIGN));
        keys.setKey(5, new Key('4', KeyEvent.VK_4, ';', KeyEvent.VK_SEMICOLON));
        keys.setKey(6, new Key('5', KeyEvent.VK_5, '%', KeyEvent.VK_UNDEFINED));
        keys.setKey(7, new Key('6', KeyEvent.VK_6, ':', KeyEvent.VK_COLON));
        keys.setKey(8, new Key('7', KeyEvent.VK_7, '?', KeyEvent.VK_UNDEFINED));
        keys.setKey(9, new Key('8', KeyEvent.VK_8, '*', KeyEvent.VK_MULTIPLY));
        keys.setKey(10, new Key('9', KeyEvent.VK_9, '(', KeyEvent.VK_LEFT_PARENTHESIS));
        keys.setKey(11, new Key('0', KeyEvent.VK_0, ')', KeyEvent.VK_RIGHT_PARENTHESIS));
        keys.setKey(12, new Key('-', KeyEvent.VK_MINUS, '_', KeyEvent.VK_UNDERSCORE));
        keys.setKey(13, new Key('=', KeyEvent.VK_EQUALS, '+', KeyEvent.VK_PLUS));
        keys.setKey(14, new Key('\b', KeyEvent.VK_BACK_SPACE));
        keys.setKey(15, new Key('\t', KeyEvent.VK_TAB));
        keys.setKey(16, new Key('\u0439', '\u0419', KeyEvent.VK_Q));
        keys.setKey(17, new Key('\u0446', '\u0426', KeyEvent.VK_W));
        keys.setKey(18, new Key('\u0443', '\u0423', KeyEvent.VK_E));
        keys.setKey(19, new Key('\u043a', '\u041a', KeyEvent.VK_R));
        keys.setKey(20, new Key('\u0435', '\u0415', KeyEvent.VK_T));
        keys.setKey(21, new Key('\u043d', '\u042d', KeyEvent.VK_Y));
        keys.setKey(22, new Key('\u0433', '\u0413', KeyEvent.VK_U));
        keys.setKey(23, new Key('\u0448', '\u0428', KeyEvent.VK_I));
        keys.setKey(24, new Key('\u0449', '\u0429', KeyEvent.VK_O));
        keys.setKey(25, new Key('\u0437', '\u0417', KeyEvent.VK_P));
        keys.setKey(26, new Key('\u0445', '\u0425', KeyEvent.VK_BRACELEFT));
        keys.setKey(27, new Key('\u044a', '\u042a', KeyEvent.VK_BRACERIGHT));
        keys.setKey(28, new Key('\n', KeyEvent.VK_ENTER));
        keys.setKey(29, new Key((char) 0, KeyEvent.VK_CONTROL));
        keys.setKey(30, new Key('\u0444', '\u0424', KeyEvent.VK_A));
        keys.setKey(31, new Key('\u044b', '\u042b', KeyEvent.VK_S));
        keys.setKey(32, new Key('\u0432', '\u0412', KeyEvent.VK_D));
        keys.setKey(33, new Key('\u0430', '\u0410', KeyEvent.VK_F));
        keys.setKey(34, new Key('\u043f', '\u041f', KeyEvent.VK_G));
        keys.setKey(35, new Key('\u0440', '\u0420', KeyEvent.VK_H));
        keys.setKey(36, new Key('\u043e', '\u041e', KeyEvent.VK_J));
        keys.setKey(37, new Key('\u043b', '\u041b', KeyEvent.VK_K));
        keys.setKey(38, new Key('\u0434', '\u0414', KeyEvent.VK_L));
        keys.setKey(39, new Key('\u0436', '\u0416', KeyEvent.VK_COLON));
        keys.setKey(40, new Key('\u044d', '\u042d', KeyEvent.VK_QUOTE));
        keys.setKey(41, new Key('`', '~', KeyEvent.VK_BACK_QUOTE));
        keys.setKey(43, new Key('\\', '|', KeyEvent.VK_BACK_SLASH));
        keys.setKey(44, new Key('\u044f', '\u042f', KeyEvent.VK_Z));
        keys.setKey(45, new Key('\u0447', '\u0427', KeyEvent.VK_X));
        keys.setKey(46, new Key('\u0441', '\u0421', KeyEvent.VK_C));
        keys.setKey(47, new Key('\u043c', '\u041c', KeyEvent.VK_V));
        keys.setKey(48, new Key('\u0438', '\u0418', KeyEvent.VK_B));
        keys.setKey(49, new Key('\u0442', '\u0422', KeyEvent.VK_N));
        keys.setKey(50, new Key('\u044c', '\u042c', KeyEvent.VK_M));
        keys.setKey(51, new Key('\u0431', '\u0411', KeyEvent.VK_LESS));
        keys.setKey(52, new Key('\u044e', '\u042e', KeyEvent.VK_GREATER));
        keys.setKey(53, new Key('.', ',', KeyEvent.VK_PERIOD));
        keys.setKey(55, new Key('*', KeyEvent.VK_MULTIPLY));

        keys.setKey(57, new Key(' ', KeyEvent.VK_SPACE));

        keys.setKey(71, new Key('7', KeyEvent.VK_NUMPAD7));
        keys.setKey(72, new Key('8', KeyEvent.VK_NUMPAD8));
        keys.setKey(73, new Key('9', KeyEvent.VK_NUMPAD9));
        keys.setKey(74, new Key('-', KeyEvent.VK_SUBTRACT));
        keys.setKey(75, new Key('4', KeyEvent.VK_NUMPAD4));
        keys.setKey(76, new Key('5', KeyEvent.VK_NUMPAD5));
        keys.setKey(77, new Key('6', KeyEvent.VK_NUMPAD6));
        keys.setKey(78, new Key('+', KeyEvent.VK_ADD));
        keys.setKey(79, new Key('1', KeyEvent.VK_NUMPAD1));
        keys.setKey(80, new Key('2', KeyEvent.VK_NUMPAD2));
        keys.setKey(81, new Key('3', KeyEvent.VK_NUMPAD3));
        keys.setKey(82, new Key('0', KeyEvent.VK_NUMPAD0));

        keys.setKey(83, new Key(',', KeyEvent.VK_DECIMAL));

        keys.setKey(86, new Key('/', KeyEvent.VK_SLASH));

        keys.setKey(42, new Key(KeyEvent.VK_SHIFT));
        keys.setKey(54, new Key(KeyEvent.VK_SHIFT));
        keys.setKey(56, new Key(KeyEvent.VK_ALT));
        keys.setKey(58, new Key(KeyEvent.VK_CAPS_LOCK));
        keys.setKey(59, new Key(KeyEvent.VK_F1));
        keys.setKey(60, new Key(KeyEvent.VK_F2));
        keys.setKey(61, new Key(KeyEvent.VK_F3));
        keys.setKey(62, new Key(KeyEvent.VK_F4));
        keys.setKey(63, new Key(KeyEvent.VK_F5));
        keys.setKey(64, new Key(KeyEvent.VK_F6));
        keys.setKey(65, new Key(KeyEvent.VK_F7));
        keys.setKey(66, new Key(KeyEvent.VK_F8));
        keys.setKey(67, new Key(KeyEvent.VK_F9));
        keys.setKey(68, new Key(KeyEvent.VK_F10));
        keys.setKey(69, new Key(KeyEvent.VK_NUM_LOCK));
        keys.setKey(70, new Key(KeyEvent.VK_SCROLL_LOCK));
        keys.setKey(87, new Key(KeyEvent.VK_F11));
        keys.setKey(88, new Key(KeyEvent.VK_F12));
        keys.setKey(96, new Key(KeyEvent.VK_INSERT));
        keys.setKey(97, new Key(KeyEvent.VK_HOME));
        keys.setKey(98, new Key(KeyEvent.VK_PAGE_UP));
        keys.setKey(99, new Key(KeyEvent.VK_DIVIDE));
        keys.setKey(100, new Key(KeyEvent.VK_PRINTSCREEN));
        keys.setKey(101, new Key(KeyEvent.VK_DELETE));
        keys.setKey(102, new Key(KeyEvent.VK_END));
        keys.setKey(103, new Key(KeyEvent.VK_PAGE_DOWN));
        keys.setKey(104, new Key(KeyEvent.VK_UP));
        keys.setKey(105, new Key(KeyEvent.VK_SEPARATOR));
        keys.setKey(110, new Key(KeyEvent.VK_ESCAPE));
        keys.setKey(111, new Key(KeyEvent.VK_FINAL));
        keys.setKey(112, new Key(KeyEvent.VK_CONTROL));
        keys.setKey(113, new Key(KeyEvent.VK_LEFT));
        keys.setKey(114, new Key(KeyEvent.VK_DOWN));
        keys.setKey(115, new Key(KeyEvent.VK_RIGHT));
        keys.setKey(116, new Key(KeyEvent.VK_PAUSE));
    }
}
