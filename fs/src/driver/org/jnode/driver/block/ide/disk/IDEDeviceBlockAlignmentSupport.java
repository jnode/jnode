package org.jnode.driver.block.ide.disk;

import org.jnode.driver.block.PartitionableBlockAlignmentSupport;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.partitions.PartitionTableEntry;

/**
 * @author Levente S\u00e1ntha
 */
public class IDEDeviceBlockAlignmentSupport<PTE extends PartitionTableEntry>
    extends PartitionableBlockAlignmentSupport<PTE>
    implements IDEDeviceAPI<PTE> {

    public IDEDeviceBlockAlignmentSupport(IDEDeviceAPI<PTE> parentApi, int alignment) {
        super(parentApi, alignment);
    }
}
