/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
interface JarConstants {
    /* The local file header */
    int LOCHDR = 30;
    int LOCSIG = 'P' | ('K' << 8) | (3 << 16) | (4 << 24);

    int LOCVER = 4;
    int LOCFLG = 6;
    int LOCHOW = 8;
    int LOCTIM = 10;
    int LOCCRC = 14;
    int LOCSIZ = 18;
    int LOCLEN = 22;
    int LOCNAM = 26;
    int LOCEXT = 28;

    /* The Data descriptor */
    int EXTSIG = 'P' | ('K' << 8) | (7 << 16) | (8 << 24);
    int EXTHDR = 16;

    int EXTCRC = 4;
    int EXTSIZ = 8;
    int EXTLEN = 12;

    /* The central directory file header */
    int CENSIG = 'P' | ('K' << 8) | (1 << 16) | (2 << 24);
    int CENHDR = 46;

    int CENVEM = 4;
    int CENVER = 6;
    int CENFLG = 8;
    int CENHOW = 10;
    int CENTIM = 12;
    int CENCRC = 16;
    int CENSIZ = 20;
    int CENLEN = 24;
    int CENNAM = 28;
    int CENEXT = 30;
    int CENCOM = 32;
    int CENDSK = 34;
    int CENATT = 36;
    int CENATX = 38;
    int CENOFF = 42;

    /* The entries in the end of central directory */
    int ENDSIG = 'P' | ('K' << 8) | (5 << 16) | (6 << 24);
    int ENDHDR = 22;

    /* The following two fields are missing in SUN JDK */
    int ENDNRD = 4;
    int ENDDCD = 6;
    int ENDSUB = 8;
    int ENDTOT = 10;
    int ENDSIZ = 12;
    int ENDOFF = 16;
    int ENDCOM = 20;

}
