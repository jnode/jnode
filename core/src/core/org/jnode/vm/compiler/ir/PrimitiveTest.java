/**
 * $Id$  
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Levente Sántha
 */
public class PrimitiveTest {

    /********* INT *******************************/
    public static int arithOptLoop(int a0, int a1, int a2) {
        int l3 = 1;
        int l4 = 3*a1;
        for (int l5=10; l5 > 0; l5-=1) {
            l3 += 2*a0 + l4;
            l4 += 1;
        }
        return l3;
    }

    public static int arithOptIntx(int a0, int a1, int a2) {
        return a0 + a1;
    }

    public static int discriminant(int a0, int a1, int a2) {
        return a1*a1 - 4*a0*a2;
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

    public static int const12(int a0, int a1) {
        int l0 = 10, l1 = 0;
        while(l0 > 0){
            l1 = a1 * l1 + a0;
            l0 = l0 - 1;
        }
        return l1;
    }

    //compile it with no optimisation (see kjc -O0 - the kopi compiler)
    public static int unconditionalJump(int a0, int a1) {
        int l0 = 1;
        for(;;){
            l0 = a0 + a1 + l0;
            for(;;){
                l0 = a0 + a1 + l0;
                for(;;){
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
        return l1 ^ l1 + l2 ^ l2 - 2  * l1 * l2;
    }

    void const5() {
        int l1 = 0, l2 = 1, l3 = 3;
        l3 = l1 + l2;
    }

    public static int const4(int a0, int a1) {
        int l1 = a1 + a0;
        int l2 = a0 * a1;
        return l1 * l1 + l2 * l2 + 2  * l1 * l2;
    }

    public static int const3(int a0, int a1) {
        int l1 = -134;
        int l2 = 2;
        int l3 = 3;
        //return (int)(1 + 2 * 3.5 + 1) % 6  ;
        //return  - ((l1 + l2 + l3 + l1* l2* l3) / l2);
        return a1 + a1 + a0;
    }

    public static int const2(int a0, int a1) {
        int l1 = -134;
        int l2 = 2;
        int l3 = 3;
        //return (int)(1 + 2 * 3.5 + 1) % 6  ;
        //return  - ((l1 + l2 + l3 + l1* l2* l3) / l2);
        return (byte) 2 + a1;
    }

    public static int erro1(int a0, int a1) {
        return a0 < a1 ? a0 : a1;
    }

    public static int ifTest(int a0, int a1) {
        int l0;
        if(a0 > a1)
            l0 = a0;
        else
            l0 = a1;
        return l0;
    }

    public static int const1(int a0, int a1) {
        int l0 = 1;
        for(;;){
            l0 = a0 + a1 + l0;
            if(l0 > 10)
                break;
        }
        return l0;
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
        return l1 * l1 + l2 * l2 + 2  * l1 * l2;
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
        return l0%l1;
    }

    public static int terniary(int a0, int a1) {
        return a0<<a1;
    }
}
