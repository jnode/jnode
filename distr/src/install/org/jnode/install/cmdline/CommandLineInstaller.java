/*
 * $Id$
 */
package org.jnode.install.cmdline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jnode.install.AbstractInstaller;
import org.jnode.install.InputContext;
import org.jnode.install.OutputContext;
import org.jnode.install.action.CopyFilesAction;
import org.jnode.install.action.GrubInstallerAction;

/**
 * @author Levente S\u00e1ntha
 */
public class CommandLineInstaller extends AbstractInstaller {

    public CommandLineInstaller() {
        //grub
        actionList.add(new GrubInstallerAction());
        //files
        actionList.add(new CopyFilesAction());
    }

    public static void main(String... argv) {
        new CommandLineInstaller().start();
    }


    protected InputContext getInputContext() {
        return new InputContext() {
            private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            public String getStringInput(String message) {
                try {
                    System.out.println(message);
                    return in.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected OutputContext getOutputContext() {
        return new OutputContext() {
            public void showMessage(String msg) {
                System.out.println(msg);
            }
        };
    }
}
