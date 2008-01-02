package org.jnode.net.nfs.nfs2.mount;

import java.util.List;

public class ExportEntry {

    private List<String> groupList;

    private String directory;

    public ExportEntry(String directory, List<String> groupList) {
        this.directory = directory;
        this.groupList = groupList;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public String getDirectory() {
        return directory;
    }

}
