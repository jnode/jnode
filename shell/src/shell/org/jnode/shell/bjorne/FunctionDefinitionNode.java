package org.jnode.shell.bjorne;

public class FunctionDefinitionNode extends CommandNode {
    private final BjorneToken name;

    private final CommandNode body;

    public FunctionDefinitionNode(final BjorneToken name, final CommandNode body) {
        super(BjorneInterpreter.CMD_FUNCTION_DEF);
        this.name = name;
        this.body = body;
    }

    public CommandNode getBody() {
        return body;
    }

    public BjorneToken getName() {
        return name;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FunctionDefinition{").append(super.toString());
        sb.append(",name=").append(name);
        sb.append(",body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) {
        return -1;
    }
}
