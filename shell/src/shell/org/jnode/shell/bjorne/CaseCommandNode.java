package org.jnode.shell.bjorne;

import org.jnode.shell.ShellException;

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
    public int execute(BjorneContext context) throws ShellException {
        int rc = -1;
        
        CharSequence expandedWord = context.expand(word.token);
        for (CaseItemNode caseItem : caseItems) {
            for (BjorneToken pattern : caseItem.getPattern()) {
                CharSequence pat = context.expand(pattern.token);
                if (context.patternMatch(expandedWord, pat)) {
                    throw new ShellException("not implemented yet");
                }
            }
        }
        
        if ((getFlags() & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
}
