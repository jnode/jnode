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


/**
 * Encode / decode for the context of text contents of an XML element.
 * 
 * @author crawley@jnode.org
 */
class XMLValueCodec implements BasePropertyFileAdapter.ValueCodec {
    public String encodeProperty(String propName, String propValue, String modifiers) {
        return propValue == null ? "" : encodeText(propValue);
    }

    public String getValidModifiers() {
        return "";
    }
    
    private String encodeText(String raw) {
        StringBuffer sb = new StringBuffer(raw.length());
        for (char ch : raw.toCharArray()) {
            switch (ch) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    // Theoretically we should throw exceptions for characters
                    // that are 'forbidden' by the XML specification; e.g. most
                    // control codes.
                    sb.append(ch);
            }
        }
        return sb.toString();
    }
}
