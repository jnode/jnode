package org.jnode.shell.bjorne;

import org.jnode.shell.BuiltinCommandInfo;
import org.jnode.shell.syntax.SyntaxBundle;

public class BjorneBuiltinCommandInfo extends BuiltinCommandInfo {

    public BjorneBuiltinCommandInfo(String commandName, SyntaxBundle syntaxBundle,
            BjorneBuiltin instance, BjorneContext context) {
        super(instance.getClass(), commandName, syntaxBundle, instance.getArgumentBundle(), instance);
        instance.setParentContext(context.getParent());
    }
}
