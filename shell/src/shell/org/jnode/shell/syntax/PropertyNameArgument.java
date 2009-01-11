package org.jnode.shell.syntax;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;


/**
 * This argument class accepts property names, with completion against the
 * names in the System properties object.
 * 
 * @author crawley@jnode.org
 */
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
    protected String doAccept(Token token) throws CommandSyntaxException {
        return token.text;
    }
    
    @Override
    public void complete(CompletionInfo completion, String partial) {
        for (Object key : System.getProperties().keySet()) {
            String name = (String) key;
            if (name.startsWith(partial)) {
                completion.addCompletion(name);
            }
        }
    }

    @Override
    protected String argumentKind() {
        return "property";
    }

}
