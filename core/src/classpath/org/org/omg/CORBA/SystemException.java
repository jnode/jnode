/* SystemException.java --
 Copyright (C) 2005, 2006 Free Software Foundation, Inc.

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


package org.omg.CORBA;

import java.io.Serializable;

/**
 * The root class for all CORBA standard exceptions.
 * 
 * @author Audrius Meskauskas (AudriusA@Bioinformatics.org)
 */
public abstract class SystemException
  extends RuntimeException
  implements Serializable
{
  /**
   * Use serialVersionUID for interoperability. Using the version 1.4 UID.
   */
  private static final long serialVersionUID = -8486391734674855519L;

  /**
   * The status of the operation that have thrown this exception.
   */
  public CompletionStatus completed;

  /**
   * <p>
   * Contains more details about the exception. The lower 12 bits contain a
   * code, defining the reason why exception has been thrown. The higher 20 bits
   * hold "Vendor Minor Codeset ID" (VMCID).
   * </p>
   * <p>
   * The Classpath specifice minor exception codes are documented in the headers
   * of the corresponding exceptions (for instance, {@link MARSHAL}).
   * </p>
   * 
   * The VMCID 0 and 0xfffff are reserved for experimental use. 
   * 
   * @see OMGVMCID
   */
  public int minor;

  /**
   * Constructs an instance of the CORBA standard exception.
   * 
   * @param a_reason a string, explaining the reason why the exceptions has been
   * thrown.
   * @param a_minor an additional error code (known as the "minor")
   * @param a_completed the task completion status.
   */
  protected SystemException(String a_reason, int a_minor,
                            CompletionStatus a_completed)
  {
    super(a_reason + " Minor: " + Integer.toHexString(a_minor) + " ("
      + (a_minor & 0xFFF) + "). Completed: "+a_completed);
    minor = a_minor;
    completed = a_completed;
  }
}
