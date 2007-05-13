/*
 * $Id$
 */
package java.lang;

import org.jnode.vm.VmMagic;

/**
 * @author Levente Sántha
 */
public class NativeDouble {
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
    public static long doubleToRawLongBits(double value) {
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
    public static double longBitsToDouble(long bits) {
        return VmMagic.longBitsToDouble(bits);
    }
}
