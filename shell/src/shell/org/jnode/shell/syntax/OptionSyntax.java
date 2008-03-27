/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.syntax;


public class OptionSyntax extends ArgumentSyntax {
    
    private final String longOptName;
    private final String shortOptName;
    
    public OptionSyntax(String argName, String optionName, char optionChar, String description) {
        super(null, argName, description);
        this.longOptName = "--" + optionName;
        this.shortOptName = "-" + optionChar;
    }

    public OptionSyntax(String argName, char optionChar, String description) {
        super(null, argName, description);
        this.longOptName = null;
        this.shortOptName = "-" + optionChar;
    }

    public OptionSyntax(String argName, String optionName, String description) {
        super(null, argName, description);
        this.longOptName = "--" + optionName;
        this.shortOptName = null;
    }

    public OptionSyntax(String argName, String optionName, char optionChar) {
        this(argName, optionName, optionChar, null);
    }

    public OptionSyntax(String argName, char optionChar) {
       this(argName, optionChar, null);
    }

    public OptionSyntax(String argName, String optionName) {
        this(argName, optionName, null);
    }

    @Override
    public String toString() {
    	return "OptionSyntax{label='" + label + 
    	", short='" + shortOptName + "', long='" + longOptName + "'}";
    }

    @Override
    public String format(ArgumentBundle bundle) {
        Argument<?> arg = bundle.getArgument(this);
        StringBuilder sb = new StringBuilder();
        if (longOptName != null) {
            sb.append(longOptName);
        }
        if (shortOptName != null) {
            if (longOptName != null) {
                sb.append(" | ");
            }
            sb.append(shortOptName);
        }
        if (!(arg instanceof FlagArgument)) {
            sb.append(" <").append(arg.format()).append('>');
        }
        return sb.toString();
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        Argument<?> arg = bundle.getArgument(this);
        if (arg instanceof FlagArgument) {
            if (longOptName == null) {
                return new MuSequence(
                        new MuSymbol(shortOptName), 
                        new MuPreset(arg.getLabel(), "true"));
            }
            else if (shortOptName == null) {
                return new MuSequence(
                        new MuSymbol(longOptName), 
                        new MuPreset(arg.getLabel(), "true"));
            }
            else {
                return new MuAlternation(
                        new MuSequence(
                                new MuSymbol(shortOptName), 
                                new MuPreset(arg.getLabel(), "true")),
                        new MuSequence(
                                new MuSymbol(longOptName), 
                                new MuPreset(arg.getLabel(), "true")));
            }
        }
        else {
            if (longOptName == null) {
                return new MuSequence(
                        new MuSymbol(shortOptName), 
                        new MuArgument(arg.getLabel()));
            }
            else if (shortOptName == null) {
                return new MuSequence(
                        new MuSymbol(longOptName), 
                        new MuArgument(arg.getLabel()));
            }
            else {
                return new MuAlternation(
                        new MuSequence(
                                new MuSymbol(shortOptName), 
                                new MuArgument(arg.getLabel())),
                        new MuSequence(
                                new MuSymbol(longOptName), 
                                new MuArgument(arg.getLabel())));
            }
        }
    }

    String getShortOptName() {
        return shortOptName;
    }

    String getLongOptName() {
        return longOptName;
    }

}
