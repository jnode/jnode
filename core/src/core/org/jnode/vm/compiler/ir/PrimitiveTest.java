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

package org.jnode.vm.compiler.ir;

/**
 * @author Levente S\u00e1ntha
 */
public class PrimitiveTest {

    /**
     * ****** INT ******************************
     */

    public static int appel() {
        int i, j, k;
        i = 1;
        j = 1;
        k = 0;
        while (k < 100) {
            if (j < 20) {
                j = i;
                k = k + 1;
            } else {
                j = k;
                k = k + 2;
            }
        }
        return j;
    }

    public static int trivial1() {
        return 35 + 17;
    }

    public static int trivial2() {
        return 1 == 1 ? 3 : 7;
    }

    public static int trivial() {
        int a = 0;
        while (a < 10) a++;
        return a;
    }

    public static int arithOptLoop(int a0, int a1, int a2) {
        int l3 = 1;
        int l4 = 3 * a1;
        for (int l5 = 10; l5 > 0; l5 -= 1) {
            l3 += 2 * a0 + l4;
            l4 += 1;
        }
        return l3;
    }

    public static int arithOptIntx(int a0, int a1, int a2) {
        return a0 + a1;
    }

    public static int discriminant(int a0, int a1, int a2) {
        return a1 * a1 - 4 * a0 * a2;
    }

    public static int simple(int a0, int a1) {
        int l0 = a1;
        return -l0;
    }

    public static int const0(int a0, int a1) {
        int l0 = 0;
        if (a0 == 0) {
            l0 = -1;
        }
        if (a0 > 0) {
            l0 = 1;
        }
        return l0;
    }

    public static int ifElse(int a0) {
        int l0;
        if (a0 == 0) {
            l0 = 1;
        } else {
            l0 = -1;
        }
        return l0;
    }

    public static int simpleWhile(int a0) {
        int l0 = 1;
        while (l0 < 10) {
            l0 += 1;
        }
        return l0;
    }

    public static int const12(int a0, int a1) {
        int l0 = 10, l1 = 0;
        while (l0 > 0) {
            l1 = a1 * l1 + a0;
            l0 = l0 - 1;
        }
        return l1;
    }

    //compile it with no optimisation (see kjc -O0 - the kopi compiler)
    public static int unconditionalJump(int a0, int a1) {
        int l0 = 1;
        for (;;) {
            l0 = a0 + a1 + l0;
            for (;;) {
                l0 = a0 + a1 + l0;
                for (;;) {
                    l0 = a0 + a1 + l0;
                    break;
                }
                break;
            }
            break;
        }
        return l0;
    }

    public static int const6(int a0, int a1) {
        int l1 = a1 | a0;
        int l2 = a0 & a1;
        return l1 ^ l1 + l2 ^ l2 - 2 * l1 * l2;
    }

    void const5() {
        int l1 = 0, l2 = 1, l3 = 3;
        l3 = l1 + l2;
    }

    public static int const4(int a0, int a1) {
        int l1 = a1 + a0;
        int l2 = a0 * a1;
        return l1 * l1 + l2 * l2 + 2 * l1 * l2;
    }

    public static int const3(int a0, int a1) {
        //int l1 = -134;
        //int l2 = 2;
        //int l3 = 3;
        //return (int)(1 + 2 * 3.5 + 1) % 6  ;
        //return  - ((l1 + l2 + l3 + l1* l2* l3) / l2);
        return a1 + a1 + a0;
    }

    public static int const2(int a0, int a1) {
        //int l1 = -134;
        //int l2 = 2;
        //int l3 = 3;
        //return (int)(1 + 2 * 3.5 + 1) % 6  ;
        //return  - ((l1 + l2 + l3 + l1* l2* l3) / l2);
        return (byte) 2 + a1;
    }

    public static int erro1(int a0, int a1) {
        return a0 < a1 ? a0 : a1;
    }

    public static int ifTest(int a0, int a1) {
        int l0;
        if (a0 > a1)
            l0 = a0;
        else
            l0 = a1;
        return l0;
    }

    public static int const13(int a0, int a1) {
        int l0 = 1;
        for (;;) {
            //l0 = a0 + a1 + l0;
            if (l0 > 0)
                break;
            l0++;
        }
        return a0;
    }

    public static int const1(int a0, int a1) {
        int i0 = a0;
        while (i0 > 0) {
            i0 = i0 - 1;
            a1 = a1 + 1;
        }
        return a1;
    }

    public static int const10(int a0, int a1) {
        //while(a0-- >0) a1++;
        return a1 + 1 + a0 * (1 + 3 * a1 + 2 * a0);
    }

    public static int terniary1(int a0, int a1) {
        return 1;
    }

    public static int terniary2(int a0, int a1) {
        return a0 < 0 ? a0 : a1;
    }

    public static int terniary3(int a0, int a1) {
        int l0 = a0 + a0;
        int l1 = a1 * a1;
        return l0 < l1 ? a0 : a1;
    }


    public static int const9(int a0, int a1) {
        int l1 = a1 + a0;
        int l2 = a0 * a1;
        return l1 * l1 + l2 * l2 + 2 * l1 * l2;
    }

    public static int terniary0(int a0, int a1) {
        int l1 = 2;
        int l2 = 3;
        int l3 = 4;
        int l4 = l1 + a0;
        int l5 = l4 + l2 + a0;
        int l6 = l5 + l3;
        int l7 = a0 + a1;
        int l8 = a0 * a1;
        int l9 = a0 * a1 + a0;
        int l10 = a0 * a1 + a1;
        return 1111117 * a0 + a1 * l6 + l7 + l8 + l9 + l10;
    }

    public static int terniary10(int a0, int a1) {
        int l0 = a0 + a1;
        int l1 = a1 - a0 - 1;
        return l0 % l1;
    }

    public static int terniary11(int a0, int a1) {
        return a0 << a1;
    }

    public static int terniary12(int a0, int a1) {
        short s = (short) (2 * a0);
        byte b = (byte) (a1 * 2);
        char c = (char) (a1 + a0);
        int l1 = a0 + s;
        int l2 = a1 + b;
        return l1 + l2 + c;
    }

    public static int terniary13(int a0, int a1) {
        byte b = (byte) (a1 + 1);
        return b;
    }

    public static float terniary14(float a0, float a1) {
        return 1 + a0 + a1;
    }

    public static int terniary15(int a0, int a1) {
        return a0 * a1 + a0 * a1;
    }

    //error
    public static int terniary16(int a0, int a1) {
        int s = 0;
        for (int i = 0; i < 10000; i++)
            s = a0 * s + a1 * s + i + 1;
        return s;
    }

    public static int terniary18(int a0, int a1) {
        return (a0 / a1 + a1 / a0 + (a0 - a1) / (a0 + a1) + (a0 + a1) / (a0 * a1)) / (a0 * a1 / (a0 + a1) + 1);
    }

    public static int terniary17(int a0, int a1) {
        return (a0 * a0 + a1 * a1 + a1 * a0) / ((a0 + a1) + (a0 + a1) / (a0 + a0));
    }

    public static int terniary20(int a0, int a1) {
        int l0 = a0 / a1;
        int l1 = (a0 + a1) / 2;
        int l2 = (a0 * a1) / (a0 + a1);
        int l3 = (l0 + l1 + l2) / (a0 + a1);
        return ((1 + l0) / (1 + l1) + (2 + l0) / (1 + l2) + (3 + l0) / (1 + l3) + (4 + l1) / (1 + l2) +
            (5 + l2) / (1 + l3)) / (a0 - a1);
    }

    public static int terniary19(int a0, int a1) {
        int l1 = a0;
        int l3 = a0 + a0;
        return 1 / (1 + a0) + (1 + l1) / (1 + l1) + (1 + l1) / (1 + l3);
    }

    public static int terniary21(int a0, int a1) {
        return (1 * a0) / (1 * a0) + (1 * a0) / (1 * a0) + (1 * a0) / (1 * a0); // + (1 * a0)/(1 * a0) +
        //(1 * a0)/(1 * a0 );
    }

    //failure
    public static int terniary22(int a0, int a1) {
        int l0 = a0 + a1;
        int l1 = a0 + a1;
        int l2 = a0 + a1;
        int l3 = a0 + a1;
        int l4 = a0 + a1;
        int l5 = a0 + a1;
        int l6 = a0 + a1;
        int l7 = a0 + a1;
        int l8 = a0 + a1;
        //int l9 = a0 + a1;
        return l0 + l1 + l2 + l3 + l4 + l5 + l6 + l7 + l8; // + l9;
    }

    public static int terniary23(int a0, int a1) {
        return ((((((a0 * a1 + a1) << a1) / a0) >>> a0) + a0 * a1) >> (a1 - 2)) +
            (((17 * a0 * a1) << 3) % (a0 + a1 + 1));
    }

    public static float terniary24(float a0, float a1) {
        //  float l0 = a0 + a1;
        //    float l1 = a0 + a1;
        //      int l2 = (int)l0 + 2;
//        float l3 = l1 + (float)l2;
        return a0; //l3;
    }

    public static int terniary(int a0, int a1) {
        float f0 = a0; // + 1.3f;
        float f1 = a1; // + 1.7f;
//        float f2 = f0 / f1;
//        float f3 = f0 * f1;
        return (int) (f0 / f1);
    }

    public static int terniary25(int a0, int a1) {
        return a0 / a1;
    }

    public static long ladd(long a0, long a1) {
        return a0 + a1;
    }

    public static long lsub(long a0, long a1) {
        return a0 - a1;
    }

    public static long lmul(long a0, long a1) {
        return a0 * a1;
    }

    public static long ldiv(long a0, long a1) {
        return a0 / a1;
    }

    public static long land(long a0, long a1) {
        return a0 & a1;
    }

    public static long lor(long a0, long a1) {
        return a0 | a1;
    }

    public static long lxor(long a0, long a1) {
        return a0 ^ a1;
    }

}
