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
 
package org.jnode.vm;

import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.Uninterruptible;


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

    /*
    private final char[] v = new char[5];
    private final char[] u = new char[5];
    private final char[] q = new char[5];

    public static boolean ucmp(//unsigned
                               final int a, //unsigned
                               final int b) {
        if ((b < 0) && (a >= 0))
            return true;
        if ((b >= 0) && (a < 0))
            return false;
        return a < b;
    }

    public static boolean ulcmp(//unsigned
                                final long a, //unsigned
                                final long b) {
        if ((b < 0L) && (a >= 0L))
            return true;
        if ((b >= 0L) && (a < 0L))
            return false;
        return a < b;
    }

    public static //unsigned
    int udiv(//unsigned
             final int a, //unsigned
             final int b) {
        if (b < 0) {
            if (a < 0) {
                if (a >= b)
                    return 1;
            }
            return 0;
        }
        if (a >= 0)
            return a / b;
        // hard case
        int a2 = a >>> 1;
        int d = a2 / b;
        int m = ((a2 % b) << 1) + (a & 1);
        return (d << 1) + m / b;
    }

    public static //unsigned
    int urem(//unsigned
             final int a, //unsigned
             final int b) {
        if (b < 0) {
            if (a < 0) {
                if (a >= b)
                    return a - b;
            }
            return a;
        }
        if (a >= 0)
            return a % b;
        // hard case
        int a2 = a >>> 1;
        int m = ((a2 % b) << 1) + (a & 1);
        return m % b;
    }

    public static //unsigned
    long umul(//unsigned
              final int a, //unsigned
              final int b) {
        char a_1 = (char) a;
        char a_2 = (char) (a >> 16);
        char b_1 = (char) b;
        char b_2 = (char) (b >> 16);
        int r1;
        long result = 0L;
        r1 = a_1 * b_1;
        if (r1 < 0) {
            result += Integer.MAX_VALUE;
            r1 -= Integer.MAX_VALUE;
        }
        result += r1;
        r1 = a_2 * b_1;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 16;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 16;
        r1 = a_1 * b_2;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 16;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 16;
        return result;
    }

    public static long ulmul(long a, long b) {
        char a_1 = (char) a;
        char a_2 = (char) (a >> 16);
        char a_3 = (char) (a >> 32);
        char a_4 = (char) (a >> 48);
        char b_1 = (char) b;
        char b_2 = (char) (b >> 16);
        char b_3 = (char) (b >> 32);
        char b_4 = (char) (b >> 48);
        int r1;
        long result = 0L;
        r1 = a_1 * b_1;
        if (r1 < 0) {
            result += Integer.MAX_VALUE;
            r1 -= Integer.MAX_VALUE;
        }
        result += r1;
        r1 = a_2 * b_1;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 16;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 16;
        r1 = a_1 * b_2;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 16;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 16;
        r1 = a_3 * b_1;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 32;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 32;
        r1 = a_2 * b_2;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 32;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 32;
        r1 = a_1 * b_3;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 32;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 32;
        r1 = a_4 * b_1;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 48;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 48;
        r1 = a_3 * b_2;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 48;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 48;
        r1 = a_2 * b_3;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 48;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 48;
        r1 = a_1 * b_4;
        if (r1 < 0) {
            result += ((long) Integer.MAX_VALUE) << 48;
            r1 -= Integer.MAX_VALUE;
        }
        result += ((long) r1) << 48;
        return result;
    }

    // ORIGINAL IMPLEMENTATION
    public static long ldiv2(long uq, long vq) {
        boolean neg = false;
        if (uq < 0L) {
            uq = -uq;
            neg = !neg;
        }
        if (vq < 0L) {
            vq = -vq;
            neg = !neg;
        }
        uq = uldivrem(uq, vq, false);
        if (neg)
            uq = -uq;
        return uq;
    }


    // ORIGINAL IMPLEMENTATION
    public static long lrem2(long uq, long vq) {
        boolean neg = false;
        if (uq < 0L) {
            uq = -uq;
            neg = !neg;
        }
        if (vq < 0L) {
            vq = -vq;
        }
        uq = uldivrem(uq, vq, true);
        if (neg)
            uq = -uq;
        return uq;
    }


    private static int HHALFQ(final long v) {
        return (int) (v >> 32);
    }

    private static int LHALFQ(final long v) {
        return (int) v;
    }

    private static char HHALF(final int v) {
        return (char) (v >> 16);
    }

    private static char LHALF(final int v) {
        return (char) v;
    }

    private static int LHUP(int v) {
        return v << 16;
    }

    private static //unsigned
    int COMBINE(final char hi, final char lo) {
        return (hi << 16) | lo;
    }

    private static //unsigned
    long COMBINEQ(//unsigned
                  final int hi, //unsigned
                  final int lo) {
        final long hiL = hi;
        final long loL = lo;
        return (hiL << 32) | (loL & 0xFFFFFFFFL);
    }

    private static final int HALF_BITS = 16;

    /*
     * Multiprecision divide.  This algorithm is from Knuth vol. 2 (2nd ed),
     * section 4.3.1, pp. 257--259.
     *
     * NOTE: the version here is adapted from NetBSD C source code (author unknown).
     *
    private static //unsigned
            long uldivrem(//unsigned
            final long uq, //unsigned
            final long vq, final boolean rem) {
        final int B = 1 << HALF_BITS;

        if (vq == 0)
            throw new ArithmeticException("Divide by zero");
        if (ulcmp(uq, vq)) {
            if (rem)
                return uq;
            return 0L;
        }
        /*
         * Break dividend and divisor into digits in base B, then
         * count leading zeros to determine m and n.  When done, we
         * will have:
         *  u = (u[1]u[2]...u[m+n]) sub B
         *  v = (v[1]v[2]...v[n]) sub B
         *  v[1] != 0
         *  1 < n <= 4 (if n = 1, we use a different division algorithm)
         *  m >= 0 (otherwise u < v, which we already checked)
         *  m + n = 4
         * and thus
         *  m = 4 - n <= 2
         *
        char u1 = HHALF(HHALFQ(uq));
        char u2 = LHALF(HHALFQ(uq));
        char u3 = HHALF(LHALFQ(uq));
        char u4 = LHALF(LHALFQ(uq));
        
        final MathSupport mathSupport = VmMagic.currentProcessor().getMathSupport();
        final char[] v = mathSupport.v; 
        v[0] = 0;
        v[1] = HHALF(HHALFQ(vq));
        v[2] = LHALF(HHALFQ(vq));
        v[3] = HHALF(LHALFQ(vq));
        v[4] = LHALF(LHALFQ(vq));
        int vi = 0;
        int n;
        for (n = 4; v[vi + 1] == 0; vi++) {
            if (--n == 1) {
                /*unsigned*/
    int rbj; /* r*B+u[j] (not root boy jim) */

    /*
                  * Change of plan, per exercise 16.
                  * r = 0;
                  * for j = 1..4:
                  *     q[j] = floor((r*B + u[j]) / v),
                  *     r = (r*B + u[j]) % v;
                  * We unroll this completely here.
                  *
                 //unsigned
                 int t = v[vi + 2]; // nonzero, by definition
                 char q1 = (char) udiv(u1, t);
                 rbj = COMBINE((char) urem(u1, t), u2);
                 char q2 = (char) udiv(rbj, t);
                 rbj = COMBINE((char) urem(rbj, t), u3);
                 char q3 = (char) udiv(rbj, t);
                 rbj = COMBINE((char) urem(rbj, t), u4);
                 char q4 = (char) udiv(rbj, t);
                 if (rem){
                   return urem(rbj, t);
                 }

                 return COMBINEQ(COMBINE(q1, q2), COMBINE(q3, q4));
             }
         }

         /*
          * By adjusting q once we determine m, we can guarantee that
          * there is a complete four-digit quotient at &qspace[1] when
          * we finally stop.
          *
         final char[] u = mathSupport.u;
         u[0] = 0;
         u[1] = u1;
         u[2] = u2;
         u[3] = u3;
         u[4] = u4;

         int ui = 0;
         int m;
         for (m = 4 - n; u[ui + 1] == 0; ++ui)
             m--;
         final char[] q = mathSupport.q;
         /*
          * In Java, q is already initialized to contain 0s.
          * Therefore, the following code is unnecessary.
          * int qi = 0;
          * for (int i = 4 - m; --i >= 0;)
          *     q[qi + i] = 0;
          * qi += 4 - m;
          *
         int qi = 4 - m;

         /*
         The above note is only true for new arrays, but as this array q
         is reused and therefore it has to reinited to 0
         *
         for (int i = 0; i < q.length; i++) {
           q[i] = 0;
         }
         /*
          * Here we run Program D, translated from MIX to C and acquiring
          * a few minor changes.
          *
          * D1: choose multiplier 1 << d to ensure v[1] >= B/2.
          *
         int d = 0;
         //unsigned
         for (int t = v[vi + 1]; ucmp(t, B / 2); t <<= 1)
             d++;
         if (d > 0) {
             shl(u, ui, m + n, d); // u <<= d
             shl(v, vi + 1, n - 1, d); // v <<= d
         }
         /*
          * D2: j = 0.
          *
         int j = 0;
         char v1 = v[vi + 1]; // for D3 -- note that v[1..n] are constant
         char v2 = v[vi + 2]; // for D3
         do {
             /*
              * D3: Calculate qhat (\^q, in TeX notation).
              * Let qhat = min((u[j]*B + u[j+1])/v[1], B-1), and
              * let rhat = (u[j]*B + u[j+1]) mod v[1].
              * While rhat < B and v[2]*qhat > rhat*B+u[j+2],
              * decrement qhat and increase rhat correspondingly.
              * Note that if rhat >= B, v[2]*qhat < rhat*B.
              *
             char uj0 = u[ui + j + 0]; // for D3 only -- note that u[j+...] change
             char uj1 = u[ui + j + 1]; // for D3 only
             char uj2 = u[ui + j + 2]; // for D3 only
             boolean sim_goto = false;
             //unsigned
             int qhat, rhat;
             if (uj0 == v1) {
                 qhat = B;
                 rhat = uj1;
                 //goto qhat_too_big;
                 sim_goto = true;
             } else {
                 //unsigned
                 int n2 = COMBINE(uj0, uj1);
                 qhat = udiv(n2, v1);
                 rhat = urem(n2, v1);
             }
             while (
                     sim_goto
                     || (ucmp(COMBINE((char) rhat, uj2), (int) umul(v2, qhat)))) {
                 //qhat_too_big:
                 sim_goto = false;
                 qhat--;
                 if ((rhat += v1) >= B)
                     break;
             }
             /*
              * D4: Multiply and subtract.
              * The variable `t' holds any borrows across the loop.
              * We split this up so that we do not require v[0] = 0,
              * and to eliminate a final special case.
              *
             int i;
             //unsigned
             int t;
             for (t = 0, i = n; i > 0; i--) {
                 t = u[ui + i + j] - (int) umul(v[vi + i], qhat) - t;
                 u[ui + i + j] = LHALF(t);
                 t = (B - HHALF(t)) & (B - 1);
             }
             t = u[ui + j] - t;
             u[ui + j] = LHALF(t);
             /*
              * D5: test remainder.
              * There is a borrow if and only if HHALF(t) is nonzero;
              * in that (rare) case, qhat was too large (by exactly 1).
              * Fix it by adding v[1..n] to u[j..j+n].
              *
             if (HHALF(t) != 0) {
                 qhat--;
                 for (t = 0, i = n; i > 0; i--) { // D6: add back.
                     t += u[ui + i + j] + v[vi + i];
                     u[ui + i + j] = LHALF(t);
                     t = HHALF(t);
                 }
                 u[ui + j] = LHALF(u[ui + j] + t);
             }
             q[qi + j] = (char) qhat;
         } while (++j <= m); // D7: loop on j.

         /*
          * If caller wants the remainder, we have to calculate it as
          * u[m..m+n] >>> d (this is at most n digits and thus fits in
          * u[m+1..m+n], but we may need more source digits).
          *
         if (rem) {
             if (d != 0) {
                 int i;
                 for (i = m + n; i > m; --i)
                     u[ui + i] =
                             (char) ((u[ui + i] >>> d)
                             | LHALF(u[ui + i - 1] << (HALF_BITS - d)));
                 u[ui + i] = 0;
             }
             return COMBINEQ(COMBINE(u[1], u[2]), COMBINE(u[3], u[4]));
         }
         return COMBINEQ(COMBINE(q[1], q[2]), COMBINE(q[3], q[4]));
     }

     private static void shl(char[] p, int off, int len, int sh) {
         int i;
         for (i = 0; i < len; i++)
             p[off + i] =
                     (char) (LHALF(p[off + i] << sh)
                     | (p[off + i + 1] >>> (HALF_BITS - sh)));
         p[off + i] = LHALF(p[off + i] << sh);
     }
     */

    /* dummy version 
    private static long ldiv2(long l1, long l2) {return l1/l2;}
    private static long lrem2(long l1, long l2) {return l1%l2;}
    */

    /*
    *
    This method
    does tests
    for
    previously failing
    cases
    *

    private static void failing() {
        long l1, l2;

        System.out.println("JDK\tNEW\tOLD");

        l1 = 843127915674713594l;
        l2 = 1;
        testD(l1, l2);

        l1 = 2907146525417257968l;
        l2 = 18;
        testD(l1, l2);

        l1 = 5484955353170252401l;
        l2 = 1;
        testR(l1, l2);

        l1 = -8074189617124730411l;
        l2 = 13;
        testR(l1, l2);

    }

    private static void testD(long l1, long l2) {
        System.out.printf("%d\t%d\t%d", l1 / l2, ldiv(l1, l2), ldiv2(l1, l2));
        if ((l1 / l2) == ldiv(l1, l2)) System.out.print(" PASS");
        else System.out.print(" FAIL");
        if ((l1 / l2) == ldiv2(l1, l2)) System.out.print(" PASS");
        else System.out.print(" FAIL");
        System.out.println();
    }

    private static void testR(long l1, long l2) {
        System.out.printf("%d\t%d\t%d", l1 % l2, lrem(l1, l2), lrem2(l1, l2));
        if ((l1 % l2) == lrem(l1, l2)) System.out.print(" PASS");
        else System.out.print(" FAIL");
        if ((l1 % l2) == lrem2(l1, l2)) System.out.print(" PASS");
        else System.out.print(" FAIL");
        System.out.println();
    }


    public static void main(String[] args) {

        System.out.println();

        failing();

        System.out.println();

        bench(true);
        bench(false);

        Random r = new Random();

        long l1 = -5776873939262739814l; // r.nextInt(); // r.nextLong();
        long l2 = 1l; //r.nextInt(50);

        System.out.println("l1 = " + l1 + "\nl2 = " + l2 + "\nl1%l2 = " + (l1 % l2));
        System.out.println("Result LREM = " + lrem(l1, l2));
        System.out.println("Result LREM2 = " + lrem2(l1, l2));

        l1 = 153;
        l2 = -5;
        System.out.println("l1 = " + l1 + "\nl2 = " + l2 + "\nl1%l2 = " + (l1 % l2));
        System.out.println("Result LREM2 = " + lrem2(l1, l2));

        l1 = -154;
        l2 = 5;
        System.out.println("l1 = " + l1 + "\nl2 = " + l2 + "\nl1%l2 = " + (l1 % l2));
        System.out.println("Result LREM2 = " + lrem2(l1, l2));

        l1 = 153;
        l2 = 5;
        System.out.println("l1 = " + l1 + "\nl2 = " + l2 + "\nl1%l2 = " + (l1 % l2));
        System.out.println("Result LREM2 = " + lrem2(l1, l2));

        l1 = -154;
        l2 = -5;
        System.out.println("l1 = " + l1 + "\nl2 = " + l2 + "\nl1%l2 = " + (l1 % l2));
        System.out.println("Result LREM2 = " + lrem2(l1, l2));

    }

    static long time(boolean nano) {
        if (nano)
            return System.nanoTime();
        return System.currentTimeMillis();
    }

    static final int SIZE = 50000;
    public static long[] random1 = new long[SIZE];
    public static long[] random2 = new long[SIZE];

    public static void bench(boolean nano) {

        final int REPEAT = 10;
        Random r = new Random();
        long temp, time, sum;

        for (int i = 0; i < SIZE; i++) {
            random1[i] = r.nextLong();
            if (random1[i] == 0)
                random1[i] = 1;
            random2[i] = r.nextInt(2000);
            if (random2[i] == 0)
                random2[i] = 1;
        }

        System.out.println("DIV:");


        sum = 0;
        for (int a = 0; a < REPEAT; a++) {
            time = time(nano);
            for (int i = 0; i < SIZE; i++) {
                temp = random1[i] / random2[i];
            }
            final long diff = time(nano) - time;
            sum += diff;
            System.out.print(diff + " ");
        }
        System.out.println("\nJDK = " + (sum));


        sum = 0;
        for (int a = 0; a < REPEAT; a++) {
            time = time(nano);
            for (int i = 0; i < SIZE; i++) {
                ldiv(random1[i], random2[i]);
            }
            final long diff = time(nano) - time;
            sum += diff;
            System.out.print(diff + " ");
        }
        System.out.println("\nldiv = " + (sum));


        sum = 0;
        for (int a = 0; a < REPEAT; a++) {
            time = time(nano);
            for (int i = 0; i < SIZE; i++) {
                ldiv2(random1[i], random2[i]);
            }
            final long diff = time(nano) - time;
            sum += diff;
            System.out.print(diff + " ");
        }
        System.out.println("\nldiv2 = " + (sum));


        System.out.println("\nREM:");


        sum = 0;
        for (int a = 0; a < REPEAT; a++) {
            time = time(nano);
            for (int i = 0; i < SIZE; i++) {
                temp = random1[i] % random2[i];
            }
            final long diff = time(nano) - time;
            sum += diff;
            System.out.print(diff + " ");
        }
        System.out.println("\nJDK = " + (sum));


        sum = 0;
        for (int a = 0; a < REPEAT; a++) {
            time = time(nano);
            for (int i = 0; i < SIZE; i++) {
                lrem(random1[i], random2[i]);
            }
            final long diff = time(nano) - time;
            sum += diff;
            System.out.print(diff + " ");
        }
        System.out.println("\nlrem = " + (sum));


        sum = 0;
        for (int a = 0; a < REPEAT; a++) {
            time = time(nano);
            for (int i = 0; i < SIZE; i++) {
                lrem2(random1[i], random2[i]);
            }
            final long diff = time(nano) - time;
            sum += diff;
            System.out.print(diff + " ");
        }
        System.out.println("\nlrem2 = " + (sum));

        // TEST WORKING STATE:

        System.out.println("\n\nTest if div and rem are working:\n");

        for (int i = 0; i < SIZE; i++) {
            temp = random1[i] / random2[i];
            if (ldiv(random1[i], random2[i]) != temp)
                System.out.println("ERROR FOR LDIV " + random1[i] + " / " + random2[i]);
            if (ldiv2(random1[i], random2[i]) != temp)
                System.out.println("ERROR FOR LDIV2 " + random1[i] + " / " + random2[i]);
        }

        for (int i = 0; i < SIZE; i++) {
            temp = random1[i] % random2[i];
            if (lrem(random1[i], random2[i]) != temp)
                System.out.println("ERROR FOR LREM " + random1[i] + " % " + random2[i]);
            if (lrem2(random1[i], random2[i]) != temp)
                System.out.println("ERROR FOR LREM2 " + random1[i] + " % " + random2[i]);
        }
    }

    */

    // greatest double that can be rounded to an int
    //public static double maxint = (double)Integer.MAX_VALUE;
    // least double that can be rounded to an int
    //public static double minint = (double)Integer.MIN_VALUE;

    // greatest double that can be rounded to a long
    //public static double maxlong = (double)Long.MAX_VALUE;
    // least double that can be rounded to a long
    //public static double minlong = (double)Long.MIN_VALUE;
}
