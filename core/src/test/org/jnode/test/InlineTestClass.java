/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InlineTestClass {

    private int x;

    public void foo2() {
        int i = 5;
        int j = -7;
        x = i + j;
    }

    public void foo3(int i) {
        try {
            int[] a = new int[ 50];
            foo3(a[ i - 1], a[ i]);
        } finally {
            System.out.println("finally");
        }
    }

    public void foo() {
        int y = virtualFinalGetX();
        y += virtualNonFinalGetXa(this);
        //y += privateGetXai(this, 5);
        y += staticGetXai(this, 3);
        //y += staticRecursiveGetXai(this, 3);
    }

    public final int virtualFinalGetX() {
        return x;
    }

    public int virtualNonFinalGetXa(Object dummy) {
        return x;
    }

    private int privateGetXai(Object dummy, int i) {
        return i;
    }

    public static int staticGetXai(Object dummy, int i) {
        return i;
    }

    public static int staticRecursiveGetXai(Object dummy, int i) {
        if (i < 10) {
            return staticRecursiveGetXai(dummy, i + 1);
        } else {
            return i;
        }
    }

    public void foo3(int a, int b) {

    }

    /*
     * public void newTest() { IMTBuilder b = new IMTBuilder();
     */
}
