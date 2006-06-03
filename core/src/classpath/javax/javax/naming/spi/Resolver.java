/* Resolver.java --
   Copyright (C) 2001, 2005, 2006  Free Software Foundation, Inc.

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


package javax.naming.spi;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NotContextException;
 
/**
 * <p>Represents the object, capable for the at least partial name resolution.
 * The object is not necessay capable for the complete name resolution and
 * need not implement the {@link Context}.</p>
 * <p>
 * Both passed parameters and returned results are owned by the caller.</p>
 * 
 * @author Warren Levy (warrenl@redhat.com)
 */
public interface Resolver
{
  /**
   * Partially resolve the name, stopping at the first instance of the context
   * that is an instance of the contextType
   * 
   * @param name the name to resolve
   * @param contextType the class of the context, on that the resolution should
   *          be terminated
   * @return the complete or partial name resolution
   * @throws NotContextException if the context of the contextType is not found
   * @throws NamingException on other failure
   */
  ResolveResult resolveToClass(Name name, Class contextType)
    throws NamingException;

  /**
   * Partially resolve the name, stopping at the first instance of the context
   * that is an instance of the contextType
   * 
   * @param name the name to resolve
   * @param contextType the class of the context, on that the resolution should
   *          be terminated
   * @return the complete or partial name resolution
   * @throws NotContextException if the context of the contextType is not found
   * @throws NamingException on other failure
   */
  ResolveResult resolveToClass(String name, Class contextType)
    throws NamingException;
}
