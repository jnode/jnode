/*
 * Copyright 1999-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.security.auth.callback;

import java.util.Locale;

/**
 * <p> Underlying security services instantiate and pass a
 * <code>LanguageCallback</code> to the <code>handle</code>
 * method of a <code>CallbackHandler</code> to retrieve the <code>Locale</code>
 * used for localizing text.
 *
 * @see javax.security.auth.callback.CallbackHandler
 */
public class LanguageCallback implements Callback, java.io.Serializable {

    private static final long serialVersionUID = 2019050433478903213L;

    /**
     * @serial
     * @since 1.4
     */
    private Locale locale;

    /**
     * Construct a <code>LanguageCallback</code>.
     */
    public LanguageCallback() { }

    /**
     * Set the retrieved <code>Locale</code>.
     *
     * <p>
     *
     * @param locale the retrieved <code>Locale</code>.
     *
     * @see #getLocale
     */
    public void setLocale(Locale locale) {
	this.locale = locale;
    }
 
    /**
     * Get the retrieved <code>Locale</code>.
     *
     * <p>
     *
     * @return the retrieved <code>Locale</code>, or null
     *		if no <code>Locale</code> could be retrieved.
     *
     * @see #setLocale
     */
    public Locale getLocale() {
	return locale;
    }
}
