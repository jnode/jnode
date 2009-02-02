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
 
package org.jnode.fs.iso9660;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ISO9660Constants {

    public static final char SEPARATOR1 = '.';
    public static final char SEPARATOR2 = ';';

    public static final String DEFAULT_ENCODING = "US-ASCII";

    public static interface VolumeDescriptorType {

        public static final int TERMINATOR = 255;

        public static final int BOOTRECORD = 0;

        public static final int PRIMARY_DESCRIPTOR = 1;

        public static final int SUPPLEMENTARY_DESCRIPTOR = 2;

        public static final int PARTITION_DESCRIPTOR = 3;
    }

}
