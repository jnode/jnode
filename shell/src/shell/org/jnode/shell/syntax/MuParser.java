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

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.SymbolSource;
import org.jnode.shell.CommandLine.Token;

public class MuParser {
    
    /**
     * A MuSyntax may contain "infinite" loops, or other pathologies that trigger
     * excessive backtracking.  To avoid problems, the 'parse' method takes a 
     * 'stepLimit' parameter that causes the parse to fail if it has not terminated
     * soon enough.  This is the default value for that parameter.
     */
    public static final int DEFAULT_STEP_LIMIT = 10000;
    
    private static final boolean DEBUG = false;
    
    private static class ChoicePoint {
        public final int sourcePos;
        public final Deque<MuSyntax> syntaxStack;
        public final MuSyntax[] choices;
        public int choiceNo;
        public final List<Argument<?>> argsModified;
        
        public ChoicePoint(int sourcePos, Deque<MuSyntax> syntaxStack, MuSyntax[] choices) {
            super();
            this.sourcePos = sourcePos;
            this.syntaxStack = syntaxStack;
            this.choices = choices;
            this.argsModified = new LinkedList<Argument<?>>();
            choiceNo = 0;
        }
    }
    
    public MuParser() {
        super();
    }
    
    public void parse(MuSyntax rootSyntax, CompletionInfo completion,
            SymbolSource<Token> source, ArgumentBundle bundle) 
    throws CommandSyntaxException, SyntaxFailureException {
        parse(rootSyntax, completion, source, bundle, DEFAULT_STEP_LIMIT);
    }
    
    /**
     * FIXME - deal with syntax error messages and completion
     * FIXME - deal with grammars that cause stack explosion
     * 
     * @param rootSyntax the root of the MuSyntax.
     * @param source the source of Tokens to be parsed
     * @param bundle the container for Argument objects; e.g. provided by the command.
     * @param stepLimit the maximum allowed parse steps allowed.  A 'stepLimit' of zero or less
     * means that there is no limit.
     * @throws CommandSyntaxException
     */
    public synchronized void parse(MuSyntax rootSyntax, CompletionInfo completion, 
            SymbolSource<Token> source, ArgumentBundle bundle, int stepLimit) 
    throws CommandSyntaxException, SyntaxFailureException {
        if (bundle != null) {
            bundle.clear();
        }
        Deque<MuSyntax> syntaxStack = new LinkedList<MuSyntax>();
        Deque<ChoicePoint> backtrackStack = new LinkedList<ChoicePoint>();
        
        syntaxStack.addFirst(rootSyntax);
        int stepCount = 0;
        while (!syntaxStack.isEmpty()) {
            if (stepLimit > 0 && ++stepCount > stepLimit) {
                throw new SyntaxFailureException("Parse exceeded the step limit (" + stepLimit + "). " +
                        "Either the command line is too large, or the syntax is too complex (or pathological)");
            }
            boolean backtrack = false;
            if (DEBUG) {
                System.err.println("syntaxStack " + syntaxStack.size() + ", " +
                        "source pos " + source.tell());
            }
            MuSyntax syntax = syntaxStack.removeFirst();
            if (DEBUG) {
                System.err.println("Trying " + syntax.format());
                if (source.hasNext()) {
                    System.err.println("source -> " + source.peek().token);
                } else {
                    System.err.println("source at end");
                }
            }
            CommandLine.Token token;
            switch (syntax.getKind()) {
            case MuSyntax.SYMBOL:
                String symbol = ((MuSymbol) syntax).getSymbol();
                token = source.hasNext() ? source.next() : null;
                
                if (completion == null) {
                    backtrack = token == null || !token.token.equals(symbol);
                }
                else {
                    if (token == null) {
                        completion.addCompletion(symbol);
                        backtrack = true;
                    }
                    else if (source.whitespaceAfterLast()) {
                        if (!token.token.equals(symbol)) {
                            backtrack = true;
                        }
                    }
                    else {
                        if (symbol.startsWith(token.token)) {
                            completion.addCompletion(symbol);
                            completion.setCompletionStart(token.start);
                        }
                        backtrack = true;
                    }
                }
                break;
            case MuSyntax.ARGUMENT:
                String argName = ((MuArgument) syntax).getArgName();
                Argument<?> arg = bundle.getArgument(argName);
                try {
                    if (source.hasNext()) {
                        token = source.next();
                        if (completion == null || source.hasNext() || source.whitespaceAfterLast()) {
                            arg.accept(token);
                            if (!backtrackStack.isEmpty()) {
                                backtrackStack.getFirst().argsModified.add(arg);
                                if (DEBUG) {
                                    System.err.println("recording undo for arg " +
                                            argName);
                                }
                            }
                        }
                        else {
                            arg.complete(completion, token.token);
                            completion.setCompletionStart(token.start);
                            backtrack = true;
                        }
                    }
                    else {
                        if (completion != null) {
                            arg.complete(completion, "");
                        }
                        backtrack = true;
                    }
                }
                catch (CommandSyntaxException ex) {
                    if (DEBUG) {
                        System.err.println("accept for arg " + argName + 
                                " threw SyntaxErrorException('" + ex.getMessage() + "'");
                    }
                    backtrack = true;
                }
                break;
            case MuSyntax.PRESET:
                MuPreset preset = (MuPreset) syntax;
                arg = bundle.getArgument(preset.getArgName());
                try {
                    arg.accept(new CommandLine.Token(preset.getPreset()));
                    if (!backtrackStack.isEmpty()) {
                        backtrackStack.getFirst().argsModified.add(arg);
                        if (DEBUG) {
                            System.err.println("recording undo for preset arg " +
                                    arg.getLabel());
                        }
                    }
                }
                catch (CommandSyntaxException ex) {
                    backtrack = true;
                }
                break;
            case MuSyntax.SEQUENCE:
                MuSyntax[] elements = ((MuSequence) syntax).getElements();
                for (int i = elements.length - 1; i >= 0; i--) {
                    syntaxStack.addFirst(elements[i]);
                }
                break;
            case MuSyntax.ALTERNATION:
                MuSyntax[] choices = ((MuAlternation) syntax).getAlternatives();
                backtrackStack.addFirst(
                        new ChoicePoint(source.tell(), syntaxStack, choices));
                syntaxStack = new SharedStack<MuSyntax>(syntaxStack);
                if (DEBUG) {
                    System.err.println("Pushed choicePoint: source = " + source.tell() + 
                            ", syntax stack pos = " + syntaxStack.size());
                }
                if (choices[0] != null) {
                    syntaxStack.addFirst(choices[0]);
                }
                break;
            case MuSyntax.BACK_REFERENCE:
                throw new SyntaxFailureException("Found an unresolved MuBackReference");
            }
            if (syntaxStack.isEmpty() && source.hasNext()) {
                backtrack = true;
            }
            if (backtrack) {
                if (DEBUG) {
                    System.err.println("backtracking ...");
                }
                while (!backtrackStack.isEmpty()) {
                    ChoicePoint choicePoint = backtrackStack.getFirst();
                    if (DEBUG) {
                        System.err.println("backtrackStack " +
                                backtrackStack.size() + ", " +
                                "syntaxStack.size() " + choicePoint.syntaxStack.size() +
                                ", " + "choiceNo " + choicePoint.choiceNo +
                                ", " + "choices.length " +
                                choicePoint.choices.length);
                        System.err.println("syntaxStack " + syntaxStack.size() +
                                ", " + "source pos " + source.tell());
                    }
                    if (DEBUG) {
                        System.err.println("syntaxStack " + syntaxStack.size() +
                                ", " + "source pos " + source.tell());
                    }
                    // Issue undo's for any argument values added.
                    for (Argument<?> arg : choicePoint.argsModified) {
                        if (DEBUG) {
                            System.err.println("undo for arg " + arg.getLabel());
                        }
                        arg.undoLastValue();
                    }
                    // If possible, take the next choice in the current choice point 
                    // and stop backtracking
                    int lastChoice = choicePoint.choices.length - 1;
                    int choiceNo = ++choicePoint.choiceNo;
                    if (choiceNo <= lastChoice) {
                        MuSyntax choice = choicePoint.choices[choiceNo];
                        choicePoint.argsModified.clear();
                        source.seek(choicePoint.sourcePos);
                        // (If this is the last choice in the choice point, we won't need to
                        // use this choice point's saved syntax stack again ...)
                        if (choiceNo == lastChoice) {
                            syntaxStack = choicePoint.syntaxStack;
                        }
                        else {
                            syntaxStack = new SharedStack<MuSyntax>(choicePoint.syntaxStack);
                        }
                        if (choice != null) {
                            syntaxStack.addFirst(choice);
                        }
                        backtrack = false;
                        if (DEBUG) {
                            System.err.println("taking choice #" + choiceNo);
                        }
                        break;
                    }
                    // Otherwise, pop the choice point and keep going.
                    if (DEBUG) {
                        System.err.println("popped choice point");
                    }
                    backtrackStack.removeFirst();
                }
                if (backtrack && completion == null) {
                    throw new CommandSyntaxException("ran out of alternatives");
                }
                if (DEBUG) {
                    System.err.println("end backtracking");
                }
            }
        }
        if (DEBUG) {
            System.err.println("succeeded");
        }
    }

}
