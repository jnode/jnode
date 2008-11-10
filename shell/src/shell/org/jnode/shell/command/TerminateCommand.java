package org.jnode.shell.command;

import java.io.PrintWriter;

import javax.isolate.Isolate;
import javax.isolate.IsolateStatus;
import javax.isolate.Link;
import javax.isolate.LinkMessage;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.vm.isolate.VmIsolate;

/**
 * Terminates an isolate.
 * The isolate to terminate is specified by its integer identifier.
 *
 * @author Levente S\u00e1ntha
 */
public class TerminateCommand extends AbstractCommand {
    private final IntegerArgument ARG_ID = new IntegerArgument(
            "id", Argument.MANDATORY | Argument.SINGLE,
            "the identifier of the isolate to terminate");

    public TerminateCommand() {
        super("Terminate an isolate");
        registerArguments(ARG_ID);
    }

    public void execute() throws Exception {
        PrintWriter err = getError().getPrintWriter();
        PrintWriter out = getOutput().getPrintWriter();
        Integer id = ARG_ID.getValue();
        if (id.equals(VmIsolate.getRoot().getId())) {
            err.println("The root isolate cannot be terminated with this command.");
            exit(1);
        }

        VmIsolate[] iso_arr = VmIsolate.getVmIsolates();
        Isolate iso = null;
        for (VmIsolate vmi : iso_arr) {
            if (id.equals(vmi.getId())) {
                iso = vmi.getIsolate();
                break;
            }
        }

        if (iso == null) {
            err.println("Isolate not found: " + id);
            exit(1);
        }

        Link link = iso.newStatusLink();
        iso.exit(-1);
        while (true) {
            LinkMessage msg = link.receive();
            if (msg.containsStatus()) {
                IsolateStatus is = msg.extractStatus();
                if (is.getState().equals(IsolateStatus.State.EXITED)) {
                    out.println("Done.");
                    break;
                }
            }
        }
    }
}
