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
 
package org.jnode.shell.bjorne;

import static org.jnode.shell.bjorne.BjorneToken.TOK_ASSIGNMENT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_CASE_WORD;
import static org.jnode.shell.bjorne.BjorneToken.TOK_COMMAND_NAME;
import static org.jnode.shell.bjorne.BjorneToken.TOK_COMMAND_WORD;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_LINE;
import static org.jnode.shell.bjorne.BjorneToken.TOK_END_OF_STREAM;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FILE_NAME;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR_NAME;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FOR_WORD;
import static org.jnode.shell.bjorne.BjorneToken.TOK_FUNCTION_NAME;
import static org.jnode.shell.bjorne.BjorneToken.TOK_HERE_END;
import static org.jnode.shell.bjorne.BjorneToken.TOK_IO_NUMBER;
import static org.jnode.shell.bjorne.BjorneToken.TOK_NAME;
import static org.jnode.shell.bjorne.BjorneToken.TOK_PATTERN;
import static org.jnode.shell.bjorne.BjorneToken.TOK_WORD;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.ArgumentCompleter;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Completable;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.syntax.AliasArgument;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;

/**
 * This class is used by the Bjorne parser to capture completion information.
 * It tries to capture the end of stream token and the one before it, together
 * with the aggregated expectSets that were used for those two tokens.  It also
 * captures the last 'simple command' node that was created, or was being being
 * when we hit whatever it was that caused us to stop the parse.
 * 
 * @author crawley@jnode.org
 */
public class BjorneCompleter implements Completable {
    
    private BjorneToken endToken;
    private BjorneToken penultimateToken;
    private long endExpectedSet;
    private long penultimateExpectedSet;
    private SimpleCommandNode command;
    private final BjorneContext context;

    public BjorneCompleter(BjorneContext context) {
        super();
        this.context = context;
    }

    @Override
    public void complete(CompletionInfo completions, CommandShell shell) throws CompletionException {
        if (endToken == null) {
            if (penultimateToken == null) {
                completeCommandWord(completions, shell, new BjorneToken(""));
                return;
            }
            endToken = penultimateToken;
            endExpectedSet = penultimateExpectedSet;
        }
        if (command != null) {
            BjorneToken[] words = command.getWords();
            if (words.length > 1 && words[words.length - 1] == penultimateToken) {
                boolean argumentAnticipated = penultimateToken.end < endToken.end;
                command.complete(completions, context, shell, argumentAnticipated);
            } else if (words.length == 1 && words[0] == penultimateToken && penultimateToken.end < endToken.end) {
                command.complete(completions, context, shell, true);
            }
        }
        String partial;
        BjorneToken token;
        long expectedSet;
        if (penultimateToken == null || penultimateToken.end < endToken.end) {
            partial = "";
            expectedSet = endExpectedSet;
            completions.setCompletionStart(endToken.start);
            token = endToken;
        } else {
            partial = penultimateToken.unparse();
            expectedSet = penultimateExpectedSet | endExpectedSet;
            completions.setCompletionStart(penultimateToken.start);
            token = penultimateToken;
        }
        if (!partial.equals(token.getText())) {
            token = new BjorneToken(token.getTokenType(), token.unparse(), token.start, token.end);
        }
        for (int i = 0; i < 64; i++) {
            if (((1L << i) & expectedSet) == 0) {
                continue;
            }
            switch (i) {
                case TOK_END_OF_LINE:
                case TOK_END_OF_STREAM:
                    // These are not completable
                    break;
                case TOK_WORD: 
                case TOK_NAME: 
                    // These are generic token types... completion is based on more specific types
                    break;
                case TOK_IO_NUMBER:
                case TOK_COMMAND_WORD:
                case TOK_FUNCTION_NAME:
                case TOK_HERE_END:
                case TOK_FOR_NAME:
                case TOK_PATTERN:
                case TOK_CASE_WORD:
                    // Ignore for purposes of completion
                    break;
                case TOK_ASSIGNMENT:
                    ArgumentCompleter ac = new ArgumentCompleter(
                            new AssignmentArgument("?", context, Argument.MANDATORY, null), token);
                    ac.complete(completions, shell);
                    break;
                case TOK_FOR_WORD:
                case TOK_FILE_NAME:
                    // Complete against the file system namespace
                    ac = new ArgumentCompleter(
                            new FileArgument("?", Argument.MANDATORY, null), token);
                    ac.complete(completions, shell);
                    break;
                case TOK_COMMAND_NAME:
                    // Complete against the command/alias/function namespaces
                    completeCommandWord(completions, shell, token);
                    break;
                default:
                    String candidate = BjorneToken.toString(i);
                    if (candidate.startsWith(partial)) {
                        completions.addCompletion(candidate);
                    }
            }
        }
    }
    
    private void completeCommandWord(CompletionInfo completions, CommandShell shell, BjorneToken token) {
        // FIXME ... do functions ...
        for (String builtinName : BjorneInterpreter.BUILTINS.keySet()) {
            if (builtinName.startsWith(token.text)) {
                completions.addCompletion(builtinName);
            }
        }
        ArgumentCompleter ac = new ArgumentCompleter(
                new AliasArgument("?", Argument.MANDATORY, null), token);
        ac.complete(completions, shell);
        ac = new ArgumentCompleter(
                new BjorneAliasNameArgument("?", context, Argument.MANDATORY, null), token);
        ac.complete(completions, shell);
    }

    public void setEndToken(BjorneToken endToken) {
        this.endToken = endToken;
    }

    public BjorneToken getEndToken() {
        return endToken;
    }

    public void setPenultimateToken(BjorneToken penultimateToken) {
        this.penultimateToken = penultimateToken;
    }

    public BjorneToken getPenultimateToken() {
        return penultimateToken;
    }

    public void setEndExpectedSet(long expectedSet) {
        this.endExpectedSet = expectedSet;
    }

    public void addToEndExpectedSet(long expectedSet) {
        this.endExpectedSet |= expectedSet;
    }

    public long getEndExpectedSet() {
        return endExpectedSet;
    }
    
    public void setPenultimateExpectedSet(long expectedSet) {
        this.penultimateExpectedSet = expectedSet;
    }

    public void addToPenultimateExpectedSet(long expectedSet) {
        this.penultimateExpectedSet |= expectedSet;
    }

    public long getPenultimateExpectedSet() {
        return penultimateExpectedSet;
    }

    public void setCommand(SimpleCommandNode command) {
        this.command = command;
    }

    public SimpleCommandNode getCommand() {
        return command;
    }
    
    @Override
    public String toString() {
        return "BjorneCompleter{endToken=" + BjorneToken.toString(endToken) +
            ",endExpectedSet={" + BjorneToken.formatExpectedSet(endExpectedSet) +
            "},penultimateToken=" + BjorneToken.toString(penultimateToken) +
            ",penultimateExpectedSet={" + BjorneToken.formatExpectedSet(penultimateExpectedSet) + 
            "},command=" + command + "}";
    }
    
}
