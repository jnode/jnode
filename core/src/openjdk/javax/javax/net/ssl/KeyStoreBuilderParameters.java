/*
 * Copyright 2003-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.net.ssl;

import java.util.*;

import java.security.KeyStore.*;

/**
 * A parameters object for X509KeyManagers that encapsulates a List
 * of KeyStore.Builders.
 *
 * @see java.security.KeyStore.Builder
 * @see X509KeyManager
 *
 * @author  Andreas Sterbenz
 * @since   1.5
 */
public class KeyStoreBuilderParameters implements ManagerFactoryParameters {

    private final List<Builder> parameters;

    /**
     * Construct new KeyStoreBuilderParameters from the specified
     * {@linkplain java.security.KeyStore.Builder}.
     *
     * @param builder the Builder object
     * @exception NullPointerException if builder is null
     */
    public KeyStoreBuilderParameters(Builder builder) {
	parameters = Collections.singletonList(builder);
    }

    /**
     * Construct new KeyStoreBuilderParameters from a List
     * of {@linkplain java.security.KeyStore.Builder}s. Note that the list
     * is cloned to protect against subsequent modification.
     *
     * @param parameters the List of Builder objects
     * @exception NullPointerException if parameters is null
     * @exception IllegalArgumentException if parameters is an empty list
     */
    public KeyStoreBuilderParameters(List<Builder> parameters) {
	this.parameters = Collections.unmodifiableList(
	    new ArrayList<Builder>(parameters));
	if (this.parameters.isEmpty()) {
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Return the unmodifiable List of the
     * {@linkplain java.security.KeyStore.Builder}s
     * encapsulated by this object.
     *
     * @return the unmodifiable List of the
     * {@linkplain java.security.KeyStore.Builder}s
     * encapsulated by this object.
     */
    public List<Builder> getParameters() {
	return parameters;
    }

}
