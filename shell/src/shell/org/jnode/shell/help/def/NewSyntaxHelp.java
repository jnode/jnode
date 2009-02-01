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

import org.jnode.shell.help.EnhancedHelp;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This Help implementation provides for 'new' syntax commands in plain text format.
 * 
 * @author crawley@jnode.org
 */
public class NewSyntaxHelp extends TextHelpBase implements EnhancedHelp {

    private final SyntaxBundle syntaxBundle;
    private final ArgumentBundle argBundle;
    
    public NewSyntaxHelp(SyntaxBundle syntaxBundle, ArgumentBundle argBundle) {
        this.syntaxBundle = syntaxBundle;
        this.argBundle = argBundle;
    }
    
    @Override
    public void help(PrintWriter pw) {
        usage(pw);
        options(pw);
        arguments(pw);
    }
    
    @Override
    public void description(PrintWriter pw) {
        if (argBundle.getDescription() != null) {
            pw.println("\n" + HelpFactory.getLocalizedHelp("help.description") + ":");
            format(pw, new TextCell[]{new TextCell(4, NOMINAL_WIDTH - 4)},
                new String[]{argBundle.getDescription()});
        }
    }

    @Override
    public void arguments(PrintWriter pw) {
        boolean first = true;
        for (Argument<?> arg : argBundle) {
            if (!(arg instanceof FlagArgument)) {
                if (first) {
                    pw.println("\n" + HelpFactory.getLocalizedHelp("help.parameters") + ":");
                    first = false;
                }
                describeArgument(arg, pw);
            }
        }
    }

    @Override
    public void options(PrintWriter pw) {
        Map<String, TreeSet<String>> flagMap = buildFlagMap(syntaxBundle);
        boolean first = true;
        for (Argument<?> arg : argBundle) {
            if (arg instanceof FlagArgument) {
                if (first) {
                    pw.println("\n" + HelpFactory.getLocalizedHelp("help.options") + ":");
                    first = false;
                }
                describeOption((FlagArgument) arg, flagMap.get(arg.getLabel()), pw);
            }
        }
    }

    @Override
    public void usage(PrintWriter pw) {
        String command = syntaxBundle.getAlias();
        String usageText = HelpFactory.getLocalizedHelp("help.usage") + ":";
        int usageLength = usageText.length();
        int commandLength = command.length();
        TextCell[] cells =
            new TextCell[]{new TextCell(0, usageLength), new TextCell(1, commandLength),
                new TextCell(1, NOMINAL_WIDTH - 2 - usageLength - commandLength)};
        String[] texts = new String[]{usageText, command, null};
        String[] texts2 = new String[]{"", "", null};
        org.jnode.shell.syntax.Syntax[] syntaxes = syntaxBundle.getSyntaxes();
        if (syntaxes.length > 0) {
            for (int i = 0; i < syntaxes.length; i++) {
                if (i == 1) {
                    texts[0] = getSpaces(usageLength);
                }
                texts[2] = syntaxes[i].format(argBundle);
                format(pw, cells, texts);
                texts2[2] = syntaxes[i].getDescription();
                format(pw, cells, texts2);
            }
        } else {
            texts[2] = "";
            format(pw, cells, texts);
        }
    }

    @Override
    public void details(PrintWriter pw) {
        // TODO Auto-generated method stub

    }

    public void describeArgument(Argument<?> arg, PrintWriter out) {
        String description = "(" + arg.getTypeDescription() + ") " + arg.getDescription();
        format(out, new TextCell[]{new TextCell(4, 16), new TextCell(2, NOMINAL_WIDTH - 22)},
            new String[]{"<" + arg.getLabel() + ">", description});
    }

    public void describeOption(FlagArgument arg, TreeSet<String> flagTokens, PrintWriter out) {
        StringBuffer sb = new StringBuffer();
        for (String flagToken : flagTokens) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append(flagToken);
        }
        format(out, new TextCell[]{new TextCell(4, 16), new TextCell(2, NOMINAL_WIDTH - 22)},
            new String[]{sb.toString(), arg.getDescription()});
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
}
