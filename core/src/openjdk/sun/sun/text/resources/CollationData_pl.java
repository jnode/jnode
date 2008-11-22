/*
 * Portions Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

public class CollationData_pl extends ListResourceBundle {

    protected final Object[][] getContents() {
        return new Object[][] {
            { "Rule",
                /* for pl, default sorting except for the following: */
                /* add d<stroke> between d and e. */
                /* add l<stroke> between l and m. */
                /* add z<abovedot> after z.       */
                "& A < a\u0328 , A\u0328 " +      // a < a-ogonek
                "& C < c\u0301 , C\u0301 " +      // c < c-acute
                "& D < \u0111, \u0110 " +         // tal : d < d-stroke
                "& E < e\u0328 , E\u0328 " +      // e < e-ogonek
                "& L < \u0142 , \u0141 " +        // l < l-stroke
                "& N < n\u0301 , N\u0301 " +      // n < n-acute
                "& O < o\u0301 , O\u0301 " +      // o < o-acute
                "& S < s\u0301 , S\u0301 " +      // s < s-acute
                "& Z < z\u0301 , Z\u0301 " +      // z < z-acute
                "< z\u0307 , Z\u0307 "            // z-dot-above
            }
        };
    }
}
