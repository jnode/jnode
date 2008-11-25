/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.doclets.standard;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.*;


public class Standard {
    
    public static final HtmlDoclet htmlDoclet = new HtmlDoclet();
    
    public static int optionLength(String option) {
        return htmlDoclet.optionLength(option);
    }
    
    public static boolean start(RootDoc root) {
        return htmlDoclet.start(root);
    }
    
    public static boolean validOptions(String[][] options,
                                   DocErrorReporter reporter) {
        return htmlDoclet.validOptions(options, reporter);
    }

    public static LanguageVersion languageVersion() {
	return htmlDoclet.languageVersion();
    }
    
}
