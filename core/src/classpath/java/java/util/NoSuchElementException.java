/* NoSuchElementException.java -- Attempt to access element that does not exist
   Copyright (C) 1998, 1999, 2001, 2005  Free Software Foundation, Inc.

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


package java.util;

/* Written using "Java Class Libraries", 2nd edition, ISBN 0-201-31002-3
 * "The Java Language Specification", ISBN 0-201-63451-1
 * plus online API docs for JDK 1.2 beta from http://www.javasoft.com.
 */

/**
 * Exception thrown when an attempt is made to access an element that does not
 * exist. This exception is thrown by the Enumeration, Iterator and
 * ListIterator classes if the nextElement, next or previous method goes
 * beyond the end of the list of elements that are being accessed. It is also
 * thrown by Vector and Stack when attempting to access the first or last
 * element of an empty collection.
 *
 * @author Warren Levy (warrenl@cygnus.com)
 * @author Eric Blake (ebb9@email.byu.edu)
 * @see Enumeration
 * @see Iterator
 * @see ListIterator
 * @see Enumeration#nextElement()
 * @see Iterator#next()
 * @see ListIterator#previous()
 * @since 1.0
 * @status updated to 1.4
 */
public class NoSuchElementException extends RuntimeException
{
	/**
	 * Compatible with JDK 1.0.
	 */
	private static final long serialVersionUID = 6769829250639411880L;

	/**
	 * Constructs a NoSuchElementException with no detail message.
	 */
  public NoSuchElementException()
  {
	}

	/**
	 * Constructs a NoSuchElementException with a detail message.
	 *
	 * @param detail the detail message for the exception
	 */
  public NoSuchElementException(String detail)
  {
		super(detail);
	}
}
