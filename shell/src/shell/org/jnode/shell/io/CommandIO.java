package org.jnode.shell.io;

public interface CommandIO {

    public static final int DIRECTION_IN = 1;
    public static final int DIRECTION_OUT =  2;
    public static final int DIRECTION_INOUT = 3;  // Not yet used
    
    public int getDirection();
    
    public boolean isTTY();
    
    public Object getSystemObject();

    public String getEncoding();
    
    public String getAssignedEncoding();

}
