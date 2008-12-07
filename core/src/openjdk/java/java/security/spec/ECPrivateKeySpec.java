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
package java.security.spec;

import java.math.BigInteger;

/**
 * This immutable class specifies an elliptic curve private key with 
 * its associated parameters.
 *
 * @see KeySpec
 * @see ECParameterSpec
 *
 * @author Valerie Peng
 *
 * @since 1.5
 */
public class ECPrivateKeySpec implements KeySpec {
    
    private BigInteger s;
    private ECParameterSpec params;

    /**
     * Creates a new ECPrivateKeySpec with the specified 
     * parameter values.
     * @param s the private value.
     * @param params the associated elliptic curve domain 
     * parameters.
     * @exception NullPointerException if <code>s</code>
     * or <code>params</code> is null.
     */
    public ECPrivateKeySpec(BigInteger s, ECParameterSpec params) {
        if (s == null) {
            throw new NullPointerException("s is null");
        }
        if (params == null) {
            throw new NullPointerException("params is null");
        }
	this.s = s;
	this.params = params;
    }
	
    /**
     * Returns the private value S.
     * @return the private value S.
     */
    public BigInteger getS() {
	return s;
    }

    /**
     * Returns the associated elliptic curve domain 
     * parameters.
     * @return the EC domain parameters.
     */
    public ECParameterSpec getParams() {
	return params;
    }
}
