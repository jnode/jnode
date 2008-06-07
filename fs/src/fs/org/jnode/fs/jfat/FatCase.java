/*
 *
 */
package org.jnode.fs.jfat;

import org.jnode.util.NumberUtils;


/**
 * @author gvt
 */
public class FatCase {
    private static final int MASK = 0x18;

    private static final int UPPER_UPPER = 0x00;
    private static final int LOWER_UPPER = 0x01;
    private static final int UPPER_LOWER = 0x02;
    private static final int LOWER_LOWER = 0x03;

    private int ncase;

    public FatCase() {
        this.ncase = UPPER_UPPER;
    }

    public FatCase(int ncase) {
        if ((ncase & ~MASK) != 0)
            throw new UnsupportedOperationException("invalid mask on " + NumberUtils.hex(ncase, 8));

        this.ncase = ((ncase & MASK) >> 3);
    }

    public FatCase(String baseName, String extName) {
        if (baseName.length() == 0)
            throw new UnsupportedOperationException("empty baseName");

        final boolean baseIsUpper = FatUtils.isUpperCase(baseName);
        final boolean baseIsLower = FatUtils.isLowerCase(baseName);

        if (extName.length() == 0) {
            if (baseIsLower)
                ncase = LOWER_UPPER;
            else
                ncase = UPPER_UPPER;
        } else {
            final boolean extIsUpper = FatUtils.isUpperCase(extName);
            final boolean extIsLower = FatUtils.isLowerCase(extName);

            if (baseIsLower && extIsLower)
                ncase = LOWER_LOWER;
            else if (baseIsUpper && extIsLower)
                ncase = UPPER_LOWER;
            else if (baseIsLower && extIsUpper)
                ncase = LOWER_UPPER;
            else
                ncase = UPPER_UPPER;
        }
    }

    public int getCase() {
        return (ncase << 3);
    }

    public boolean isLowerBase() {
        return (isLowerUpper() || isLowerLower());
    }

    public boolean isUpperBase() {
        return (isUpperLower() || isUpperUpper());
    }

    public boolean isLowerExt() {
        return (isUpperLower() || isLowerLower());
    }

    public boolean isUpperExt() {
        return (isLowerUpper() || isUpperUpper());
    }

    public boolean isUpperUpper() {
        return (ncase == UPPER_UPPER);
    }

    public boolean isLowerUpper() {
        return (ncase == LOWER_UPPER);
    }

    public boolean isUpperLower() {
        return (ncase == UPPER_LOWER);
    }

    public boolean isLowerLower() {
        return (ncase == LOWER_LOWER);
    }

    public void setLowerLower() {
        ncase = LOWER_LOWER;
    }

    public String toString() {
        StrWriter out = new StrWriter();

        switch (ncase) {
            case UPPER_UPPER:
                out.print("UUUUUUUU.UUU");
                break;

            case LOWER_UPPER:
                out.print("LLLLLLLL.UUU");
                break;

            case UPPER_LOWER:
                out.print("UUUUUUUU.LLL");
                break;

            case LOWER_LOWER:
                out.print("LLLLLLLL.LLL");
                break;
        }

        return out.toString();
    }
}
