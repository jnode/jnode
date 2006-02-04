/* gnu.java.beans.DefaultExceptionListener
   Copyright (C) 2004, 2006 Free Software Foundation, Inc.

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

package gnu.java.beans;

import java.beans.ExceptionListener;

/** The DefaultExceptionListener is the default implementation of the
 * {@link ExceptionListener} interface. An instance of
 * this class is used whenever the user provided no
 * <code>ExceptionListener</code> instance on its own.
 *
 * <p>The implementation just writes the exception's message
 * to <code>System.err</code> and is used by the {@link java.beans.Encoder}
 * and the {@link java.beans.XMLDecoder}.
 * </p>
 *
 * @author Robert Schuster (robertschuster@fsfe.org)
 */
public class DefaultExceptionListener implements ExceptionListener
{
  public final static DefaultExceptionListener INSTANCE
    = new DefaultExceptionListener();
  
  public void exceptionThrown(Exception e)
  {
    System.err.println("exception thrown: "
                       + e + " - message: "
                       + e.getMessage());
  }
  
}
