package org.jnode.apps.jpartition.consoleview;

import org.jnode.apps.jpartition.consoleview.components.Labelizer;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.util.NumberUtils;

class PartitionLabelizer implements Labelizer<Partition> {
    static final PartitionLabelizer INSTANCE = new PartitionLabelizer();

    public String getLabel(Partition partition) {
        if (partition == null) {
            throw new NullPointerException("partition is null");
        }

        StringBuilder sb = new StringBuilder();

        sb.append('[').append(partition.getStart()).append(',').append(partition.getEnd()).append(
                ']');
        sb.append(" (").append(NumberUtils.toBinaryByte(partition.getSize())).append(") ");
        String format = partition.isUsed() ? partition.getFormat() : "unused";
        sb.append(format);

        return sb.toString();
    }
}
