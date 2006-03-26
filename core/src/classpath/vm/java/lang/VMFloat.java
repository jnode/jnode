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
 
package java.lang;

import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.MagicPermission;

/*
 * This class is a reference version, mainly for compiling a class library jar.
 * It is likely that VM implementers replace this with their own version that
 * can communicate effectively with the VM.
 */

/**
 * Code relocated from java.lang.Float by
 * 
 * @author Dave Grove <groved@us.ibm.com>
 */
@MagicPermission
final class VMFloat {
	/**
	 * Convert the float to the IEEE 754 floating-point "single format" bit
	 * layout. Bit 31 (the most significant) is the sign bit, bits 30-23 (masked
	 * by 0x7f800000) represent the exponent, and bits 22-0 (masked by
	 * 0x007fffff) are the mantissa. This function collapses all versions of NaN
	 * to 0x7fc00000. The result of this function can be used as the argument to
	 * <code>Float.intBitsToFloat(int)</code> to obtain the original
	 * <code>float</code> value.
	 * 
	 * @param value
	 *            the <code>float</code> to convert
	 * @return the bits of the <code>float</code>
	 * @see #intBitsToFloat(int)
	 */
	static int floatToIntBits(float value) {
		if (Float.isNaN(value)) {
			return 0x7fc00000;
		} else {
			return VmMagic.floatToRawIntBits(value);
		}
	}

	/**
	 * Convert the float to the IEEE 754 floating-point "single format" bit
	 * layout. Bit 31 (the most significant) is the sign bit, bits 30-23 (masked
	 * by 0x7f800000) represent the exponent, and bits 22-0 (masked by
	 * 0x007fffff) are the mantissa. This function leaves NaN alone, rather than
	 * collapsing to a canonical value. The result of this function can be used
	 * as the argument to <code>Float.intBitsToFloat(int)</code> to obtain the
	 * original <code>float</code> value.
	 * 
	 * @param value
	 *            the <code>float</code> to convert
	 * @return the bits of the <code>float</code>
	 * @see #intBitsToFloat(int)
	 */
	static int floatToRawIntBits(float value) {
		return VmMagic.floatToRawIntBits(value);
	}

	/**
	 * Convert the argument in IEEE 754 floating-point "single format" bit
	 * layout to the corresponding float. Bit 31 (the most significant) is the
	 * sign bit, bits 30-23 (masked by 0x7f800000) represent the exponent, and
	 * bits 22-0 (masked by 0x007fffff) are the mantissa. This function leaves
	 * NaN alone, so that you can recover the bit pattern with
	 * <code>Float.floatToRawIntBits(float)</code>.
	 * 
	 * @param bits
	 *            the bits to convert
	 * @return the <code>float</code> represented by the bits
	 * @see #floatToIntBits(float)
	 * @see #floatToRawIntBits(float)
	 */
	static float intBitsToFloat(int bits) {
		return VmMagic.intBitsToFloat(bits);
	}
} // class VMFloat
