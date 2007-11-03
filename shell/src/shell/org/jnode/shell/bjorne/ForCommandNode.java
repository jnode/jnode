package org.jnode.shell.bjorne;

public class ForCommandNode extends CommandNode {
    private final CommandNode body;

    private final BjorneToken var;

    private final BjorneToken[] words;

    public ForCommandNode(BjorneToken var, BjorneToken[] words, CommandNode body) {
        super(BjorneInterpreter.CMD_FOR);
        this.body = body;
        this.var = var;
        this.words = words;
    }

    public CommandNode getBody() {
        return body;
    }

    public BjorneToken getVar() {
        return var;
    }

    public BjorneToken[] getWords() {
        return words;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LoopCommand{").append(super.toString());
        sb.append(",var=").append(var);
        if (words != null) {
            sb.append(",words=");
            appendArray(sb, words);
        }
        sb.append(",body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) {
        return -1;
    }
}
