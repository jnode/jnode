/* StringLengthFunction.java -- 
   Copyright (C) 2004 Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

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

package gnu.xml.xpath;

import java.util.List;
import org.w3c.dom.Node;

/**
 * The <code>string-length</code> function returns the number of characters
 * in the string.
 * If the argument is omitted, it defaults to the context node converted to
 * a string, in other words the string-value of the context node.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
final class StringLengthFunction
  extends Expr
{

  final Expr arg;

  StringLengthFunction(List args)
  {
    this(args.isEmpty() ? null : (Expr) args.get(0));
  }
  
  StringLengthFunction(Expr arg)
  {
    this.arg = arg;
  }

  public Object evaluate(Node context, int pos, int len)
  {
    Object val = (arg == null) ? null : arg.evaluate(context, pos, len);
    String s = _string(context, val);
    return new Double((double) s.length());
  }

  public Expr clone(Object context)
  {
    return new StringLengthFunction((arg == null) ? null :
                                    arg.clone(context));
  }

  public String toString()
  {
    return (arg == null) ? "string-length()" : "string-length(" + arg + ")";
  }
  
}
