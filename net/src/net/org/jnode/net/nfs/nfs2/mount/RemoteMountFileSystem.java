package org.jnode.net.nfs.nfs2.mount;

public class RemoteMountFileSystem {

    private String host;

    private String remoteDirectory;

    public RemoteMountFileSystem(String host, String remoteDirectory) {
        this.host = host;
        this.remoteDirectory = remoteDirectory;
    }

    public String getHost() {
        return host;
    }

    public String getRemoteDirectory() {
        return remoteDirectory;
    }
}
