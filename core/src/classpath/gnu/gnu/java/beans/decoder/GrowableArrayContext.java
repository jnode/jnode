/* gnu.java.beans.decoder.GrowableArrayContext
   Copyright (C) 2004 Free Software Foundation, Inc.

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
package gnu.java.beans.decoder;

import java.lang.reflect.Array;
import java.util.ArrayList;

/** A Context implementation for a growable array. The array
 * elements have to be set using expressions.
 *
 * @author Robert Schuster
 */
class GrowableArrayContext extends AbstractContext
{
    private Class klass;
    private ArrayList list = new ArrayList();
    private Object array;

    GrowableArrayContext(String id, Class newClass)
    {
        setId(id);
        klass = newClass;
    }

    /* (non-Javadoc)
     * @see gnu.java.beans.decoder.Context#addObject(java.lang.Object)
     */
    public void addParameterObject(Object o) throws AssemblyException
    {
        if (!klass.isInstance(o))
            throw new AssemblyException(
                new IllegalArgumentException(
                    "Cannot add object "
                        + o
                        + " to array where the elements are of class "
                        + klass));

        list.add(o);
    }

    /* (non-Javadoc)
     * @see gnu.java.beans.decoder.Context#reportStatement()
     */
    public void notifyStatement(Context outerContext) throws AssemblyException
    {
        throw new AssemblyException(
            new IllegalArgumentException("Statements inside a growable array are not allowed."));
    }

    /* (non-Javadoc)
     * @see gnu.java.beans.decoder.Context#endContext(gnu.java.beans.decoder.Context)
     */
    public Object endContext(Context outerContext) throws AssemblyException
    {
        if (array == null)
        {
            array = Array.newInstance(klass, list.size());

            for (int i = 0; i < list.size(); i++)
                Array.set(array, i, list.get(i));
        }

        return array;
    }

    /* (non-Javadoc)
     * @see gnu.java.beans.decoder.Context#subContextFailed()
     */
    public boolean subContextFailed()
    {
        // returns false to indicate that assembling the array does not fail only because
        // a subelement failed
        return false;
    }

    /* (non-Javadoc)
     * @see gnu.java.beans.decoder.Context#set(int, java.lang.Object)
     */
    public void set(int index, Object o) throws AssemblyException
    {
        if (array == null)
        {
            if (klass.isInstance(o))
                list.add(index, o);
            else
                throw new AssemblyException(
                    new IllegalArgumentException("Argument is not compatible to array component type."));
        }
        else
            Array.set(array, index, o);
    }

    /* (non-Javadoc)
     * @see gnu.java.beans.decoder.Context#get(int)
     */
    public Object get(int index) throws AssemblyException
    {
        if (array == null)
            return list.get(index);
        else
            return Array.get(array, index);
    }

    public Object getResult()
    {
        return array;
    }
}
