package org.jnode.fs.nfs.nfs2.rpc.mount;

public class MountException extends Exception {

    public MountException(String message) {
	super(message);
    }

    public MountException(String message, Throwable e) {
	super(message, e);
    }

}
