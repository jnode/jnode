/* gnu/regexp/RETokenLookBehind.java
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

package gnu.regexp;

/**
 * @author Ito Kazumitsu
 */
final class RETokenLookBehind extends REToken
{
  REToken re;
  boolean negative;

  RETokenLookBehind(REToken re, boolean negative) throws REException {
    super(0);
    this.re = re;
    this.negative = negative;
  }

  int getMaximumLength() {
    return 0;
  }

  boolean match(CharIndexed input, REMatch mymatch)
  {
    int max = re.getMaximumLength();
    CharIndexed behind = input.lookBehind(mymatch.index, max);
    REMatch trymatch = (REMatch)mymatch.clone();
    REMatch trymatch1 = (REMatch)mymatch.clone();
    REMatch newMatch = null;
    int curIndex = trymatch.index + behind.length() - input.length();
    trymatch.index = 0;
    RETokenMatchHereOnly stopper = new RETokenMatchHereOnly(curIndex);
    REToken re1 = (REToken) re.clone();
    re1.chain(stopper);
    if (re1.match(behind, trymatch)) {
      if (negative) return false;
      if (next(input, trymatch1))
        newMatch = trymatch1;
    }

    if (newMatch != null) {
      if (negative) return false;
      //else
      mymatch.assignFrom(newMatch);
      return true;
    }
    else { // no match
      if (negative)
        return next(input, mymatch);
      //else
      return false;
    }
  }

    void dump(StringBuffer os) {
	os.append("(?<");
	os.append(negative ? '!' : '=');
	re.dumpAll(os);
	os.append(')');
    }

    private static class RETokenMatchHereOnly extends REToken {

        int getMaximumLength() { return 0; }

	private int index;

	RETokenMatchHereOnly(int index) {
	    super(0);
	    this.index = index;
	}

	boolean match(CharIndexed input, REMatch mymatch) {
	    return index == mymatch.index;
	}

        void dump(StringBuffer os) {}

    }
}

