/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.syntax;

import org.jnode.nanoxml.XMLElement;


/**
 * A RepeatedSyntax instance specifies that a given 'child' syntax may appear
 * some number of times.
 * 
 * @author crawley@jnode.org
 */
public class RepeatSyntax extends GroupSyntax {
    
    private final Syntax child;
    private final int minCount;
    private final int maxCount;

    /**
     * Construct syntax with caller-specified repetition count range and a label.
     * 
     * @param label this Syntax's label
     * @param child the child Syntax that may be repeated.
     * @param minCount the minimum number of occurrences required.
     * @param maxCount the maximum number of occurrences allowed.
     * @param description the description for this syntax
     */
    public RepeatSyntax(String label, Syntax child, int minCount, int maxCount, String description) {
        super(label, description, child);
        if (minCount < 0 || maxCount < minCount) {
            throw new IllegalArgumentException("bad min/max counts");
        }
        this.child = child;
        this.minCount = minCount;
        this.maxCount = maxCount;
    }
    
    /**
     * Construct syntax with caller-specified repetition count range and a label.
     * 
     * @param label this Syntax's label
     * @param child the child Syntax that may be repeated.
     * @param minCount the minimum number of occurrences required.
     * @param maxCount the maximum number of occurrences allowed.
     */
    public RepeatSyntax(String label, Syntax child, int minCount, int maxCount) {
        this(label, child, minCount, maxCount, null);
    }

    /**
     * Construct syntax with caller-specified repetition count range.
     * 
     * @param child the child Syntax that may be repeated.
     * @param minCount the minimum number of occurrences required.
     * @param maxCount the maximum number of occurrences allowed.
     */
    public RepeatSyntax(Syntax child, int minCount, int maxCount) {
        this(null, child, minCount, maxCount, null);
    }
    
    /**
     * Construct syntax which can repeated from zero to many times.
     * 
     * @param child the child Syntax that may be repeated.
     */
    public RepeatSyntax(Syntax child) {
        this(null, child, 0, Integer.MAX_VALUE, null);
    }
    
    @Override
    public String toString() {
        return "RepeatedSyntax{" + super.toString() + 
            "minCount=" + minCount + ", maxCount=" + maxCount + "}";
    }

    @Override
    public MuSyntax prepare(ArgumentBundle bundle) {
        // The result of 'prepare' is rather ugly for the unusual cases.  One
        // alternative would be to add new MuSyntax constructs, and that would 
        // make the Mu parsing code more complicated.  Another might be to 
        // create a special Argument class that would operate as a counter.
        String label = MuSyntax.genLabel();
        MuSyntax childSyntax = child.prepare(bundle);
        MuSyntax res, tail;
        if (maxCount == Integer.MAX_VALUE) {
            tail = new MuAlternation(label, 
                    null, 
                    new MuSequence(childSyntax, new MuBackReference(label)));
        } else {
            int tailCount = maxCount - minCount;
            tail = null;
            while (tailCount-- > 0) {
                tail = new MuAlternation(
                        (MuSyntax) null, 
                        (tail == null) ? childSyntax : new MuSequence(childSyntax, tail));
            }
        }
        if (minCount == 0) {
            res = tail;
        } else if (minCount == 1) {
            res = (tail == null) ? childSyntax : new MuSequence(childSyntax, tail);
        } else {
            MuSyntax[] sequence = new MuSyntax[minCount];
            for (int i = 0; i < minCount; i++) {
                sequence[i] = childSyntax;
            }
            res = (tail == null) ? new MuSequence(sequence) :
                new MuSequence(new MuSequence(sequence), tail);
        }
        if (maxCount == Integer.MAX_VALUE) {
            res.resolveBackReferences();
        }
        return res;
    }

    @Override
    public String format(ArgumentBundle bundle) {
        if (minCount == 0) {
            if (maxCount == Integer.MAX_VALUE) {
                return "[ " + child.format(bundle) + " ... ]";
            } else if (maxCount == 1) {
                return "[ " + child.format(bundle) + "]";
            } else {
                return "[ " + child.format(bundle) + " ..." + maxCount + " ]";
            }
        } else if (minCount == 1) {
            if (maxCount == Integer.MAX_VALUE) {
                return child.format(bundle) + " ...";
            } else if (maxCount == 1) {
                return child.format(bundle);
            } else {
                return child.format(bundle) + " ..." + maxCount;
            }
        } else {
            if (maxCount == Integer.MAX_VALUE) {
                return child.format(bundle) + " " + minCount + "...";
            } else {
                return child.format(bundle) + " " + minCount + "..." + maxCount;
            }
        }
    }
    


    @Override
    public XMLElement toXML() {
        XMLElement element = basicElement("optionSet");
        if (minCount > 0) {
            element.setAttribute("minCount", minCount);
        }
        if (minCount != Integer.MAX_VALUE) {
            element.setAttribute("maxCount", maxCount);
        }
        return element;
    }

}
