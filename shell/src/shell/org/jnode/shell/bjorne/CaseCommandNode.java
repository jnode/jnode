package org.jnode.shell.bjorne;

public class CaseCommandNode extends CommandNode {

    private final BjorneToken word;

    private final CaseItemNode[] caseItems;

    public CaseCommandNode(BjorneToken word, CaseItemNode[] caseItems) {
        super(BjorneInterpreter.CMD_CASE);
        this.word = word;
        this.caseItems = caseItems;
    }

    public CaseItemNode[] getCaseItems() {
        return caseItems;
    }

    public BjorneToken getWord() {
        return word;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CaseCommand{").append(super.toString());
        sb.append(",word=").append(word);
        if (caseItems != null) {
            sb.append(",caseItems=");
            CommandNode.appendArray(sb, caseItems);
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) {
        return -1;
    }
}
