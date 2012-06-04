/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.fs.smbfs;

public class Format {
    /**
     * Formats the number following printf conventions.
     * Main limitation: Can only handle one format parameter at a time
     * Use multiple Format objects to format more than one number
     *
     * @param s the format string following printf conventions
     *          The string has a prefix, a format code and a suffix. The prefix and suffix
     *          become part of the formatted output. The format code directs the
     *          formatting of the (single) parameter to be formatted. The code has the
     *          following structure
     *          <ul>
     *          <li> a % (required)
     *          <li> a modifier (optional)
     *          <dl>
     *          <dt> + <dd> forces display of + for positive numbers
     *          <dt> 0 <dd> show leading zeroes
     *          <dt> - <dd> align left in the field
     *          <dt> space <dd> prepend a space in front of positive numbers
     *          <dt> # <dd> use "alternate" format. Add 0 or 0x for octal or hexadecimal numbers.
     *          Don't suppress trailing zeroes in general floating point format.
     *          </dl>
     *          <li> an integer denoting field width (optional)
     *          <li> a period followed by an integer denoting precision (optional)
     *          <li> a format descriptor (required)
     *          <dl>
     *          <dt>f <dd> floating point number in fixed format
     *          <dt>e, E <dd> floating point number in exponential notation (scientific format).
     *          The E format results in an uppercase E for the exponent (1.14130E+003), the e format in a lowercase e.
     *          <dt>g, G <dd> floating point number in general format (fixed format for small numbers,
     *          exponential format for large numbers). Trailing zeroes are suppressed.
     *          The G format results in an uppercase E for the exponent (if any), the g format in a lowercase e.
     *          <dt>d, i <dd> integer in decimal
     *          <dt>x <dd> integer in hexadecimal
     *          <dt>o <dd> integer in octal
     *          <dt>s <dd> string
     *          <dt>c <dd> character
     *          </dl>
     *          </ul>
     * @throws IllegalArgumentException if bad format
     */

    public Format(String s) {
        width = 0;
        precision = -1;
        pre = "";
        post = "";
        leading_zeroes = false;
        show_plus = false;
        alternate = false;
        show_space = false;
        left_align = false;
        fmt = ' ';

        int length = s.length();
        int parse_state = 0;
        // 0 = prefix, 1 = flags, 2 = width, 3 = precision,
        // 4 = format, 5 = end
        int i = 0;

        while (parse_state == 0) {
            if (i >= length) parse_state = 5;
            else if (s.charAt(i) == '%') {
                if (i < length - 1) {
                    if (s.charAt(i + 1) == '%') {
                        pre = pre + '%';
                        i++;
                    } else
                        parse_state = 1;
                } else throw new java.lang.IllegalArgumentException();
            } else
                pre = pre + s.charAt(i);
            i++;
        }
        while (parse_state == 1) {
            if (i >= length) parse_state = 5;
            else if (s.charAt(i) == ' ') show_space = true;
            else if (s.charAt(i) == '-') left_align = true;
            else if (s.charAt(i) == '+') show_plus = true;
            else if (s.charAt(i) == '0') leading_zeroes = true;
            else if (s.charAt(i) == '#') alternate = true;
            else {
                parse_state = 2;
                i--;
            }
            i++;
        }
        while (parse_state == 2) {
            if (i >= length) parse_state = 5;
            else if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
                width = width * 10 + s.charAt(i) - '0';
                i++;
            } else if (s.charAt(i) == '.') {
                parse_state = 3;
                precision = 0;
                i++;
            } else
                parse_state = 4;
        }
        while (parse_state == 3) {
            if (i >= length) parse_state = 5;
            else if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
                precision = precision * 10 + s.charAt(i) - '0';
                i++;
            } else
                parse_state = 4;
        }
        if (parse_state == 4) {
            if (i >= length) parse_state = 5;
            else fmt = s.charAt(i);
            i++;
        }
        if (i < length)
            post = s.substring(i, length);
    }

    /**
     * prints a formatted number following printf conventions
     *
     * @param s   a PrintStream
     * @param fmt the format string
     * @param x   the double to print
     */

    public static void print(java.io.PrintStream s, String fmt, double x) {
        s.print(new Format(fmt).form(x));
    }

    /**
     * prints a formatted number following printf conventions
     *
     * @param s   a PrintStream
     * @param fmt the format string
     * @param x   the long to print
     */
    public static void print(java.io.PrintStream s, String fmt, long x) {
        s.print(new Format(fmt).form(x));
    }

    /**
     * prints a formatted number following printf conventions
     *
     * @param s   a PrintStream
     * @param fmt the format string
     * @param x   the character to
     */

    public static void print(java.io.PrintStream s, String fmt, char x) {
        s.print(new Format(fmt).form(x));
    }

    /**
     * prints a formatted number following printf conventions
     *
     * @param s a PrintStream, fmt the format string
     * @param x a string that represents the digits to print
     */

    public static void print(java.io.PrintStream s, String fmt, String x) {
        s.print(new Format(fmt).form(x));
    }

    /**
     * Converts a string of digits (decimal, octal or hex) to an integer
     *
     * @param s a string
     * @return the numeric value of the prefix of s representing a base 10 integer
     */

    public static int atoi(String s) {
        return (int) atol(s);
    }

    /**
     * Converts a string of digits (decimal, octal or hex) to a long integer
     *
     * @param s a string
     * @return the numeric value of the prefix of s representing a base 10 integer
     */

    public static long atol(String s) {
        int i = 0;

        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        if (i < s.length() && s.charAt(i) == '0') {
            if (i + 1 < s.length() && (s.charAt(i + 1) == 'x' || s.charAt(i + 1) == 'X'))
                return parseLong(s.substring(i + 2), 16);
            else return parseLong(s, 8);
        } else return parseLong(s, 10);
    }

    private static long parseLong(String s, int base) {
        int i = 0;
        int sign = 1;
        long r = 0;

        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        if (i < s.length() && s.charAt(i) == '-') {
            sign = -1;
            i++;
        } else if (i < s.length() && s.charAt(i) == '+') {
            i++;
        }
        while (i < s.length()) {
            char ch = s.charAt(i);
            if ('0' <= ch && ch < '0' + base)
                r = r * base + ch - '0';
            else if ('A' <= ch && ch < 'A' + base - 10)
                r = r * base + ch - 'A' + 10;
            else if ('a' <= ch && ch < 'a' + base - 10)
                r = r * base + ch - 'a' + 10;
            else
                return r * sign;
            i++;
        }
        return r * sign;
    }

    /**
     * Converts a string of digits to an double
     *
     * @param s a string
     */

    public static double atof(String s) {
        int i = 0;
        int sign = 1;
        double r = 0; // integer part
        double p = 1; // exponent of fractional part
        int state = 0; // 0 = int part, 1 = frac part

        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        if (i < s.length() && s.charAt(i) == '-') {
            sign = -1;
            i++;
        } else if (i < s.length() && s.charAt(i) == '+') {
            i++;
        }
        while (i < s.length()) {
            char ch = s.charAt(i);
            if ('0' <= ch && ch <= '9') {
                if (state == 0)
                    r = r * 10 + ch - '0';
                else if (state == 1) {
                    p = p / 10;
                    r = r + p * (ch - '0');
                }
            } else if (ch == '.') {
                if (state == 0) state = 1;
                else return sign * r;
            } else if (ch == 'e' || ch == 'E') {
                long e = (int) parseLong(s.substring(i + 1), 10);
                return sign * r * Math.pow(10, e);
            } else return sign * r;
            i++;
        }
        return sign * r;
    }

    /**
     * Formats a double into a string (like sprintf in C)
     *
     * @param x the number to format
     * @return the formatted string
     * @throws IllegalArgumentException if bad argument
     */

    public String form(double x) {
        String r;
        if (precision < 0) precision = 6;
        int s = 1;
        if (x < 0) {
            x = -x;
            s = -1;
        }
        if (fmt == 'f')
            r = fixed_format(x);
        else if (fmt == 'e' || fmt == 'E' || fmt == 'g' || fmt == 'G')
            r = exp_format(x);
        else throw new java.lang.IllegalArgumentException();

        return pad(sign(s, r));
    }

    /**
     * Formats a long integer into a string (like sprintf in C)
     *
     * @param x the number to format
     * @return the formatted string
     */

    public String form(long x) {
        String r;
        int s = 0;
        if (fmt == 'd' || fmt == 'i') {
            if (x < 0) {
                r = ("" + x).substring(1);
                s = -1;
            } else {
                r = "" + x;
                s = 1;
            }
        } else if (fmt == 'o')
            r = convert(x, 3, 7, "01234567");
        else if (fmt == 'x')
            r = convert(x, 4, 15, "0123456789abcdef");
        else if (fmt == 'X')
            r = convert(x, 4, 15, "0123456789ABCDEF");
        else throw new java.lang.IllegalArgumentException();

        return pad(sign(s, r));
    }

    /**
     * Formats a character into a string (like sprintf in C)
     *
     * @param c the value to format
     * @return the formatted string
     */
    public String form(char c) {
        if (fmt != 'c')
            throw new java.lang.IllegalArgumentException();

        String r = "" + c;
        return pad(r);
    }

    /**
     * Formats a string into a larger string (like sprintf in C)
     *
     * @param s the value to format
     * @return the formatted string
     */
    public String form(String s) {
        if (fmt != 's')
            throw new java.lang.IllegalArgumentException();
        if (precision >= 0 && precision < s.length())
            s = s.substring(0, precision);
        return pad(s);
    }

    /**
     * a test stub for the format class
     */
    public static void main(String[] a) {
        double x = 1.23456789012;
        double y = 123;
        double z = 1.2345e30;
        double w = 1.02;
        double u = 1.234e-5;
        int d = 0xCAFE;
        Format.print(System.out, "x = |%f|\n", x);
        Format.print(System.out, "u = |%20f|\n", u);
        Format.print(System.out, "x = |% .5f|\n", x);
        Format.print(System.out, "w = |%20.5f|\n", w);
        Format.print(System.out, "x = |%020.5f|\n", x);
        Format.print(System.out, "x = |%+20.5f|\n", x);
        Format.print(System.out, "x = |%+020.5f|\n", x);
        Format.print(System.out, "x = |% 020.5f|\n", x);
        Format.print(System.out, "y = |%#+20.5f|\n", y);
        Format.print(System.out, "y = |%-+20.5f|\n", y);
        Format.print(System.out, "z = |%20.5f|\n", z);

        Format.print(System.out, "x = |%e|\n", x);
        Format.print(System.out, "u = |%20e|\n", u);
        Format.print(System.out, "x = |% .5e|\n", x);
        Format.print(System.out, "w = |%20.5e|\n", w);
        Format.print(System.out, "x = |%020.5e|\n", x);
        Format.print(System.out, "x = |%+20.5e|\n", x);
        Format.print(System.out, "x = |%+020.5e|\n", x);
        Format.print(System.out, "x = |% 020.5e|\n", x);
        Format.print(System.out, "y = |%#+20.5e|\n", y);
        Format.print(System.out, "y = |%-+20.5e|\n", y);

        Format.print(System.out, "x = |%g|\n", x);
        Format.print(System.out, "z = |%g|\n", z);
        Format.print(System.out, "w = |%g|\n", w);
        Format.print(System.out, "u = |%g|\n", u);
        Format.print(System.out, "y = |%.2g|\n", y);
        Format.print(System.out, "y = |%#.2g|\n", y);

        Format.print(System.out, "d = |%d|\n", d);
        Format.print(System.out, "d = |%20d|\n", d);
        Format.print(System.out, "d = |%020d|\n", d);
        Format.print(System.out, "d = |%+20d|\n", d);
        Format.print(System.out, "d = |% 020d|\n", d);
        Format.print(System.out, "d = |%-20d|\n", d);
        Format.print(System.out, "d = |%20.8d|\n", d);
        Format.print(System.out, "d = |%x|\n", d);
        Format.print(System.out, "d = |%20X|\n", d);
        Format.print(System.out, "d = |%#20x|\n", d);
        Format.print(System.out, "d = |%020X|\n", d);
        Format.print(System.out, "d = |%20.8x|\n", d);
        Format.print(System.out, "d = |%o|\n", d);
        Format.print(System.out, "d = |%020o|\n", d);
        Format.print(System.out, "d = |%#20o|\n", d);
        Format.print(System.out, "d = |%#020o|\n", d);
        Format.print(System.out, "d = |%20.12o|\n", d);

        Format.print(System.out, "s = |%-20s|\n", "Hello");
        Format.print(System.out, "s = |%-20c|\n", '!');

        // regression test to confirm fix of reported bugs

        Format.print(System.out, "|%i|\n", Long.MIN_VALUE);

        Format.print(System.out, "|%6.2e|\n", 0.0);
        Format.print(System.out, "|%6.2g|\n", 0.0);

        Format.print(System.out, "|%6.2f|\n", 9.99);
        Format.print(System.out, "|%6.2f|\n", 9.999);

        Format.print(System.out, "|%6.0f|\n", 9.999);
    }

    private static String repeat(char c, int n) {
        if (n <= 0) return "";
        StringBuffer s = new StringBuffer(n);
        for (int i = 0; i < n; i++) s.append(c);
        return s.toString();
    }

    private static String convert(long x, int n, int m, String d) {
        if (x == 0) return "0";
        String r = "";
        while (x != 0) {
            r = d.charAt((int) (x & m)) + r;
            x = x >>> n;
        }
        return r;
    }

    private String pad(String r) {
        String p = repeat(' ', width - r.length());
        if (left_align) return pre + r + p + post;
        else return pre + p + r + post;
    }

    private String sign(int s, String r) {
        String p = "";
        if (s < 0) p = "-";
        else if (s > 0) {
            if (show_plus) p = "+";
            else if (show_space) p = " ";
        } else {
            if (fmt == 'o' && alternate && r.length() > 0 && r.charAt(0) != '0') p = "0";
            else if (fmt == 'x' && alternate) p = "0x";
            else if (fmt == 'X' && alternate) p = "0X";
        }
        int w = 0;
        if (leading_zeroes)
            w = width;
        else if ((fmt == 'd' || fmt == 'i' || fmt == 'x' || fmt == 'X' || fmt == 'o')
            && precision > 0) w = precision;

        return p + repeat('0', w - p.length() - r.length()) + r;
    }

    private String fixed_format(double d) {
        boolean removeTrailing
            = (fmt == 'G' || fmt == 'g') && !alternate;
        // remove trailing zeroes and decimal point

        if (d > 0x7FFFFFFFFFFFFFFFL) return exp_format(d);
        if (precision == 0)
            return (long) (d + 0.5) + (removeTrailing ? "" : ".");

        long whole = (long) d;
        double fr = d - whole; // fractional part
        if (fr >= 1 || fr < 0) return exp_format(d);

        double factor = 1;
        String leading_zeroes = "";
        for (int i = 1; i <= precision && factor <= 0x7FFFFFFFFFFFFFFFL; i++) {
            factor *= 10;
            leading_zeroes = leading_zeroes + "0";
        }
        long l = (long) (factor * fr + 0.5);
        if (l >= factor) {
            l = 0;
            whole++;
        } // CSH 10-25-97

        String z = leading_zeroes + l;
        z = "." + z.substring(z.length() - precision, z.length());

        if (removeTrailing) {
            int t = z.length() - 1;
            while (t >= 0 && z.charAt(t) == '0') t--;
            if (t >= 0 && z.charAt(t) == '.') t--;
            z = z.substring(0, t + 1);
        }

        return whole + z;
    }

    private String exp_format(double d) {
        String f = "";
        int e = 0;
        double dd = d;
        double factor = 1;
        if (d != 0) {
            while (dd > 10) {
                e++;
                factor /= 10;
                dd = dd / 10;
            }
            while (dd < 1) {
                e--;
                factor *= 10;
                dd = dd * 10;
            }
        }
        if ((fmt == 'g' || fmt == 'G') && e >= -4 && e < precision)
            return fixed_format(d);

        d = d * factor;
        f = f + fixed_format(d);

        if (fmt == 'e' || fmt == 'g')
            f = f + "e";
        else
            f = f + "E";

        String p = "000";
        if (e >= 0) {
            f = f + "+";
            p = p + e;
        } else {
            f = f + "-";
            p = p + (-e);
        }

        return f + p.substring(p.length() - 3, p.length());
    }

    private int width;
    private int precision;
    private String pre;
    private String post;
    private boolean leading_zeroes;
    private boolean show_plus;
    private boolean alternate;
    private boolean show_space;
    private boolean left_align;
    private char fmt; // one of cdeEfgGiosxXos
}





