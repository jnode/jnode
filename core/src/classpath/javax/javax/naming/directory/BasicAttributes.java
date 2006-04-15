/* BasicAttributes.java --
   Copyright (C) 2000, 2001, 2004, 2005, 2006  Free Software Foundation, Inc.

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


package javax.naming.directory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * @author Tom Tromey (tromey@redhat.com)
 * @date June 22, 2001
 */
public class BasicAttributes implements Attributes
{
  private static final long serialVersionUID = 4980164073184639448L;
  
  public BasicAttributes ()
  {
    this (false);
  }

  public BasicAttributes (boolean ignoreCase)
  {
    this.ignoreCase = ignoreCase;
    this.attributes = new Vector ();
  }

  public BasicAttributes (String attrID, Object val)
  {
    this (attrID, val, false);
  }

  public BasicAttributes (String attrID, Object val, boolean ignoreCase)
  {
    this.ignoreCase = ignoreCase;
    attributes = new Vector ();
    attributes.add (new BasicAttribute (attrID, val));
  }

  public Object clone ()
  {
    // Slightly inefficient as we make a garbage Vector here.
    BasicAttributes ba = new BasicAttributes (ignoreCase);
    ba.attributes = (Vector) attributes.clone ();
    return ba;
  }

  /**
   * Returns true if and only if the given Object is an instance of
   * Attributes, the given attributes both do or don't ignore case for
   * IDs and the collection of attributes is the same.
   */
  public boolean equals (Object obj)
  {
    if (! (obj instanceof Attributes))
      return false;

    Attributes bs = (Attributes) obj;
    if (ignoreCase != bs.isCaseIgnored()
	|| attributes.size () != bs.size ())
      return false;

    NamingEnumeration bas = bs.getAll();
    while (bas.hasMoreElements())
      {
	Attribute a = (Attribute) bas.nextElement();
	Attribute b = get(a.getID ());
	if (! a.equals(b))
	  return false;
      }

    return true;
  }

  public Attribute get (String attrID)
  {
    for (int i = 0; i < attributes.size (); ++i)
      {
	Attribute at = (Attribute) attributes.get (i);
	if ((ignoreCase && attrID.equalsIgnoreCase (at.getID ()))
	    || (! ignoreCase && attrID.equals (at.getID ())))
	  return at;
      }

    return null;
  }

  public NamingEnumeration getAll ()
  {
    return new BasicAttributesEnumeration (false);
  }

  public NamingEnumeration getIDs ()
  {
    return new BasicAttributesEnumeration (true);
  }

  public int hashCode ()
  {
    int val = 0;
    for (int i = 0; i < attributes.size (); ++i)
      val += attributes.get (i).hashCode ();
    return val;
  }

  public boolean isCaseIgnored ()
  {
    return ignoreCase;
  }

  public Attribute put (Attribute attr)
  {
    Attribute r = remove (attr.getID ());
    attributes.add (attr);
    return r;
  }

  public Attribute put (String attrID, Object val)
  {
    return put (new BasicAttribute (attrID, val));
  }

  public Attribute remove (String attrID)
  {
    for (int i = 0; i < attributes.size (); ++i)
      {
	Attribute at = (Attribute) attributes.get (i);
	if ((ignoreCase && attrID.equalsIgnoreCase (at.getID ()))
	    || (! ignoreCase && attrID.equals (at.getID ())))
	  {
	    attributes.remove (i);
	    return at;
	  }
      }

    return null;
  }

  public int size ()
  {
    return attributes.size ();
  }

  public String toString ()
  {
    String r = "";
    for (int i = 0; i < attributes.size (); ++i)
      {
	if (i > 0)
	  r += "; ";
	r += attributes.get (i).toString ();
      }
    return r;
  }

  // This is set by the serialization spec.
  private boolean ignoreCase;
  // Package-private to avoid a trampoline.
  transient Vector attributes;

  private void readObject(ObjectInputStream s) throws IOException,
      ClassNotFoundException
  {
    s.defaultReadObject();
    int size = s.readInt();
    attributes = new Vector(size);    
    for (int i = 0; i < size; i++)
      attributes.add(s.readObject());
  }

  private void writeObject(ObjectOutputStream s) throws IOException
  {
    s.defaultWriteObject();
    s.writeInt(attributes.size());
    for (int i = 0; i < attributes.size(); i++)
      s.writeObject(attributes.get(i));
  }

  // Used when enumerating.
  private class BasicAttributesEnumeration implements NamingEnumeration
  {
    int where = 0;
    boolean id;

    public BasicAttributesEnumeration (boolean id)
    {
      this.id = id;
    }

    public void close () throws NamingException
    {
    }

    public boolean hasMore () throws NamingException
    {
      return hasMoreElements ();
    }

    public Object next () throws NamingException
    {
      return nextElement ();
    }

    public boolean hasMoreElements ()
    {
      return where < attributes.size ();
    }

    public Object nextElement () throws NoSuchElementException
    {
      if (where >= attributes.size ())
	throw new NoSuchElementException ("no more elements");
      Attribute at = (Attribute) attributes.get (where);
      ++where;
      return id ? (Object) at.getID () : (Object) at;
    }
  }
}
