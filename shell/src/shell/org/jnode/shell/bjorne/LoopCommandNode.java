package org.jnode.shell.bjorne;

import org.jnode.shell.ShellException;
import static org.jnode.shell.bjorne.BjorneInterpreter.*;

public class LoopCommandNode extends CommandNode {
    private final CommandNode cond;

    private final CommandNode body;

    public LoopCommandNode(int nodeType, CommandNode cond, CommandNode body) {
        super(nodeType);
        this.body = body;
        this.cond = cond;
    }

    public CommandNode getBody() {
        return body;
    }

    public CommandNode getCond() {
        return cond;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LoopCommand{").append(super.toString());
        sb.append(",cond=").append(cond);
        sb.append(",body=").append(body);
        sb.append('}');
        return sb.toString();
    }

    public int execute(BjorneContext context) throws ShellException {
        int rc = 0;
        while (true) {
            rc = cond.execute(context);
            if (context.getLastReturnCode() == 0 ^ getNodeType() == CMD_UNTIL) {
                break;
            }
            try {
                if (body != null) {
                    body.execute(context);
                }
            } catch (BjorneControlException ex) {
                int control = ex.getControl();
                if (control == BRANCH_BREAK || control == BRANCH_CONTINUE) {
                    if (ex.getCount() > 1) {
                        ex.decrementCount();
                        throw ex;
                    }
                    if (control == BRANCH_BREAK) {
                        break;
                    } else {
                        continue;
                    }
                } else {
                    throw ex;
                }
            }
        }
        return rc;
    }
}
