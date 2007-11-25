package org.jnode.shell.bjorne;

import org.jnode.shell.ShellException;

public class IfCommandNode extends CommandNode {

    private final CommandNode cond;

    private final CommandNode thenPart;

    private final CommandNode elsePart;

    public IfCommandNode(int commandType, CommandNode cond,
            CommandNode thenPart, CommandNode elsePart) {
        super(commandType);
        this.cond = cond;
        this.thenPart = thenPart;
        this.elsePart = elsePart;
    }

    public CommandNode getCond() {
        return cond;
    }

    public CommandNode getElsePart() {
        return elsePart;
    }

    public CommandNode getThenPart() {
        return thenPart;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("IfCommand{").append(super.toString());
        sb.append(",cond=").append(cond);
        if (thenPart != null) {
            sb.append(",thenPart=").append(thenPart);
        }
        if (elsePart != null) {
            sb.append(",elsePart=").append(elsePart);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int execute(BjorneContext context) throws ShellException {
        int rc = cond.execute(context);
        if (rc == 0) {
            if (thenPart != null) {
                return thenPart.execute(context);
            }
        } else {
            if (elsePart != null) {
                return elsePart.execute(context);
            }
        }
        if ((getFlags() & BjorneInterpreter.FLAG_BANG) != 0) {
            rc = (rc == 0) ? -1 : 0;
        }
        return rc;
    }
}
