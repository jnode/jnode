/**
 * $Id$  
 */
package org.jnode.test;

import org.jnode.util.NumberUtils;

/**
 * @author Levente S?ntha
 */
public class DoubleTest2 {
    public static void main(String[] argv){
        System.out.println(test1());
        System.out.println(test2());
        System.out.println(test3());
        
        System.out.println(NumberUtils.hex(Double.doubleToLongBits(1.3)));
        System.out.println(NumberUtils.hex(Double.doubleToRawLongBits(1.3)));

        System.out.println(toString(Double.longBitsToDouble(Double.doubleToLongBits(1.3)), false));
        System.out.println(toString(Double.longBitsToDouble(Double.doubleToRawLongBits(1.3)), false));
    }

    private static double test1(){
        return (long)1.3;
    }

    private static double test2(){
        return 1.3;
    }
    
    private static double test3() {
    	double i = 13;
    	return i / 10;
    }
    
    public static String toString(double v, boolean isFloat) {
        
        final int MAX_DIGITS = isFloat ? 10 : 19;
        
        if (Double.isNaN(v))
            return "NaN";
        if (v == Double.POSITIVE_INFINITY)
            return "Infinity";
        if (v == Double.NEGATIVE_INFINITY)
            return "-Infinity";

        boolean negative = (Double.doubleToLongBits(v) & (1L << 63)) != 0;
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
