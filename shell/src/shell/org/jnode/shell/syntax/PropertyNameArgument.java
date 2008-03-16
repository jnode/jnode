package org.jnode.shell.syntax;

import org.jnode.shell.Completable;
import org.jnode.shell.CommandLine.Token;

public class PropertyNameArgument extends Argument<String> {
    
    public PropertyNameArgument(String label, int flags, String description) {
        super(label, flags, new String[0], description);
    }

    public PropertyNameArgument(String label, int flags) {
        this(label, flags, null);
    }

    public PropertyNameArgument(String label) {
        this(label, 0);
    }
    
    @Override
    public Completable createCompleter(String value, int start, int end) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void doAccept(Token token) throws CommandSyntaxException {
        addValue(token.token);
    }
    
    @Override
    protected String argumentKind() {
        return "property";
    }

}
