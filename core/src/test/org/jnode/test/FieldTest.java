/*
 * $Id$
 */
package org.jnode.test;

import java.lang.reflect.Field;


public class FieldTest {

    public static String test;

    public static void main(String[] argv) throws Exception {
        Field f = FieldTest.class.getField("test");
        System.out.println(f.getType());
    }
}
