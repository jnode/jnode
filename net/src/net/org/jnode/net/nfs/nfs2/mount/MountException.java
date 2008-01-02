package org.jnode.net.nfs.nfs2.mount;

public class MountException extends Exception {

    public MountException(String message) {
        super(message);
    }

    public MountException(String message, Throwable e) {
        super(message, e);
    }

}
