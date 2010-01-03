/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.command.argument;

import java.util.Arrays;

import org.jnode.command.util.NumberRange;
//import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.syntax.Argument;

/**
 * Captures a list of multiple number ranges.
 *
 * A number list is represented by a comma delimited list of integers
 * and ranges. A range is represented by two integers separated by a hyphen.
 * Either of the integers may be omitted (but not both), to represent a range
 * that spans from a single point, to the minimum or maximum values of the range.
 *
 * In other words, this argument captures the following syntax:
 * <code>
 * list ::= range | list,range
 * range ::= integer | -integer | integer- | integer-integer
 * <code>
 * Note that integer is any number in the range 0-Integer.MAX_VALUE, and may be limited
 * to a smaller range via constructor arguments.
 *
 * When a number list has been captured, it may be altered from the form and order
 * it was found in on the command line. The Command can be certain that the ranges
 * returned by this argument will have the following properties.
 *
 * <ul>
 * <li>Ranges will never span beyond min/max
 * <li>Ranges will be in sorted order.
 * <li>Individual ranges will not overlap, or be sequential. Such ranges will be concatenated
 *     to form a single range. This means that 3-5,4-7 and 3,4,5,6,7 will form the range 3-7.
 * </ul>
 *
 * @author chris boertien
 */
public class NumberListArgument extends Argument<NumberRange> {
    
    private final String listDelim;
    private final String rangeDelim;
    private final int min;
    private final int max;
    
    public NumberListArgument(String label, int flags, int min, int max, String desc) {
        super(label, flags | Argument.MULTIPLE, new NumberRange[0], desc);
        this.rangeDelim = "-";
        this.listDelim = ",";
        this.min = min;
        this.max = max;
    }
    
    public NumberListArgument(String label, int flags, String desc) {
        this(label, flags, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, desc);
    }
    
    @Override
    protected NumberRange doAccept(Token token, int flags) throws CommandSyntaxException {
        NumberRange[] ranges = parseList(token.text);
        if (ranges.length == 1) {
            return ranges[0];
        }
        
        Arrays.sort(ranges);
        
        // concat
        for (int i = 1; i < ranges.length;) {
            if (ranges[i - 1].end() >= (ranges[i].start() - 1)) {
                ranges = concat(ranges, i - 1, i);
            } else {
                i++;
            }
        }
        for (int i = 0; i < (ranges.length - 1); i++) {
            values.add(ranges[i]);
        }
        return ranges[ranges.length - 1];
    }
    
    private NumberRange[] concat(NumberRange[] ranges, int i, int j) {
        NumberRange[] newRanges = new NumberRange[ranges.length - (j - i)];
        System.arraycopy(ranges, 0, newRanges, 0, i);
        if (j < (ranges.length - 1)) {
            System.arraycopy(ranges, j + 1, newRanges, j, newRanges.length - j);
        }
        newRanges[i] = new NumberRange(ranges[i].start(), ranges[j].end(), min - 1, max + 1);
        return newRanges;
    }
    
    private static final boolean debug = false;
    
    @SuppressWarnings("unused")
    private void error(String s) {
        if (debug) System.err.println(s);
    }
    
    /**
     * Parse a number list
     *
     * list ::= range | list,range
     */
    private NumberRange[] parseList(String text) throws CommandSyntaxException {
        int delimPos = text.indexOf(listDelim);
        if (delimPos == -1) {
            return new NumberRange[] {parseRange(text)};
        } else {
            String[] rangesText = text.split(listDelim);
            NumberRange[] ranges = new NumberRange[rangesText.length];
            for (int i = 0; i < ranges.length; i++) {
                ranges[i] = parseRange(rangesText[i]);
            }
            return ranges;
        }
    }
    
    /**
     * Parse a number range.
     *
     * range ::= integer | -integer | integer- | integer-integer
     */
    private NumberRange parseRange(String text) throws CommandSyntaxException {
        int delimPos = text.indexOf(rangeDelim);
        int start;
        int end;
        if (text.equals(rangeDelim)) {
            throw new CommandSyntaxException("Invalid number range");
        }
        if (delimPos == -1) {
            // this is ok, as we allow a single integer to represent the range n-n
            start = end = parseInt(text);
        } else if (delimPos == 0) {
            start = min;
            end = parseInt(text.substring(rangeDelim.length(), text.length()));
        } else if (delimPos == (text.length() - rangeDelim.length())) {
            start = parseInt(text.substring(0, delimPos));
            end = max;
        } else {
            start = parseInt(text.substring(0, delimPos));
            end   = parseInt(text.substring(delimPos + rangeDelim.length(), text.length()));
        }
        try {
            return new NumberRange(start, end, min - 1, max + 1);
        } catch (IllegalArgumentException ex) {
            throw new CommandSyntaxException("Invalid number range");
        }
    }
    
    private int parseInt(String text) throws CommandSyntaxException {
        int n;
        try {
            n = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw new CommandSyntaxException("Invalid number range");
        }
        if (n < min || n > max) {
            throw new CommandSyntaxException("Number out of range");
        }
        return n;
    }
    
    @Override
    protected String state() {
        return "min=" + min + ",max=" + max;
    }
    
    @Override
    protected String argumentKind() {
        return "number-list";
    }
}
