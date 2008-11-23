/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.tools.native2ascii.resources;

import java.util.ListResourceBundle;

public class MsgNative2ascii_zh_CN extends ListResourceBundle {

    public Object[][] getContents() {
        return new Object[][] {
        {"err.bad.arg", "-encoding \u9700\u8981\u53c2\u6570"},
        {"err.cannot.read",  "\u65e0\u6cd5\u8bfb\u53d6 {0}\u3002"},
        {"err.cannot.write", "\u65e0\u6cd5\u5199\u5165 {0}\u3002"},
        {"usage", "\u7528\u6cd5\uff1anative2ascii" +
         " [-reverse] [-encoding \u7f16\u7801] [\u8f93\u5165\u6587\u4ef6 [\u8f93\u51fa\u6587\u4ef6]]"},
        };
    }
}
