/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package gnu.java.lang;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;

/**
 * @author Nicolas Geoffray (nicolas.geoffray@menlina.com)
 * @since 1.5
 * jnode-speciffic - removed natives
 * //TODO implement it
 */
final class VMInstrumentationImpl
{
 
  /**
   * Returns if the current JVM supports class redefinition
   * 
   * @return true if the current JVM supports class redefinition
   */
  static boolean isRedefineClassesSupported() {return false;}
    
  /**
   * Redefines classes given as parameters. The method has to call
   * the callTransformers from InstrumentationImpl
   *
   * @param inst an instrumentation object
   * @param definitions an array of bytecode<->class correspondance
   *
   * @throws ClassNotFoundException if a class cannot be found 
   * @throws java.lang.instrument.UnmodifiableClassException if a class cannot be modified 
   * @throws UnsupportedOperationException if the JVM does not support
   * redefinition or the redefinition made unsupported changes
   * @throws ClassFormatError if a class file is not valid
   * @throws NoClassDefFoundError if a class name is not equal to the name
   * in the class file specified
   * @throws UnsupportedClassVersionError if the class file version numbers
   * are unsupported
   * @throws ClassCircularityError if circularity occured with the new
   * classes
   * @throws LinkageError if a linkage error occurs 
   */
  static void redefineClasses(Instrumentation inst,
      ClassDefinition[] definitions){}
 
  /**
   * Get all the classes loaded by the JVM.
   * 
   * @return an array containing all the classes loaded by the JVM. The array
   * is empty if no class is loaded.
   */
  static Class[] getAllLoadedClasses() { return new Class[0];}

  /**
   * Get all the classes loaded by a given class loader
   * 
   * @param loader the loader
   * 
   * @return an array containing all the classes loaded by the given loader.
   * The array is empty if no class was loaded by the loader.
   */
  static Class[] getInitiatedClasses(ClassLoader loader){ return new Class[0];};

  /**
   * Get the size of an object. The object is not null
   * 
   * @param objectToSize the object
   * @return the size of the object
   */
  static long getObjectSize(Object objectToSize) {return 0;}

}
