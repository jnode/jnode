/*
 * $Id$
 */
package org.jnode.install.action;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.fs.jfat.command.JGrub;
import org.jnode.install.AbstractInstaller;
import org.jnode.install.ActionInput;
import org.jnode.install.ActionOutput;
import org.jnode.install.InputContext;
import org.jnode.install.InstallerAction;
import org.jnode.install.OutputContext;

/**
 * @author Levente S\u00e1ntha
*/
public class GrubInstallerAction implements InstallerAction {
    private JGrub jgrub;
    public ActionInput getInput(final InputContext inContext) {
        return new ActionInput() {
            public AbstractInstaller.Step collect() {
                try {
                    String deviceID = inContext.getStringInput("Enter the installation disk device name (example: hda0) : ");

                    Device disk = DeviceUtils.getDevice(deviceID);
                    JGrub jgrub = new JGrub(System.out, System.err, disk);

                    inContext.setStringValue(ActionConstants.INSTALL_ROOT_DIR, jgrub.getMountPoint());
                    return AbstractInstaller.Step.forth;
                } catch(Exception e){
                    return AbstractInstaller.Step.back;
                }
            }
        };
    }

    public void execute() throws Exception {
        jgrub.install();
    }

    public ActionOutput getOutput(OutputContext outContext) {
        return null;
    }
}
