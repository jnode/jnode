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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.jnode.shell.CommandLine.Token;


/**
 * An OptionSet syntax is like a Powerset syntax, except that:
 * <ol>
 * <li>the child syntaxes must all be OptionSyntaxes, and
 * <li>the syntax supports combining of flag options; e.g. "-ab" means "-a -b" 
 * </ol>
 * 
 * @author crawley@jnode.org
 *
 */
public class OptionSetSyntax extends GroupSyntax {
    
    private static class FlagSetArgument extends Argument<Boolean> {
        
        private final List<OptionSyntax> flagOptions;
        private final ArgumentBundle bundle;
        
        FlagSetArgument(String label, ArgumentBundle bundle, List<OptionSyntax> flagOptions) {
            super(label, OPTIONAL + MULTIPLE, new Boolean[0], null);
            this.flagOptions = flagOptions;
            this.bundle = bundle;
        }

        @Override
        protected void doAccept(Token token) throws CommandSyntaxException {
            String value = token.token;
            int len = value.length();
            if (len < 2 || value.charAt(0) != '-') {
                throw new CommandSyntaxException("'" + value + "' is not a flag set");
            }
            for (int i = 1; i < len; i++) {
                char ch = value.charAt(i);
                String shortOptName = "-" + ch;
                boolean found = false;
                for (OptionSyntax flagOption : flagOptions) {
                    if (shortOptName.equals(flagOption.getShortOptName())) {
                        bundle.getArgument(flagOption).accept(new Token(shortOptName));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new CommandSyntaxException("'" + ch + "' is not a known flag");
                }
            }
        }

        @Override
        protected String argumentKind() {
            return "flags";
        }
    }
    
    private static Comparator<OptionSyntax> SHORT_NAME_ORDER =
        new Comparator<OptionSyntax>() {

            public int compare(OptionSyntax o1, OptionSyntax o2) {
                char c1 = o1.getShortOptName().charAt(1);
                char c2 = o2.getShortOptName().charAt(1);
                boolean l1 = Character.isLowerCase(c1);
                boolean l2 = Character.isLowerCase(c2);
                if (l1 == l2) {
                    return (int) c2 - (int) c1;
                }
                else {
                    return l1 ? -1 : +1;
                }
            }
        
    };
    
    private final OptionSyntax[] optionSyntaxes;
    
    public OptionSetSyntax(String label, String description, OptionSyntax... syntaxes) {
        super(label, description, syntaxes);
        this.optionSyntaxes = syntaxes;
    }

    public OptionSetSyntax(String label, OptionSyntax... syntaxes) {
        this(label, null, syntaxes);
    }

    public OptionSetSyntax(OptionSyntax... syntaxes) {
        this(null, null, syntaxes);
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        ArrayList<OptionSyntax> flagOptions = new ArrayList<OptionSyntax>();
        MuSyntax[] childMuSyntaxes = new MuSyntax[optionSyntaxes.length];
        for (int i = 0; i < optionSyntaxes.length; i++) {
            OptionSyntax childSyntax = optionSyntaxes[i];
            Argument<?> arg = bundle.getArgument(childSyntax);
            if (arg instanceof FlagArgument && childSyntax.getShortOptName() != null) {
                flagOptions.add(childSyntax);
            }
            childMuSyntaxes[i] = childSyntax.prepare(bundle);
        }
        if (!flagOptions.isEmpty()) {
            // We deal with combined flag options by adding a proxy argument to the
            // bundle whose 'accept' method will unpick (say) '-ab', and dispatch
            // it to the corresponding FlagArguments.
            String label = MuSyntax.genLabel();
            FlagSetArgument arg = new FlagSetArgument(label, bundle, flagOptions);
            bundle.addArgument(arg);
            childMuSyntaxes = new MuSyntax[] {
                   new MuAlternation(childMuSyntaxes),
                   new MuArgument(label)
            };
        }
        String label = this.label == null ? MuSyntax.genLabel() : this.label;
        MuSyntax res = new MuAlternation(label, null, 
                new MuSequence(new MuAlternation((String) null, childMuSyntaxes),
                        new MuBackReference(label)));
        res.resolveBackReferences();
        return res;
    }

    @Override
    public String format(ArgumentBundle bundle) {
        TreeSet<OptionSyntax> shortFlagOpts = new TreeSet<OptionSyntax>(SHORT_NAME_ORDER);
        for (int i = 0; i < optionSyntaxes.length; i++) {
            OptionSyntax optionSyntax = optionSyntaxes[i];
            Argument<?> arg = bundle.getArgument(optionSyntax);
            if (arg instanceof FlagArgument &&
                    optionSyntax.getShortOptName() != null && 
                    optionSyntax.getLongOptName() == null) {
                shortFlagOpts.add(optionSyntax);
            }
        }
        StringBuilder sb = new StringBuilder();
        if (!shortFlagOpts.isEmpty()) {
            sb.append("[ -");
            for (OptionSyntax optionSyntax : shortFlagOpts) {
                sb.append(optionSyntax.getShortOptName().charAt(1));
            }
            sb.append(" ]");
        }
        for (int i = 0; i < optionSyntaxes.length; i++) {
            OptionSyntax optionSyntax = optionSyntaxes[i];
            if (!shortFlagOpts.contains(optionSyntax)) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(optionSyntax.format(bundle));
            }
        }
        return sb.toString();
    }

}