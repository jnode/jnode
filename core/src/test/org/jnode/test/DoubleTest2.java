/**
 * $Id$  
 */
package org.jnode.test;

/**
 * @author Levente S?ntha
 */
public class DoubleTest2 {
    public static void main(String[] argv){
        System.out.println(test1());
        System.out.println(test2());
    }

    private static double test1(){
        return (long)1.3;
    }

    private static double test2(){
        return 1.3;
    }
}
