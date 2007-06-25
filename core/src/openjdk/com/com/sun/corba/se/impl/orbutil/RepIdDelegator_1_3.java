/*
 * Copyright 2000-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.corba.se.impl.orbutil;

import org.omg.CORBA.ORB;
import java.io.Serializable;
import java.util.Hashtable;
import java.net.MalformedURLException;
import com.sun.corba.se.impl.io.TypeMismatchException;
import com.sun.corba.se.impl.util.RepositoryId;

/**
 * Delegates to the RepositoryId_1_3 implementation in
 * com.sun.corba.se.impl.orbutil.  This is necessary to
 * overcome the fact that many of RepositoryId's methods
 * are static.
 */
public final class RepIdDelegator_1_3 
    implements RepositoryIdStrings, 
               RepositoryIdUtility,
               RepositoryIdInterface
{
    // RepositoryIdFactory methods

    public String createForAnyType(Class type) {
        return RepositoryId_1_3.createForAnyType(type);
    }

    public String createForJavaType(Serializable ser)
        throws TypeMismatchException
    {
        return RepositoryId_1_3.createForJavaType(ser);
    }
               
    public String createForJavaType(Class clz)
        throws TypeMismatchException
    {
        return RepositoryId_1_3.createForJavaType(clz);
    }

    public String createSequenceRepID(java.lang.Object ser) {
        return RepositoryId_1_3.createSequenceRepID(ser);
    }

    public String createSequenceRepID(Class clazz) {
        return RepositoryId_1_3.createSequenceRepID(clazz);
    }

    public RepositoryIdInterface getFromString(String repIdString) {
        return new RepIdDelegator_1_3(RepositoryId_1_3.cache.getId(repIdString));
    }

    // RepositoryIdUtility methods
    
    public boolean isChunkedEncoding(int valueTag) {
        return RepositoryId.isChunkedEncoding(valueTag);
    }

    public boolean isCodeBasePresent(int valueTag) {
        return RepositoryId.isCodeBasePresent(valueTag);
    }

    public String getClassDescValueRepId() {
        return RepositoryId_1_3.kClassDescValueRepID;
    }

    public String getWStringValueRepId() {
        return RepositoryId_1_3.kWStringValueRepID;
    }

    public int getTypeInfo(int valueTag) {
        return RepositoryId.getTypeInfo(valueTag);
    }

    public int getStandardRMIChunkedNoRepStrId() {
        return RepositoryId.kPreComputed_StandardRMIChunked_NoRep;
    }

    public int getCodeBaseRMIChunkedNoRepStrId() {
        return RepositoryId.kPreComputed_CodeBaseRMIChunked_NoRep;
    }

    public int getStandardRMIChunkedId() {
        return RepositoryId.kPreComputed_StandardRMIChunked;
    }

    public int getCodeBaseRMIChunkedId() {
        return RepositoryId.kPreComputed_CodeBaseRMIChunked;
    }

    public int getStandardRMIUnchunkedId() {
        return RepositoryId.kPreComputed_StandardRMIUnchunked;
    }

    public int getCodeBaseRMIUnchunkedId() {
        return RepositoryId.kPreComputed_CodeBaseRMIUnchunked;
    }

    public int getStandardRMIUnchunkedNoRepStrId() {
	return RepositoryId.kPreComputed_StandardRMIUnchunked_NoRep;
    }

    public int getCodeBaseRMIUnchunkedNoRepStrId() {
        return RepositoryId.kPreComputed_CodeBaseRMIUnchunked_NoRep;
    }

    // RepositoryIdInterface methods

    public Class getClassFromType() throws ClassNotFoundException {
        return delegate.getClassFromType();
    }

    public Class getClassFromType(String codebaseURL) 
        throws ClassNotFoundException, MalformedURLException
    {
        return delegate.getClassFromType(codebaseURL);
    }

    public Class getClassFromType(Class expectedType,
                                  String codebaseURL) 
        throws ClassNotFoundException, MalformedURLException
    {
        return delegate.getClassFromType(expectedType, codebaseURL);
    }

    public String getClassName() {
        return delegate.getClassName();
    }

    // Constructor used for factory/utility cases
    public RepIdDelegator_1_3() {}

    // Constructor used by getIdFromString.  All non-static
    // RepositoryId methods will use the provided delegate.
    private RepIdDelegator_1_3(RepositoryId_1_3 _delegate) {
        this.delegate = _delegate;
    }

    private RepositoryId_1_3 delegate = null;

    public String toString() {
        if (delegate != null)
            return delegate.toString();
        else
            return this.getClass().getName();
    }

    public boolean equals(Object obj) {
        if (delegate != null)
            return delegate.equals(obj);
        else
            return super.equals(obj);
    }
}
