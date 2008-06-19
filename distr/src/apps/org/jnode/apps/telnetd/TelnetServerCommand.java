package org.jnode.apps.telnetd;

import java.io.InputStream;
import java.util.Properties;

import net.wimpi.telnetd.TelnetD;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class TelnetServerCommand {
    public static void main(String[] args) throws Exception {
        try {
            // 1. prepare daemon
            Properties props = new Properties();
            InputStream is = TelnetServerCommand.class.getResourceAsStream("telnetd.properties");
            props.load(is);

            TelnetD daemon = TelnetD.createTelnetD(props);

            // 2.start serving/accepting connections
            daemon.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
