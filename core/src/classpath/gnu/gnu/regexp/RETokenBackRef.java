/* gnu/regexp/RETokenBackRef.java
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

final class RETokenBackRef extends REToken {
  private int num;
  private boolean insens;
  
  RETokenBackRef(int subIndex, int num, boolean insens) {
    super(subIndex);
    this.num = num;
    this.insens = insens;
  }

  // should implement getMinimumLength() -- any ideas?

    boolean match(CharIndexed input, REMatch mymatch) {
	if (num >= mymatch.start.length) return false;
	if (num >= mymatch.end.length) return false;
	int b,e;
	b = mymatch.start[num];
	e = mymatch.end[num];
	if ((b==-1)||(e==-1)) return false; // this shouldn't happen, but...
	int origin = mymatch.index;
	for (int i=b; i<e; i++) {
	    char c1 = input.charAt(mymatch.index+i-b);
	    char c2 = input.charAt(i);
	    if (c1 != c2) {
		if (insens) {
		    if (c1 != Character.toLowerCase(c2) &&
			c1 != Character.toUpperCase(c2)) {
		return false;
	    }
	}
		else {
		    return false;
		}
	    }
	}
	mymatch.index += e-b;
	boolean result = next(input, mymatch);
	if (result) mymatch.empty = (mymatch.index == origin);
	return result;
    }
    
    void dump(StringBuffer os) {
	os.append('\\').append(num);
    }
}


