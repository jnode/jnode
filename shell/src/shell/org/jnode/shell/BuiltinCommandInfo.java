package org.jnode.shell;

import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.SyntaxBundle;

public abstract class BuiltinCommandInfo extends CommandInfo {

    public BuiltinCommandInfo(Class<?> clazz, String commandName, SyntaxBundle syntaxBundle,
            ArgumentBundle argBundle, Command instance) {
        super(clazz, commandName, syntaxBundle, argBundle, instance);
    }

}
