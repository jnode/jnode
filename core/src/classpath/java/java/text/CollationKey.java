/* CollationKey.java -- Precomputed collation value
   Copyright (C) 1998 Free Software Foundation, Inc.

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


package java.text;

/**
  * This class represents a pre-computed series of bits representing a
  * <code>String</code> for under a particular <code>Collator</code>.  This
  * value may be compared bitwise against another <code>CollationKey</code>
  * representing a different <code>String</code> under the same
  * <code>Collator</code> in a manner than is usually more efficient than
  * using the raw <code>Collator</code> compare methods.  There is overhead
  * associated with calculating this value, so it is generally not
  * advisable to compute <code>CollationKey</code>'s unless multiple 
  * comparisons against a <code>String</code> will be done.  (For example,
  * in a sort routine).
  * <p>
  * This class cannot be instantiated directly.  Instead, a 
  * <code>CollationKey</code> is created by calling the
  * <code>getCollationKey</code> method on an instance of <code>Collator</code>.
  *
  * @version 0.0
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  */
public final class CollationKey implements Comparable
{

/*
 * Instance Variables
 */

/**
  * This is the <code>Collator</code> this object was created from.
  */
private Collator collator;

/**
  * This is the <code>String</code> this object represents.
  */
private String str;

/**
  * This is the bit value for this key.
  */
private byte[] key;

/*************************************************************************/

/*
 * Constructors (no public constructors)
 */

CollationKey(Collator collator, String str, byte[] key)
{
  this.collator = collator;
  this.str = str;
  this.key = key;
}

/*************************************************************************/

/*
 * Instance Methods
 */

/**
  * This method returns the <code>String</code> that this object was created
  * from.
  *
  * @return The source <code>String</code> for this object.
  */
public String
getSourceString()
{
  return(str);
}

/*************************************************************************/

/**
  * This method returns the collation bit sequence as a byte array.
  *
  * @param A byte array containing the collation bit sequence.
  */
public byte[]
toByteArray()
{
  return(key);
}

/*************************************************************************/

/**
  * This method compares the specified object to this one.  The specified
  * object must be an instance of <code>CollationKey</code> or an exception
  * will be thrown.  An integer is returned which indicates whether the
  * specified object is less than, greater than, or equal to this object.
  *
  * @param obj The <code>Object</code> to compare against this one.
  *
  * @return A negative integer if this object is less than the specified object, 0 if it is equal or a positive integer if it is greater than the specified object.
  */
public int
compareTo(Object obj)
{
  return(compareTo((CollationKey)obj));
}

/*************************************************************************/

/**
  * This method compares the specified object to this one.  An integer is 
  * returned which indicates whether the specified object is less than, 
  * greater than, or equal to this object.
  *
  * @param ck The <code>CollationKey</code> to compare against this one.
  *
  * @return A negative integer if this object is less than the specified object, 0 if it is equal or a positive integer if it is greater than the specified object.
  */
public int
compareTo(CollationKey ck)
{
  int i;
  for (i = 0; i < key.length; i++)
    {
      if (ck.key.length <= i)
        return(1);

      if (key[i] < ck.key[i])
        return(-1);

      if (key[i] > ck.key[i])
        return(1);
    }

  if (i == ck.key.length)
    return(0);
  else
    return(-1);
}

/*************************************************************************/

/**
  * This method tests the specified <code>Object</code> for equality with
  * this object.  This will be true if and only if:
  * <p>
  * <ul>
  * <li>The specified object must not be <code>null</code>
  * <li>The specified object is an instance of <code>CollationKey</code>.
  * <li>The specified object was created from the same <code>Collator</code>
  * as this object.
  * <li>The specified object has the same source string and bit key as
  * this object.
  * </ul>
  *
  * @param obj The <code>Object</code> to test for equality.
  *
  * @return <code>true</code> if the specified object is equal to this one, <code>false</code> otherwise.
  */
public boolean
equals(Object obj)
{
  if (obj == null)
    return(false);

  if (!(obj instanceof CollationKey))
    return(false);

  CollationKey ck = (CollationKey)obj;

  if (!ck.collator.equals(collator))
    return(false);

  if (!ck.getSourceString().equals(getSourceString()))
    return(false);

  if (!ck.toByteArray().equals(toByteArray()))
    return(false);

  return(true);
}

/*************************************************************************/

/**
  * This method returns a hash value for this object.  The hash value
  * returned will be the hash code of the bit key so that identical bit
  * keys will return the same value.
  *
  * @return A hash value for this object.
  */
public int
hashCode()
{
  return(key.hashCode());
}

} // class CollationKey

