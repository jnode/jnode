package org.jnode.net.nfs.nfs2.mount;

public class MountException extends Exception {

    private static final long serialVersionUID = 4594895661006354815L;

    public MountException(String message) {
        super(message);
    }

    public MountException(String message, Throwable e) {
        super(message, e);
    }
}
