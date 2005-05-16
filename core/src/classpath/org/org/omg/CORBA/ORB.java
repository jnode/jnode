/* ORB.java --
   Copyright (C) 2005 Free Software Foundation, Inc.

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


package org.omg.CORBA;

import gnu.CORBA.Restricted_ORB;
import gnu.CORBA.fixedTypeCode;
import gnu.CORBA.generalTypeCode;
import gnu.CORBA.gnuContext;
import gnu.CORBA.recordTypeCode;
import gnu.CORBA.recursiveTypeCode;

import java.applet.Applet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

/**
 * When creating an ORB instance is being created, the class name
 * is searched in the following locations:
 * <p>
 * 1. Applet parameter or application string array, if any.<br>
 * 2. The properties parameter, if any.<br>
 * 3. The System properties.<br>
 * 4. The orb.properties file located in the user.home directory (if any).<br>
 * 5. The orb.properties file located in the java.home/lib directory (if any).
 * </p>
 *
 * The supported properties are:
 * <table border="1">
 * <tr><td> org.omg.CORBA.ORBClass</td><td>The class,
 *   implementing the functional ORB, returned by
 *   {@link #init(Applet, Properties)} or
 *   {@link #init(String[], Properties)} </td></tr>
 * <tr><td>org.omg.CORBA.ORBSingletonClass</td><td>The class,
 *   implementing the restricted ORB, returned by
 *   {@link #init()}.
 * </td></tr>
 * <tr><td>org.omg.CORBA.ORBInitRef</td><td>Specifies the
 * initial reference, accessible by name with the method
 * {@link resolve_initial_references(String)}.
 * </table>
 * The command line accepts the same properties as a keys. When specifying
 * in the command line, the prefix org.omg.CORBA can be omitted,
 * for instance<code> -ORBInitRef NameService=IOR:aabbccdd....</code>
 *
 * @author Audrius Meskauskas (AudriusA@Bioinformatics.org)
 */
public abstract class ORB
{
  /**
  * By default, {@link init(String[], Properties)} and
  * {@link init(Applet, Properties} return
  * the built-in fully functional ORB is returned. If the
  * <code>props</code> contains the property org.omg.CORBA.ORBClass,
  * the value of this property is used as a class name to instantiate
  * a user-defined ORB.
  */
  private static final String FUNCTIONAL_ORB = "org.omg.CORBA.ORBClass";

  /**
   * The name of the restricted ORB property.
   */
  private static final String RESTRICTED_ORB =
    "org.omg.CORBA.ORBSingletonClass";

  /**
   * The class, implementing the default fully functional ORB.
   */
  private static final String DEFAULT_FUNCTIONAL_ORB = "gnu.CORBA.Functional_ORB";

  /**
   * The class, implementing the default restricted ORB.
   */
  private static final String DEFAULT_RESTRICTED_ORB = "gnu.CORBA.Restricted_ORB";


  /**
   * Connect the given CORBA object to this ORB. After the object is
   * connected, it starts receiving remote invocations via this ORB.
   *
   * The OMG group recommends to use Portable Object Adapter (POA) instead
   * of calling this method.
   *
   * This method is implemented in the derived Gnu Classpah classes,
   * returned by ORB.init(..). In this abstract class, the implementation
   * just throws {@link NO_IMPLEMENT}.
   *
   * @param object the org.omg.CORBA.Object to connect.
   */
  public void connect(org.omg.CORBA.Object object)
  {
    throw new NO_IMPLEMENT();
  }

  /**
   * Disconnect the given CORBA object from this ORB. The object will be
   * no longer receiving the remote invocations. In response to the
   * remote invocation on this object, the ORB will send the
   * exception {@link OBJECT_NOT_EXIST}. The object, however, is not
   * destroyed and can receive the local invocations.
   *
   * This method is implemented in the derived Gnu Classpah classes,
   * returned by ORB.init(..). In this abstract class, the implementation
   * just throws {@link NO_IMPLEMENT}.
   *
   * @param object the object to disconnect.
   */
  public void disconnect(org.omg.CORBA.Object object)
  {
    throw new NO_IMPLEMENT();
  }


  /**
   * Create alias typecode for the given typecode.
   */
  public abstract TypeCode create_alias_tc(String id, String name,
                                           TypeCode typecode
                                          );

  /**
   * Create an instance of the CORBA {@link Any} with the type, intialised
   * to {@link TCKind#tc_null}
   */
  public abstract Any create_any();

  /**
   * Create a typecode, defining an array of the given elements.
   *
   * @param length the size of array
   * @param element_type the array component type.
   *
   * @return the corresponding typecode.
   */
  public abstract TypeCode create_array_tc(int length, TypeCode element_type);

  /**
   * Creates an empty CORBA <code>ContextList</code>.
   *
   * @return the newly created context list.
   */
  public abstract ContextList create_context_list();

  /**
   * Create a typecode, defining the given enumeration.
   *
   * @param id the id.
   * @param name the name.
   * @param members the memebers
   * @return the created enumeration.
   */
  public abstract TypeCode create_enum_tc(String id, String name,
                                          String[] members
                                         );

  /**
   * Create an environment (container for exceptions).
   *
   * @return the created container.
   */
  public abstract Environment create_environment();

  /**
   * Creates an empty exception list.
   *
   * @return the newly created list.
   */
  public abstract ExceptionList create_exception_list();

  /**
   * Create the exception typecode.
   *
   * @param id the id of exception.
   * @param name the name of exception.
   * @param members the members of exception.
   */
  public abstract TypeCode create_exception_tc(String id, String name,
                                               StructMember[] members
                                              );

  /**
   * Creates a TypeCode object for CORBA <code>fixed</code> that is
   * mapped to java {@link java.math.BigDecimal}.
   *
   * @param digits the number of digits in that <code>fixed</code>.
   * @param scale the number of digits after the decimal point.
   *
   * @return the corresponding TypeCode.
   */
  public TypeCode create_fixed_tc(short digits, short scale)
  {
    fixedTypeCode r = new fixedTypeCode();
    r.setDigits(digits);
    r.setScale(scale);
    return r;
  }

  /**
   * Creates a typecode, representing the IDL interface.
   *
   * @param id the interface repository id.
   * @param name the interface name.
   *
   * @return the created typecode.
   */
  public abstract TypeCode create_interface_tc(String id, String name);

  /**
   * Create an instance of a new {@link NVList}.
   *
   * @param count the initial size of the list. If more elements are added,
   * the list automatically expands.
   *
   * @return the created list.
   */
  public abstract NVList create_list(int count);

  /**
   * Create a new named value.
   *
   * @param name the name of the named value
   * @param any the content of the named value.
   * @param flags the flags of the named value
   *
   * @return the named value.
   */
  public abstract NamedValue create_named_value(String s, Any any, int flags);

  /**
   * Create a new CDR output stream, where the parameter values can be written
   * during the method invocation.
   *
   * @return a stream to write values into.
   */
  public abstract org.omg.CORBA.portable.OutputStream create_output_stream();

  /**
   * Create typecode, defining the sequence of the elements, having
   * the given type.
   *
   * @param bound the maximal length of the sequence, 0 if not restricted.
   *
   * @param element_type the sequence element type.
   *
   * @return the typecode.
   */
  public abstract TypeCode create_sequence_tc(int bound, TypeCode element_type);

  /**
   * Create a TypeCode, representing the CORBA <code>string</code>.
   *
   * @param bound the maximal length of the string, 0 is unlimited.
   *
   * @return the corresponding string typecode.
   */
  public abstract TypeCode create_string_tc(int bound);

  /**
   * Create the typecode, defining the given IDL structure.
   *
   * The TypeCode object is initialized with the given id, name, and members.
   * @param id the Id of this type.
   * @param the name of this type.
   * @param members the member list.
   *
   * @return the typecode.
   */
  public abstract TypeCode create_struct_tc(String id, String name,
                                            StructMember[] members
                                           );

  /**
   * Create the typecode, defining the given IDL union.
   *
   * The TypeCode object is initialized with the given id, name, discriminator
   * and members.
   *
   * @param id the Id of this type.
   * @param the name of this type.
   * @param discriminator the union discriminator.
   * @param members the member list.
   *
   * @return the typecode.
   */
  public abstract TypeCode create_union_tc(String id, String name,
                                           TypeCode discriminator,
                                           UnionMember[] members
                                          );

  /**
   * Create a TypeCode, representing the CORBA <code>wstring</code>.
   *
   * @param bound the maximal length of the string, 0 is unlimited.
   *
   * @return the corresponding string typecode.
   */
  public abstract TypeCode create_wstring_tc(int bound);

  /**
   * Create a typecode which serves as a placeholder for typcode, containing
   * recursion.
   *
   * @param id the id of the recursive typecode, for that this typecode
   * serves as a placeholder.
   */
  public TypeCode create_recursive_tc(String id)
  {
    return new recursiveTypeCode(id);
  }

  /**
   * Create value box typecode.
   */
  public TypeCode create_value_box_tc(String id, String name,
                                      TypeCode boxed_type
                                     )
  {
    generalTypeCode t = new generalTypeCode(TCKind.tk_value_box);
    t.setName(name);
    t.setId(id);
    t.setContentType(boxed_type);
    return t;
  }

  /**
   * Create IDL value type code.
   */
  public TypeCode create_value_tc(String id, String name, short type_modifier,
                                  TypeCode concrete_base, ValueMember[] members
                                 )
  {
    recordTypeCode r = new recordTypeCode(TCKind.tk_value);
    r.setId(id);
    r.setName(name);
    r.setTypeModifier(type_modifier);
    r.setConcreteBase_type(concrete_base);

    for (int i = 0; i < members.length; i++)
      {
        r.add(members [ i ]);
      }

    return r;
  }

  /**
   * Get the default context of this ORB. This is an initial root of all
   * contexts.
   *
   * The default method returns a new context with the empty name and
   * no parent context.
   *
   * @return the default context of this ORB.
   *
   * @throws NO_IMPLEMENT for the Singleton ORB, returned by
   * the parameterless {@link init()}.
   */
  public Context get_default_context()
  {
    return new gnuContext("", null);
  }

  /**
   * Return thg typecode, representing the given primitive object type.
   *
   * @param the kind of the primitive typecode.
   *
   * @return the typecode of the primitve typecode.
   */
  public abstract TypeCode get_primitive_tc(TCKind tcKind);

  /**
   * Returns so-called Singleton ORB, a highly restricted version
   * that cannot communicate over network. This ORB is provided
   * for the potentially malicious applets with heavy security restrictions.
   *
   * The returned Singleton ORB can only create typecodes,
   * {@link Any}, {@link ContextList}, {@link NVList} and
   * {@link org.omg.CORBA.portable.OutputStream} that writes to an
   * internal buffer.
   *
   * All other methods throw the {@link NO_IMPLEMENT} exception, additionally
   * printing the error message about the potential attempt to violate
   * the security rules.
   *
   * The implementing ORB class, used in this method, is found as described
   * in the header.
   *
   * @return the working derivative of ORB, implementing the methods
   * of this abstract class.
   */
  public static ORB init()
  {
    String orb_cn = getORBName(null, RESTRICTED_ORB);
    if (orb_cn == null)
      return Restricted_ORB.Singleton;
    else
      return createORB(null, orb_cn);
  }

  /**
   * Creates the working instance of ORB for an applet.
   *
   * By default the built-in fully functional ORB is returned. The ORB class
   * is found as described in the header of this class.
   *
   * @param applet the applet. The property org.omg.CORBA.ORBClass,
   * if present, defines the used ORB implementation class. If this
   * property is not present, the ORB class is found as described in the
   * class header.
   *
   * @param props the properties, may be <code>null</code>.
   *
   * @return a newly created functional derivative of this abstract class.
   */
  public static ORB init(Applet applet, Properties props)
  {
    String ocn = applet.getParameter(FUNCTIONAL_ORB);
    ORB orb = createORB(props, ocn);
    orb.set_parameters(applet, props);

    return orb;
  }

  /**
   * Creates the working instance of ORB for a
   * standalone application.
   *
   * By default the built-in fully functional ORB is returned. The ORB class
   * is found as described in the header of this class.
   *
   * @param the parameters, passed to the applications
   * <code>main(String[] args)</code> method, may be <code>null</code>.
   * The parameter -org.omg.CORBA.ORBClass <class name>
   * if present, defines the used ORB implementation class. If this
   * property is not present, the ORB class is found as described in the
   * class header.

   *
   * @param props application specific properties, may be <code>null</code>.
   *
   * @return a newly created functional derivative of this abstract class.
   */
  public static ORB init(String[] args, Properties props)
  {
    String ocn = null;

    String orbKey = "-" + FUNCTIONAL_ORB;

    if (args != null)
      if (args.length >= 2)
        {
          for (int i = 0; i < args.length - 1; i++)
            {
              if (args [ i ].equals(orbKey))
                ocn = args [ i + 1 ];
            }
        }

    ORB orb = createORB(props, ocn);

    orb.set_parameters(args, props);
    return orb;
  }

  /**
   * List the initially available CORBA objects (services).
   *
   * @return a list of services.
   *
   * @see resolve_initial_references(String)
   */
  public abstract String[] list_initial_services();

  /**
   * Find and return the easily accessible CORBA object, addressed
   * by name.  The returned object is typically casted to the more
   * specific reference using the <code>narrow(Object)</code> method
   * of its helper.
   *
   * @param name the object name.
   * @return the object
   * @throws org.omg.CORBA.ORBPackage.InvalidName if the given name
   * is not associated with the known object.
   */
  public abstract Object resolve_initial_references(String name)
    throws org.omg.CORBA.ORBPackage.InvalidName;

  /**
   * Get the IOR reference string for the given object.
   * IOR can be compared with the Internet address for a web page,
   * it provides means to locate the CORBA service on the web.
   * IOR contains the host address, port number, the object identifier
   * (key) inside the server, the communication protocol version,
   * supported charsets and so on.
   *
   * @param the CORBA object
   * @return the object IOR representation.
   * @see string_to_object(String)
   */
  public abstract String object_to_string(Object forObject);

  /**
   * Find and return the CORBA object, addressed by the given
   * IOR string representation. The object can (an usually is)
   * located on a remote computer, possibly running a different
   * (not necessary java) CORBA implementation. The returned
   * object is typically casted to the more specific reference
   * using the <code>narrow(Object)</code> method of its helper.
   *
   * @param IOR the object IOR representation string.
   *
   * @return the found CORBA object.
   * @see object_to_string(org.omg.CORBA.Object)
   */
  public abstract Object string_to_object(String IOR);

  /**
   * Start listening on the input socket. This method
   * blocks the current thread until {@link #shutdown(boolean)}
   * is called and shutdown process is completed.
   */
  public void run()
  {
  }

  /**
   * Shutdown the ORB server.
   *
   * @param wait_for_completion if true, the current thread is
   * suspended untile the shutdown process is complete.
   */
  public void shutdown(boolean wait_for_completion)
  {
  }

  /**
   * Destroy this server, releasing the occupied resources.
   * The default method returns without action.
   */
  public void destroy()
  {
  }

  /**
   * Set the ORB parameters. This method is normally called from
   * {@link #init(String[], Properties)}.
   *
   * @param para the parameters, that were passed as the parameters
   * to the  <code>main(String[] args)</code> method of the current standalone
   * application.
   *
   * @param props application specific properties that were passed
   * as a second parameter in {@link init(String[], Properties)}).
   * Can be <code>null</code>.
   */
  protected abstract void set_parameters(String[] para, Properties props);

  /**
   * Set the ORB parameters. This method is normally called from
   * {@link #init(Applet, Properties)}.
   *
   * @param app the current applet.
   *
   * @param props application specific properties, passed as the second
   * parameter in {@link #init(Applet, Properties)}.
   * Can be <code>null</code>.
   */
  protected abstract void set_parameters(Applet app, Properties props);

  /**
   * Checks if the communication over network is allowed.
   * @throws java.lang.SecurityException
   */
  private static final void checkNetworkingPermission(String host, int port)
                                               throws SecurityException
  {
    SecurityManager security = System.getSecurityManager();
    if (security != null)
      {
        security.checkConnect(host, port);
      }
  }

  /**
   * Get the ORB class name.
   */
  private static String getORBName(Properties props, String property)
  {
    String orb_cn = null;

    if (props != null)
      orb_cn = props.getProperty(property, null);

    if (orb_cn == null)
      orb_cn = System.getProperty(property, null);

    if (orb_cn == null)
      orb_cn = checkFile(property, "user.home", null);

    if (orb_cn == null)
      orb_cn = checkFile(property, "java.home", "lib");

    return orb_cn;
  }

  /**
   * Check if the property is defined in the existsting file orb.properties.
   *
   * @param property the property
   * @param dir the system property, defining the folder where the
   * file could be expected.
   * @param subdir subfolder where to look for the file.
   *
   * @return the property value, null if not found or file does not exist.
   */
  private static String checkFile(String property, String dir, String subdir)
  {
    try
      {
        File f = new File(dir);
        if (!f.exists())
          return null;

        if (subdir != null)
          f = new File(f, subdir);

        f = new File(f, "orb.properties");

        if (!f.exists())
          return null;

        Properties p = new Properties();
        p.load(new BufferedInputStream(new FileInputStream(f)));

        return p.getProperty(property, null);
      }
    catch (IOException ex)
      {
        return null;
      }
  }

  /**
   * Create ORB when its name is possibly known.
   *
   * @param props properties, possibly containing the ORB name.
   * @param orbClassName the direct ORB class name, overriding
   * other possible locations, or null if not specified.
   */
  private static ORB createORB(Properties props, String orbClassName)
  {
    ORB orb = null;

    if (orbClassName == null)
      {
        orbClassName = getORBName(props, FUNCTIONAL_ORB);

        if (orbClassName == null)
          orbClassName = DEFAULT_FUNCTIONAL_ORB;
      }

    try
      {
        orb = (ORB) Class.forName(orbClassName).newInstance();
      }
    catch (ClassNotFoundException ex)
      {
        noORB(orbClassName, ex);
      }
    catch (IllegalAccessException ex)
      {
        noORB(orbClassName, ex);
      }
    catch (InstantiationException ex)
      {
        noORB(orbClassName, ex);
      }

    return orb;
  }

  /**
   * Throw the runtime exception.
   *
   * @param orb_c the ORB class name.
   * @param why the explaining chained exception.
   */
  private static void noORB(String orb_c, Throwable why)
  {
    throw new RuntimeException("The ORB " + orb_c + " cannot be instantiated.",
                               why
                              );
  }
}
