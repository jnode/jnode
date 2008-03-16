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

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;


/**
 * The MuSyntax class and related classes provide an in-memory representation of BNF-like 
 * grammars for use in the command line syntax subsystem.  The key difference between the
 * Mu and BNF grammar families is that the former has hooks for custom token recognition and 
 * state capture via the MuArgument construct.
 * 
 * @author crawley@jnode.org
 */
public abstract class MuSyntax {
    public static final int SYMBOL = 0;
    public static final int ARGUMENT = 1;
    public static final int PRESET = 2;
    public static final int ALTERNATION = 3;
    public static final int SEQUENCE = 4;
    public static final int BACK_REFERENCE = 5;
    
    String label;
    
    public MuSyntax(String label) {
        this.label = label;
        if ("".equals(label)) {
            throw new IllegalArgumentException("empty label");
        }
    }
    
    public String getLabel() {
        return label;
    }
    
    public abstract int getKind();
    
    static class FormatState {
        final ArrayDeque<MuSyntax> work = new ArrayDeque<MuSyntax>();
        final HashSet<MuSyntax> done = new HashSet<MuSyntax>();
        private final HashMap<MuSyntax, String> syntaxToLabel = new HashMap<MuSyntax, String>();
        private final MuSyntax start;
        private int count = 1;
        
        FormatState(MuSyntax start) {
            this.start = start;
        }
        
        String getLabel(MuSyntax syntax) {
            String ll = syntaxToLabel.get(syntax);
            if (ll == null) {
                String l = syntax.label;
                if (l == null) {
                    l = (syntax == start) ? "*Start*" : ("*P" + count + "*");
                }
                ll = (syntax.getKind() == BACK_REFERENCE) ?
                        ("<[" + l + "]>") : ('<' + l + '>');
                syntaxToLabel.put(syntax, ll);
            }
            return ll;
        }
    }

    /**
     * Render this MuSyntax object and its descendants nodes as text, using a BNF-like syntax.
     * The 'label' values (if present) will supply the BNF production names.  (Note: if
     * the 'label' values are non-unique, the formatted text will be malformed.)
     * 
     * @return a BNF-like rendering of the MuSyntax tree.
     */
    public final String format() {
        StringBuilder sb = new StringBuilder();
        FormatState state = new FormatState(this);
        state.work.add(this);
        while (!state.work.isEmpty()) {
            MuSyntax syntax = state.work.removeFirst();
            if (!state.done.add(syntax)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            String l = state.getLabel(syntax);
            sb.append(l).append(" ::= ");
            sb.append(syntax.format(state));
        }
        return sb.toString();
    }

    abstract String format(FormatState state);

    final String formatInline(FormatState state) {
        if (label != null) {
            state.work.add(this);
            return state.getLabel(this);
        }
        else {
            return format(state);
        }
    }
    
    static class ResolveState {
        public final HashMap<String, MuSyntax> refMap = 
            new HashMap<String, MuSyntax>();
        public final HashSet<MuSyntax> seen = new HashSet<MuSyntax>();
    }
    
    /**
     * Resolve any MuBackReference instances in this syntax tree.
     * <p>
     * The algorithm has the following limitations:
     * <ul>
     * <li>References to ancestor syntax nodes (true back references) are guaranteed 
     * to be resolvable.
     * References to sibling or cousin syntax nodes may not be resolvable, depending
     * on whether or not the target is encountered before the reference in the
     * tree traversal order.
     * <li>If SyntaxFailureException is thrown, the syntax tree may be left in a 
     * partially resolved state.
     * </ul>
     * 
     * @throws SyntaxFailureException if any MuBackReference cannot be resolved.
     */
    public final void resolveBackReferences() throws SyntaxFailureException {
        resolveBackReferences(new ResolveState());
    }
    
    abstract MuSyntax resolveBackReferences(ResolveState state) throws SyntaxFailureException;

    private static long count;
    public static String genLabel() {
        return "**" + count++ + "**";
    }
}