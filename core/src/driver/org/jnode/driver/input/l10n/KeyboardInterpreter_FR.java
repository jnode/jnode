/*
 * $Id$
 */

package org.jnode.driver.input.l10n;

import org.jnode.driver.input.*;

import java.awt.event.KeyEvent;


/**
 * KeyboardInterpreter_FR.java
 *
 * @author Created by Marc DENTY on Feb 01 2004
 * @author Martin Husted Hartvig
 * @since 0.15
 */
public class KeyboardInterpreter_FR extends KeyboardInterpreter
{

  protected void initKeys(Keys keys)
  {
    Key key;

    keys.setKey(2, new Key('&', '1', '¹', KeyEvent.VK_1));
    keys.setKey(3, new Key('é', '2', '~', KeyEvent.VK_2));
    keys.setKey(4, new Key('"', KeyEvent.VK_QUOTEDBL, '3', KeyEvent.VK_3, '#', KeyEvent.VK_NUMBER_SIGN));
    keys.setKey(5, new Key('\'', KeyEvent.VK_QUOTE, '4', KeyEvent.VK_4, '{', KeyEvent.VK_BRACELEFT));
    keys.setKey(6, new Key('(', KeyEvent.VK_LEFT_PARENTHESIS, '5', KeyEvent.VK_5, '[', KeyEvent.VK_OPEN_BRACKET));
    keys.setKey(7, new Key('-', KeyEvent.VK_MINUS, '6', KeyEvent.VK_6, '|', KeyEvent.VK_UNDEFINED));
    keys.setKey(8, new Key('è', '7', '`', KeyEvent.VK_7));
    keys.setKey(9, new Key('_', KeyEvent.VK_UNDERSCORE, '8', KeyEvent.VK_8, '\\', KeyEvent.VK_BACK_SLASH));
    keys.setKey(10, new Key('ç', '9', '^', KeyEvent.VK_9));
    keys.setKey(11, new Key('à', KeyEvent.VK_UNDEFINED, '0', KeyEvent.VK_0, '@', KeyEvent.VK_AT));
    keys.setKey(12, new Key(')', KeyEvent.VK_RIGHT_PARENTHESIS, '°', KeyEvent.VK_UNDEFINED, ']', KeyEvent.VK_CLOSE_BRACKET));
    keys.setKey(13, new Key('=', KeyEvent.VK_EQUALS, '?', KeyEvent.VK_UNDEFINED, '}', KeyEvent.VK_BRACERIGHT));

    /*
    key = new Key();
    key.setAltGrChar('|');
    key.setAltGrVirtuelKey(KeyEvent.VK_DEAD_ACUTE);
    key.setLowerVirtuelKey(KeyEvent.VK_DEAD_ACUTE);
    key.setUpperVirtuelKey(KeyEvent.VK_DEAD_GRAVE);
    keys.setKey(13, key);
*/
    keys.setKey(14, new Key('\b', KeyEvent.VK_BACK_SPACE));
    keys.setKey(15, new Key('\t', KeyEvent.VK_TAB));

    keys.setKey(16, new Key('a', 'A', 'æ', KeyEvent.VK_A));
    key = keys.getKey(16);
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_ACUTE, new char[]{'á','Á'});
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_GRAVE, new char[]{'à','À'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_DIAERESIS, new char[]{'ä', 'Ä'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_CIRCUMFLEX, new char[]{'â', 'Â'});
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_TILDE, new char[]{'ã','Ã'});

    keys.setKey(17, new Key('z', 'Z', '«', KeyEvent.VK_Z));

//    keys.setKey(18, new Key('e',KeyEvent.VK_E,'E',KeyEvent.VK_E,'€',KeyEvent.VK_EURO_SIGN));
    keys.setKey(18, new Key('e', KeyEvent.VK_E, 'E', KeyEvent.VK_E, '¤', KeyEvent.VK_UNDEFINED));
    key = keys.getKey(18);
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_ACUTE, new char[]{'é','É'});
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_GRAVE, new char[]{'è','È'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_DIAERESIS, new char[]{'ë', 'Ë'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_CIRCUMFLEX, new char[]{'ê', 'Ê'});

    keys.setKey(19, new Key('r', 'R', '¶', KeyEvent.VK_R));
    keys.setKey(20, new Key('t', 'T', '?', KeyEvent.VK_T));

    keys.setKey(21, new Key('y', 'Y', KeyEvent.VK_Y));
    key = keys.getKey(21);
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_ACUTE, new char[]{'ý','Ý'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_DIAERESIS, new char[]{'ÿ'});

    keys.setKey(22, new Key('u', 'U', KeyEvent.VK_U));
    key = keys.getKey(22);
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_ACUTE, new char[]{'ú','Ú'});
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_GRAVE, new char[]{'ù','Ù'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_DIAERESIS, new char[]{'ü', 'Ü'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_CIRCUMFLEX, new char[]{'û', 'Û'});


    keys.setKey(23, new Key('i', 'I', KeyEvent.VK_I));
    key = keys.getKey(23);
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_ACUTE, new char[]{'í','Í'});
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_GRAVE, new char[]{'ì','Ì'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_DIAERESIS, new char[]{'ï', 'Ï'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_CIRCUMFLEX, new char[]{'î', 'Î'});


    keys.setKey(24, new Key('o', 'O', 'ø', KeyEvent.VK_O));
    key = keys.getKey(24);
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_ACUTE, new char[]{'ó','Ó'});
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_GRAVE, new char[]{'ò','Ò'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_DIAERESIS, new char[]{'ö', 'Ö'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_CIRCUMFLEX, new char[]{'ô', 'Ô'});
//    key.addDeadKeyChar(KeyEvent.VK_DEAD_TILDE, new char[]{'õ','Õ'});

    keys.setKey(25, new Key('p', 'P', 'þ', KeyEvent.VK_P));
    keys.setKey(26, new Key('^', KeyEvent.VK_DEAD_CIRCUMFLEX, 'ï', KeyEvent.VK_DEAD_DIAERESIS));
    keys.setKey(27, new Key('$', '£', '?', KeyEvent.VK_DOLLAR));

    /*
    key = new Key();
    key.setLowerVirtuelKey(KeyEvent.VK_DEAD_DIAERESIS);
    key.setUpperVirtuelKey(KeyEvent.VK_DEAD_CIRCUMFLEX);
    key.setAltGrVirtuelKey(KeyEvent.VK_DEAD_TILDE);
    keys.setKey(27, key);
*/
    keys.setKey(28, new Key('\n', KeyEvent.VK_ENTER));
    keys.setKey(29, new Key((char) 0, KeyEvent.VK_CONTROL));
    keys.setKey(30, new Key('q', KeyEvent.VK_Q, 'Q', KeyEvent.VK_Q, '@', KeyEvent.VK_AT));
    keys.setKey(31, new Key('s', 'S', 'ß', KeyEvent.VK_S));
    keys.setKey(32, new Key('d', 'D', 'ð', KeyEvent.VK_D));
    keys.setKey(33, new Key('f', 'F', '?', KeyEvent.VK_F));
    keys.setKey(34, new Key('g', 'G', '?', KeyEvent.VK_G));
    keys.setKey(35, new Key('h', 'H', '?', KeyEvent.VK_H));
    keys.setKey(36, new Key('j', 'J', 'j', KeyEvent.VK_J));
    keys.setKey(37, new Key('k', 'K', KeyEvent.VK_K));
    keys.setKey(38, new Key('l', 'L', '?', KeyEvent.VK_L));
    keys.setKey(39, new Key('m', 'M', 'µ', KeyEvent.VK_M));
    keys.setKey(40, new Key('ù', '%', KeyEvent.VK_UNDEFINED));
    keys.setKey(41, new Key('²', '~', '¬', KeyEvent.VK_UNDEFINED));
    keys.setKey(43, new Key('*', 'µ', '»', KeyEvent.VK_MULTIPLY));
    keys.setKey(44, new Key('w', 'W', '¢', KeyEvent.VK_W));
    keys.setKey(45, new Key('x', 'X', KeyEvent.VK_X));
    keys.setKey(46, new Key('c', 'C', KeyEvent.VK_C));
    keys.setKey(47, new Key('v', 'V', 'n', KeyEvent.VK_V));
    keys.setKey(48, new Key('b', 'B', KeyEvent.VK_B));
    keys.setKey(49, new Key('n', 'N', KeyEvent.VK_N));

/*
    key = keys.getKey(49);
    key.addDeadKeyChar(KeyEvent.VK_DEAD_TILDE, new char[]{'ñ','Ñ'});
*/
    keys.setKey(50, new Key(',', KeyEvent.VK_COMMA, '?', KeyEvent.VK_UNDEFINED, '.', KeyEvent.VK_PERIOD));
    keys.setKey(51, new Key(';', KeyEvent.VK_SEMICOLON, '.', KeyEvent.VK_PERIOD, '?', KeyEvent.VK_UNDEFINED));
    keys.setKey(52, new Key(':', KeyEvent.VK_COLON, '/', KeyEvent.VK_SLASH, '?', KeyEvent.VK_UNDEFINED));
//    keys.setKey(52, new Key('.',KeyEvent.VK_PERIOD,':',KeyEvent.VK_COLON));
    keys.setKey(53, new Key('!', '§', KeyEvent.VK_EXCLAMATION_MARK));

//    keys.setKey(55, new Key('*',KeyEvent.VK_MULTIPLY));

    keys.setKey(57, new Key(' ', KeyEvent.VK_SPACE));
/*
    key = keys.getKey(57);
    key.addDeadKeyChar(KeyEvent.VK_DEAD_ACUTE, new char[]{'´'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_GRAVE, new char[]{'`'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_DIAERESIS, new char[]{'¨'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_CIRCUMFLEX, new char[]{'^'});
    key.addDeadKeyChar(KeyEvent.VK_DEAD_TILDE, new char[]{'~'});
*/
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
    keys.setKey(83, new Key('.', KeyEvent.VK_DECIMAL));
    keys.setKey(86, new Key('<', KeyEvent.VK_LESS, '>', KeyEvent.VK_GREATER, '|', KeyEvent.VK_UNDEFINED));

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
    keys.setKey(111, new Key('/', '/', '/', KeyEvent.VK_SLASH));
    keys.setKey(112, new Key(KeyEvent.VK_CONTROL));
    keys.setKey(113, new Key(KeyEvent.VK_LEFT));
    keys.setKey(114, new Key(KeyEvent.VK_DOWN));
    keys.setKey(115, new Key(KeyEvent.VK_RIGHT));
    keys.setKey(116, new Key(KeyEvent.VK_PAUSE));
  }




//	protected int lastDeadVK = -1;
//	protected int lastFlags = -1;


  /**
   * Method interpretExtendedScanCode this method sould be used to handle the dead keys and other special keys
   *
   * @param    scancode            an int
   * @param    vk                  an int
   * @param    released            a  boolean
   *
   * @return   the char to use or throws an Exception
   * @exception   UnsupportedKeyException is thrown if the current key is not handled by this method
   * @exception   DeadKeyException is thrown if the current key is a dead key
   *
   * @author 	Marc DENTY
   * @version  2/8/2004
   * @since 0.15
   */
  /*
protected char interpretExtendedScanCode(int scancode, int vk, boolean released) throws UnsupportedKeyException, DeadKeyException {
    boolean deadKey = false;
//		System.err.print("vk = "+vk);
    switch(vk) {
      case KeyEvent.VK_DEAD_CIRCUMFLEX:
        lastDeadVK = KeyEvent.VK_DEAD_CIRCUMFLEX;
        lastFlags = getFlags();
        deadKey = true;
        break;
    }
    if(deadKey) {
//			System.err.println("Dead key pressed : vk="+vk+", flags="+flags);
      throw new DeadKeyException();
    } else {
      try {
        switch(lcharMap[scancode]) {
          case 'a':
            switch(lastDeadVK) {
              case KeyEvent.VK_DEAD_CIRCUMFLEX:
                if(lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                  return 'ä';
                } else {
                  return 'â';
                }
              default:
                throw new UnsupportedKeyException();
            }
          case 'e':
            switch(lastDeadVK) {
              case KeyEvent.VK_DEAD_CIRCUMFLEX:
                if(lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                  return 'ë';
                } else {
                  return 'ê';
                }
              default:
                throw new UnsupportedKeyException();
            }
          case 'i':
            switch(lastDeadVK) {
              case KeyEvent.VK_DEAD_CIRCUMFLEX:
                if(lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                  return 'ï';
                } else {
                  return 'î';
                }
              default:
                throw new UnsupportedKeyException();
            }
          case 'o':
            switch(lastDeadVK) {
              case KeyEvent.VK_DEAD_CIRCUMFLEX:
                if(lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                  return 'ö';
                } else {
                  return 'ô';
                }
              default:
                throw new UnsupportedKeyException();
            }
          case 'u':
            switch(lastDeadVK) {
              case KeyEvent.VK_DEAD_CIRCUMFLEX:
                if(lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                  return 'ü';
                } else {
                  return 'û';
                }
              default:
                throw new UnsupportedKeyException();
            }
          case 'y':
            switch(lastDeadVK) {
              case KeyEvent.VK_DEAD_CIRCUMFLEX:
                if(lastFlags == InputEvent.SHIFT_DOWN_MASK) {
                  return 'ÿ';
                } else {
                  throw new UnsupportedKeyException();
                }
              default:
                throw new UnsupportedKeyException();
            }
          default:
            throw new UnsupportedKeyException();
        }
      } finally {
        if(!released) {
//					System.err.println("deadKey reset");
          lastDeadVK = -1;
          lastFlags = -1;
        }
      }
    }
  }
*/




  /**
   * Initialize the mapping between scancode and virtual key code.
   * <p> To implement new variants of a KeyboardInterpreter simply
   * overload this method and redefine the keys and characters of your
   * keyboard.</p>
   * <p>I do not know how other keyborards are made, I think Kanji,
   * Japanese variants can be implemented this way. You'll need to add some arrays.</p>
   *
   */

/*
	protected void initVkMap(int []vkMap, char[] lcharMap, char[] ucharMap, char[] altGrCharMap) {
		vkMap[0] = KeyEvent.VK_UNDEFINED;
		vkMap[1] = KeyEvent.VK_ESCAPE;
		vkMap[2] = KeyEvent.VK_AMPERSAND;
		vkMap[3] = KeyEvent.VK_E; // TODO : remap to é
		vkMap[4] = KeyEvent.VK_QUOTEDBL;
		vkMap[5] = KeyEvent.VK_QUOTE;
		vkMap[6] = KeyEvent.VK_OPEN_BRACKET;
		vkMap[7] = KeyEvent.VK_MINUS;
		vkMap[8] = KeyEvent.VK_DEAD_GRAVE; // TODO: remap to è
		vkMap[9] = KeyEvent.VK_UNDERSCORE;
		vkMap[10] = KeyEvent.VK_DEAD_CEDILLA;
		vkMap[11] = KeyEvent.VK_DEAD_ACUTE;
		vkMap[12] = KeyEvent.VK_CLOSE_BRACKET;
		vkMap[13] = KeyEvent.VK_EQUALS;
		vkMap[14] = KeyEvent.VK_BACK_SPACE;
		vkMap[15] = KeyEvent.VK_TAB;
		vkMap[16] = KeyEvent.VK_A;
		vkMap[17] = KeyEvent.VK_Z;
		vkMap[18] = KeyEvent.VK_E;
		vkMap[19] = KeyEvent.VK_R;
		vkMap[20] = KeyEvent.VK_T;
		vkMap[21] = KeyEvent.VK_Y;
		vkMap[22] = KeyEvent.VK_U;
		vkMap[23] = KeyEvent.VK_I;
		vkMap[24] = KeyEvent.VK_O;
		vkMap[25] = KeyEvent.VK_P;
		vkMap[26] = KeyEvent.VK_DEAD_CIRCUMFLEX;
		vkMap[27] = KeyEvent.VK_DOLLAR;
		vkMap[28] = KeyEvent.VK_ENTER;
		vkMap[29] = KeyEvent.VK_CONTROL;
		vkMap[30] = KeyEvent.VK_Q;
		vkMap[31] = KeyEvent.VK_S;
		vkMap[32] = KeyEvent.VK_D;
		vkMap[33] = KeyEvent.VK_F;
		vkMap[34] = KeyEvent.VK_G;
		vkMap[35] = KeyEvent.VK_H;
		vkMap[36] = KeyEvent.VK_J;
		vkMap[37] = KeyEvent.VK_K;
		vkMap[38] = KeyEvent.VK_L;
		vkMap[39] = KeyEvent.VK_M;
		vkMap[40] = KeyEvent.VK_QUOTE; //'%' not found...
		vkMap[41] = KeyEvent.VK_UNDEFINED; // '²' not found
		vkMap[42] = KeyEvent.VK_SHIFT;
		vkMap[43] = KeyEvent.VK_MULTIPLY;
		vkMap[44] = KeyEvent.VK_W;
		vkMap[45] = KeyEvent.VK_X;
		vkMap[46] = KeyEvent.VK_C;
		vkMap[47] = KeyEvent.VK_V;
		vkMap[48] = KeyEvent.VK_B;
		vkMap[49] = KeyEvent.VK_N;
		vkMap[50] = KeyEvent.VK_COMMA;
		vkMap[51] = KeyEvent.VK_SEMICOLON;
		vkMap[52] = KeyEvent.VK_COLON;
		vkMap[53] = KeyEvent.VK_EXCLAMATION_MARK;
		vkMap[54] = KeyEvent.VK_SHIFT;
		vkMap[55] = KeyEvent.VK_CONTROL;
		vkMap[56] = KeyEvent.VK_ALT;
		vkMap[57] = KeyEvent.VK_SPACE;
		vkMap[58] = KeyEvent.VK_CAPS_LOCK;
		vkMap[59] = KeyEvent.VK_F1;
		vkMap[60] = KeyEvent.VK_F2;
		vkMap[61] = KeyEvent.VK_F3;
		vkMap[62] = KeyEvent.VK_F4;
		vkMap[63] = KeyEvent.VK_F5;
		vkMap[64] = KeyEvent.VK_F6;
		vkMap[65] = KeyEvent.VK_F7;
		vkMap[66] = KeyEvent.VK_F8;
		vkMap[67] = KeyEvent.VK_F9;
		vkMap[68] = KeyEvent.VK_F10;
		vkMap[69] = KeyEvent.VK_NUM_LOCK;
		vkMap[70] = KeyEvent.VK_SCROLL_LOCK;
		vkMap[71] = KeyEvent.VK_NUMPAD7;
		vkMap[72] = KeyEvent.VK_NUMPAD8;
		vkMap[73] = KeyEvent.VK_NUMPAD9;
		vkMap[74] = KeyEvent.VK_SUBTRACT;
		vkMap[75] = KeyEvent.VK_NUMPAD4;
		vkMap[76] = KeyEvent.VK_NUMPAD5;
		vkMap[77] = KeyEvent.VK_NUMPAD6;
		vkMap[78] = KeyEvent.VK_ADD;
		vkMap[79] = KeyEvent.VK_NUMPAD1;
		vkMap[80] = KeyEvent.VK_NUMPAD2;
		vkMap[81] = KeyEvent.VK_NUMPAD3;
		vkMap[82] = KeyEvent.VK_NUMPAD0;
		vkMap[83] = KeyEvent.VK_DECIMAL;
		vkMap[84] = KeyEvent.VK_PRINTSCREEN;
		vkMap[86] = KeyEvent.VK_LESS;
		vkMap[87] = KeyEvent.VK_F11;
		vkMap[88] = KeyEvent.VK_F12;
		
		vkMap[96] = KeyEvent.VK_INSERT;
		vkMap[97] = KeyEvent.VK_HOME;
		vkMap[98] = KeyEvent.VK_PAGE_UP;
		vkMap[99] = KeyEvent.VK_DIVIDE;
		vkMap[100] = KeyEvent.VK_PRINTSCREEN;
		vkMap[101] = KeyEvent.VK_DELETE;
		vkMap[102] = KeyEvent.VK_END;
		vkMap[103] = KeyEvent.VK_PAGE_DOWN;
		vkMap[104] = KeyEvent.VK_UP;
		vkMap[105] = KeyEvent.VK_SEPARATOR;
		vkMap[111] = KeyEvent.VK_SLASH;
		vkMap[112] = KeyEvent.VK_CONTROL;
		vkMap[113] = KeyEvent.VK_LEFT;
		vkMap[114] = KeyEvent.VK_DOWN;
		vkMap[115] = KeyEvent.VK_RIGHT;
		vkMap[116] = KeyEvent.VK_PAUSE;
		
		lcharMap[2] = '&';
		lcharMap[3] = 'é';
		lcharMap[4] = '"';
		lcharMap[5] = '\'';
		lcharMap[6] = '(';
		lcharMap[7] = '-';
		lcharMap[8] = 'è';
		lcharMap[9] = '_';
		lcharMap[10] = 'ç';
		lcharMap[11] = 'à';
		lcharMap[12] = ')';
		lcharMap[13] = '=';
		lcharMap[14] = '\b';
		lcharMap[15] = '\t';
		lcharMap[16] = 'a';
		lcharMap[17] = 'z';
		lcharMap[18] = 'e';
		lcharMap[19] = 'r';
		lcharMap[20] = 't';
		lcharMap[21] = 'y';
		lcharMap[22] = 'u';
		lcharMap[23] = 'i';
		lcharMap[24] = 'o';
		lcharMap[25] = 'p';
		lcharMap[26] = '^';
		lcharMap[27] = '$';
		lcharMap[28] = '\n';
		lcharMap[30] = 'q';
		lcharMap[31] = 's';
		lcharMap[32] = 'd';
		lcharMap[33] = 'f';
		lcharMap[34] = 'g';
		lcharMap[35] = 'h';
		lcharMap[36] = 'j';
		lcharMap[37] = 'k';
		lcharMap[38] = 'l';
		lcharMap[39] = 'm';
		lcharMap[40] = 'ù';
		lcharMap[41] = '²';
		lcharMap[43] = '*';
		lcharMap[44] = 'w';
		lcharMap[45] = 'x';
		lcharMap[46] = 'c';
		lcharMap[47] = 'v';
		lcharMap[48] = 'b';
		lcharMap[49] = 'n';
		lcharMap[50] = ',';
		lcharMap[51] = ';';
		lcharMap[52] = ':';
		lcharMap[53] = '!';
//		lcharMap[55] = ' ';
		lcharMap[57] = ' ';
		lcharMap[71] = '7';
		lcharMap[72] = '8';
		lcharMap[73] = '9';
		lcharMap[74] = '-';
		lcharMap[75] = '4';
		lcharMap[76] = '5';
		lcharMap[77] = '6';
		lcharMap[78] = '+';
		lcharMap[79] = '1';
		lcharMap[80] = '2';
		lcharMap[81] = '3';
		lcharMap[82] = '0';
		lcharMap[83] = '.';
		
		lcharMap[86] = '<';
		lcharMap[111] = '/';
		
		ucharMap[2] = '1';
		ucharMap[3] = '2';
		ucharMap[4] = '3';
		ucharMap[5] = '4';
		ucharMap[6] = '5';
		ucharMap[7] = '6';
		ucharMap[8] = '7';
		ucharMap[9] = '8';
		ucharMap[10] = '9';
		ucharMap[11] = '0';
		ucharMap[12] = '°';
		ucharMap[13] = '+';
		ucharMap[14] = '\b';
		ucharMap[15] = '\t';
		ucharMap[16] = 'A';
		ucharMap[17] = 'Z';
		ucharMap[18] = 'E';
		ucharMap[19] = 'R';
		ucharMap[20] = 'T';
		ucharMap[21] = 'Y';
		ucharMap[22] = 'U';
		ucharMap[23] = 'I';
		ucharMap[24] = 'O';
		ucharMap[25] = 'P';
		ucharMap[26] = 'ï';
		ucharMap[27] = '£';
		ucharMap[28] = '\n';
		ucharMap[30] = 'Q';
		ucharMap[31] = 'S';
		ucharMap[32] = 'D';
		ucharMap[33] = 'F';
		ucharMap[34] = 'G';
		ucharMap[35] = 'H';
		ucharMap[36] = 'J';
		ucharMap[37] = 'K';
		ucharMap[38] = 'L';
		ucharMap[39] = 'M';
		ucharMap[40] = '%';
		ucharMap[41] = '~';
		ucharMap[43] = 'µ';
		ucharMap[44] = 'W';
		ucharMap[45] = 'X';
		ucharMap[46] = 'C';
		ucharMap[47] = 'V';
		ucharMap[48] = 'B';
		ucharMap[49] = 'N';
		ucharMap[50] = '?';
		ucharMap[51] = '.';
		ucharMap[52] = '/';
		ucharMap[53] = '§';
//		ucharMap[55] = ' ';
		ucharMap[57] = ' ';
		ucharMap[71] = '7';
		ucharMap[72] = '8';
		ucharMap[73] = '9';
		ucharMap[74] = '-';
		ucharMap[75] = '4';
		ucharMap[76] = '5';
		ucharMap[77] = '6';
		ucharMap[78] = '+';
		ucharMap[79] = '1';
		ucharMap[80] = '2';
		ucharMap[81] = '3';
		ucharMap[82] = '0';
		ucharMap[83] = '.';
		
		ucharMap[86] = '>';
		ucharMap[111] = '/';
		
		altGrCharMap[41] = '¬';
		int i = 2;
		altGrCharMap[i++]  = '¹'; // 2
		altGrCharMap[i++]  = '~'; // 3
		altGrCharMap[i++]  = '#'; // 4
		altGrCharMap[i++]  = '{'; // 5
		altGrCharMap[i++]  = '['; // 6
		altGrCharMap[i++]  = '|'; // 7
		altGrCharMap[i++]  = '`'; // 8
		altGrCharMap[i++]  = '\\'; // 9
		altGrCharMap[i++]  = '^'; // 10
		altGrCharMap[i++]  = '@'; // 11
		altGrCharMap[i++]  = ']'; // 12
		altGrCharMap[i++]  = '}'; // 13
//		altGrCharMap[i++]  = ' ';// backspace do nothing
//		altGrCharMap[i++]  = ' ';// tab do nothing
		i+=2;
		altGrCharMap[i++]  = 'æ'; // 16
		altGrCharMap[i++]  = '«'; // 17
		altGrCharMap[i++]  = '¤'; // 18
		altGrCharMap[i++]  = '¶'; // 19
		altGrCharMap[i++]  = '?'; // 20
//		altGrCharMap[i++]  = ' '; // y -> do nothing
//		altGrCharMap[i++]  = ' '; // u -> do nothing
//		altGrCharMap[i++]  = ' '; // i -> do nothing
		i+=3;
		altGrCharMap[i++]  = 'ø'; // 24
		altGrCharMap[i++]  = 'þ'; // 25
//		altGrCharMap[i++]  = ' '; // dead ^ -> do nothing
		i++;
		altGrCharMap[i++]  = '?'; // 27
//		altGrCharMap[i++]  = ' '; // enter -> do nothing
//		altGrCharMap[i++]  = ' '; // Ctrl -> do nothing
		i+=2;
		altGrCharMap[i++]  = '@'; // 30
		altGrCharMap[i++]  = 'ß'; // 31
		altGrCharMap[i++]  = 'ð'; // 32
		altGrCharMap[i++]  = '?'; // 33
		altGrCharMap[i++]  = '?'; // 34
		altGrCharMap[i++]  = '?'; // 35
		altGrCharMap[i++]  = 'j'; // 36
//		altGrCharMap[i++]  = ' '; // k -> do nothing
		i++;
		altGrCharMap[i++]  = '?'; // 38
		altGrCharMap[i++]  = 'µ'; // 39
//		altGrCharMap[i++]  = ' '; // % -> do nothing
//		altGrCharMap[i++]  = ' '; // * -> do nothing
		i+=2;
		altGrCharMap[i++]  = '?'; // 42
		altGrCharMap[i++]  = '»'; // 43
		altGrCharMap[i++]  = '¢'; // 44
//		altGrCharMap[i++]  = ' '; // v -> do nothing
//		altGrCharMap[i++]  = ' '; // b -> do nothing
		i+=2;
		altGrCharMap[i++]  = 'n'; // 47
//		altGrCharMap[i++]  = ' '; // , -> do nothing
//		altGrCharMap[i++]  = ' '; // ; -> do nothing
		i+=2;
		altGrCharMap[i++]  = '·'; //  50
		altGrCharMap[i++]  = '?'; //  51
		altGrCharMap[i++]  = '?'; //  52
		
		altGrCharMap[86]   = '|';
		altGrCharMap[111]   = '/';
	}
*/
}



