/*
 * Copyright 2001-2002 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 */
package com.sun.corba.se.impl.orbutil;

import java.io.*;
import java.util.Hashtable;

/**
 * Implements legacy behavior from Ladybird to maintain
 * backwards compatibility.
 */
public class IIOPInputStream_1_3_1 extends com.sun.corba.se.impl.io.IIOPInputStream
{
    public IIOPInputStream_1_3_1()
    	throws java.io.IOException {
        super();
    }

    /**
     * Before JDK 1.3.1_01, the PutField/GetField implementation
     * actually sent a Hashtable.
     */
    public ObjectInputStream.GetField readFields()
    	throws IOException, ClassNotFoundException, NotActiveException {

	Hashtable fields = (Hashtable)readObject();
	return new LegacyHookGetFields(fields);
    }
}
