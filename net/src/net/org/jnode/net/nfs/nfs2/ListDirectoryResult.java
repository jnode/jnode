package org.jnode.net.nfs.nfs2;

import java.util.List;

public class ListDirectoryResult {

    private List<Entry> entryList;

    private boolean eof;

    public List<Entry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
    }

    public boolean isEof() {
        return eof;
    }

    public void setEof(boolean eof) {
        this.eof = eof;
    }

}
