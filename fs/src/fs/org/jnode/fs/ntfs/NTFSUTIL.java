/*
 * $Id$
 */
package org.jnode.fs.ntfs;

import java.util.Date;

/**
 * @author vali
 */
public class NTFSUTIL {

    public static Date getDateForNTFSTimes(long _100ns) {
        long timeoffset = Math.abs((369 * 365 + 89) * 24 * 3600 * 10000000);
        long time = (Math.abs(_100ns) - timeoffset);

        System.out.println("hours" + ((Math.abs(time) / 1000) / 60) / 60);
        Date date = new Date(time);
        System.out.println(date.toLocaleString());
        return date;
    }
}