package org.jnode.apps.jpartition.model;

import java.util.ArrayList;
import java.util.List;

public class CustomDevice extends Device {
    CustomDevice(String name, long size) {
        super(name, size, null, asList(new Partition(0L, size, false)));
    }

    private static List<Partition> asList(Partition partition) {
        List<Partition> list = new ArrayList<Partition>();
        list.add(partition);
        return list;
    }
}
