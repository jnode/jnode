package org.jnode.shell.help.argument;

import java.util.Collection;

public abstract class StringListArgument extends ListArgument<String> {
    public StringListArgument(String name, String description, boolean multi) {
        super(name, description, multi);
    }

    public StringListArgument(String name, String description) {
        super(name, description, false);
    }

    @Override
    public String getArgValue(String value) {
        return value;
    }

    @Override
    protected String toStringArgument(String arg) {
        return arg;
    }

    @Override
    protected boolean isPartOfArgument(String argument, String part) {
        return argument.startsWith(part);
    }

    @Override
    public int compare(String choice1, String choice2) {
        return choice1.compareTo(choice2);
    }

    protected abstract Collection<String> getValues();
}
