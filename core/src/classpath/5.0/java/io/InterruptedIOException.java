/* InterruptedIOException.java -- an I/O operation was interrupted
   Copyright (C) 1998, 1999, 2001, 2002, 2005  Free Software Foundation, Inc.

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


package java.io;

/**
  * This exception is thrown when a in process I/O operation is interrupted
  * for some reason.  The field bytesTransferred will contain the number of
  * bytes that were read/written prior to the interruption.
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  * @author Tom Tromey (tromey@cygnus.com)
  * @see Thread#interrupt()
  * @status updated to 1.4
  */
public class InterruptedIOException extends IOException
{
  /**
   * Compatible with JDK 1.0+.
   */
  private static final long serialVersionUID = 4020568460727500567L;

  /**
   * The number of bytes read/written prior to the interruption.
   *
   * @serial count of bytes successfully transferred
   */
  public int bytesTransferred;

  /**
   * Create an extends without a descriptive error message.
   */
  public InterruptedIOException()
  {
  }

  /**
   * Create an exception with a descriptive error message.
   *
   * @param message the descriptive error message
   */
  public InterruptedIOException(String message)
  {
    super(message);
  }

  /**
   * Create an exception with a descriptive error message and count of
   * bytes transferred.
   *
   * @param message the descriptive error message
   * @param bytesTransferred number of bytes tranferred before interruption
   */
  InterruptedIOException(String message, int bytesTransferred)
  {
    super(message);
    this.bytesTransferred = bytesTransferred;
  }
} // class InterruptedIOException
