/* TransformerException.java -- 
   Copyright (C) 2003, 2006 Free Software Foundation, Inc.

This file is a part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or (at
your option) any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
USA

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
exception statement from your version.  */


package gnu.javax.crypto.assembly;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 */
public class TransformerException extends Exception
{

  // Constants and variables
  // -------------------------------------------------------------------------

  private Throwable _exception = null;

  // Constructor(s)
  // -------------------------------------------------------------------------

  public TransformerException()
  {
    super();
  }

  public TransformerException(String details)
  {
    super(details);
  }

  public TransformerException(Throwable cause)
  {
    super();

    this._exception = cause;
  }

  public TransformerException(String details, Throwable cause)
  {
    super(details);

    this._exception = cause;
  }

  // Class methods
  // -------------------------------------------------------------------------

  // Instant methods
  // -------------------------------------------------------------------------

  public Throwable getCause()
  {
    return _exception;
  }

  /**
   * Prints this exception's stack trace to <code>System.err</code>. If this
   * exception has a root exception; the stack trace of the root exception is
   * also printed to <code>System.err</code>.
   */
  public void printStackTrace()
  {
    super.printStackTrace();
    if (_exception != null)
      {
        _exception.printStackTrace();
      }
  }

  /**
   * Prints this exception's stack trace to a print stream. If this exception
   * has a root exception; the stack trace of the root exception is also
   * printed to the print stream.
   *
   * @param ps the non-null print stream to which to print.
   */
  public void printStackTrace(PrintStream ps)
  {
    super.printStackTrace(ps);
    if (_exception != null)
      {
        _exception.printStackTrace(ps);
      }
  }

  /**
   * Prints this exception's stack trace to a print writer. If this exception
   * has a root exception; the stack trace of the root exception is also
   * printed to the print writer.
   *
   * @param pw the non-null print writer to use for output.
   */
  public void printStackTrace(PrintWriter pw)
  {
    super.printStackTrace(pw);
    if (_exception != null)
      {
        _exception.printStackTrace(pw);
      }
  }

  /**
   * Returns the string representation of this exception. The string
   * representation contains this exception's class name, its detailed
   * messsage, and if it has a root exception, the string representation of the
   * root exception. This string representation is meant for debugging and not
   * meant to be interpreted programmatically.
   *
   * @return the non-null string representation of this exception.
   * @see Throwable#getMessage()
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer(this.getClass().getName()).append(": ").append(
                                                                                      super.toString());
    if (_exception != null)
      {
        sb.append("; caused by: ").append(_exception.toString());
      }
    return sb.toString();
  }
}
