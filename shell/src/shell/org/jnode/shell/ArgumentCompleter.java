package org.jnode.shell;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.CompletionException;

public class ArgumentCompleter implements Completable {
    
    private final Argument argument;
    private final CommandLine.Token token;
    
    public ArgumentCompleter(Argument argument, CommandLine.Token token) {
        this.argument = argument;
        this.token = token;
    }

    public void complete(CompletionInfo completion, CommandShell shell)
    throws CompletionException {
        argument.complete(completion, token == null ? "" : token.token);
        if (token != null) {
            completion.setCompletionStart(token.start);
        }
    }

}
