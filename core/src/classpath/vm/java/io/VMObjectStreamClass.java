/* VMObjectStreamClass.java -- VM helper functions for ObjectStreamClass
   Copyright (C) 2003  Free Software Foundation, Inc.

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

package java.io;

import java.lang.reflect.Field;

final class VMObjectStreamClass {
	/**
	  * Returns true if CLAZZ has a static class initializer
	  * (a.k.a. <clinit>).
	  */
	static boolean hasClassInitializer(Class clazz) {
		return false;
	}
	
	static void setBooleanNative(Field field, Object obj, boolean value) {
	    // TODO implement me
	}
	static void setByteNative(Field field, Object obj, byte value) {
	    // TODO implement me
	}
	static void setCharNative(Field field, Object obj, char value) {
	    // TODO implement me
	}
	static void setShortNative(Field field, Object obj, short value) {
	    // TODO implement me
	}
	static void setIntNative(Field field, Object obj, int value) {
	    // TODO implement me
	}
	static void setFloatNative(Field field, Object obj, float value) {
	    // TODO implement me
	}
	static void setLongNative(Field field, Object obj, long value) {
	    // TODO implement me
	}
	static void setDoubleNative(Field field, Object obj, double value) {
	    // TODO implement me
	}
	static void setObjectNative(Field field, Object obj, Object value) {
	    // TODO implement me
	}
}
