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
 
package org.jnode.apps.jpartition;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import junit.framework.TestSuite;

import org.jnode.apps.jpartition.consoleview.ConsoleViewFactory;
import org.jnode.apps.jpartition.model.TestEmptyDevice;
import org.jnode.apps.jpartition.model.TestNonEmptyDevice;
import org.jnode.apps.jpartition.model.TestOSFacade;
import org.jnode.apps.jpartition.model.TestRemovePartitionFromDevice;
import org.jnode.apps.jpartition.swingview.FileDeviceView;
import org.jnode.apps.jpartition.utils.device.AbstractIDEDevice;
import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.jnode.fs.jfat.command.JGrub;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Class used for both running all the JUnit tests of JPartition and 
 * an emulated version of JPartition's console mode. 
 * 
 * @author fabien
 *
 */
@RunWith(Suite.class)
@SuiteClasses({TestNonEmptyDevice.class, TestEmptyDevice.class,
    TestRemovePartitionFromDevice.class, TestOSFacade.class })
public class JPartitionTest extends TestSuite {
    static {
        // when not in JNode, must be called before anything
        // invoking InitialNaming
        DeviceUtils.initJNodeCore();
    }

    /**
     * Main method used for testing purpose.
     * @param args
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable {
        final ViewFactory vf = new ConsoleViewFactory(System.in, System.out, System.err);
        final ErrorReporter errorReporter = vf.createErrorReporter();
        final Thread t = new Thread() {
            public void run() {
                try {
                    new FileDeviceView(errorReporter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();

        // DeviceUtils.createFakeDevice(new ErrorReporter());
        AbstractIDEDevice dev = DeviceUtils.createFileDevice();
        JGrub jgrub = new JGrub(new PrintWriter(new OutputStreamWriter(System.out)), dev);
        jgrub.install();

        JPartitionCommand.main(args);
    }
}
