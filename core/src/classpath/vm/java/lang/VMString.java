/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package java.lang;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/*
 * This class is a reference version, mainly for compiling a class library
 * jar.  It is likely that VM implementers replace this with their own
 * version that can communicate effectively with the VM.
 */

/**
 * Code relocated from java.lang.String by 
 * @author Dave Grove <groved@us.ibm.com>
 */
final class VMString
{

  /**
   * Holds the references for each intern()'d String. If all references to
   * the string disappear, and the VM properly supports weak references,
   * the String will be GC'd.
   */
  private static final WeakHashMap internTable = new WeakHashMap();

  /**
   * Fetches this String from the intern hashtable. If two Strings are
   * considered equal, by the equals() method, then intern() will return the
   * same String instance. ie. if (s1.equals(s2)) then
   * (s1.intern() == s2.intern()). All string literals and string-valued
   * constant expressions are already interned.
   *
   * @param str the String to intern
   * @return the interned String
   */
  static String intern(String str)
  {
    synchronized (internTable)
      {
        WeakReference ref = (WeakReference) internTable.get(str);
        if (ref != null)
          {
            String s = (String) ref.get();
            // If s is null, then no strong references exist to the String;
            // the weak hash map will soon delete the key.
            if (s != null)
              return s;
          }
        internTable.put(str, new WeakReference(str));
      }
    return str;
  }

} // class VMString
