/**
 * KeyboardInterpreter_FR.java
 *
 * @author Created by Marc DENTY on Feb 01 2004
 * @since 0.15
 */

package org.jnode.driver.input.l10n;
import org.jnode.driver.input.KeyboardInterpreter;
import java.awt.event.KeyEvent;



public class KeyboardInterpreter_FR extends KeyboardInterpreter {
	
	/**
	 * Initialize the mapping between scancode and virtual key code.
	 * @param vkMap
	 */
	protected void initVkMap(int[] vkMap, char[] lcharMap, char[] ucharMap, char[] altGrCharMap) {
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
		vkMap[111] = KeyEvent.VK_FINAL;
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
		
		altGrCharMap[41] = '¬';
		int i = 2;
		altGrCharMap[i++]  = '¹';
		altGrCharMap[i++]  = '~';
		altGrCharMap[i++]  = '#';
		altGrCharMap[i++]  = '{';
		altGrCharMap[i++]  = '[';
		altGrCharMap[i++]  = '|';
		altGrCharMap[i++]  = '`';
		altGrCharMap[i++]  = '\\';
		altGrCharMap[i++]  = '^';
		altGrCharMap[i++]  = '@';
		altGrCharMap[i++]  = ']';
		altGrCharMap[i++]  = '}';
//		altGrCharMap[i++]  = ' ';// backspace do nothing
//		altGrCharMap[i++]  = ' ';// tab do nothing
		i+=2;
		altGrCharMap[i++]  = 'æ';
		altGrCharMap[i++]  = '«';
		altGrCharMap[i++]  = '¤';
		altGrCharMap[i++]  = '¶';
		altGrCharMap[i++]  = '?';
//		altGrCharMap[i++]  = ' '; // y -> do nothing
//		altGrCharMap[i++]  = ' '; // u -> do nothing
//		altGrCharMap[i++]  = ' '; // i -> do nothing
		i+=3;
		altGrCharMap[i++]  = 'ø';
		altGrCharMap[i++]  = 'þ';
//		altGrCharMap[i++]  = ' '; // dead ^ -> do nothing
		i++;
		altGrCharMap[i++]  = '?';
//		altGrCharMap[i++]  = ' '; // enter -> do nothing
//		altGrCharMap[i++]  = ' '; // Ctrl -> do nothing
		i+=2;
		altGrCharMap[i++]  = '@';
		altGrCharMap[i++]  = 'ß';
		altGrCharMap[i++]  = 'ð';
		altGrCharMap[i++]  = '?';
		altGrCharMap[i++]  = '?';
		altGrCharMap[i++]  = '?';
		altGrCharMap[i++]  = 'j';
//		altGrCharMap[i++]  = ' '; // k -> do nothing
		i++;
		altGrCharMap[i++]  = '?';
		altGrCharMap[i++]  = 'µ';
//		altGrCharMap[i++]  = ' '; // % -> do nothing
//		altGrCharMap[i++]  = ' '; // * -> do nothing
		i+=2;
		altGrCharMap[i++]  = '?';
		altGrCharMap[i++]  = '»';
		altGrCharMap[i++]  = '¢';
//		altGrCharMap[i++]  = ' '; // v -> do nothing
//		altGrCharMap[i++]  = ' '; // b -> do nothing
		i+=2;
		altGrCharMap[i++]  = 'n';
//		altGrCharMap[i++]  = ' '; // , -> do nothing
//		altGrCharMap[i++]  = ' '; // ; -> do nothing
		i+=2;
		altGrCharMap[i++]  = '·';
		altGrCharMap[i++]  = '?';
		altGrCharMap[i++]  = '?';

		altGrCharMap[86]   = '|';
	}
}

