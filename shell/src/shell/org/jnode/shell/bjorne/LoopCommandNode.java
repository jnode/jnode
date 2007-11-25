package org.jnode.shell.bjorne;

import org.jnode.shell.ShellException;

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
            if (context.getLastReturnCode() == 0 ^ 
                getNodeType() == BjorneInterpreter.CMD_UNTIL) {
                break;
            }
            try {
                if (body != null) {
                    body.execute(context);
                }
            } catch (BjorneControlException ex) {
                int control = ex.getControl();
                if (control == BjorneInterpreter.BRANCH_BREAK || 
                    control == BjorneInterpreter.BRANCH_CONTINUE) {
                    if (ex.getCount() > 1) {
                        ex.decrementCount();
                        throw ex;
                    }
                    if (control == BjorneInterpreter.BRANCH_BREAK) {
                        break;
                    } else {
                        continue;
                    }
                } else {
                    throw ex;
                }
            }
        }
        if ((getFlags() & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
}
