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

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.syntax.ArgumentBundle;

/**
 * @author qades
 * @author Fabien DUMINY (fduminy@jnode.org)
 */
public class DefaultHelp extends Help {
	public static final String RESOURCE_NAME = "messages.properties";
	
    /**
     * Create a new instance
     */
    public DefaultHelp() {
    }

    /**
     * Shows the complete help for a command.
     * @see Help#help(org.jnode.shell.help.Help.Info, String) 
     */
    public void help(Info info, String command) {
        final Syntax[] syntaxes = info.getSyntaxes();
        final String name = command == null ? info.getName() : command;
        for (int i = 0; i < syntaxes.length; i++) {
            help(name, syntaxes[i]);
            if (i < syntaxes.length)
                System.out.println();
        }
    }

    @Override
    public void help(org.jnode.shell.syntax.Syntax syntax,
            ArgumentBundle bundle, String command) {
        usage(syntax, bundle, command);

        boolean first = true;
        for (org.jnode.shell.syntax.Argument<?> arg : bundle) {
            if (first) {
                System.out.println("\n" + Help.getLocalizedHelp("help.parameters") + ":");
                first = false;
            }
            describeArgument(arg);
        }
    }

    /**
     * Shows the help for a command syntax.
     */
    public void help(String name, Syntax syntax) {
        usage(name, syntax);

        final Parameter[] params = syntax.getParams();
        if (params.length != 0)
            System.out.println("\n" + Help.getLocalizedHelp("help.parameters") + ":");
        for (int i = 0; i < params.length; i++)
            params[i].describe(this);
    }

    /**
     * Shows the usage information of a command.
     */
    public void usage(Info info) {
        final Syntax[] syntaxes = info.getSyntaxes();
        for (int i = 0; i < syntaxes.length; i++)
            usage(info.getName(), syntaxes[i]);
    }

    /**
     * Shows the usage information of a command.
     */
    public void usage(String name, Syntax syntax) {
        StringBuilder line = new StringBuilder(name);
        final Parameter[] params = syntax.getParams();
        for (int i = 0; i < params.length; i++)
            line.append(' ').append(params[i].format());
        System.out.println(Help.getLocalizedHelp("help.usage") + ": " + line);
        format(new Cell[]{new Cell(4, 54)}, new String[]{syntax.getDescription()});
    }

    @Override
    public void usage(org.jnode.shell.syntax.Syntax syntax,
            ArgumentBundle bundle, String command) {
        System.out.println(Help.getLocalizedHelp("help.usage") + ": " + 
                command + syntax.format(bundle));
        format(new Cell[]{new Cell(4, 54)}, new String[]{bundle.getDescription()});
    }
    
    public void describeParameter(Parameter param) {
        format(new Cell[]{new Cell(2, 18), new Cell(2, 53)}, 
                new String[]{param.getName(), param.getDescription()});
    }

    public void describeArgument(Argument arg) {
        format(new Cell[]{new Cell(4, 16), new Cell(2, 53)},
                new String[]{arg.getName(), arg.getDescription()});
    }

    @Override
    public void describeArgument(org.jnode.shell.syntax.Argument<?> arg) {
        format(new Cell[]{new Cell(4, 16), new Cell(2, 53)},
                new String[]{arg.getLabel(), arg.getDescription()});
    }

    protected void format(Cell[] cells, String[] texts) {
        if (cells.length != texts.length)
            throw new IllegalArgumentException("Number of cells and texts must match");

        String[] remains = new String[texts.length];
        for (int i = 0; i < texts.length; i++)
            remains[i] = texts[i];

        StringBuilder result = new StringBuilder();
        while (true) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < cells.length; i++) {
                String field = cells[i].fit(remains[i]);
                remains[i] = remains[i].substring(field.length());
                buf.append(cells[i].stamp(field.trim()));
            }
            String line = buf.toString();
            if (line.trim().length() == 0)
                break;
            result.append(line).append('\n');
        }
        System.out.print(result.toString());
    }

    protected class Cell {
        final String field;
        final int margin;
        final int width;

        Cell(int margin, int width) {
            this.margin = margin;
            this.width = width;

            // for performance, we pre-build the field mask
            StringBuilder field = new StringBuilder();
            for (int i = 0; i < margin + width; i++)
                field.insert(0, ' ');
            this.field = field.toString();
        }

        String fit(String text) {
            String hardFit = text.substring(0, Math.min(width, text.length()));
            int lastSpace = hardFit.lastIndexOf(" ");
            return hardFit.substring(0, 
                    (hardFit.endsWith(" ") && (lastSpace > width / 4) ? lastSpace : hardFit.length()));
        }

        String stamp(String text) {
            if (text.length() > field.length())
                throw new IllegalArgumentException("Text length exceeds field width");
            return field.substring(0, margin) + text + field.substring(0, width - text.length());
        }
    }


}
