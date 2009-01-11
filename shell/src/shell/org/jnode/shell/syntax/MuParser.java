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

import org.apache.log4j.Logger;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.SymbolSource;
import org.jnode.shell.CommandLine.Token;
import org.jnode.shell.syntax.CommandSyntaxException.Context;

/**
 * This class implements parsing of a token stream against a MuSyntax graph.  The parser 
 * binds token values against Argument instances as it goes, and does full backtracking 
 * when it reaches a point where it cannot make forward progress.  
 * <p>
 * When we are doing a normal parse, the various alternatives in the MuSyntax graph are
 * explored until either there is a successful parse, or we run out of alternatives.  The
 * latter case results in an exception and a failed parse.
 * <p>
 * When we are doing a completion parse, all alternatives are explored irrespective of 
 * parse success.
 * <p>
 * A MuSyntax may contain "infinite" loops, or other pathologies that trigger
 * excessive backtracking.  To avoid problems, the 'parse' method takes a 
 * 'stepLimit' parameter that causes the parse to fail if it has not terminated
 * soon enough.
 * 
 * @author crawley@jnode.org
 */
public class MuParser {
    
    /**
     * This is the default value for the stepLimit parameter.
     */
    public static final int DEFAULT_STEP_LIMIT = 10000;
    
    private static final boolean DEBUG = false;
    private static final Logger log = Logger.getLogger(MuParser.class);
    
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
            this.choiceNo = 0;
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("CP{");
            sb.append("sourcePos=").append(sourcePos);
            sb.append(",syntaxStack=").append(showStack(syntaxStack, true));
            sb.append(",choiceNo=").append(choiceNo).append("...").append("}");
            return sb.toString();
        }
    }
    
    public MuParser() {
        super();
    }
    
    /**
     * Parse Tokens against a MuSyntax using a default stepLimit.  On success, tokens will
     * have been used to populate Argument values in the ArgumentBundle.
     * 
     * @param rootSyntax the root of the MuSyntax graph.
     * @param completion if this is not <code>null</null>, do a completion parse, and record
     *     the completions here.
     * @param source the source of Tokens to be parsed
     * @param bundle the container for Argument objects; e.g. provided by the command.
     * @throws CommandSyntaxException
     */
    public void parse(MuSyntax rootSyntax, CompletionInfo completion,
            SymbolSource<Token> source, ArgumentBundle bundle) 
        throws CommandSyntaxException, SyntaxFailureException {
        parse(rootSyntax, completion, source, bundle, DEFAULT_STEP_LIMIT);
    }
    
    /**
     * Parse Tokens against a MuSyntax using a default stepLimit.  On success, tokens will
     * have been used to populate Argument values in the ArgumentBundle.
     * 
     * @param rootSyntax the root of the MuSyntax graph.
     * @param completion if this is not <code>null</null>, do a completion parse, and record
     *     the completions here.
     * @param source the source of Tokens to be parsed
     * @param bundle the container for Argument objects; e.g. provided by the command.
     * @param stepLimit the maximum allowed parse steps allowed.  A 'stepLimit' of zero or less
     * means that there is no limit.
     * @throws CommandSyntaxException
     */
    public synchronized void parse(MuSyntax rootSyntax, CompletionInfo completion, 
            SymbolSource<Token> source, ArgumentBundle bundle, int stepLimit) 
        throws CommandSyntaxException, SyntaxFailureException {
        // FIXME - deal with syntax error messages and completion
        // FIXME - deal with grammars that cause stack explosion
        if (bundle != null) {
            // FIXME - why am I doing this here?  Is it just for the unit tests?
            bundle.clear();
        }
        Deque<MuSyntax> syntaxStack = new LinkedList<MuSyntax>();
        Deque<ChoicePoint> backtrackStack = new LinkedList<ChoicePoint>();
        if (DEBUG) {
            log.debug("Parsing with rootSyntax = " + (rootSyntax == null ? "null" : rootSyntax.format()));
        }
        if (rootSyntax == null) {
            if (source.hasNext()) {
                throw new CommandSyntaxException("No arguments expected for this command");
            }
            return;
        }
        List<Context> argFailures = new LinkedList<Context>();
        syntaxStack.addFirst(rootSyntax);
        int stepCount = 0;
        while (!syntaxStack.isEmpty()) {
            if (stepLimit > 0 && ++stepCount > stepLimit) {
                throw new SyntaxFailureException("Parse exceeded the step limit (" + stepLimit + "). " +
                        "Either the command line is too large, or the syntax is too complex (or pathological)");
            }
            boolean backtrack = false;
            if (DEBUG) {
                log.debug("syntaxStack % " + showStack(syntaxStack, true));
            }
            MuSyntax syntax = syntaxStack.removeFirst();
            if (DEBUG) {
                log.debug("Trying kind = " + syntax.getKind() + ", syntax = " + syntax.format());
                if (source.hasNext()) {
                    log.debug("source -> " + source.peek().text);
                } else {
                    log.debug("source at end");
                }
            }
            CommandLine.Token token = null;
            switch (syntax.getKind()) {
                case MuSyntax.SYMBOL:
                    String symbol = ((MuSymbol) syntax).getSymbol();
                    token = source.hasNext() ? source.next() : null;

                    if (completion == null || source.hasNext()) {
                        backtrack = token == null || !token.text.equals(symbol);
                    } else {
                        if (token == null) {
                            completion.addCompletion(symbol);
                            backtrack = true;
                        } else if (source.whitespaceAfterLast()) {
                            if (!token.text.equals(symbol)) {
                                backtrack = true;
                            }
                        } else {
                            if (symbol.startsWith(token.text)) {
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
                                        log.debug("recording undo for arg " + argName);
                                    }
                                }
                            } else {
                                arg.complete(completion, token.text);
                                completion.setCompletionStart(token.start);
                                backtrack = true;
                            }
                        } else {
                            if (completion != null) {
                                arg.complete(completion, "");
                            }
                            backtrack = true;
                        }
                    } catch (CommandSyntaxException ex) {
                        argFailures.add(new Context(token, syntax, source.tell(), ex));
                        if (DEBUG) {
                            log.debug("accept for arg " + argName + " threw SyntaxErrorException('" + 
                                    ex.getMessage() + "'");
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
                                log.debug("recording undo for preset arg " + arg.getLabel());
                            }
                        }
                    } catch (CommandSyntaxException ex) {
                        argFailures.add(new Context(null, syntax, source.tell(), ex));
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

                    // The test below optimizes the case where there is only one
                    // alternative. This
                    // avoids the non-trivial cost of creating a choicepoint,
                    // backtracking, etc.
                    if (choices.length > 1) {
                        ChoicePoint choicePoint = new ChoicePoint(source.tell(), syntaxStack, choices);
                        backtrackStack.addFirst(choicePoint);
                        syntaxStack = new SharedStack<MuSyntax>(syntaxStack);
                        if (DEBUG) {
                            log.debug("pushed choicePoint - " + choicePoint);
                        }
                    }
                    if (choices[0] != null) {
                        syntaxStack.addFirst(choices[0]);
                    }
                    if (DEBUG) {
                        log.debug("syntaxStack " + showStack(syntaxStack, true));
                    }
                    break;
                case MuSyntax.BACK_REFERENCE:
                    throw new SyntaxFailureException("Found an unresolved MuBackReference");
                default:
                    throw new SyntaxFailureException("Unknown MuSyntax kind (" + syntax.getKind() + ")");
            }
            if (syntaxStack.isEmpty()) {
                if (source.hasNext()) {
                    if (DEBUG) {
                        log.debug("exhausted syntax stack too soon");
                    }
                    backtrack = true;
                }
                if (completion != null && !backtrackStack.isEmpty()) {
                    if (DEBUG) {
                        log.debug("try alternatives for completion");
                    }
                    backtrack = true;
                }
            }
            if (backtrack) {
                if (DEBUG) {
                    log.debug("backtracking ...");
                }
                while (!backtrackStack.isEmpty()) {
                    ChoicePoint choicePoint = backtrackStack.getFirst();
                    if (DEBUG) {
                        log.debug("top choicePoint - " + choicePoint);
                        log.debug("syntaxStack " + showStack(syntaxStack, true));
                    }
                    // Issue undo's for any argument values added.
                    for (Argument<?> arg : choicePoint.argsModified) {
                        if (DEBUG) {
                            log.debug("undo for arg " + arg.getLabel());
                        }
                        arg.undoLastValue();
                    }
                    // If possible, take the next choice in the current choice
                    // point
                    // and stop backtracking
                    int lastChoice = choicePoint.choices.length - 1;
                    int choiceNo = ++choicePoint.choiceNo;
                    if (choiceNo <= lastChoice) {
                        MuSyntax choice = choicePoint.choices[choiceNo];
                        choicePoint.argsModified.clear();
                        source.seek(choicePoint.sourcePos);
                        // (If this is the last choice in the choice point, we
                        // won't need to
                        // use this choice point's saved syntax stack again ...)
                        if (choiceNo == lastChoice) {
                            syntaxStack = choicePoint.syntaxStack;
                        } else {
                            syntaxStack = new SharedStack<MuSyntax>(choicePoint.syntaxStack);
                        }
                        if (choice != null) {
                            syntaxStack.addFirst(choice);
                        }
                        backtrack = false;
                        if (DEBUG) {
                            log.debug("taking choice #" + choiceNo);
                            log.debug("syntaxStack : " + showStack(syntaxStack, true));
                        }
                        break;
                    }
                    // Otherwise, pop the choice point and keep going.
                    if (DEBUG) {
                        log.debug("popped choice point");
                    }
                    backtrackStack.removeFirst();
                }
                // If we are still backtracking and we are out of choices ...
                if (backtrack) {
                    if (completion == null) {
                        throw new CommandSyntaxException("ran out of alternatives", argFailures);
                    } else {
                        if (DEBUG) {
                            log.debug("end completion");
                        }
                        return;
                    }
                }
                if (DEBUG) {
                    log.debug("end backtracking");
                }
            }
        }
        if (DEBUG) {
            log.debug("succeeded");
        }
    }

    
    private static String showStack(Deque<MuSyntax> stack, boolean oneLine) {
        StringBuffer sb = new StringBuffer();
        for (MuSyntax syntax : stack) {
            if (sb.length() > 0) {
                sb.append(", ");
                if (!oneLine) {
                    sb.append("\n    ");
                }
            }
            sb.append(syntax.format());
        }
        return sb.toString();
    }
}
