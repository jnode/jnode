package org.jnode.shell.bjorne;

import org.apache.log4j.Logger;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.Completable;
import org.jnode.shell.help.CompletionException;

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
        } else if (command != null) {
            BjorneToken[] words = command.getWords();
            if (words.length > 0 && words[words.length - 1] == penultimateToken) {
                command.complete(completion, context, shell);
            } else if (words.length == 0) {
                new CommandLine(null, null).complete(completion, shell);
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
            ",endExpectedSet=0x" + Long.toHexString(endExpectedSet) + 
            ",penultimateToken=" + toString(penultimateToken) +
            ",penultimateExpectedSet=0x" + Long.toHexString(penultimateExpectedSet) + ",command=" + command + "}";
    }
    
    private String toString(BjorneToken token) {
        return token != null ? (token + "/" + token.start + "/" + token.end) : "null";
    }
}
