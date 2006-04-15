/* MethodGenerator.java -- Generates methods for GIOP rmic compiler.
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
*/

package gnu.classpath.tools.giop.grmic;

import gnu.classpath.tools.AbstractMethodGenerator;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Keeps information about the single method and generates the code fragments,
 * related to that method.
 * 
 * @author Audrius Meskauskas, Lithuania (audriusa@Bioinformatics.org)
 */
public class MethodGenerator implements AbstractMethodGenerator
{
  /**
   * The method being defined.
   */
  Method method;

  /**
   * The parent code generator.
   */
  GiopRmicCompiler rmic;
  
  /**
   * The previous method in the list, null for the first element. 
   * Used to avoid repretetive inclusion of the same hash code label.
   */
  MethodGenerator previous = null;
  
  /**
   * The hash character position.
   */
  int hashCharPosition;

  /**
   * Create the new method generator for the given method.
   * 
   * @param aMethod
   *          the related method.
   * @param aRmic
   *          the Rmic generator instance, where more class - related
   *          information is defined.
   */
  public MethodGenerator(Method aMethod, GiopRmicCompiler aRmic)
  {
    method = aMethod;
    rmic = aRmic;
  }
  
  /**
   * Get the method name.
   * 
   * @return the name of the method.
   */
  public String getGiopMethodName()
  {
    String m = method.getName();
    if (m.startsWith("get"))
      return "_get_J" + m.substring("get".length());
    else if (m.startsWith("set"))
      return "_set_J" + m.substring("set".length());
    else
      return m;
  }

  /**
   * Get the method parameter declaration.
   * 
   * @return the string - method parameter declaration.
   */
  public String getArgumentList()
  {
    StringBuffer b = new StringBuffer();

    Class[] args = method.getParameterTypes();

    for (int i = 0; i < args.length; i++)
      {
        b.append(rmic.name(args[i]));
        b.append(" p" + i);
        if (i < args.length - 1)
          b.append(", ");
      }
    return b.toString();
  }

  /**
   * Get the method parameter list only (no type declarations). This is used to
   * generate the method invocations statement.
   * 
   * @return the string - method parameter list.
   */
  public String getArgumentNames()
  {
    StringBuffer b = new StringBuffer();

    Class[] args = method.getParameterTypes();

    for (int i = 0; i < args.length; i++)
      {
        b.append(" p" + i);
        if (i < args.length - 1)
          b.append(", ");
      }
    return b.toString();
  }

  /**
   * Get the list of exceptions, thrown by this method.
   * 
   * @return the list of exceptions.
   */
  public String getThrows()
  {
    StringBuffer b = new StringBuffer();

    Class[] args = method.getExceptionTypes();

    for (int i = 0; i < args.length; i++)
      {
        b.append(rmic.name(args[i]));
        if (i < args.length - 1)
          b.append(", ");
      }
    return b.toString();
  }

  /**
   * Generate this method for the Stub class.
   * 
   * @return the method body for the stub class.
   */
  public String generateStubMethod()
  {
    String templateName;

    Properties vars = new Properties(rmic.vars);
    vars.put("#return_type", rmic.name(method.getReturnType()));
    vars.put("#method_name", method.getName());
    vars.put("#giop_method_name", getGiopMethodName());    
    vars.put("#argument_list", getArgumentList());
    vars.put("#argument_names", getArgumentNames());

    vars.put("#argument_write", getStubParaWriteStatement());

    if (method.getReturnType().equals(void.class))
      vars.put("#read_return", "return;");
    else
      vars.put("#read_return",
               "return "
                   + GiopIo.getReadStatement(method.getReturnType(), rmic));
    String thr = getThrows();
    if (thr.length() > 0)
      vars.put("#throws", "\n    throws " + thr);
    else
      vars.put("#throws", "");

    if (method.getReturnType().equals(void.class))
      templateName = "StubMethodVoid.jav";
    else
      {
        vars.put("#write_result",
                 GiopIo.getWriteStatement(method.getReturnType(), "result",
                                          rmic));
        templateName = "StubMethod.jav";
      }
    
    String template = rmic.getResource(templateName);        
    String generated = rmic.replaceAll(template, vars);
    return generated;
  }
  
  /**
   * Generate this method handling fragment for the Tie class.
   * 
   * @return the fragment to handle this method for the Tie class.
   */
  public String generateTieMethod()
  {
    String templateName;

    Properties vars = new Properties(rmic.vars);
    vars.put("#return_type", rmic.name(method.getReturnType()));
    vars.put("#method_name", method.getName());
    vars.put("#giop_method_name", getGiopMethodName());    
    vars.put("#argument_list", getArgumentList());
    vars.put("#argument_names", getArgumentNames());

    vars.put("#argument_write", getStubParaWriteStatement());
    
    if (previous == null || previous.getHashChar()!=getHashChar())
      vars.put("#hashCodeLabel","    case '"+getHashChar()+"':");
    else
      vars.put("#hashCodeLabel","    // also '"+getHashChar()+"':");

    if (method.getReturnType().equals(void.class))
      templateName = "TieMethodVoid.jav";
    else
      {
        vars.put("#write_result",
                 GiopIo.getWriteStatement(method.getReturnType(), "result",
                                          rmic));
        templateName = "TieMethod.jav";
      }
    vars.put("#read_and_define_args", getRda());

    String template = rmic.getResource(templateName);
    String generated = rmic.replaceAll(template, vars);
    return generated;
  }  
  
  /**
   * Generate sentences for Reading and Defining Arguments.
   * 
   * @return the sequence of sentences for reading and defining arguments.
   */
  public String getRda()
  {
    StringBuffer b = new StringBuffer();
    Class[] args = method.getParameterTypes();

    for (int i = 0; i < args.length; i++)
      {
        b.append("                ");
        b.append(rmic.name(args[i]));
        b.append(" ");
        b.append("p"+i);
        b.append(" = ");
        b.append(GiopIo.getReadStatement(args[i], rmic));
        if (i<args.length-1)
          b.append("\n");
      }
    return b.toString();
  }

  /**
   * Get the write statement for writing parameters inside the stub.
   * 
   * @return the write statement.
   */
  public String getStubParaWriteStatement()
  {
    StringBuffer b = new StringBuffer();
    Class[] args = method.getParameterTypes();

    for (int i = 0; i < args.length; i++)
      {
        b.append("             ");
        b.append(GiopIo.getWriteStatement(args[i], "p" + i, rmic));
        b.append("\n");
      }
    return b.toString();
  }
  
  /**
   * Get the hash char.
   */
  public char getHashChar()
  {
    return getGiopMethodName().charAt(hashCharPosition);
  }
}
