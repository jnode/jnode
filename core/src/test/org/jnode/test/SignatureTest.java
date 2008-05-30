/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.test;

import junit.framework.TestCase;
import org.jnode.vm.classmgr.Signature;

/**
 * @author epr
 */
public class SignatureTest extends TestCase {

    /**
     * Constructor for SignatureTest.
     *
     * @param arg0
     */
    public SignatureTest(String arg0) {
        super(arg0);
    }

    public void testSignatureObject() {
        String res = Signature.toSignature(Object.class);
        assertEquals("Ljava/lang/Object;", res);
    }

    public void testSignatureChar() {
        String res = Signature.toSignature(Character.TYPE);
        assertEquals("C", res);
    }

    public void testSignatureCharArray() {
        String res = Signature.toSignature(char[].class);
        assertEquals("[C", res);
    }
}
