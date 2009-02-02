/*
 * $Id$
 *
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

import java.io.File;
import org.jnode.install.AbstractInstaller;
import org.jnode.install.ActionInput;
import org.jnode.install.ActionOutput;
import org.jnode.install.CopyFile;
import org.jnode.install.InputContext;
import org.jnode.install.InstallerAction;
import org.jnode.install.OutputContext;
import org.jnode.install.ProgressEvent;
import org.jnode.install.ProgressListener;

/**
 * @author Levente S\u00e1ntha
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
