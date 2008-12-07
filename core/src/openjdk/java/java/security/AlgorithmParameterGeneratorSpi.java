/*
 * Copyright 1997-1999 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.security;

import java.security.spec.AlgorithmParameterSpec;

/**
 * This class defines the <i>Service Provider Interface</i> (<b>SPI</b>)
 * for the <code>AlgorithmParameterGenerator</code> class, which 
 * is used to generate a set of parameters to be used with a certain algorithm.
 *
 * <p> All the abstract methods in this class must be implemented by each 
 * cryptographic service provider who wishes to supply the implementation
 * of a parameter generator for a particular algorithm.
 *
 * <p> In case the client does not explicitly initialize the
 * AlgorithmParameterGenerator (via a call to an <code>engineInit</code>
 * method), each provider must supply (and document) a default initialization.
 * For example, the Sun provider uses a default modulus prime size of 1024
 * bits for the generation of DSA parameters.
 *
 * @author Jan Luehe
 *
 *
 * @see AlgorithmParameterGenerator
 * @see AlgorithmParameters
 * @see java.security.spec.AlgorithmParameterSpec
 *
 * @since 1.2
 */

public abstract class AlgorithmParameterGeneratorSpi {

    /**
     * Initializes this parameter generator for a certain size
     * and source of randomness.
     *
     * @param size the size (number of bits).
     * @param random the source of randomness.
     */
    protected abstract void engineInit(int size, SecureRandom random);

    /**
     * Initializes this parameter generator with a set of
     * algorithm-specific parameter generation values.
     *
     * @param genParamSpec the set of algorithm-specific parameter generation values.
     * @param random the source of randomness.
     *
     * @exception InvalidAlgorithmParameterException if the given parameter
     * generation values are inappropriate for this parameter generator.
     */
    protected abstract void engineInit(AlgorithmParameterSpec genParamSpec,
				       SecureRandom random)
	throws InvalidAlgorithmParameterException;

    /**
     * Generates the parameters.
     *
     * @return the new AlgorithmParameters object.
     */
    protected abstract AlgorithmParameters engineGenerateParameters();
}
