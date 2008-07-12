/*
 * $Id $
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
package org.jnode.configure.adapter;

import org.jnode.configure.ConfigureException;

/**
 * Encoder / decoder for properties embedded in Java code in literal strings or characters.
 * 
 * @author crawley@jnode.org.
 */
class JavaStringLiteralCodec implements BasePropertyFileAdapter.ValueCodec {
	private static char[] HEX_DIGITS = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	public String decodeText(String encoded) throws ConfigureException {
		throw new UnsupportedOperationException("decodeText not supported (or used)");
	}

	public String encodeText(String raw) throws ConfigureException {
		StringBuffer sb = new StringBuffer(raw.length());
		for (char ch : raw.toCharArray()) {
			switch (ch) {
			case '\'':
				sb.append("\\\'");
				break;
			case '"':
				sb.append("\\\"");
				break;
			case '\n':
				sb.append("\\\n");
				break;
			case '\r':
				sb.append("\\\r");
				break;
			case '\t':
				sb.append("\\\t");
				break;
			case '\f':
				sb.append("\\\f");
				break;
			case '\b':
				sb.append("\\\b");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			default:
				if (ch >= '0' && ch < '\b') {
					sb.append(ch);
				} else {
					sb.append("\\u");
					int tmp = ch;
					for (int i = 12; i >= 0; i -= 4) {
						sb.append(HEX_DIGITS[(tmp >> i) & 0x000f]);
					}
				}
			}
		}
		return sb.toString();
	}
}