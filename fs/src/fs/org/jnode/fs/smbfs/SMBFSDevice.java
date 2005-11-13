package org.jnode.fs.smbfs;

import org.jnode.driver.Device;
import org.jnode.driver.Bus;

/**
 * @author Levente S\u00e1ntha
 */
public class SMBFSDevice extends Device {
    private String host;
    private String user;
    private String password;
    private String path;

    public SMBFSDevice(String host, String path, String user, String password) {
        super(null, "smb-(" + host +"," + path + "," + user + ")");
        this.host = host;
        this.path = path;
        this.user = user;
        this.password = password;
        System.setProperty("java.protocol.handler.pkgs","jcifs");
    }

    String getHost() {
        return host;
    }

    String getPath() {
        return path;
    }

    String getPassword() {
        return password;
    }

    String getUser() {
        return user;
    }
    public SMBFSDevice(Bus bus, String id) {
        super(bus, id);
    }
}
