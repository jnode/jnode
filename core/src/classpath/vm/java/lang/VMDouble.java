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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package java.lang;

import org.jnode.vm.VmMagic;

/**
 * VM specific double routines.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VMDouble {
    private char[] chars;

    private int length;

    private int index;

    /**
     * Convert the double to the IEEE 754 floating-point "double format" bit
     * layout. Bit 63 (the most significant) is the sign bit, bits 62-52 (masked
     * by 0x7ff0000000000000L) represent the exponent, and bits 51-0 (masked by
     * 0x000fffffffffffffL) are the mantissa. This function collapses all
     * versions of NaN to 0x7ff8000000000000L. The result of this function can
     * be used as the argument to <code>Double.longBitsToDouble(long)</code>
     * to obtain the original <code>double</code> value.
     * 
     * @param value
     *            the <code>double</code> to convert
     * @return the bits of the <code>double</code>
     * @see #longBitsToDouble(long)
     */
    public static long doubleToLongBits(double value) {
        if (Double.isNaN(value)) {
            return 0x7ff8000000000000L;
        } else {
            return VmMagic.doubleToRawLongBits(value);
        }
    }

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
     * @see #longBitsToDouble(long)
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
     * @see #doubleToLongBits(double)
     * @see #doubleToRawLongBits(double)
     */
    public static double longBitsToDouble(long bits) {
        return VmMagic.longBitsToDouble(bits);
    }

    private int parseUnsignedInt() throws NumberFormatException {
        if (index >= length)
            throw new NumberFormatException();

        int start = index;
        int value = 0;

        for (; index < length; index++) {
            int d = Character.digit(chars[index], 10);
            if (d < 0)
                break;
            value *= 10;
            value += d;
        }

        if (index == start)
            throw new NumberFormatException();

        return value;
    }

    private int parseSignedInt() throws NumberFormatException {
        if (index >= length)
            throw new NumberFormatException();

        char sign = ' ';

        switch (chars[index]) {
        case '-':
            sign = '-';
            index++;
            break;

        case '+':
            sign = '+';
            index++;
            break;
        }

        int value = parseUnsignedInt();

        return (sign == '-') ? -value : value;
    }

    private double parseFractionalPart(boolean nonEmpty)
            throws NumberFormatException {
        if (index >= length)
            throw new NumberFormatException();

        int start = index;
        double value = 0.0d;

        for (; index < length; index++) {
            int d = Character.digit(chars[index], 10);
            if (d < 0)
                break;
            value += d;
            value /= 10;
        }

        if (nonEmpty && (index == start))
            throw new NumberFormatException();

        return value;
    }

    private double parseExponent(double value) throws NumberFormatException {
        if (index >= chars.length)
            return value;

        switch (chars[index]) {
        case 'f':
        case 'F':
        case 'd':
        case 'D':
                return value;

        case 'e':
        case 'E':
            index++;
            break;

        default:
            throw new NumberFormatException();
        }

        int exponent = parseSignedInt();

        char ch = 0;
        if (index < chars.length && ((ch = chars[index]) != 'f' && ch != 'F' && ch != 'd' && ch != 'D'))
            throw new NumberFormatException();

        return value * Math.pow(10.0, exponent);
    }

    public static double parseDouble(String s) {
        return new VMDouble(s).parse();
    }
    
    public double parse() throws NumberFormatException {
        if (index >= chars.length)
            throw new NumberFormatException();

        char sign = '+';

        switch (chars[index]) {
        case '-':
            sign = '-';
            index++;
            break;

        case '+':
            sign = '+';
            index++;
            break;
        }

        if (index >= chars.length)
            throw new NumberFormatException();

        double value = 0;

        if (chars[index] == '.') {
            index++;
            value = parseFractionalPart(true);
            value = parseExponent(value);
        } else {
            value = parseUnsignedInt();

            if (index < chars.length){
                if(chars[index] == '.'){
                    index++;
                    value += parseFractionalPart(false);
                }
            }


            value = parseExponent(value);
        }

        return (sign == '-') ? -value : value;
    }

    public VMDouble(String s) {
        chars = s.toCharArray();
        length = chars.length;
        index = 0;
    }

    /**
     * Convert the <code>double</code> to a <code>String</code>.
     * Floating-point string representation is fairly complex: here is a rundown
     * of the possible values. "<code>[-]</code>" indicates that a negative
     * sign will be printed if the value (or exponent) is negative. "
     * <code>&lt;number&gt;</code>" means a string of digits ('0' to '9'). "
     * <code>&lt;digit&gt;</code>" means a single digit ('0' to '9'). <br>
     * 
     * <table border=1>
     * <tr>
     * <th>Value of Double</th>
     * <th>String Representation</th>
     * </tr>
     * <tr>
     * <td>[+-] 0</td>
     * <td><code>[-]0.0</code></td>
     * </tr>
     * <tr>
     * <td>Between [+-] 10 <sup>-3 </sup> and 10 <sup>7 </sup>, exclusive</td>
     * <td><code>[-]number.number</code></td>
     * </tr>
     * <tr>
     * <td>Other numeric value</td>
     * <td><code>[-]&lt;digit&gt;.&lt;number&gt;
     *          E[-]&lt;number&gt;</code>
     * </td>
     * </tr>
     * <tr>
     * <td>[+-] infinity</td>
     * <td><code>[-]Infinity</code></td>
     * </tr>
     * <tr>
     * <td>NaN</td>
     * <td><code>NaN</code></td>
     * </tr>
     * </table>
     * 
     * Yes, negative zero <em>is</em> a possible value. Note that there is
     * <em>always</em> a <code>.</code> and at least one digit printed after
     * it: even if the number is 3, it will be printed as <code>3.0</code>.
     * After the ".", all digits will be printed except trailing zeros. The
     * result is rounded to the shortest decimal number which will parse back to
     * the same double.
     * 
     * <p>
     * To create other output formats, use {@link java.text.NumberFormat}.
     * 
     * @XXX specify where we are not in accord with the spec.
     * 
     * @param v
     *            the <code>double</code> to convert
     * @return the <code>String</code> representing the <code>double</code>
     */
    public static String toString(double v, boolean isFloat) {
        
        final int MAX_DIGITS = isFloat ? 10 : 19;
        
        if (Double.isNaN(v))
            return "NaN";
        if (v == Double.POSITIVE_INFINITY)
            return "Infinity";
        if (v == Double.NEGATIVE_INFINITY)
            return "-Infinity";

        boolean negative = (doubleToLongBits(v) & (1L << 63)) != 0;
        double m = negative ? -v : v;

        if (m == 0.0d)
            return negative ? "-0.0" : "0.0";

        StringBuffer result = new StringBuffer(MAX_DIGITS * 2);
        if (negative) {
            result.append('-');
        }

        if (m >= 1e-3 && m < 1e7) {
            int digits = 0;
            long digit = (long) (m - 0.5d);
            result.append(digit);
            result.append('.');
            m -= digit;
            while ((m > 0.0d) && (digits < MAX_DIGITS)) {
                m *= 10.0d;
                digit = (long) (m - 0.5d);
                m -= digit;
                result.append(digit);
                digits++;
            }
            if (digits == 0) {
                result.append('0');
            }
        } else {
            int exponent = (int) (Math.log(m) / Math.log(10.0d));
            double mantissa = m / Math.pow(10.0d, exponent);
            result.append(toString(mantissa, isFloat));
            result.append('E');
            result.append(Integer.toString(exponent));
        }

        return result.toString();
    }
}
