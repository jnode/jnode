/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.fs.ntfs;

import java.text.DateFormat;
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
        System.out.println(DateFormat.getInstance().format(date));
        return date;
    }
}
