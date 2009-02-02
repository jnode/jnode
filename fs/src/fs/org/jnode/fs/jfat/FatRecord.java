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
 
package org.jnode.fs.jfat;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jnode.util.NumberUtils;

public class FatRecord {
    private static final Logger log = Logger.getLogger(FatRecord.class);

    private static final int MAXLONGENTRIES = 20;

    private FatShortDirEntry shortEntry;
    private final Vector<FatLongDirEntry> longEntries;
    private String longName;

    public FatRecord() {
        init();
        longEntries = new Vector<FatLongDirEntry>(MAXLONGENTRIES);
    }

    public FatRecord(FatDirectory parent, FatName name) throws IOException {
        init();

        FatFileSystem fs = parent.getFatFileSystem();
        FatDirEntry[] free;
        FatShortDirEntry s;

        int n = name.getNumberOfComponents();

        if (!name.isMangled() && (n != 1))
            throw new IOException("not mangled and n=" + n);

        if (name.isMangled()) {
            free = parent.getFatFreeEntries(n + 1);
            longEntries = new Vector<FatLongDirEntry>(n + 1);
        } else {
            free = parent.getFatFreeEntries(1);
            longEntries = new Vector<FatLongDirEntry>(0);
        }

        s = new FatShortDirEntry(fs, name, free[free.length - 1].getIndex());

        if (name.isMangled()) {
            FatLongDirEntry l = new FatLongDirEntry(
                    fs, name.getComponent(n - 1), (byte) n, s.getChkSum(),
                    true, free[0].getIndex());

            addSetFatDirEntry(parent, l);

            for (int i = (n - 2); i >= 0; i--) {
                l = new FatLongDirEntry(fs, name.getComponent(i), (byte) (i + 1),
                        s.getChkSum(), false, free[n - 1 - i].getIndex());

                addSetFatDirEntry(parent, l);
            }
        }

        close(s);
    }

    private void init() {
        shortEntry = null;
        longName = null;
    }

    private void clearLongEntries() {
        longEntries.clear();
    }

    private void mustBeOpen() {
        if (shortEntry != null) {
            // FIXME ... this is clearly the wrong exception
            throw new UnsupportedOperationException("FatRecord is closed");
        }
    }

    private void mustBeClose() {
        if (shortEntry == null) {
            // FIXME ... this is clearly the wrong exception
            throw new UnsupportedOperationException("FatRecord is open");
        }
    }

    public void clear() {
        init();
        clearLongEntries();
    }

    public void add(FatLongDirEntry entry) {
        mustBeOpen();
        if (entry.isDamaged()) {
            // FIXME ... this is clearly the wrong exception
            throw new UnsupportedOperationException("Damaged entry, shouldn't happen");
        }
        longEntries.add(entry);
    }

    private void addSetFatDirEntry(FatDirectory parent, FatLongDirEntry entry) throws IOException {
        add(entry);
        parent.setFatDirEntry(entry);
    }

    public void close(FatShortDirEntry entry) {
        mustBeOpen();
        shortEntry = entry;
        mustBeClose();

        longName = getShortName();

        if (longEntries.isEmpty())
            return;

        if (!longEntries.firstElement().isLast()) {
            log.debug("last long vector element discarded for " + getShortName());
            clearLongEntries();
            return;
        }

        int i;
        StringBuilder lname = new StringBuilder(longEntries.size() * FatLongDirEntry.NAMELENGTH);

        int last = longEntries.size() - 1;
        byte chkSum = getChkSum();

        for (i = last; i >= 0; i--) {
            FatLongDirEntry l = longEntries.get(i);

            int ordinal = last - i + 1;

            if (l.getOrdinal() != ordinal) {
                log.debug("ordinal orphaned vector discarded for " + getShortName());
                clearLongEntries();
                return;
            }

            if (l.getChkSum() != chkSum) {
                log.debug("chksum orphaed vector discarded for " + getShortName());
                clearLongEntries();
                return;
            }

            lname.append(l.getComponent());
        }

        longName = lname.toString();
    }

    public FatShortDirEntry getShortEntry() {
        mustBeClose();
        return shortEntry;
    }

    public String getShortName() {
        return getShortEntry().getShortName();
    }

    public boolean hasLongEntries() {
        mustBeClose();
        return !longEntries.isEmpty();
    }

    public int size() {
        mustBeClose();
        return longEntries.size() + 1;
    }

    public byte getChkSum() {
        mustBeClose();
        return shortEntry.getChkSum();
    }

    public Vector<FatLongDirEntry> getLongEntries() {
        mustBeClose();
        return longEntries;
    }

    public String getLongName() {
        mustBeClose();
        return longName;
    }

    public String toString() {
        mustBeClose();

        StrWriter out = new StrWriter();

        out.println("********************************************************");
        out.println("FatRecord (Closed)");
        out.println("********************************************************");
        out.println("ShortName\t" + "<" + getShortName() + ">");
        out.println("ShortIndex\t" + getShortEntry().getIndex());
        out.println("ChckSum\t\t" + NumberUtils.hex(getChkSum(), 2));
        out.println("Size\t\t" + size());
        for (int i = size() - 2; i >= 0; i--) {
            FatLongDirEntry l = longEntries.get(i);
            out.print(" [" + i + "]\t\t");
            out.print("Ord(" + l.getOrdinal() + ")\t");
            out.print("Chk(" + NumberUtils.hex(l.getChkSum(), 2) + ")\t");
            out.print("Idx(" + l.getIndex() + ")\t");
            out.println("<" + l.getComponent() + ">");
        }
        out.println("LongName\t" + "<" + getLongName() + ">");
        out.print("********************************************************");

        return out.toString();
    }
}
