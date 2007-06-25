/*
 * Portions Copyright 1997-2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996, 1997 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - 1998 - All Rights Reserved
 *
 * The original version of this source code and documentation
 * is copyrighted and owned by Taligent, Inc., a wholly-owned
 * subsidiary of IBM. These materials are provided under terms
 * of a License Agreement between Taligent and Sun. This technology
 * is protected by multiple US and International patents.
 *
 * This notice and attribution to Taligent may not be removed.
 * Taligent is a registered trademark of Taligent, Inc.
 *
 */

package sun.text.resources;

import java.util.ListResourceBundle;

public class FormatData_sq extends ListResourceBundle {
    /**
     * Overrides ListResourceBundle
     */
    protected final Object[][] getContents() {
        return new Object[][] {
            { "MonthNames",
                new String[] {
                    "janar", // january
                    "shkurt", // february
                    "mars", // march
                    "prill", // april
                    "maj", // may
                    "qershor", // june
                    "korrik", // july
                    "gusht", // august
                    "shtator", // september
                    "tetor", // october
                    "n\u00ebntor", // november
                    "dhjetor", // december
                    "" // month 13 if applicable
                }
            },
            { "MonthAbbreviations",
                new String[] {
                    "Jan", // abb january
                    "Shk", // abb february
                    "Mar", // abb march
                    "Pri", // abb april
                    "Maj", // abb may
                    "Qer", // abb june
                    "Kor", // abb july
                    "Gsh", // abb august
                    "Sht", // abb september
                    "Tet", // abb october
                    "N\u00ebn", // abb november
                    "Dhj", // abb december
                    "" // abb month 13 if applicable
                }
            },
            { "DayNames",
                new String[] {
                    "e diel", // Sunday
                    "e h\u00ebn\u00eb", // Monday
                    "e mart\u00eb", // Tuesday
                    "e m\u00ebrkur\u00eb", // Wednesday
                    "e enjte", // Thursday
                    "e premte", // Friday
                    "e shtun\u00eb" // Saturday
                }
            },
            { "DayAbbreviations",
                new String[] {
                    "Die", // abb Sunday
                    "H\u00ebn", // abb Monday
                    "Mar", // abb Tuesday
                    "M\u00ebr", // abb Wednesday
                    "Enj", // abb Thursday
                    "Pre", // abb Friday
                    "Sht" // abb Saturday
                }
            },
            { "AmPmMarkers",
                new String[] {
                    "PD", // am marker
                    "MD" // pm marker
                }
            },
            { "Eras",
                new String[] { // era strings
                    "p.e.r.",
                    "n.e.r."
                }
            },
            { "NumberElements",
                new String[] {
                    ",", // decimal separator
                    ".", // group (thousands) separator
                    ";", // list separator
                    "%", // percent sign
                    "0", // native 0 digit
                    "#", // pattern digit
                    "-", // minus sign
                    "E", // exponential
                    "\u2030", // per mille
                    "\u221e", // infinity
                    "\ufffd" // NaN
                }
            },
            { "DateTimePatterns",
                new String[] {
                    "h.mm.ss.a z", // full time pattern
                    "h.mm.ss.a z", // long time pattern
                    "h:mm:ss.a", // medium time pattern
                    "h.mm.a", // short time pattern
                    "yyyy-MM-dd", // full date pattern
                    "yyyy-MM-dd", // long date pattern
                    "yyyy-MM-dd", // medium date pattern
                    "yy-MM-dd", // short date pattern
                    "{1} {0}" // date-time pattern
                }
            },
            { "DateTimePatternChars", "GanjkHmsSEDFwWxhKzZ" },
        };
    }
}
