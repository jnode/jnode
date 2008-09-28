package org.jnode.shell.isolate;

import javax.isolate.Isolate;
import javax.isolate.Link;

import org.jnode.shell.CommandRunner;
import org.jnode.vm.isolate.link.ObjectLinkMessage;

public class IsolateCommandLauncher {

    /**
     * @param args
     */
    public static void main(String[] args) {
        Link cl = Isolate.getLinks()[0];
        CommandRunner cr;
        try {
            ObjectLinkMessage message = (ObjectLinkMessage) cl.receive();
            cr = (CommandRunner) message.extract();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        cr.run();
    }
}
