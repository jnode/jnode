/* SimpleAttributeSet.java --
   Copyright (C) 2004, 2005 Free Software Foundation, Inc.

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


package javax.swing.text;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

public class SimpleAttributeSet
  implements MutableAttributeSet, Serializable, Cloneable
{
  /** The serialization UID (compatible with JDK1.5). */
  private static final long serialVersionUID = 8267656273837665219L;

  public static final AttributeSet EMPTY = new SimpleAttributeSet();

  Hashtable tab;

  public SimpleAttributeSet()
  {
    this(null);
  }
  
  public SimpleAttributeSet(AttributeSet a)
  {
    tab = new Hashtable();
    if (a != null)
      addAttributes(a);
  }

  public void addAttribute(Object name, Object value)
  {
    tab.put(name, value);
  }

  public void addAttributes(AttributeSet attributes)
  {
    Enumeration e = attributes.getAttributeNames();
    while (e.hasMoreElements())
      {
        Object name = e.nextElement();
        Object val = attributes.getAttribute(name);
        tab.put(name, val);
      }
  }

  public Object clone()
  {
    SimpleAttributeSet s = new SimpleAttributeSet();
    s.tab = (Hashtable) tab.clone();
    return s;
  }

  /**
   * Returns true if the given name and value represent an attribute
   * found either in this AttributeSet or in its resolve parent hierarchy.
   * @param name the key for the attribute
   * @param value the value for the attribute
   * @return true if the attribute is found here or in this set's resolve
   * parent hierarchy
   */
  public boolean containsAttribute(Object name, Object value)
  {
    return (tab.containsKey(name) && tab.get(name).equals(value)) || 
      (getResolveParent() != null && getResolveParent().
       containsAttribute(name, value));
  }
  
  /**
   * Returns true if the given name and value are found in this AttributeSet.
   * Does not check the resolve parent.
   * @param name the key for the attribute
   * @param value the value for the attribute
   * @return true if the attribute is found in this AttributeSet
   */
  boolean containsAttributeLocally(Object name, Object value)
  {
    return tab.containsKey(name) 
      && tab.get(name).equals(value);
  }

  public boolean containsAttributes(AttributeSet attributes)
  {
    Enumeration e = attributes.getAttributeNames();
    while (e.hasMoreElements())
      {
        Object name = e.nextElement();
        Object val = attributes.getAttribute(name);
        if (! containsAttribute(name, val))
          return false;		
      }
    return true;
  }

  public AttributeSet copyAttributes()
  {
    return (AttributeSet) clone();
  }

  public boolean equals(Object obj)
  {
    return 
      (obj instanceof AttributeSet)
      && this.isEqual((AttributeSet) obj);
  }

  public Object getAttribute(Object name)
  {
    Object val = tab.get(name);
    if (val != null) 
      return val;

    Object p = getResolveParent();
    if (p != null && p instanceof AttributeSet)
      return (((AttributeSet)p).getAttribute(name));

    return null;
  }

  public int getAttributeCount()
  {
    return tab.size();
  }

  public Enumeration getAttributeNames()
  {
    return tab.keys();
  }

  public AttributeSet getResolveParent()
  {
    return (AttributeSet) tab.get(ResolveAttribute);
  }

  public int hashCode()
  {
    return tab.hashCode();
  }

  public boolean isDefined(Object attrName)
  {
    return tab.containsKey(attrName);
  }

  public boolean isEmpty()
  {
    return tab.isEmpty();	
  }

  /**
   * Returns true if the given set has the same number of attributes
   * as this set and <code>containsAttributes(attr)</code> returns
   * true.
   */
  public boolean isEqual(AttributeSet attr)
  {
    return getAttributeCount() == attr.getAttributeCount()
      && this.containsAttributes(attr);
  }
    
  public void removeAttribute(Object name)
  {
    tab.remove(name);
  }

  /**
   * Removes attributes from this set if they are found in the 
   * given set.  Only attributes whose key AND value are removed.
   * Removes attributes only from this set, not from the resolving parent.
   */
  public void removeAttributes(AttributeSet attributes)
  {
    Enumeration e = attributes.getAttributeNames();
    while (e.hasMoreElements())
      {
        Object name = e.nextElement();
        Object val = attributes.getAttribute(name);
        if (containsAttributeLocally(name, val))
          removeAttribute(name);     
      }
  }

  public void removeAttributes(Enumeration names)
  {
    while (names.hasMoreElements())
      {
        removeAttribute(names.nextElement());
      }	
  }

  public void setResolveParent(AttributeSet parent)
  {
    addAttribute(ResolveAttribute, parent);
  }
    
  public String toString()
  {
    return tab.toString();
  }    
}
