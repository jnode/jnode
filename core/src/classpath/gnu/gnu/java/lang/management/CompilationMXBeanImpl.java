/* CompilationMXBeanImpl.java - Implementation of a compilation bean
   Copyright (C) 2006 Free Software Foundation

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

package gnu.java.lang.management;

import gnu.classpath.SystemProperties;

import java.lang.management.CompilationMXBean;

import javax.management.NotCompliantMBeanException;

/**
 * Provides access to information about the JIT 
 * compiler of the virtual machine, if one exists.
 * Instances of this bean are obtained by calling
 * {@link ManagementFactory#getCompilationMXBean()},
 * if this is the case.
 *
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @since 1.5
 */
public final class CompilationMXBeanImpl
  extends BeanImpl
  implements CompilationMXBean
{

  /**
   * Constant for compiler name.
   */
  private static final String COMPILER_NAME = "gnu.java.compiler.name";

  /**
   * Constant for compilation time support.
   */
  private static final String COMPILATION_TIME_SUPPORT = 
    "gnu.java.lang.management.CompilationTimeSupport";

  /**
   * Constructs a new <code>CompilationMXBeanImpl</code>.
   *
   * @throws NotCompliantMBeanException if this class doesn't implement
   *                                    the interface or a method appears
   *                                    in the interface that doesn't comply
   *                                    with the naming conventions.
   */
  public CompilationMXBeanImpl()
    throws NotCompliantMBeanException
  {
    super(CompilationMXBean.class);
  }

  public String getName()
  {
    return SystemProperties.getProperty(COMPILER_NAME);
  }
  
  public boolean isCompilationTimeMonitoringSupported()
  {
    return SystemProperties.getProperty(COMPILATION_TIME_SUPPORT) != null;
  }

  public long getTotalCompilationTime()
  {
    if (isCompilationTimeMonitoringSupported())
      return VMCompilationMXBeanImpl.getTotalCompilationTime();
    else
      throw new UnsupportedOperationException("Compilation time monitoring "
					      + "is not supported");
  }

}
