/* gnu/regexp/RETokenOneOf.java
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
import java.util.Vector;
import java.util.Stack;

final class RETokenOneOf extends REToken {
  private Vector options;
  private boolean negative;
  // True if this RETokenOneOf is supposed to match only one character,
  // which is typically the case of a character class expression.
  private boolean matchesOneChar;

  private Vector addition;
  // This Vector addition is used to store nested character classes.
  // For example, if the original expression is
  //    [2-7a-c[f-k][m-z]&&[^p-v][st]]
  // the basic part /2-7a-c/ is stored in the Vector options, and
  // the additional part /[f-k][m-z]&&[^p-v][st]/ is stored in the
  // Vector addition in the following order (Reverse Polish Notation):
  //           -- The matching result of the basic part is assumed here. 
  //    [f-k]  -- REToken
  //    "|"    -- or
  //    [m-z]  -- REToken
  //    "|"    -- or
  //    false
  //    [^p-v] -- REToken
  //    "|"    -- or
  //    [st]   -- REToken
  //    "|"    -- or
  //    "&"    -- and
  //
  // As it is clear from the explanation above, the Vector addition is
  // effective only when this REToken originates from a character class
  // expression.

  // This constructor is used for convenience when we know the set beforehand,
  // e.g. \d --> new RETokenOneOf("0123456789",false, ..)
  //      \D --> new RETokenOneOf("0123456789",true, ..)

  RETokenOneOf(int subIndex, String optionsStr, boolean negative, boolean insens) {
    super(subIndex);
    options = new Vector();
    this.negative = negative;
    for (int i = 0; i < optionsStr.length(); i++)
      options.addElement(new RETokenChar(subIndex,optionsStr.charAt(i),insens));
    matchesOneChar = true;
  }

  RETokenOneOf(int subIndex, Vector options, boolean negative) {
    super(subIndex);
    this.options = options;
    this.negative = negative;
    matchesOneChar = negative;
  }

  RETokenOneOf(int subIndex, Vector options, Vector addition, boolean negative) {
    super(subIndex);
    this.options = options;
    this.addition = addition;
    this.negative = negative;
    matchesOneChar = (negative || addition != null);
  }

  int getMinimumLength() {
    if (matchesOneChar) return 1;
    int min = Integer.MAX_VALUE;
    int x;
    for (int i=0; i < options.size(); i++) {
      if ((x = ((REToken) options.elementAt(i)).getMinimumLength()) < min)
	min = x;
    }
    return min;
  }

  int getMaximumLength() {
    if (matchesOneChar) return 1;
    int max = 0;
    int x;
    for (int i=0; i < options.size(); i++) {
      if ((x = ((REToken) options.elementAt(i)).getMaximumLength()) > max)
	max = x;
    }
    return max;
  }

    boolean match(CharIndexed input, REMatch mymatch) {
      setHitEnd(input, mymatch);
      if (matchesOneChar) return matchOneChar(input, mymatch);
      else return matchOneRE(input, mymatch);
    }

    boolean matchOneChar(CharIndexed input, REMatch mymatch) {
      REMatch tryMatch;
      boolean tryOnly;
      if (addition == null) {
	  tryMatch = mymatch;
	  tryOnly = false;
      }
      else {
	  tryMatch = (REMatch) mymatch.clone();
	  tryOnly = true;
      }
      boolean b = negative ?
        matchN(input, tryMatch, tryOnly) :
        matchP(input, tryMatch, tryOnly);
      if (addition == null) return b;

      Stack stack = new Stack();
      stack.push(new Boolean(b));
      for (int i=0; i < addition.size(); i++) {
	Object obj = addition.elementAt(i);
	if (obj instanceof REToken) {
	  b = ((REToken)obj).match(input, (REMatch)mymatch.clone());
	  stack.push(new Boolean(b));
	}
	else if (obj instanceof Boolean) {
	  stack.push(obj);
	}
	else if (obj.equals("|")) {
	  b = ((Boolean)stack.pop()).booleanValue();
	  b = ((Boolean)stack.pop()).booleanValue() || b;
	  stack.push(new Boolean(b));
	}
	else if (obj.equals("&")) {
	  b = ((Boolean)stack.pop()).booleanValue();
	  b = ((Boolean)stack.pop()).booleanValue() && b;
	  stack.push(new Boolean(b));
	}
	else {
	  throw new RuntimeException("Invalid object found");
	}
      }
      b = ((Boolean)stack.pop()).booleanValue();
      if (b) {
        ++mymatch.index;
        return next(input, mymatch);
      }
      return false;
    }

    private boolean matchN(CharIndexed input, REMatch mymatch, boolean tryOnly) {
    if (input.charAt(mymatch.index) == CharIndexed.OUT_OF_BOUNDS)
      return false;

    REMatch newMatch = null;
    REMatch last = null;
    REToken tk;
    for (int i=0; i < options.size(); i++) {
	tk = (REToken) options.elementAt(i);
	REMatch tryMatch = (REMatch) mymatch.clone();
	if (tk.match(input, tryMatch)) { // match was successful
	    return false;
	} // is a match
    } // try next option

      if (tryOnly) return true;
    ++mymatch.index;
    return next(input, mymatch);
  }

    private boolean matchP(CharIndexed input, REMatch mymatch, boolean tryOnly) {
    REToken tk;
    for (int i=0; i < options.size(); i++) {
	tk = (REToken) options.elementAt(i);
	REMatch tryMatch = (REMatch) mymatch.clone();
	if (tk.match(input, tryMatch)) { // match was successful
	  if (tryOnly) return true;
	  if (next(input, tryMatch)) {
	      mymatch.assignFrom(tryMatch);
	      return true;
	  }
	}
      }
      return false;
    }

  private boolean matchOneRE(CharIndexed input, REMatch mymatch) {
      REMatch newMatch = findMatch(input, mymatch);
      if (newMatch != null) {
	  mymatch.assignFrom(newMatch);
	    return true;
      }
	    return false;
	}

  REMatch findMatch(CharIndexed input, REMatch mymatch) {
      if (matchesOneChar) return super.findMatch(input, mymatch);
      return findMatch(input, mymatch, 0);
  }

  REMatch backtrack(CharIndexed input, REMatch mymatch, Object param) {
      return findMatch(input, mymatch, ((Integer)param).intValue());
  }

  private REMatch findMatch(CharIndexed input, REMatch mymatch, int optionIndex) {
      for (int i = optionIndex; i < options.size(); i++) {
          REToken tk = (REToken) options.elementAt(i);
	  tk = (REToken) tk.clone();
	  tk.chain(getNext());
          REMatch tryMatch = (REMatch) mymatch.clone();
          if (tryMatch.backtrackStack == null) {
            tryMatch.backtrackStack = new BacktrackStack();
	  }
	  boolean stackPushed = false;
	  if (i + 1 < options.size()) {
            tryMatch.backtrackStack.push(new BacktrackStack.Backtrack(
              this, input, mymatch, new Integer(i + 1)));
	    stackPushed = true;
          }
	  boolean b = tk.match(input, tryMatch);
	  if (b) {
	     return tryMatch;
	  }
	  if (stackPushed) tryMatch.backtrackStack.pop();
      }
      return null; 
  }

  boolean returnsFixedLengthMatches() { return matchesOneChar; }

  int findFixedLengthMatches(CharIndexed input, REMatch mymatch, int max) {
      if (!matchesOneChar)
          return super.findFixedLengthMatches(input, mymatch, max);
      int numRepeats = 0;
      REMatch m = (REMatch) mymatch.clone();
      REToken tk = (REToken) this.clone();
      tk.chain(null);
      while (true) {
	  if (numRepeats >= max) break;
          m = tk.findMatch(input, m);
          if (m == null) break;
          numRepeats++;
      }
      return numRepeats;
    }

  void dump(StringBuffer os) {
    os.append(negative ? "[^" : "(?:");
    for (int i = 0; i < options.size(); i++) {
      if (!negative && (i > 0)) os.append('|');
      ((REToken) options.elementAt(i)).dumpAll(os);
    }
    os.append(negative ? ']' : ')');
  }  
}
