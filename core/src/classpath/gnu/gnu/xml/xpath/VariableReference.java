/* VariableReference.java -- 
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

package gnu.xml.xpath;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Node;
import gnu.xml.transform.Bindings;

public class VariableReference
  extends Expr
{

  final XPathVariableResolver resolver;
  final QName name;

  public VariableReference(XPathVariableResolver resolver, QName name)
  {
    this.resolver = resolver;
    this.name = name;
  }

  public Object evaluate(Node context, int pos, int len)
  {
    if (resolver != null)
      {
        if (resolver instanceof Bindings)
          {
            // Needs context to operate properly
            return ((Bindings) resolver).get(name, context, pos, len);
          }
        return resolver.resolveVariable(name);
      }
    throw new IllegalStateException("no variable resolver");
  }

  public Expr clone(Object context)
  {
    XPathVariableResolver r = resolver;
    if (context instanceof XPathVariableResolver)
      {
        r = (XPathVariableResolver) context;
      }
    return new VariableReference(r, name);
  }

  public boolean references(QName var)
  {
    return name.equals(var);
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer("$");
    String prefix = name.getPrefix();
    if (prefix != null && !"".equals(prefix))
      {
        buf.append(prefix);
        buf.append(':');
      }
    buf.append(name.getLocalPart());
    return buf.toString();
  }
  
}
