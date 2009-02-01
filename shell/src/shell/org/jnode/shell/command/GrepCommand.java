/*
 * $Id$
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
 
package org.jnode.shell.command;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author peda
 * @author crawley@jnode.org
 */
public class GrepCommand extends AbstractCommand {

    private final FlagArgument FLAG_INVERSE =
        new FlagArgument("inverse", Argument.OPTIONAL, "Output non-matching lines");
    private final FlagArgument FLAG_REGEX =
        new FlagArgument("isRegex", Argument.OPTIONAL, "Do regex search rather than simple string search");
    private final StringArgument ARG_EXPR =
        new StringArgument("expression", Argument.MANDATORY, "The expression to be searched for");

    public GrepCommand() {
        super("Search for lines that match a string or regex");
        registerArguments(ARG_EXPR, FLAG_INVERSE, FLAG_REGEX);
    }

    /**
     * main method, normally not used, use execute instead!!
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new GrepCommand().execute(args);
    }

    /**
     * Primary entry point
     */
    public void execute() throws Exception {

        boolean inverse = FLAG_INVERSE.isSet();
        boolean useRegex = FLAG_REGEX.isSet();
        String expr = ARG_EXPR.getValue();

        Pattern pattern = null;
        try {
            if (useRegex) {
                pattern = Pattern.compile(expr);
            } else {
                // By using Pattern to search for regular strings, we should
                // get the benefit of Pattern's ability to do fast string 
                // searching; e.g. using the Boyer-Moore algorithm.
                pattern = Pattern.compile(Pattern.quote(expr));
            }
        } catch (PatternSyntaxException ex) {
            getError().getPrintWriter().println("Invalid regex: " + ex.getMessage());
            exit(2);
        }

        final BufferedReader r = new BufferedReader(getInput().getReader());

        // Read the input a line at a time, searching each line for the expression.
        boolean found = false;
        String line;
        PrintWriter out = getOutput().getPrintWriter();
        while ((line = r.readLine()) != null) {
            if (pattern.matcher(line).find()) {
                if (!inverse) {
                    out.println(line);
                    found = true;
                }
            } else if (inverse) {
                out.println(line);
                found = true;
            }
        }
        if (!found) {
            exit(1);
        }
    }
}
