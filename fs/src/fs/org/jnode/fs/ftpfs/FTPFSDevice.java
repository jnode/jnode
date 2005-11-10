package org.jnode.fs.ftpfs;

import org.jnode.driver.Device;
import org.jnode.driver.Bus;

/**
 * @author Levente S\u00e1ntha
 */
public class FTPFSDevice extends Device {
    private String host;
    private String user;
    private String password;
    
    public FTPFSDevice(String host, String user, String password) {
        super(null, "ftp-(" + host +"," + user + ")");
        this.host = host;
        this.user = user;
        this.password = password;
    }

    String getHost() {
        return host;
    }

    String getPassword() {
        return password;
    }

    String getUser() {
        return user;
    }
}
