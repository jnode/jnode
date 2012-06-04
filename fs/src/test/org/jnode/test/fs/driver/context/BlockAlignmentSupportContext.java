/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.test.fs.driver.context;

import org.jmock.MockObjectTestCase;
import org.jnode.driver.block.BlockAlignmentSupport;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.test.fs.driver.BlockDeviceAPIContext;
import org.jnode.test.support.TestConfig;

public class BlockAlignmentSupportContext extends BlockDeviceAPIContext {
    public BlockAlignmentSupportContext() {
        super("BlockAlignmentSupport");
    }

    public void init(TestConfig config, MockObjectTestCase testCase) throws Exception {
        super.init(config, testCase);

        BlockDeviceAPIContext parentCtx = createParentBlockDeviceAPI();
        BlockDeviceAPI api = new BlockAlignmentSupport(parentCtx.getApi(), 512);
        init(parentCtx, api, null);
    }
}
