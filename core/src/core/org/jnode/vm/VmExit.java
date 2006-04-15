package org.jnode.vm;

/**
 * @author Levente S\u00e1ntha
 */
public class VmExit extends Error {
    private int status;

    public VmExit(int status) {
        super("VM exit, status: " + status);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
