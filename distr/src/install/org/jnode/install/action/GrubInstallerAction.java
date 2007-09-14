/*
 * $Id$
 */
package org.jnode.install.action;

import org.jnode.fs.jfat.command.JGrubInstallCommand;
import org.jnode.install.*;

/**
 * @author Levente S\u00e1ntha
*/
public class GrubInstallerAction implements InstallerAction {
    private String disk;
    private String partition;
    private String targetDirectory;
    public ActionInput getInput(final InputContext inContext) {
        return new ActionInput() {
            public AbstractInstaller.Step collect() {
                try {
                    disk = inContext.getStringInput("Enter the installation disk device name: ");
                    partition = inContext.getStringInput("Enter the partition number: ");
                    targetDirectory = "/devices/" + disk + partition + "/";
                    inContext.setStringValue(ActionConstants.INSTALL_ROOT_DIR, targetDirectory);
                    return AbstractInstaller.Step.forth;
                } catch(Exception e){
                    return AbstractInstaller.Step.back;
                }
            }
        };
    }

    public void execute() throws Exception {
        JGrubInstallCommand.main(disk,"-p", partition, targetDirectory);
    }

    public ActionOutput getOutput(OutputContext outContext) {
        return null;
    }
}
