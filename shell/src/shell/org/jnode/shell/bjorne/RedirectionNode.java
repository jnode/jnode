package org.jnode.shell.bjorne;

public class RedirectionNode {
    public final int redirectionType;

    public final BjorneToken io;

    public final BjorneToken arg;

    public RedirectionNode(final int redirectionType, BjorneToken io,
            BjorneToken arg) {
        super();
        this.redirectionType = redirectionType;
        this.io = io;
        this.arg = arg;
    }

    public BjorneToken getArg() {
        return arg;
    }

    public BjorneToken getIo() {
        return io;
    }

    public int getRedirectionType() {
        return redirectionType;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Redirect{");
        sb.append("redirectionType=").append(redirectionType);
        if (io != null) {
            sb.append(",io=").append(io);
        }
        if (arg != null) {
            sb.append(",arg=").append(arg);
        }
        sb.append("}");
        return sb.toString();
    }
}
