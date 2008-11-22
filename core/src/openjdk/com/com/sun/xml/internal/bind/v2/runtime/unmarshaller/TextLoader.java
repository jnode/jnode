/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
package com.sun.xml.internal.bind.v2.runtime.unmarshaller;

import com.sun.xml.internal.bind.api.AccessorException;
import com.sun.xml.internal.bind.v2.runtime.Transducer;

import org.xml.sax.SAXException;

/**
 * Unmarshals a text into an object.
 *
 * <p>
 * If the caller can use {@link LeafPropertyLoader}, that's usually faster.
 *
 * @see LeafPropertyLoader
 * @see ValuePropertyLoader
 * @author Kohsuke Kawaguchi
 */
public class TextLoader extends Loader {

    private final Transducer xducer;

    public TextLoader(Transducer xducer) {
        super(true);
        this.xducer = xducer;
    }

    public void text(UnmarshallingContext.State state, CharSequence text) throws SAXException {
        try {
            state.target = xducer.parse(text);
        } catch (AccessorException e) {
            handleGenericException(e,true);
        } catch (RuntimeException e) {
            handleParseConversionException(state,e);
        }
    }
}
