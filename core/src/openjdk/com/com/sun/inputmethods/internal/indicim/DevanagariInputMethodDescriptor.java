/*
 * Copyright 2002 Sun Microsystems, Inc.  All Rights Reserved.
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
 * (C) Copyright IBM Corp. 2000 - All Rights Reserved
 *
 * The original version of this source code and documentation is
 * copyrighted and owned by IBM. These materials are provided
 * under terms of a License Agreement between IBM and Sun.
 * This technology is protected by multiple US and International
 * patents. This notice and attribution to IBM may not be removed.
 *
 */

package com.sun.inputmethods.internal.indicim;

import java.awt.Image;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class DevanagariInputMethodDescriptor implements InputMethodDescriptor {

    static final Locale HINDI = new Locale("hi", "IN");

    public DevanagariInputMethodDescriptor() {
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getAvailableLocales
     */
    public Locale[] getAvailableLocales() {
        return new Locale[] { HINDI };
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#hasDynamicLocaleList
     */
    public boolean hasDynamicLocaleList() {
        return false;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodDisplayName
     */
    public synchronized String getInputMethodDisplayName(Locale inputLocale, Locale displayLanguage) {
	try {
	    ResourceBundle resources = ResourceBundle.getBundle("com.sun.inputmethods.internal.indicim.resources.DisplayNames", displayLanguage);
            return resources.getString("DisplayName.Devanagari");
	} catch (MissingResourceException mre) {
	    return "Devanagari Input Method";
	}
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodIcon
     */
    public Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#createInputMethod
     */
    public InputMethod createInputMethod() throws Exception {
        IndicInputMethodImpl impl = new IndicInputMethodImpl(
            DevanagariTables.keyboardMap, 
	    DevanagariTables.joinWithNukta, 
	    DevanagariTables.nuktaForm, 
	    DevanagariTables.substitutionTable);
            
        return new IndicInputMethod(HINDI, impl);
    }

    public String toString() {
        return getClass().getName();
    }
}
