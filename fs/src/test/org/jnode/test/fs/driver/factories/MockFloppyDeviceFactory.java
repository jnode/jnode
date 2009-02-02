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
 
package org.jnode.test.fs.driver.factories;

import org.jmock.cglib.Mock;
import org.jmock.core.stub.ReturnStub;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.CHS;
import org.jnode.driver.block.Geometry;
import org.jnode.driver.block.floppy.FDC;
import org.jnode.driver.block.floppy.FloppyControllerBus;
import org.jnode.driver.block.floppy.FloppyDevice;
import org.jnode.driver.block.floppy.FloppyDriveParameters;
import org.jnode.driver.block.floppy.FloppyDriveParametersCommand;
import org.jnode.driver.block.floppy.FloppyDriver;
import org.jnode.driver.block.floppy.FloppyIdCommand;
import org.jnode.driver.block.floppy.FloppyParameters;
import org.jnode.driver.block.floppy.FloppyReadSectorCommand;
import org.jnode.driver.block.floppy.FloppySeekCommand;
import org.jnode.driver.block.floppy.FloppyWriteSectorCommand;
import org.jnode.driver.block.floppy.support.FloppyDeviceFactory;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.test.support.MockInitializer;
import org.jnode.test.support.MockUtils;

public class MockFloppyDeviceFactory extends AbstractMockDeviceFactory
    implements FloppyDeviceFactory {
    public MockFloppyDeviceFactory() {
    }

//    public class SetDriverStub implements Stub
//    {
//        public StringBuffer describeTo( StringBuffer buffer ) {
//            return buffer.append("set the Driver for a Device");
//        }
//
//        public Object invoke( Invocation invocation ) throws Throwable {
//            invocation.invokedMethod.invoke(invocation.invokedObject,
//                  new Object[]{invocation.parameterValues.get(0)});
//            //((Device)invocation.invokedObject).setDriver((Driver) invocation.parameterValues.get(0));
//            
//            return null;
//        }
//    }

    ////////////////////////////////////////////////////
    //   FloppyDeviceFactory interface implementation //
    ////////////////////////////////////////////////////

    public FloppyDevice createDevice(FloppyControllerBus bus, int drive, FloppyDriveParameters dp) {
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockFloppyDevice) {
//                mockFloppyDevice.expects(testCase.atLeastOnce()).method("setDriver").withAnyArguments().will(
//                        new SetDriverStub());

                //mockFloppyDevice.expects(testCase.atLeastOnce()).method("setDriver").withAnyArguments();
                //mockFloppyDevice.expects(testCase.once()).method("setDriver").
                // with(new IsInstanceOf(FloppyDriver.class));
                //mockFloppyDevice.expects(testCase.once()).method("setDriver").with(new IsInstanceOf(Driver.class));

                FloppyDriver driver = new FloppyDriver();
                mockFloppyDevice.expects(testCase.atLeastOnce()).method("getDriver").withNoArguments().
                    will(new ReturnStub(driver));
            }

        };

        Class[] argCls = new Class[]{FloppyControllerBus.class, int.class, FloppyDriveParameters.class};
        Object[] args = new Object[]{bus, new Integer(drive), dp};
        //return (FloppyDevice) MockUtils.createMockObject(FloppyDevice.class, initializer, argCls, args);
        return new FloppyDevice(bus, drive, dp);
    }

    public FDC createFDC(Device device) throws DriverException, ResourceNotFreeException {
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockFDC) {
                Integer nbStubFloppy = new Integer(1);
                mockFDC.expects(testCase.once()).method("getDriveCount").withNoArguments().
                    will(new ReturnStub(nbStubFloppy));

                FloppyDriveParameters drvParams = createFloppyDriveParameters();
                mockFDC.expects(testCase.atLeastOnce()).method("getDriveParams").withAnyArguments().
                    will(new ReturnStub(drvParams));

                Boolean diskChanged = Boolean.FALSE;
                mockFDC.expects(testCase.atLeastOnce()).method("diskChanged").withAnyArguments().
                    will(new ReturnStub(diskChanged));

                mockFDC.expects(testCase.once()).method("release").withNoArguments();
                mockFDC.expects(testCase.once()).method("reset").withNoArguments();
                mockFDC.expects(testCase.atLeastOnce()).method("executeAndWait").withAnyArguments();
            }
        };

        return MockUtils.createMockObject(FDC.class, initializer);
    }
    ////////////////////////////////////////////////////

    //////////////////////////////
    //   Private factory method //
    //////////////////////////////

    private FloppyDriveParameters createFloppyDriveParameters() {
        int cmosType = 1; // non-zero value for a drive that is present
        FloppyParameters[] fp = {
            //new FloppyParameters(new Geometry(16, 16, 16),  0,  0, 0, "16x16x16")
            new FloppyParameters(new Geometry(2, 2, 2), 0, 0, 0, "2x2x2")
        };

        return new FloppyDriveParameters(cmosType, 0, 0, 0, 0, "StubFloppyParam", fp);
    }

    public FloppyDriveParametersCommand createFloppyDriveParametersCommand(int drive, FloppyDriveParameters dp,
                                                                           FloppyParameters fp) {
        Class[] argCls = new Class[]{int.class, FloppyDriveParameters.class, FloppyParameters.class};
        Object[] args = new Object[]{new Integer(drive), dp, fp};
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockCmd) {
                mockCmd.expects(testCase.atLeastOnce()).method("hasError").withNoArguments().
                    will(new ReturnStub(Boolean.FALSE));
            }
        };

        FloppyDriveParametersCommand cmd = (FloppyDriveParametersCommand) MockUtils
            .createMockObject(FloppyDriveParametersCommand.class, initializer, argCls, args);
        return cmd;
    }

    public FloppySeekCommand createFloppySeekCommand(int drive, int cylinder) {
        Class[] argCls = new Class[]{int.class, int.class};
        Object[] args = new Object[]{new Integer(drive), new Integer(cylinder)};
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockFloppyDevice) {
//                FloppyDriver driver = new FloppyDriver();        
//                mockFloppyDevice.expects(testCase.atLeastOnce()).method("getDriver").withNoArguments().
//                        will(new ReturnStub(driver));
            }
        };

        FloppySeekCommand cmd =
            (FloppySeekCommand) MockUtils.createMockObject(FloppySeekCommand.class, initializer, argCls, args);
        return cmd;
    }

    public FloppyReadSectorCommand createFloppyReadSectorCommand(int drive, Geometry geometry, CHS chs,
                                                                 int currentSectorSize, boolean b, int gap1Size,
                                                                 byte[] dest, int destOffset) {
        Class[] argCls = new Class[]{int.class, Geometry.class, CHS.class, int.class, boolean.class, int.class,
            byte[].class, int.class};
        Object[] args = new Object[]{new Integer(drive), geometry, chs, new Integer(currentSectorSize),
            Boolean.valueOf(b), new Integer(gap1Size), dest, new Integer(destOffset)};
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockCmd) {
                mockCmd.expects(testCase.atLeastOnce()).method("hasError").withNoArguments().
                    will(new ReturnStub(Boolean.FALSE));
            }
        };

        FloppyReadSectorCommand cmd = (FloppyReadSectorCommand) MockUtils
            .createMockObject(FloppyReadSectorCommand.class, initializer, argCls, args);
        return cmd;
    }

    public FloppyWriteSectorCommand createFloppyWriteSectorCommand(int drive, Geometry geometry, CHS chs,
                                                                   int currentSectorSize, boolean b, int gap1Size,
                                                                   byte[] src, int srcOffset) {
        Class[] argCls = new Class[]{int.class, Geometry.class, CHS.class, int.class, boolean.class, int.class,
            byte[].class, int.class};
        Object[] args = new Object[]{new Integer(drive), geometry, chs, new Integer(currentSectorSize),
            Boolean.valueOf(b), new Integer(gap1Size), src, new Integer(srcOffset)};
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockCmd) {
                mockCmd.expects(testCase.atLeastOnce()).method("hasError").withNoArguments().
                    will(new ReturnStub(Boolean.FALSE));
            }
        };

        FloppyWriteSectorCommand cmd = (FloppyWriteSectorCommand) MockUtils
            .createMockObject(FloppyWriteSectorCommand.class, initializer, argCls, args);
        return cmd;
    }

    public FloppyIdCommand createFloppyIdCommand(int drive) {
        Class[] argCls = new Class[]{int.class};
        Object[] args = new Object[]{new Integer(drive)};
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockCmd) {
                mockCmd.expects(testCase.atLeastOnce()).method("hasError").withNoArguments().
                    will(new ReturnStub(Boolean.FALSE));

                Integer sectorSizeIdx = new Integer(2); //FloppyConstants.SECTOR_LENGTH[2]=512
                mockCmd.expects(testCase.atLeastOnce()).method("getSectorSize").withNoArguments().
                    will(new ReturnStub(sectorSizeIdx));
            }
        };

        FloppyIdCommand cmd =
            (FloppyIdCommand) MockUtils.createMockObject(FloppyIdCommand.class, initializer, argCls, args);
        return cmd;
    }
}
