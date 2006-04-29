/* ClassDefinition.java -- Class that binds a class with a new class file
   Copyright (C) 2005  Free Software Foundation, Inc.

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


package java.lang.instrument;

/**
 * This class binds a class that will be redefined with a new
 * class file.
 *
 * @author Nicolas Geoffray (nicolas.geoffray@menlina.com)
 * @see Instrumentation#redefineClasses(java.lang.instrument.ClassDefinition[])
 * @since 1.5
 */
public final class ClassDefinition
{

  /* The class it's related */
  private Class theClass;

  /* The new bytecode of theClass */
  private byte[] theClassFile;

  /**
   * @param theClass the Class that will be redefined
   * @param theClassFile the new class file
   * @throws NullPointerException if one of the argument is null
   */
  /* FIXME[GENERICS]: Signature should be (Class<?>, byte[]) */
  public ClassDefinition(Class theClass, byte[] theClassFile)
  {
    if (theClass == null || theClassFile == null)
      throw new NullPointerException();
    this.theClass = theClass;
    this.theClassFile = theClassFile;
  }

  /**
   * @return the Class
   */
  /* FIXME[GENERICS]: Should return Class<?> */
  public Class getDefinitionClass()
  {
    return theClass;
  }

  /**
   * @return the bytes
   */
  public byte[] getDefinitionClassFile()
  {
    return theClassFile;
  }
}
