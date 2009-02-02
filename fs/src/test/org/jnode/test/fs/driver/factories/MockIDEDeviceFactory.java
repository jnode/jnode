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
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.jmock.core.stub.ReturnStub;
import org.jnode.driver.Device;
import org.jnode.driver.bus.ide.DefaultIDEControllerDriver;
import org.jnode.driver.bus.ide.IDEBus;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.driver.bus.ide.IDEDeviceFactory;
import org.jnode.driver.bus.ide.IDEDriveDescriptor;
import org.jnode.driver.bus.ide.IDEIO;
import org.jnode.partitions.ibm.IBMPartitionTable;
import org.jnode.partitions.ibm.IBMPartitionTableEntry;
import org.jnode.test.fs.driver.BlockDeviceAPIContext;
import org.jnode.test.fs.driver.Partition;
import org.jnode.test.support.ContextManager;
import org.jnode.test.support.MockInitializer;
import org.jnode.test.support.MockUtils;

public class MockIDEDeviceFactory extends AbstractMockDeviceFactory implements
    IDEDeviceFactory {
    public IDEDevice createIDEDevice(IDEBus bus, boolean primary,
                                     boolean master, String name, IDEDriveDescriptor descriptor,
                                     DefaultIDEControllerDriver controller) {
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockIDEDevice) {
                // Boolean diskChanged = Boolean.FALSE;
                // mockFDC.expects(testCase.atLeastOnce()).method("diskChanged").withAnyArguments().
                // will(new ReturnStub(diskChanged));
            }
        };

        return MockUtils.createMockObject(IDEDevice.class,
            initializer);
    }

    public IDEBus createIDEBus(Device parent, boolean primary) {
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockIDEBus) {
                mockIDEBus.expects(testCase.atLeastOnce()).method(
                    "executeAndWait").withAnyArguments();
            }
        };

        Class[] argCls = new Class[]{Device.class, boolean.class};
        Object[] args = new Object[]{parent, Boolean.valueOf(primary)};

        return (IDEBus) MockUtils.createMockObject(IDEBus.class, initializer,
            argCls, args);
    }

    public IDEIO createIDEIO(Device parent, boolean primary) {
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockIDEIO) {
                Integer irq = new Integer(13);
                mockIDEIO.expects(testCase.atLeastOnce()).method("getIrq")
                    .withNoArguments().will(new ReturnStub(irq));

                mockIDEIO.expects(testCase.atLeastOnce()).method(
                    "setControlReg").withAnyArguments();

                Integer statusReg = new Integer(13);
                mockIDEIO.expects(testCase.atLeastOnce())
                    .method("getStatusReg").withNoArguments().will(new ReturnStub(statusReg));
            }
        };

        // Class[] argCls = new Class[]{Device.class, boolean.class};
        // Object[] args = new Object[]{parent, Boolean.valueOf(primary)};

        // return (IDEIO) MockUtils.createMockObject(IDEIO.class, initializer,
        // argCls, args);
        return MockUtils.createMockObject(IDEIO.class, initializer);
    }

    private IBMPartitionTableEntry createEntry(int partNum,
                                               final boolean extended, final long startLba, final long nbSectors) {
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockEntry) {
                Boolean valid = Boolean.TRUE;
                mockEntry.expects(testCase.atLeastOnce()).method("isValid")
                    .withNoArguments().will(new ReturnStub(valid));

                Boolean bExtended = Boolean.valueOf(extended);
                mockEntry.expects(testCase.atLeastOnce()).method("isExtended")
                    .withNoArguments().will(new ReturnStub(bExtended));

                Long lStartLba = new Long(0);
                mockEntry.expects(testCase.atLeastOnce()).method("getStartLba")
                    .withNoArguments().will(new ReturnStub(lStartLba));

                Long lNbSectors = new Long(nbSectors);
                mockEntry.expects(testCase.atLeastOnce())
                    .method("getNrSectors").withNoArguments().will(new ReturnStub(lNbSectors));
            }
        };

        Class[] argCls = new Class[]{byte[].class, int.class};
        Object[] args = new Object[]{new byte[IDEConstants.SECTOR_SIZE],
            new Integer(partNum)};

        return (IBMPartitionTableEntry) MockUtils.createMockObject(
            IBMPartitionTableEntry.class, initializer, argCls, args);
    }

    public class GetEntryStub implements Stub {
        private Partition[] partitions;

        public GetEntryStub(Partition[] parts) {
            this.partitions = parts;
        }

        public StringBuffer describeTo(StringBuffer buffer) {
            return buffer.append("get partition entry");
        }

        @SuppressWarnings("unchecked")
        public Object invoke(Invocation invocation) throws Throwable {
            int index = ((Integer) invocation.parameterValues.get(0))
                .intValue();
            Partition part = partitions[index];
            IBMPartitionTableEntry entry = createEntry(index,
                part.isExtended(), part.getStartLba(), part.getNbSectors());
            return entry;
        }
    }

    public IBMPartitionTable createIBMPartitionTable(byte[] bs, Device dev) {
        MockInitializer initializer = new MockInitializer() {
            public void init(Mock mockTable) {
                final BlockDeviceAPIContext context = (BlockDeviceAPIContext) ContextManager
                    .getInstance().getContext();
                final Partition[] parts = context.getPartitions();
                log.debug("with " + parts.length + " partitions");

                Integer nbParts = new Integer(parts.length);
                mockTable.expects(testCase.atLeastOnce()).method("getLength")
                    .withNoArguments().will(new ReturnStub(nbParts));

                mockTable.expects(testCase.atLeastOnce()).method("getEntry")
                    .withAnyArguments().will(new GetEntryStub(parts));
            }
        };

        Class[] argCls = new Class[]{byte[].class, Device.class};
        Object[] args = new Object[]{bs, dev};

        return (IBMPartitionTable) MockUtils.createMockObject(
            IBMPartitionTable.class, initializer, argCls, args);
    }
}
