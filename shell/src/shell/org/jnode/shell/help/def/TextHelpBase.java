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
 
package org.jnode.shell.help.def;

import java.io.PrintWriter;

/**
 * This base class for the text-mode help implementation classes provides some simple
 * helper classes and methods for laying out text.
 * 
 * @author crawley@jnode.org
 */
public abstract class TextHelpBase {
    
    public static final String RESOURCE_NAME = "messages.properties";
    
    // FIXME ...
    static final int NOMINAL_WIDTH = 75;
    
    /* start with 80 spaces ... */
    private static String spaces =
        "                                                                                ";

    protected void format(PrintWriter out, TextCell[] cells, String[] texts) {
        if (cells.length != texts.length) {
            throw new IllegalArgumentException("Number of cells and texts must match");
        }
        // The text remaining to be formatted for each column.
        String[] remains = new String[texts.length];
        // The total count of characters remaining
        int remainsCount = 0;
        // Initialize 'remains' and 'remainsCount' for the first iteration
        for (int i = 0; i < texts.length; i++) {
            remains[i] = (texts[i] == null) ? "" : texts[i].trim();
            remainsCount += remains[i].length();
        }

        StringBuilder result = new StringBuilder();
        // Repeat while there is still text to output.
        while (remainsCount > 0) {
            // Each iteration uses 'fit' to get up to 'cell.width' characters from each column
            // and then uses 'stamp' to append to them to the buffer with the leading margin 
            // and trailing padding as required.
            remainsCount = 0;
            for (int i = 0; i < cells.length; i++) {
                String field = cells[i].fit(remains[i]);
                remains[i] = remains[i].substring(field.length());
                remainsCount += remains[i].length();
                result.append(cells[i].stamp(field.trim()));
            }
            result.append('\n');
        }
        out.print(result.toString());
    }

    /**
     * Get a String consisting of 'count' spaces.
     *
     * @param count the number of spaces
     * @return the string
     */
    protected static String getSpaces(int count) {
        // The following assumes that 1) StringBuilder.append is efficient if you
        // preallocate the StringBuilder, 2) StringBuilder.toString() does no character 
        // copying, and 3) String.substring(...) also does no character copying.
        int len = spaces.length();
        if (count > len) {
            StringBuilder sb = new StringBuilder(count);
            for (int i = 0; i < count; i++) {
                sb.append(' ');
            }
            spaces = sb.toString();
            return spaces;
        } else if (count == len) {
            return spaces;
        } else {
            return spaces.substring(0, count);
        }
    }

    /**
     * A Cell is a template for formatting text for help messages.  (It is 'protected' so that
     * the unit test can declare a subclass ...)
     */
    protected static class TextCell {

        final String field;
        final int margin;
        final int width;

        /**
         * Construct a Cell with a leading margin and a text width.
         *
         * @param margin the number of leading spaces for the Cell
         * @param width  the width of the text part of the Cell
         */
        protected TextCell(int margin, int width) {
            this.margin = margin;
            this.width = width;

            // for performance, we pre-build the field mask
            this.field = getSpaces(margin + width);
        }

        /**
         * Heuristically, split of a head substring of 'text' to fit within this Cell's width.  We try
         * to split at a space character, but if this will make the text too ragged, we simply chop.
         */
        protected String fit(String text) {
            if (width >= text.length()) {
                return text;
            }
            String hardFit = text.substring(0, width);
            if (hardFit.endsWith(" ")) {
                return hardFit;
            }
            int lastSpace = hardFit.lastIndexOf(' ');
            if (lastSpace > 3 * width / 4) {
                return hardFit.substring(0, lastSpace);
            } else {
                return hardFit;
            }
        }

        /**
         * Stamp out a line with leading and trailing spaces to fill the Cell.
         */
        protected String stamp(String text) {
            if (text.length() > field.length())
                throw new IllegalArgumentException("Text length exceeds field width");
            return field.substring(0, margin) + text + field.substring(0, width - text.length());
        }
    }
}
