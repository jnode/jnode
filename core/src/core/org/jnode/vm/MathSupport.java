/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm;

import org.jnode.annotation.MagicPermission;
import org.jnode.annotation.Uninterruptible;


/**
 * In MathSupport ldiv and lrem will be used as SoftBytecodes. I.e. normal Java classes that contain ldiv or
 * lrem bytecodes will in fact call these Java methods.
 *
 * @author epr
 * @author peda
 */
@MagicPermission
@Uninterruptible
public final class MathSupport {

    /**
     * New Implementation
     */
    public static long ldiv(long num, long den) {
        if (num == Long.MIN_VALUE) {
            if (den == Long.MIN_VALUE) {
                return 1;
            } else {
                long q = ldiv(num + 1, den);
                long r = num + 1 - q * den;
                if (Math.abs(r) == Math.abs(den) - 1) {
                    return q < 0 ? q - 1 : q + 1;
                } else {
                    return q;
                }
            }
        }

        if (den == Long.MIN_VALUE) {
            return 0;
        }

        boolean neg = false;
        if (num < 0) {
            num = -num;
            neg = true;
        }
        if (den < 0) {
            den = -den;
            neg = !neg;
        }
        if (num < den) return 0;
        if (den == 0) throw new ArithmeticException("Divide by zero");

        long qBit = 1;
        while (den >= 0) {
            den = den << 1;
            qBit = qBit << 1;
        }

        den >>>= 1;
        qBit >>>= 1;

        long result = 0;
        while (qBit != 0) {
            if (den <= num) {
                result += qBit;
                num -= den;
            }
            den = den >>> 1;
            qBit = qBit >>> 1;
        }

        if (neg)
            return -result;
        return result;
    }
    /**
     * new implementation
     */
    public static long lrem(long num, long den) {
        if (num == Long.MIN_VALUE) {
            if (den == Long.MIN_VALUE) {
                return 0;
            } else {
                long r = lrem(num + 1, den);
                if (Math.abs(r) == Math.abs(den) - 1) {
                    return 0;
                } else {
                    return r < 0 ? r - 1 : r > 0 ? r + 1 : -1;
                }
            }
        }

        if (den == Long.MIN_VALUE) {
            return num;
        }

        final boolean neg;
        if (num < 0) {
            num = -num;
            neg = true;
        } else neg = false;
        if (den < 0) {
            den = -den;
        }
        if (num < den) return neg ? -num : num;

        if (den == 0)
            throw new ArithmeticException("Divide by zero");

        long qBit = 1;
        while (den >= 0) {
            den <<= 1;
            qBit <<= 1;
        }

        den >>>= 1;
        qBit >>>= 1;

        while (qBit != 0) {
            if (den <= num) {
                num -= den;
            }
            qBit >>>= 1;
            den >>>= 1;
        }

        if (neg)
            return -num;
        return num;
    }
}
