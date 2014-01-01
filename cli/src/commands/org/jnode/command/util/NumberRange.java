/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.util;

/**
 * An immutable representation of a sequential number range.
 * 
 * A number range uses two number of values to denote points of
 * positive and negative infinity. By default, these values are
 * Integer.MAX_VALUE and Integer.MIN_VALUE respectively. If start
 * or end are equal to or beyond the infinity values, then the
 * range is considered to be infinite in that direction.
 *
 * It is worth noting that the Comparable interface is sensitive
 * to the points of positive and negative infinity. Two number ranges
 * can have different start and end points and be considered equal if
 * both ranges are also infinite in that direction.
 *
 * @author chris boertien
 */
public final class NumberRange implements Comparable<NumberRange> {

    private final int start;
    private final int end;
    private final int negativeInfinity;
    private final int positiveInfinity;
    
    /**
     * Creates a number range.
     *
     * This number range uses posInf and negInf to denote positive and negative
     * infinity respectively.
     *
     * @param start the start of the range
     * @param end the end of the range
     * @param posInf the point of positive infinity
     * @param negInf the point of negative infinity
     * @throws IllegalArgumentException if start &gt; end or negInf &gt;= posInf
     */
    public NumberRange(int start, int end, int negInf, int posInf) {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
        if (negInf >= posInf) {
            throw new IllegalArgumentException("negInf >= posInf");
        }
        this.start = start;
        this.end = end;
        this.negativeInfinity = negInf;
        this.positiveInfinity = posInf;
    }
    
    /**
     * Creates a number range.
     *
     * This number range uses Integer.MAX_VALUE and Integer.MIN_VALUE to denote
     * positive and negative infinity respectively.
     *
     * @param start the start of the range
     * @param end the end of the range
     * @throws IllegalArgumentException if start &gt; end
     */
    public NumberRange(int start,  int end) {
        this(start, end, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    /**
     * Compares this NumberRange to another NumberRange for order.
     *
     * NumberRanges are ordered first by their start position, and second
     * by their end position.
     *
     * A NumberRange that is negatively infinite is considered less than one that
     * is not negatively infinite. Also a NumberRange that is not positively infinite
     * is considered less than one that is positively infinite.
     *
     * @param that the NumberRange to compare this one to
     * @return -1 if this comes before that, 1 if that comes before this, 0 if they are equal.
     */
    public int compareTo(NumberRange that) {
        if (equals(that)) {
            return 0;
        }
        
        boolean a1 = isStartInfinite();
        boolean a2 = isEndInfinite();
        boolean b1 = that.isStartInfinite();
        boolean b2 = that.isEndInfinite();
        
        if (a1 ^ b1) {
            return a1 ? -1 : 1;
        }
        if ((a1 && b1) || (start == that.start)) {
            if (a2 ^ b2) {
                return a2 ? -1 : 1;
            }
            return end - that.end;
        }
        return start - that.start;
    }
    
    /**
     * Tests this NumberRange for equivalence to that NumberRange.
     *
     * Two NumberRanges are considered equal if:
     * <ul>
     * <li>if both are negatively infinite AND both are positively infinite
     * <li>if both are negatively infinite AND neither are positively infinite AND both have the same end point.
     * <li>if neither are negatively infinite AND both have the same start point AND both are positvely infinite
     * <li>if neither are negatively infinite AND both have the same start point AND
     *     neither are positively infinite AND both have the same end point.
     * </ul>
     *
     * @param that the NumberRage to compare with
     * @return true if the NumberRanges are equal
     */
    public boolean equals(NumberRange that) {
        boolean a1 = isStartInfinite();
        boolean a2 = isEndInfinite();
        boolean b1 = that.isStartInfinite();
        boolean b2 = that.isEndInfinite();
        
        return ((a1 && b1) || (!(a1 ^ b1) && (start == that.start))) 
            && ((a2 && b2) || (!(a2 ^ b2) && (end == that.end)));
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (!(that instanceof NumberRange)) {
            return false;
        }
        return equals((NumberRange) that);
    }
    
    @Override
    public String toString() {
        return start + "-" + end;
    }
    
    /**
     * The starting position of this number range.
     *
     * Even if the start value is beyond the negative infinity point
     * this will still return the value of the start position.
     *
     * @return the start
     */
    public int start() {
        return start;
    }
    
    /**
     * The ending position of this number range.
     *
     * Even if the end value is beyond the positive infinity point
     * this will still return the value of the start position.
     *
     * @return the end
     */
    public int end() {
        return end;
    }
    
    /**
     * Does this NumberRange represent a range that is positively infinite.
     *
     * @return true if the end position is &gt;= positive infinity.
     */
    public boolean isEndInfinite() {
        return end >= positiveInfinity;
    }
    
    /**
     * Does this NumberRange represent a range that is negatively infinite.
     *
     * @return true if the start position is &lt;= negative infinity
     */
    public boolean isStartInfinite() {
        return start <= negativeInfinity;
    }
}
