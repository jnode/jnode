/*
  Copyright (c) 1996, 1997, 1998, 1999, 2002 Free Software Foundation, Inc.

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

package gnu.java.rmi.server;

import java.io.ObjectStreamClass;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.lang.ClassNotFoundException;
import java.lang.reflect.Proxy;

public class RMIObjectInputStream
	extends ObjectInputStream {

public RMIObjectInputStream(InputStream strm) throws IOException {
	super(strm);
	enableResolveObject(true);
}

protected Class resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
	String annotation = (String)getAnnotation();
	
	try {
		if(annotation == null)
		    return (RMIClassLoader.loadClass(desc.getName()));
		else
		    return (RMIClassLoader.loadClass(annotation, desc.getName()));
	}
	catch (MalformedURLException _) {
		throw new ClassNotFoundException(desc.getName());
	}
}

//Separate it for override by MarshalledObject
protected Object getAnnotation()
	    throws IOException, ClassNotFoundException
{
    return readObject();
}
	
protected Class resolveProxyClass(String intfs[])
        throws IOException, ClassNotFoundException
{
    String annotation = (String)getAnnotation();
	
    Class clss[] = new Class[intfs.length];
    if(annotation == null)
        clss[0] = RMIClassLoader.loadClass(intfs[0]);
    else
        clss[0] = RMIClassLoader.loadClass(annotation, intfs[0]);
    
    //assume all interfaces can be loaded by the same classloader
    ClassLoader loader = clss[0].getClassLoader();
    for (int i = 0; i < intfs.length; i++)
        clss[i] = Class.forName(intfs[i], false, loader);
        
    try {
    return Proxy.getProxyClass(loader, clss);
	} catch (IllegalArgumentException e) {
	    throw new ClassNotFoundException(null, e);
	}  
}

protected Object readValue(Class valueClass) throws IOException, ClassNotFoundException {
    if(valueClass.isPrimitive()){
        if(valueClass == Boolean.TYPE)
            return new Boolean(readBoolean());
        if(valueClass == Byte.TYPE)
            return new Byte(readByte());
        if(valueClass == Character.TYPE)
            return new Character(readChar());
        if(valueClass == Short.TYPE)
            return new Short(readShort());
        if(valueClass == Integer.TYPE)
            return new Integer(readInt());
        if(valueClass == Long.TYPE)
            return new Long(readLong());
        if(valueClass == Float.TYPE)
            return new Float(readFloat());
        if(valueClass == Double.TYPE)
            return new Double(readDouble());
        else
            throw new Error("Unsupported primitive class: " + valueClass);
    } else
        return readObject();
}

}
