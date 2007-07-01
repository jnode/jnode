/*
 * $Id$
 */
package org.jnode.install.cmdline;

import org.jnode.install.*;
import org.jnode.install.action.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * @author Levente Sántha
 */
public class CommandLineInstaller extends AbstractInstaller {

    public CommandLineInstaller() {
        //grub
        actionList.add(new GrubInstallerAction());
        //files
        actionList.add(new CopyFilesAction());
    }

    public static void main(String...argv){
        new CommandLineInstaller().start();
    }


    protected InputContext getInputContext() {
        return new InputContext() {
            private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            public String getStringInput(String message) {
                try {
                    System.out.println(message);
                    return in.readLine();
                }catch(IOException e){
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
