/* gnu/regexp/RETokenRepeated.java
   Copyright (C) 2006 Free Software Foundation, Inc.

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


package gnu.java.util.regex;

// import java.util.Vector;
// import java.util.Stack;

final class RETokenRepeated extends REToken {
    private REToken token;
    private int min,max;
    private boolean stingy;
    private boolean possessive;
    private int tokenFixedLength;

    RETokenRepeated(int subIndex, REToken token, int min, int max) {
    super(subIndex);
    this.token = token;
    this.min = min;
    this.max = max;
	if (token.returnsFixedLengthMatches()) {
	    tokenFixedLength = token.getMaximumLength();
	}
	else {
	    tokenFixedLength = -1;
	}
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

    int getMaximumLength() {
        if (max == Integer.MAX_VALUE) return Integer.MAX_VALUE;
    int tmax = token.getMaximumLength();
    if (tmax == Integer.MAX_VALUE) return tmax;
    return (max * tmax);
    }

    // The comment "MUST make a clone" below means that some tests
    // failed without doing clone(),

    private static class DoablesFinder {
	private REToken tk;
	private CharIndexed input;
	private REMatch rematch;
	private boolean findFirst;

        private DoablesFinder(REToken tk, CharIndexed input, REMatch mymatch) {
	    this.tk = tk;
	    this.input = input;
	    this.rematch = (REMatch) mymatch.clone(); // MUST make a clone
	    this.rematch.backtrackStack = new BacktrackStack();
	    findFirst = true;
	}

	private REMatch find() {
	    int origin = rematch.index;
	    REMatch rem;
	    if (findFirst) {
		rem = tk.findMatch(input, rematch);
		findFirst = false;
    }
	    else {
	        while (true) {
		    if (rematch.backtrackStack.empty()) {
			rem = null;
			break;
        }
		    BacktrackStack.Backtrack bt = rematch.backtrackStack.pop();
		    rem = bt.token.backtrack(bt.input, bt.match, bt.param);
		    if (rem != null) break;
        }
	    }
	    if (rem == null) return null;
	    if (rem.index == origin) rem.empty = true;
	    rematch = rem;
	    return (REMatch) rem.clone(); // MUST make a clone.
    }

	boolean noMore() {
	    return rematch.backtrackStack.empty();
	}
    }

    REMatch findMatch(CharIndexed input, REMatch mymatch) {
        if (tokenFixedLength >= 0) return findMatchFixedLength(input, mymatch);
	BacktrackStack stack = new BacktrackStack();
	stack.push(new StackedInfo(input, 0, mymatch, null, null));
	return findMatch(stack);
    }

    REMatch backtrack(CharIndexed input, REMatch mymatch, Object param) {
        if (tokenFixedLength >= 0) return backtrackFixedLength(input, mymatch, param);
	return findMatch((BacktrackStack)param);
        }

    private static class StackedInfo extends BacktrackStack.Backtrack {
        int numRepeats;
	int[] visited;
        DoablesFinder finder;
        StackedInfo(CharIndexed input, int numRepeats, REMatch match,
	        int[] visited, DoablesFinder finder) {
	    super(null, input, match, null);
            this.numRepeats = numRepeats;
	    this.visited = visited;
            this.finder = finder;
    }
    }

    private REMatch findMatch(BacktrackStack stack) {
        // Avoid using recursive calls.
	MAIN_LOOP:
	while (true) {

	if (stack.empty()) return null;
	StackedInfo si = (StackedInfo)(stack.peek());
	CharIndexed input = si.input;
        int numRepeats = si.numRepeats;
        REMatch mymatch = si.match;
	int[] visited = si.visited;
        DoablesFinder finder = si.finder;
        
	if (mymatch.backtrackStack == null)
	  mymatch.backtrackStack = new BacktrackStack();

	if (numRepeats >= max) {
	    stack.pop();
	    REMatch m1 = matchRest(input, mymatch);
	    if (m1 != null) {
		if (! stack.empty()) {
	            m1.backtrackStack.push(new BacktrackStack.Backtrack(
		        this, input, mymatch, stack));
		}
		return m1;
	    }
	    if (stingy) {
		continue MAIN_LOOP;
	    }
	    return null;
	}

        if (finder == null) {
	    finder = new DoablesFinder(token, input, mymatch);
	    si.finder = finder;
	}

        if (numRepeats < min) {
	    while (true) {
	        REMatch doable = finder.find();
	        if (doable == null) {
		    if (stack.empty()) return null;
		    stack.pop();
		    continue MAIN_LOOP;
		}
		if (finder.noMore()) stack.pop();
		int newNumRepeats = (doable.empty ? min : numRepeats + 1);
		stack.push(new StackedInfo(
		    input, newNumRepeats, doable, visited, null));
		continue MAIN_LOOP;
	    }
	}

	if (visited == null) visited = initVisited();

        if (stingy) {
	    REMatch nextMatch = finder.find();
	    if (nextMatch != null && !nextMatch.empty) {
	        stack.push(new StackedInfo(
	            input, numRepeats + 1, nextMatch, visited, null));
	    }
	    else {
		stack.pop();
	    }  	
	    REMatch m1 = matchRest(input, mymatch);
	    if (m1 != null) {
		if (!stack.empty()) {
	            m1.backtrackStack.push(new BacktrackStack.Backtrack(
		        this, input, mymatch, stack));
            }
	        return m1;
	    }
	    else {
		continue MAIN_LOOP;
        }
        }

	visited = addVisited(mymatch.index, visited);

        DO_THIS:
        do {

        boolean emptyMatchFound = false;

	    DO_ONE_DOABLE:
	    while (true) {

	    REMatch doable = finder.find();
	    if (doable == null) {
		break DO_THIS;
	    }
	    if (doable.empty) emptyMatchFound = true;

        if (!emptyMatchFound) {
		int n = doable.index;
            if (! visitedContains(n, visited)) {
                visited = addVisited(n, visited);
            }
		else {
		    continue DO_ONE_DOABLE;
            }
	        stack.push(new StackedInfo(
		    input, numRepeats + 1, doable, visited, null));
	        REMatch m1 = findMatch(stack);
		if (possessive) {
		    return m1;
        }
            if (m1 != null) {
		    m1.backtrackStack.push(new BacktrackStack.Backtrack(
                        this, input, mymatch, stack));
                    return m1;
                }
        }
            else {
	        REMatch m1 = matchRest(input, doable);
		if (possessive) {
                    return m1;
                }
	        if (m1 != null) {
		    if (! stack.empty()) {
		        m1.backtrackStack.push(new BacktrackStack.Backtrack(
                            this, input, mymatch, stack));
            }
		    return m1;
        }
        }

	    } // DO_ONE_DOABLE

        } while (false); // DO_THIS only once;

	if (!stack.empty()) {
	    stack.pop();
            }
	if (possessive) {
	    stack.clear();
        }
	REMatch m1 = matchRest(input, mymatch);
	if (m1 != null) {
	    if (! stack.empty()) {
	        m1.backtrackStack.push(new BacktrackStack.Backtrack(
	            this, input, mymatch, stack));
    }
	    return m1;
    }

	} // MAIN_LOOP
    }

    boolean match(CharIndexed input, REMatch mymatch) {
	setHitEnd(input, mymatch);
	REMatch m1 = findMatch(input, mymatch);
	if (m1 != null) {
	    mymatch.assignFrom(m1);
	    return true;
	}
	return false;
    }    

    // Array visited is an array of character positions we have already
    // visited. visited[0] is used to store the effective length of the
    // array.
    private static int[] initVisited() {
	int[] visited = new int[32];
	visited[0] = 0;
	return visited;
    }

    private static boolean visitedContains(int n, int[] visited) {
	// Experience tells that for a small array like this,
	// simple linear search is faster than binary search.
	for (int i = 1; i < visited[0]; i++) {
	    if (n == visited[i]) return true;
	}
	return false;
    }

    private static int[] addVisited(int n, int[] visited) {
	if (visitedContains(n, visited)) return visited;
	if (visited[0] >= visited.length - 1) {
	    int[] newvisited = new int[visited.length + 32];
	    System.arraycopy(visited, 0, newvisited, 0, visited.length);
	    visited = newvisited;
    }
	visited[0]++;
	visited[visited[0]] = n;
	return visited;
    }

    private REMatch matchRest(CharIndexed input, final REMatch newMatch) {
	if (next(input, newMatch)) {
	    return newMatch;
	}
	return null;
    }

    private REMatch findMatchFixedLength(CharIndexed input, REMatch mymatch) {
	if (mymatch.backtrackStack == null)
	  mymatch.backtrackStack = new BacktrackStack();
        int numRepeats = token.findFixedLengthMatches(input, (REMatch)mymatch.clone(), max);
	if (numRepeats == Integer.MAX_VALUE) numRepeats = min;
	int count = numRepeats - min + 1;
        if (count <= 0) return null;
	int index = 0;
	if (!stingy) index = mymatch.index + (tokenFixedLength * numRepeats);
	else index = mymatch.index + (tokenFixedLength * min);
	return findMatchFixedLength(input, mymatch, index, count);
    }

    private REMatch backtrackFixedLength(CharIndexed input, REMatch mymatch,
    	    Object param) {
	int[] params = (int[])param;
        int index = params[0];
	int count = params[1];
	return findMatchFixedLength(input, mymatch, index, count);
    }        

    private REMatch findMatchFixedLength(CharIndexed input, REMatch mymatch,
    	    	    int index, int count) {
        REMatch tryMatch = (REMatch) mymatch.clone();
	while (true) {
	    tryMatch.index = index;
	    REMatch m = matchRest(input, tryMatch);
	    count--;
	    if (stingy) index += tokenFixedLength;
	    else index -= tokenFixedLength;
	    if (possessive) return m;
	    if (m != null) {
		if (count > 0) {
	            m.backtrackStack.push(new BacktrackStack.Backtrack(
		        this, input, mymatch,
			new int[] {index, count}));
	        }
		return m;
        }
	    if (count <= 0) return null;
    }
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
