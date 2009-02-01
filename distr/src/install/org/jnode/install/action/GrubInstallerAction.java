/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.install.action;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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
                    String deviceID =
                        inContext.getStringInput("Enter the installation disk device name (example: hda0) : ");

                    Device disk = DeviceUtils.getDevice(deviceID);
                    JGrub jgrub = new JGrub(new PrintWriter(new OutputStreamWriter(System.out)), disk);

                    inContext.setStringValue(ActionConstants.INSTALL_ROOT_DIR, jgrub.getMountPoint());
                    return AbstractInstaller.Step.forth;
                } catch (Exception e) {
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
