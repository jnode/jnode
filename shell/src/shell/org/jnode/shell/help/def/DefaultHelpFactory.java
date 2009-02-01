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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.jnode.shell.Command;
import org.jnode.shell.CommandInfo;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.SyntaxBundle;
import org.jnode.shell.syntax.SyntaxManager;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 * @author crawley@jnode.org
 */
public class DefaultHelpFactory extends HelpFactory {
    public static final String RESOURCE_NAME = "messages.properties";
    private static final int NOMINAL_WIDTH = 75;
    /* start with 80 spaces ... */
    private static String spaces =
        "                                                                                ";

    /**
     * Create a new instance
     */
    public DefaultHelpFactory() {
    }
    
    @Override
    public Help getHelp(String alias, CommandInfo cmdInfo) throws HelpException {
        SyntaxBundle syntaxes = null;
        ArgumentBundle bundle = null;
        try {
            final Shell shell = ShellUtils.getShellManager().getCurrentShell();
            final SyntaxManager syntaxManager = shell.getSyntaxManager();
            Command cmd = cmdInfo.createCommandInstance();
            if (cmd != null) {
                bundle = cmd.getArgumentBundle();
                syntaxes = syntaxManager.getSyntaxBundle(alias);
                if (syntaxes == null) {
                    syntaxes = new SyntaxBundle(alias, bundle.createDefaultSyntax());
                }
            } 
        } catch (Exception ex) {
            throw new HelpException(ex.getMessage(), ex);
        }

        if (syntaxes != null && bundle != null) {
            return new NewSyntaxHelp(syntaxes, bundle);
        } else {
            return null;
        }
    }

    @Override
    public void help(SyntaxBundle syntaxes, ArgumentBundle bundle, PrintWriter out) {
        usage(syntaxes, bundle, out);
        if (bundle.getDescription() != null) {
            out.println("\n" + HelpFactory.getLocalizedHelp("help.description") + ":");
            format(out, new Cell[]{new Cell(4, NOMINAL_WIDTH - 4)},
                new String[]{bundle.getDescription()});
        }
        Map<String, TreeSet<String>> flagMap = buildFlagMap(syntaxes);
        boolean first = true;
        for (org.jnode.shell.syntax.Argument<?> arg : bundle) {
            if (arg instanceof FlagArgument) {
                if (first) {
                    out.println("\n" + HelpFactory.getLocalizedHelp("help.options") + ":");
                    first = false;
                }
                describeOption((FlagArgument) arg, flagMap.get(arg.getLabel()), out);
            }
        }
        first = true;
        for (org.jnode.shell.syntax.Argument<?> arg : bundle) {
            if (!(arg instanceof FlagArgument)) {
                if (first) {
                    out.println("\n" + HelpFactory.getLocalizedHelp("help.parameters") + ":");
                    first = false;
                }
                describeArgument(arg, out);
            }
        }
    }

    private Map<String, TreeSet<String>> buildFlagMap(SyntaxBundle syntaxes) {
        HashMap<String, TreeSet<String>> res = new HashMap<String, TreeSet<String>>();
        for (org.jnode.shell.syntax.Syntax syntax : syntaxes.getSyntaxes()) {
            buildFlagMap(syntax, res);
        }
        return res;
    }

    private void buildFlagMap(org.jnode.shell.syntax.Syntax syntax,
                              HashMap<String, TreeSet<String>> res) {
        if (syntax instanceof OptionSyntax) {
            OptionSyntax os = (OptionSyntax) syntax;
            String key = os.getArgName();
            TreeSet<String> options = res.get(key);
            if (options == null) {
                options = new TreeSet<String>();
                res.put(key, options);
            }
            String shortOptName = os.getShortOptName();
            if (shortOptName != null) {
                options.add(shortOptName);
            }
            String longOptName = os.getLongOptName();
            if (longOptName != null) {
                options.add(longOptName);
            }
        } else {
            for (org.jnode.shell.syntax.Syntax child : syntax.getChildren()) {
                buildFlagMap(child, res);
            }
        }
    }

    @Override
    public void usage(SyntaxBundle syntaxBundle, ArgumentBundle bundle, PrintWriter out) {
        String command = syntaxBundle.getAlias();
        String usageText = HelpFactory.getLocalizedHelp("help.usage") + ":";
        int usageLength = usageText.length();
        int commandLength = command.length();
        Cell[] cells =
            new Cell[]{new Cell(0, usageLength), new Cell(1, commandLength),
                new Cell(1, NOMINAL_WIDTH - 2 - usageLength - commandLength)};
        String[] texts = new String[]{usageText, command, null};
        String[] texts2 = new String[]{"", "", null};
        org.jnode.shell.syntax.Syntax[] syntaxes = syntaxBundle.getSyntaxes();
        if (syntaxes.length > 0) {
            for (int i = 0; i < syntaxes.length; i++) {
                if (i == 1) {
                    texts[0] = getSpaces(usageLength);
                }
                texts[2] = syntaxes[i].format(bundle);
                format(out, cells, texts);
                texts2[2] = syntaxes[i].getDescription();
                format(out, cells, texts2);
            }
        } else {
            texts[2] = "";
            format(out, cells, texts);
        }
    }
    
    @Override
    public void describeArgument(org.jnode.shell.syntax.Argument<?> arg, PrintWriter out) {
        String description = "(" + arg.getTypeDescription() + ") " + arg.getDescription();
        format(out, new Cell[]{new Cell(4, 16), new Cell(2, NOMINAL_WIDTH - 22)},
            new String[]{"<" + arg.getLabel() + ">", description});
    }

    @Override
    public void describeOption(FlagArgument arg, TreeSet<String> flagTokens, PrintWriter out) {
        StringBuffer sb = new StringBuffer();
        for (String flagToken : flagTokens) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(flagToken);
        }
        format(out, new Cell[]{new Cell(4, 16), new Cell(2, NOMINAL_WIDTH - 22)},
            new String[]{sb.toString(), arg.getDescription()});
    }

    protected void format(PrintWriter out, Cell[] cells, String[] texts) {
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
    protected static class Cell {

        final String field;
        final int margin;
        final int width;

        /**
         * Construct a Cell with a leading margin and a text width.
         *
         * @param margin the number of leading spaces for the Cell
         * @param width  the width of the text part of the Cell
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
