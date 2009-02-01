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
