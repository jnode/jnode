/*
* @(#)DebugHelper.java.m4	1.11 07/05/05
*
* Copyright 1999-2001 Sun Microsystems, Inc.  All Rights Reserved.
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
 * This class is produced by using the m4 preprocessor to produce
 * a .java file containing debug or release versions of the
 * DebugHelper class.
 */
 
package sun.awt;

import java.lang.reflect.*;
import java.util.*;

public abstract class DebugHelper {
    static {
        NativeLibLoader.loadLibraries();
    }

    /* name the DebugHelper member var must be declared as */
    protected static final String	DBG_FIELD_NAME = "dbg";
    protected static final String	DBG_ON_FIELD_NAME = "on";

 
/* RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE */
    public static final boolean		on = false;
    private static final DebugHelper	dbgStub = new DebugHelperStub();

    static void init() {
	// nothing to do in release mode
    }

    public static final DebugHelper create(Class classToDebug) {
	return dbgStub;
    }
/* RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE */


    public abstract void setAssertOn(boolean enabled);
    public abstract void setTraceOn(boolean enabled);
    public abstract void setDebugOn(boolean enabled);
    public abstract void println(Object object);
    public abstract void print(Object object);
    public abstract void printStackTrace();
    public abstract void assertion(boolean expr);
    public abstract void assertion(boolean expr, String msg);
}

final class DebugHelperStub extends DebugHelper
{
    /* stub methods for production builds */
    public void setAssertOn(boolean enabled) {}
    public void setTraceOn(boolean enabled) {}
    public void setDebugOn(boolean enabled) {}
    public void println(Object object) {}
    public void print(Object object) {}
    public void printStackTrace() {}
    public void assertion(boolean expr) {}
    public void assertion(boolean expr, String msg) {}
}
