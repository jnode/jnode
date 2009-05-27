package org.jnode.shell.bjorne;

import org.apache.log4j.Logger;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Completable;
import org.jnode.shell.help.CompletionException;
import static org.jnode.shell.bjorne.BjorneToken.*;

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
    public void complete(CompletionInfo completion, CommandShell shell) throws CompletionException {
        Logger.getLogger(BjorneCompleter.class).debug(toString());
        if (endToken == null) {
            new CommandLine(null, null).complete(completion, shell);
            return;
        }
        if (command != null) {
            BjorneToken[] words = command.getWords();
            if (words.length > 0 && words[words.length - 1] == penultimateToken) {
                boolean argumentAnticipated = penultimateToken.end < endToken.end;
                command.complete(completion, context, shell, argumentAnticipated);
            } else if (words.length == 0) {
                new CommandLine(null, null).complete(completion, shell);
            }
        }
        String partial;
        long expectedSet;
        if (penultimateToken == null || penultimateToken.end < endToken.end) {
            partial = "";
            expectedSet = endExpectedSet;
            completion.setCompletionStart(endToken.start);
        } else {
            partial = penultimateToken.unparse();
            expectedSet = penultimateExpectedSet | endExpectedSet;
            completion.setCompletionStart(penultimateToken.start);
        }
        Logger.getLogger(BjorneCompleter.class).debug(
                "Combined expected set = " + BjorneToken.formatExpectedSet(expectedSet));
        Logger.getLogger(BjorneCompleter.class).debug("partial = '" + partial + "'");
        expectedSet &= ~(TOK_END_OF_LINE_BIT | TOK_END_OF_STREAM_BIT | TOK_WORD_BIT | 
                TOK_NAME_BIT | TOK_ASSIGNMENT_BIT | TOK_IO_NUMBER_BIT);
        for (int i = 0; i < 64; i++) {
            if (((1L << i) & expectedSet) == 0) {
                continue;
            }
            String candidate = BjorneToken.toString(i);
            if (candidate.startsWith(partial)) {
                completion.addCompletion(candidate);
            }
        }
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
        return "BjorneCompleter{endToken=" + toString(endToken) +
            ",endExpectedSet={" + BjorneToken.formatExpectedSet(endExpectedSet) +
            "},penultimateToken=" + toString(penultimateToken) +
            ",penultimateExpectedSet={" + BjorneToken.formatExpectedSet(penultimateExpectedSet) + 
            "},command=" + command + "}";
    }
    
    private String toString(BjorneToken token) {
        return token != null ? (token + "/" + token.start + "/" + token.end) : "null";
    }
}
