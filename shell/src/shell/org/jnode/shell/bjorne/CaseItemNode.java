package org.jnode.shell.bjorne;

public class CaseItemNode {
    private final BjorneToken[] pattern;

    private final CommandNode body;

    public CaseItemNode(BjorneToken[] pattern, CommandNode body) {
        this.pattern = pattern;
        this.body = body;
    }

    public CommandNode getBody() {
        return body;
    }

    public BjorneToken[] getPattern() {
        return pattern;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CaseItem{");
        if (pattern != null) {
            sb.append(",pattern=");
            CommandNode.appendArray(sb, pattern);
        }
        if (body != null) {
            sb.append(",body=").append(body);
        }
        sb.append("}");
        return sb.toString();
    }
}
