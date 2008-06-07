/*
 *
 */
package org.jnode.fs.jfat;

/**
 * @author gvt
 */
public class FatAttr {
    private static final int READONLY = 0x01;
    private static final int HIDDEN = 0x02;
    private static final int SYSTEM = 0x04;
    private static final int LABEL = 0x08;
    private static final int DIRECTORY = 0x10;
    private static final int ARCHIVE = 0x20;
    private static final int LONGNAME = READONLY | HIDDEN | SYSTEM | LABEL; // 0x0F
    private static final int MASK = LONGNAME | DIRECTORY | ARCHIVE; // 0x3F

    private int attr;

    public FatAttr(int attr) {
        this.attr = attr;
    }

    public FatAttr() {
        this.attr = 0;
    }

    public boolean equals(Object o) {
        if (o instanceof FatAttr)
            return (((FatAttr) o).getAttr() & MASK) == (attr & MASK);
        else
            return false;
    }

    public int getAttr() {
        return attr;
    }

    private boolean isAttr(int attr) {
        return ((this.attr & attr) != 0);
    }

    private void setAttr(int attr, boolean value) {
        if (value)
            this.attr |= attr;
        else
            this.attr &= ~attr;
    }

    public boolean isReadOnly() {
        return isAttr(READONLY);
    }

    public void setReadOnly(boolean value) {
        setAttr(READONLY, value);
    }

    public boolean isHidden() {
        return isAttr(HIDDEN);
    }

    public void setHidden(boolean value) {
        setAttr(HIDDEN, value);
    }

    public boolean isSystem() {
        return isAttr(SYSTEM);
    }

    public void setSystem(boolean value) {
        setAttr(SYSTEM, value);
    }

    public boolean isLabel() {
        return isAttr(LABEL);
    }

    public void setLabel(boolean value) {
        setAttr(LABEL, value);
    }

    public boolean isDirectory() {
        return isAttr(DIRECTORY);
    }

    public void setDirectory(boolean value) {
        setAttr(DIRECTORY, value);
    }

    public boolean isArchive() {
        return isAttr(ARCHIVE);
    }

    public void setArchive(boolean value) {
        setAttr(ARCHIVE, value);
    }

    public boolean isLong() {
        //
        // as prescribed on fatgen 1.03 page 33
        //
        return ((attr & MASK) == LONGNAME);
    }

    public void setLong() {
        setAttr(LONGNAME, true);
        setAttr(ARCHIVE, false);
        setAttr(DIRECTORY, false);
    }

    public String toString() {
        StrWriter out = new StrWriter();

        if (isLong()) {
            //
            // as prescribed on fatgen 1.03 page 33
            // have to be the first to be tested with "isLong()"
            //
            out.print("LONG");
        } else {
            if (isReadOnly())
                out.print("R");

            if (isHidden())
                out.print("H");

            if (isSystem())
                out.print("S");

            if (isArchive())
                out.print("A");

            if (isLabel())
                out.print("L");

            if (isDirectory())
                out.print("D");
        }

        return out.toString();
    }
}
