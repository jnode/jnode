package org.jnode.shell.io;

public abstract class AbstractCommandIO implements CommandIO {
    
    private String assignedEncoding;
    
    @Override
    public final String getAssignedEncoding() {
        return assignedEncoding;
    }

    @Override
    public abstract int getDirection();

    @Override
    public final String getEncoding() {
        return assignedEncoding != null ? assignedEncoding : getImpliedEncoding();
    }

    protected abstract String getImpliedEncoding();

    @Override
    public abstract Object getSystemObject();

    @Override
    public abstract boolean isTTY();
}
