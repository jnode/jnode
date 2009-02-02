/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package java.lang;

import org.jnode.vm.VmMagic;

/**
 * @author Levente S\u00e1ntha
 */
class NativeDouble {
    /**
     * Convert the double to the IEEE 754 floating-point "double format" bit
     * layout. Bit 63 (the most significant) is the sign bit, bits 62-52 (masked
     * by 0x7ff0000000000000L) represent the exponent, and bits 51-0 (masked by
     * 0x000fffffffffffffL) are the mantissa. This function leaves NaN alone,
     * rather than collapsing to a canonical value. The result of this function
     * can be used as the argument to <code>Double.longBitsToDouble(long)</code>
     * to obtain the original <code>double</code> value.
     *
     * @param value
     *            the <code>double</code> to convert
     * @return the bits of the <code>double</code>
     * @see java.lang.Double#longBitsToDouble(long)
     */
    static long doubleToRawLongBits(double value) {
        return VmMagic.doubleToRawLongBits(value);
    }

    /**
     * Convert the argument in IEEE 754 floating-point "double format" bit
     * layout to the corresponding float. Bit 63 (the most significant) is the
     * sign bit, bits 62-52 (masked by 0x7ff0000000000000L) represent the
     * exponent, and bits 51-0 (masked by 0x000fffffffffffffL) are the mantissa.
     * This function leaves NaN alone, so that you can recover the bit pattern
     * with <code>Double.doubleToRawLongBits(double)</code>.
     *
     * @param bits
     *            the bits to convert
     * @return the <code>double</code> represented by the bits
     * @see java.lang.Double#doubleToLongBits(double)
     * @see #doubleToRawLongBits(double)
     */
    static double longBitsToDouble(long bits) {
        return VmMagic.longBitsToDouble(bits);
    }
}
