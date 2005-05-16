/* gnuNVList.java --
   Copyright (C) 2005 Free Software Foundation, Inc.

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


package gnu.CORBA;

import org.omg.CORBA.Any;
import org.omg.CORBA.Bounds;
import org.omg.CORBA.NVList;
import org.omg.CORBA.NamedValue;

/**
 * The implementation of {@link NVList}.
 * @author Audrius Meskauskas (AudriusA@Bioinformatics.org)
 */
public class gnuNVList
  extends NVList
{
  /**
   * The list of the named values.
   */
  protected corbaArrayList list;

  /**
   * Creates the list with the default initial size.
   */
  public gnuNVList()
  {
    list = new corbaArrayList();
  }

  /**
   * Creates the list with the given initial size.
   */
  public gnuNVList(int initial_size)
  {
    list = new corbaArrayList(initial_size);
  }

  /** {@inheritDoc} */
  public NamedValue add(int a_flags)
  {
    return add_value(null, null, a_flags);
  }

  /** {@inheritDoc} */
  public NamedValue add_item(String a_name, int a_flags)
  {
    return add_value(a_name, null, a_flags);
  }

  /** {@inheritDoc} */
  public NamedValue add_value(String a_name, Any a_value, int a_flags)
  {
    gnuNamedValue n = new gnuNamedValue();
    n.setName(a_name);
    n.setValue(a_value);
    n.setFlags(a_flags);
    list.add(n);
    return n;
  }
  
  /**
   * Add the given named value to the list directly.
   * 
   * @param value the named vaue to add.
   */
  public void add(NamedValue value)
  {
    list.add(value);
  }  
  

  /** {@inheritDoc} */
  public int count()
  {
    return list.size();
  }

  /** {@inheritDoc} */
  public NamedValue item(int at)
                  throws Bounds
  {
    return (NamedValue) list.item(at);
  }

  /** {@inheritDoc} */
  public void remove(int at)
              throws Bounds
  {
    list.drop(at);
  }
}
