/*
 * $Id$
 */
package org.jnode.install.action;

import org.jnode.install.InstallerAction;
import org.jnode.install.ActionInput;
import org.jnode.install.CopyFile;
import org.jnode.install.ProgressListener;
import org.jnode.install.ProgressEvent;
import org.jnode.install.ActionOutput;
import org.jnode.install.InputContext;
import org.jnode.install.OutputContext;
import org.jnode.install.AbstractInstaller;
import java.io.File;

/**
 * @author Levente Sántha
*/
public class CopyFilesAction implements InstallerAction {
    private String targetDirectory;
    public ActionInput getInput(final InputContext inContext) {
        return new ActionInput() {
            public AbstractInstaller.Step collect() {
                targetDirectory = inContext.getStringValue(ActionConstants.INSTALL_ROOT_DIR);
                return AbstractInstaller.Step.forth;
            }
        };
    }

    public void execute() throws Exception {
        System.out.println("Installing jnode32.gz ...");
        CopyFile cf = new CopyFile(true);
        cf.setSource(new File("/devices/sg0/jnode32.gz"));
        cf.setDestination(new File(targetDirectory + "jnode32.gz"));
        cf.addProgressListener(new ProgressListener() {
            public void progress(ProgressEvent e) {
                System.out.print('.');
            }
        });
        cf.execute();
        System.out.println("Done.");

        System.out.println("Installing full.jgz ...");
        cf = new CopyFile(true);
        cf.setSource(new File("/devices/sg0/full.jgz"));
        cf.setDestination(new File(targetDirectory + "full.jgz"));
        cf.addProgressListener(new ProgressListener() {
            public void progress(ProgressEvent e) {
                System.out.print('.');
            }
        });
        cf.execute();
        System.out.println("Done.");
    }

    public ActionOutput getOutput(OutputContext outContext) {
        return null;
    }
}
