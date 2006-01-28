/* gnu/regexp/RETokenRepeated.java
   Copyright (C) 1998-2001, 2004 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package gnu.regexp;

import java.util.Vector;

final class RETokenRepeated extends REToken {
    private REToken token;
    private int min,max;
    private boolean stingy;
    private boolean possessive;
    private boolean alwaysEmpty; // Special case of {0}
    
    RETokenRepeated(int subIndex, REToken token, int min, int max) {
	super(subIndex);
	this.token = token;
	this.min = min;
	this.max = max;
	alwaysEmpty = (min == 0 && max == 0);
    }

    /** Sets the minimal matching mode to true. */
    void makeStingy() {
	stingy = true;
    }
    
    /** Queries if this token has minimal matching enabled. */
    boolean isStingy() {
	return stingy;
    }
    
    /** Sets possessive matching mode to true. */
    void makePossessive() {
        possessive = true;
    }

    /** Queries if this token has possessive matching enabled. */
    boolean isPossessive() {
        return possessive;
    }
    
    /**
     * The minimum length of a repeated token is the minimum length
     * of the token multiplied by the minimum number of times it must
     * match.
     */
    int getMinimumLength() {
	return (min * token.getMinimumLength());
    }

    // We do need to save every possible point, but the number of clone()
    // invocations here is really a killer for performance on non-stingy
    // repeat operators.  I'm open to suggestions...

    // Hypothetical question: can you have a RE that matches 1 times,
    // 3 times, 5 times, but not 2 times or 4 times?  Does having
    // the subexpression back-reference operator allow that?

    boolean match(CharIndexed input, REMatch mymatch) {
	int origin = mymatch.index;
	// number of times we've matched so far
	int numRepeats = 0; 
	
	// Possible positions for the next repeat to match at
	REMatch newMatch = mymatch;
	REMatch last = null;
	REMatch current;

	// Add the '0-repeats' index
	// positions.elementAt(z) == position [] in input after <<z>> matches
	Vector positions = new Vector();
	positions.addElement(newMatch);
	
	// Declare variables used in loop
	REMatch doables;
	REMatch doablesLast;
	REMatch recurrent;
	int lastIndex = mymatch.index;

	do {
	    // Check for stingy match for each possibility.
	    if ((stingy && (numRepeats >= min)) || alwaysEmpty) {
		REMatch result = matchRest(input, newMatch);
		if (result != null) {
		    mymatch.assignFrom(result);
		    mymatch.empty = (mymatch.index == origin);
		    return true;
		}
		else {
	    	// Special case of {0}. It must always match an empty string.
		    if (alwaysEmpty) return false;
		}
	    }

	    doables = null;
	    doablesLast = null;

	    // try next repeat at all possible positions
	    for (current = newMatch; current != null; current = current.next) {
		recurrent = (REMatch) current.clone();
		if (token.match(input, recurrent)) {
		    // add all items in current to doables array
		    if (doables == null) {
			doables = recurrent;
			doablesLast = recurrent;
		    } else {
			// Order these from longest to shortest
			// Start by assuming longest (more repeats)
			doablesLast.next = recurrent;
		    }
		    // Find new doablesLast
		    while (doablesLast.next != null) {
			doablesLast = doablesLast.next;
		    }
		}
	    }
	    // if none of the possibilities worked out, break out of do/while
	    if (doables == null) break;
	    
	    // reassign where the next repeat can match
	    newMatch = doables;
	    
	    // increment how many repeats we've successfully found
	    ++numRepeats;
	    
	    positions.addElement(newMatch);

	    // doables.index == lastIndex occurs either
	    //   (1) when an empty string was the longest
	    //       that matched this token.
	    //       And this case occurs either
	    //         (1-1) when this token is always empty,
	    //               for example "()" or "(())".
	    //         (1-2) when this token is not always empty
	    //               but can match an empty string, for example,
	    //               "a*", "(a|)".
	    // or
	    //   (2) when the same string matches this token many times.
	    //       For example, "acbab" itself matches "a.*b" and
	    //       its substrings "acb" and "ab" also match.
	    if (doables.index == lastIndex) {
	        if (doables.empty) {
	    	    // Case (1): We break here, otherwise we will fall
	    	    //          into an endless loop.
		    if (numRepeats < min) numRepeats = min;
		    break;
		}
	        else {
		    // Case (2): We cannot break here because, for example,
		    // "acbacb" matches "a.*b" up to 2 times but
		    // not 3 times.  So we have to check numRepeats >= min.
		    // But we do not have to go further until numRepeats == max
		    // because the more numRepeats grows, the shorter the
		    // substring matching this token becomes. 
		    if (numRepeats > min) {
			// This means the previous match was successful,
			// and that must be the best match.  This match
			// resulted in shortening the matched substring.
			numRepeats--;
	    		positions.remove(positions.size() - 1);
			break;
		    }
		    if (numRepeats == min) break;
		}
	    }		
	    lastIndex = doables.index;
	} while (numRepeats < max);
	
	// If there aren't enough repeats, then fail
	if (numRepeats < min) return false;
	
	// We're greedy, but ease off until a true match is found 
	int posIndex = positions.size();
	
	// At this point we've either got too many or just the right amount.
	// See if this numRepeats works with the rest of the regexp.
	REMatch allResults = null;
	REMatch allResultsLast = null;

	REMatch results = null;
	int indexCount = posIndex - min;
	if (indexCount <= 0) {
	    // This case occurs when we exited the previous do loop before
	    // numRepeats >= min because an empty string matched the token.
	    // In this case, an empty string can match as many times as
	    // desired.
	    indexCount = 1;
	}
	while (indexCount-- > 0) {
	    --posIndex;
	    newMatch = (REMatch) positions.elementAt(posIndex);
	    results = matchRest(input, newMatch);
	    if (results != null) {
		if (allResults == null) {
		    allResults = results;
		    allResultsLast = results;
		} else {
		    // Order these from longest to shortest
		    // Start by assuming longest (more repeats)
		    allResultsLast.next = results;
		}
		// Find new doablesLast
		while (allResultsLast.next != null) {
		    allResultsLast = allResultsLast.next;
		}
	    }
	    // else did not match rest of the tokens, try again on smaller sample
	    // or break out when performing possessive matching
	    if (possessive) break;
	}
	if (allResults != null) {
	    mymatch.assignFrom(allResults); // does this get all?
	    mymatch.empty = (mymatch.index == origin);
	    return true;
	}
	// If we fall out, no matches.
	return false;
    }

    private REMatch matchRest(CharIndexed input, final REMatch newMatch) {
	REMatch current, single;
	REMatch doneIndex = null;
	REMatch doneIndexLast = null;
	// Test all possible matches for this number of repeats
	for (current = newMatch; current != null; current = current.next) {
	    // clone() separates a single match from the chain
	    single = (REMatch) current.clone();
	    if (next(input, single)) {
		// chain results to doneIndex
		if (doneIndex == null) {
		    doneIndex = single;
		    doneIndexLast = single;
		} else {
		    doneIndexLast.next = single;
		}
		// Find new doneIndexLast
		while (doneIndexLast.next != null) {
		    doneIndexLast = doneIndexLast.next;
		}
	    }
	}
	return doneIndex;
    }

    void dump(StringBuffer os) {
	os.append("(?:");
	token.dumpAll(os);
	os.append(')');
	if ((max == Integer.MAX_VALUE) && (min <= 1))
	    os.append( (min == 0) ? '*' : '+' );
	else if ((min == 0) && (max == 1))
	    os.append('?');
	else {
	    os.append('{').append(min);
	    if (max > min) {
		os.append(',');
		if (max != Integer.MAX_VALUE) os.append(max);
	    }
	    os.append('}');
	}
	if (stingy) os.append('?');
    }
}
