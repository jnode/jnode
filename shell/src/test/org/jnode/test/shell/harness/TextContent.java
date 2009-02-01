/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.test.shell.harness;

public class TextContent {
    private final String rawText;
    private final boolean trim;
    private final String processedText;

    public TextContent(String rawText, boolean trim) {
        super();
        this.rawText = rawText;
        this.trim = trim;
        this.processedText = trim ? processText(rawText) : rawText;
    }

    public String getRawText() {
        return rawText;
    }

    public boolean isTrim() {
        return trim;
    }

    @Override
    public String toString() {
        return processedText;
    }

    public boolean matches(String actual) {
        return trim ? processedText.equals(detab(actual)) : rawText.equals(actual);
    }

    /**
     * The processing performed is as follows:
     * <ol>
     * <li>All TABs are expanded
     * <li>If the first character is a newline it is removed.
     * <li>Trim the last line if it consists of spaces without a final newline.
     * <li>Count the number of leading spaces on the (now) first line.
     * <li>Remove this number of leading spaces from all lines.
     * <li>Make sure that the last line has a final newline.
     * </ol>
     *
     * @param text
     * @return
     */
    private String processText(String text) {
        String tmp = detab(text);
        if (tmp.length() > 0 && tmp.charAt(0) == '\n') {
            tmp = tmp.substring(1);
        }
        if (tmp.charAt(tmp.length() - 1) != '\n') {
            for (int i = tmp.length() - 1; i >= 0; i--) {
                char ch = tmp.charAt(i);
                if (ch == '\n') {
                    tmp = tmp.substring(0, i);
                    break;
                } else if (ch != ' ') {
                    break;
                }
            }
        }
        int count;
        int len = tmp.length();
        for (count = 0; count < len && tmp.charAt(count) == ' '; count++) {
             /**/
        }
        if (count > 0) {
            StringBuilder sb = new StringBuilder(len);
            int pos = 0;
            for (int i = 0; i < len; i++) {
                char ch = tmp.charAt(i);
                switch (ch) {
                    case ' ':
                        if (pos++ >= count) {
                            sb.append(' ');
                        }
                        break;
                    case '\n':
                        sb.append('\n');
                        pos = 0;
                        break;
                    default:
                        pos = count + 1;
                        sb.append(ch);
                }
            }
            tmp = sb.toString();
        }
        if (tmp.charAt(tmp.length() - 1) != '\n') {
            tmp += '\n';
        }
        return tmp;
    }

    private String detab(String text) {
        final int len = text.length();
        StringBuilder sb = new StringBuilder(len);
        int count = 0;
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            if (ch == '\t') {
                sb.append(' ');
                while (count++ % 8 != 0) {
                    sb.append(' ');
                }
            } else if (ch == '\n') {
                count = 0;
                sb.append('\n');
            } else {
                count++;
                sb.append(ch);
            }
        }
        return sb.toString();
    }

}
