/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.shell.bjorne;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnode.shell.ShellSyntaxException;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentSyntax;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.OptionSyntax;
import org.jnode.shell.syntax.OptionalSyntax;
import org.jnode.shell.syntax.RepeatSyntax;
import org.jnode.shell.syntax.SequenceSyntax;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * This class implements the 'read' built-in.
 * 
 * @author crawley@jnode.org
 */
final class ReadBuiltin extends BjorneBuiltin {
    private static final SyntaxBundle SYNTAX = 
        new SyntaxBundle("read", new SequenceSyntax(
                new OptionalSyntax(new OptionSyntax("noEscape", 'r')),
                new RepeatSyntax(new ArgumentSyntax("varName"), 0, Integer.MAX_VALUE)));
    
    static final Factory FACTORY = new Factory() {
        public BjorneBuiltinCommandInfo buildCommandInfo(BjorneContext context) {
            return new BjorneBuiltinCommandInfo("read", SYNTAX, new ReadBuiltin(context), context);
        }
    };
    
    
    private final VariableNameArgument varNameArg; 
    private final FlagArgument noEscapeArg = new FlagArgument(
            "noEscape", Argument.OPTIONAL, "if set, '\' does not escape a newline");
    
    private final BjorneContext context;
    Pattern ifsSplittingPattern;
    Pattern ifsTrimmingPattern;
    
    ReadBuiltin(BjorneContext context) {
        super("Read a line of input and repopulate the shell 'args'");
        this.context = context;
        varNameArg = new VariableNameArgument(
                "varName", context, Argument.OPTIONAL | Argument.MULTIPLE, "shell variables to be set");
        registerArguments(noEscapeArg, varNameArg);
    }

    public void execute() throws Exception {
        boolean escapeCheck = !noEscapeArg.isSet();
        String line = readLine(getInput().getReader(), escapeCheck);
        String[] varNames = varNameArg.getValues();
        if (varNames.length > 0) {
            String[] fields = extractFields(line, varNames.length);
            for (int i = 0; i < varNames.length; i++) {
                String value = (i >= fields.length || fields[i] == null) ? "" : fields[i];
                context.getParent().setVariable(varNames[i], value);
            }
        }
    }
    
    private String[] extractFields(String line, int nosVars) throws ShellSyntaxException {
        String ifs = context.variable("IFS");
        if (ifs == null) {
            ifs = " \t\n";
        } else if (ifs.length() == 0) {
            return new String[]{line};
        } 

        String[] fields = new String[nosVars];
        createIfsPatterns(ifs);
        String content;
        if (ifsTrimmingPattern != null) {
            Matcher trimMatcher = ifsTrimmingPattern.matcher(line);
            trimMatcher.matches();
            content = trimMatcher.group(1);
        } else {
            content = line;
        }
        if (line.length() == 0) {
            return new String[0];
        }
        Matcher fieldMatcher = null;
        for (int i = 0; i < fields.length - 1; i++) {
            if (fieldMatcher == null) {
                fieldMatcher = ifsSplittingPattern.matcher(content);
            } else {
                fieldMatcher.reset(content);
            }
            if (fieldMatcher.matches()) {
                fields[i] = fieldMatcher.group(1);
                content = fieldMatcher.group(2);
            } else {
                fields[i] = content;
                content = null;
                break;
            }
        }
        if (content != null) {
            fields[fields.length - 1] = content;
        }
        return fields;
    }

    private void createIfsPatterns(String ifs) {
        if (ifs.equals(" ") || ifs.equals("\t") || ifs.equals("\n")) {
            ifsTrimmingPattern = Pattern.compile("[ \\t\\n]*(.*[^ \\t\\n])[ \\t\\n]*");
            ifsSplittingPattern = Pattern.compile("([^ \\t\\n]+)[ \\t\\n]+([^ \\t\\n].*)");
        } else {
            // First separate the IFS into whitespace and non-whitespace characters,
            // adding '\' escapes for any that characters that need to be escaped.
            StringBuilder sb1 = new StringBuilder(4);
            StringBuilder sb2 = new StringBuilder(4);
            for (char ch : ifs.toCharArray()) {
                switch (ch) {
                    case ' ':
                        sb1.append(' ');
                        break;
                    case '\t':
                        sb1.append("\\t");
                        break;
                    case '\n':
                        sb1.append("\\n");
                        break;
                    case '.':
                    case '?':
                    case '*':
                    case '+':
                    case '[':
                    case ']':
                    case '(':
                    case ')':
                    case '|':
                    case '{':
                    case '}':
                    case '\\':
                    case '^':
                    case '$':
                    case '-':
                        sb2.append('\\').append(ch);
                        break;
                    default:
                        sb2.append("\\\n");
                        break;
                }
            }
            String ifsWhitespace = sb1.toString();
            String ifsNonWhitespace = sb2.toString();
            // If we have any IFS whitespace, create the pattern to trim it.
            if (ifsWhitespace.length() == 0) {
                ifsTrimmingPattern = null;
            } else {
                ifsTrimmingPattern = Pattern.compile(
                        "[" + ifsWhitespace + "]*(.*[^" + ifsWhitespace + "])[" + ifsWhitespace + "]*");
            }
            // Create the pattern to split a (possibly empty) field
            if (ifsWhitespace.length() > 0 && ifsNonWhitespace.length() > 0) {
                ifsSplittingPattern = Pattern.compile(
                        "([^" + ifsWhitespace + ifsNonWhitespace + "]*)[" + ifsWhitespace + "]*[" + 
                        ifsNonWhitespace + "][" + ifsWhitespace + "]*(|[^" + ifsWhitespace + "].*)");
            } else if (ifsWhitespace.length() > 0) {
                ifsSplittingPattern = Pattern.compile(
                        "([^" + ifsWhitespace + "]*)[" + ifsWhitespace + "]+(|[^" + ifsWhitespace + "].*)");
            } else {
                ifsSplittingPattern = Pattern.compile(
                        "([^" + ifsNonWhitespace + "]*)[" + ifsNonWhitespace + "](.*)");
            }
        }
    }

    private String readLine(Reader reader, boolean escapeCheck) throws IOException {
        StringBuilder sb = new StringBuilder(40);
        int ch;
        while ((ch = reader.read()) != -1 && ch != '\n') {
            if (ch == '\\' && escapeCheck) {
                ch = reader.read();
                if (ch == -1) {
                    sb.append('\\');
                    break;
                } else if (ch != '\n') {
                    sb.append('\\').append((char) ch);
                }
            } else {
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }
}
