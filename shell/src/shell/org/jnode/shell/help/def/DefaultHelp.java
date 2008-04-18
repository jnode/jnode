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
 
package org.jnode.shell.help.def;

import java.io.PrintStream;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public class DefaultHelp extends Help {
	public static final String RESOURCE_NAME = "messages.properties";
	private static final int NOMINAL_WIDTH = 75;
	private static String spaces = // start with 80 ...
        "                                                                                ";
	
    /**
     * Create a new instance
     */
    public DefaultHelp() {
    }

    /**
     * Shows the complete help for a command.
     * @see Help#help(org.jnode.shell.help.Help.Info, String) 
     */
    public void help(Info info, String command, PrintStream out) {
        final Syntax[] syntaxes = info.getSyntaxes();
        final String name = command == null ? info.getName() : command;
        for (int i = 0; i < syntaxes.length; i++) {
            help(name, syntaxes[i], out);
            if (i < syntaxes.length) {
                out.println();
            }
        }
    }

    @Override
    public void help(SyntaxBundle syntaxes, ArgumentBundle bundle, PrintStream out) {
        usage(syntaxes, bundle, out);
        if (bundle.getDescription() != null) {
            out.println("\n" + Help.getLocalizedHelp("help.description") + ":");
            format(out, new Cell[]{new Cell(4, NOMINAL_WIDTH - 4)}, 
                    new String[]{bundle.getDescription()});
        }
        boolean first = true;
        for (org.jnode.shell.syntax.Argument<?> arg : bundle) {
            if (first) {
                out.println("\n" + Help.getLocalizedHelp("help.parameters") + ":");
                first = false;
            }
            describeArgument(arg, out);
        }
    }

    /**
     * Shows the help for a command syntax.
     */
    public void help(String name, Syntax syntax, PrintStream out) {
        usage(name, syntax, out);
        if (syntax.getDescription() != null) {
            out.println("\n" + Help.getLocalizedHelp("help.description") + ":");
            format(out, new Cell[]{new Cell(4, NOMINAL_WIDTH - 4)},
                    new String[]{syntax.getDescription()});
        }
        final Parameter[] params = syntax.getParams();
        if (params.length != 0) {
            out.println("\n" + Help.getLocalizedHelp("help.parameters") + ":");
            for (int i = 0; i < params.length; i++) {
                params[i].describe(this, out);
            }
        }
    }

    /**
     * Shows the usage information of a command.
     */
    public void usage(Info info, PrintStream out) {
        final Syntax[] syntaxes = info.getSyntaxes();
        for (int i = 0; i < syntaxes.length; i++) {
            usage(info.getName(), syntaxes[i], out);
        }
    }

    /**
     * Shows the usage information of a command.
     */
    public void usage(String name, Syntax syntax, PrintStream out) {
        StringBuilder line = new StringBuilder(name);
        final Parameter[] params = syntax.getParams();
        for (int i = 0; i < params.length; i++) {
            line.append(' ').append(params[i].format());
        }
        out.println(Help.getLocalizedHelp("help.usage") + ": " + line);
    }

    @Override
    public void usage(SyntaxBundle syntaxBundle, ArgumentBundle bundle, PrintStream out) {
        String command = syntaxBundle.getAlias();
        String usageText = Help.getLocalizedHelp("help.usage") + ":";
        int usageLength = usageText.length();
        int commandLength = command.length();
        Cell[] cells = new Cell[]{
                new Cell(0, usageLength), 
                new Cell(1, commandLength), 
                new Cell(1, NOMINAL_WIDTH - 2 - usageLength - commandLength)};
        String[] texts = new String[]{usageText, command, null};
        String[] texts2 = new String[]{"", "", null};
        org.jnode.shell.syntax.Syntax[] syntaxes = syntaxBundle.getSyntaxes();
        for (int i = 0; i < syntaxes.length; i++) {
            if (i == 1) {
                texts[0] = getSpaces(usageLength);
            }
            texts[2] = syntaxes[i].format(bundle);
            format(out, cells, texts);
            texts2[2] = syntaxes[i].getDescription();
            format(out, cells, texts2);
        }
    }
    
    public void describeParameter(Parameter param, PrintStream out) {
        format(out, new Cell[]{new Cell(2, 18), new Cell(2, NOMINAL_WIDTH - 22)}, 
                new String[]{param.getName(), param.getDescription()});
    }

    public void describeArgument(Argument arg, PrintStream out) {
        format(out, new Cell[]{new Cell(4, 16), new Cell(2, NOMINAL_WIDTH - 22)},
                new String[]{arg.getName(), arg.getDescription()});
    }

    @Override
    public void describeArgument(org.jnode.shell.syntax.Argument<?> arg, PrintStream out) {
        format(out, new Cell[]{new Cell(4, 16), new Cell(2, NOMINAL_WIDTH - 22)},
                new String[]{arg.getLabel(), arg.getDescription()});
    }

    protected void format(PrintStream out, Cell[] cells, String[] texts) {
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
     * @param count the number of spaces
     * @return the string
     */
    private static String getSpaces(int count) {
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
        }
        else if (count == len) {
            return spaces;
        }
        else {
            return spaces.substring(0, count);
        }
    }

    /**
     * A Cell is a template for formatting text for help messages.  (It is 'protected' so that
     * the unit test can declare a subclass ...)
     */
    protected static class Cell {
        
        final String field;
        final int margin;
        final int width;

        /**
         * Construct a Cell with a leading margin and a text width.
         * @param margin the number of leading spaces for the Cell
         * @param width the width of the text part of the Cell
         */
        protected Cell(int margin, int width) {
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
            }
            else {
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
