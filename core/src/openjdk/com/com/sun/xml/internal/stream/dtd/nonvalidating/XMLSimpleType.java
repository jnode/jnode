/*
 * Copyright 2005 Sun Microsystems, Inc.  All Rights Reserved.
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
 * 
 * THIS FILE WAS MODIFIED BY SUN MICROSYSTEMS, INC.
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 */

package com.sun.xml.internal.stream.dtd.nonvalidating;
/**
 * @version $Id: XMLSimpleType.java,v 1.2 2005/11/03 17:16:39 jeffsuttor Exp $
 */
public class XMLSimpleType {
    
    //
    // Constants
    //
    
    /** TYPE_CDATA */
    public static final short TYPE_CDATA = 0;
    
    /** TYPE_ENTITY */
    public static final short TYPE_ENTITY = 1;
    
    /** TYPE_ENUMERATION */
    public static final short TYPE_ENUMERATION = 2;
    
    /** TYPE_ID */
    public static final short TYPE_ID = 3;
    
    /** TYPE_IDREF */
    public static final short TYPE_IDREF = 4;
    
    /** TYPE_NMTOKEN */
    public static final short TYPE_NMTOKEN = 5;
    
    /** TYPE_NOTATION */
    public static final short TYPE_NOTATION = 6;
    
    /** TYPE_NAMED */
    public static final short TYPE_NAMED = 7;
    
    /** DEFAULT_TYPE_DEFAULT */
    public static final short DEFAULT_TYPE_DEFAULT = 3;
    
    /** DEFAULT_TYPE_FIXED */
    public static final short DEFAULT_TYPE_FIXED = 1;
    
    /** DEFAULT_TYPE_IMPLIED */
    public static final short DEFAULT_TYPE_IMPLIED = 0;
    
    /** DEFAULT_TYPE_REQUIRED */
    public static final short DEFAULT_TYPE_REQUIRED = 2;
    
    //
    // Data
    //
    
    /** type */
    public short type;
    
    /** name */
    public String name;
    
    /** enumeration */
    public String[] enumeration;
    
    /** list */
    public boolean list;
    
    /** defaultType */
    public short defaultType;
    
    /** defaultValue */
    public String defaultValue;
    
    /** non-normalized defaultValue */
    public String nonNormalizedDefaultValue;
    
    
    //
    // Methods
    //
    
    /**
     * setValues
     *
     * @param type
     * @param name
     * @param enumeration
     * @param list
     * @param defaultType
     * @param defaultValue
     * @param nonNormalizedDefaultValue
     * @param datatypeValidator
     */
    public void setValues(short type, String name, String[] enumeration,
    boolean list, short defaultType,
    String defaultValue, String nonNormalizedDefaultValue){
        
        this.type              = type;
        this.name              = name;
        // REVISIT: Should this be a copy? -Ac
        if (enumeration != null && enumeration.length > 0) {
            this.enumeration = new String[enumeration.length];
            System.arraycopy(enumeration, 0, this.enumeration, 0, this.enumeration.length);
        }
        else {
            this.enumeration = null;
        }
        this.list              = list;
        this.defaultType       = defaultType;
        this.defaultValue      = defaultValue;
        this.nonNormalizedDefaultValue      = nonNormalizedDefaultValue;
        
    } // setValues(short,String,String[],boolean,short,String,String,DatatypeValidator)
    
    /** Set values. */
    public void setValues(XMLSimpleType simpleType) {
        
        type = simpleType.type;
        name = simpleType.name;
        // REVISIT: Should this be a copy? -Ac
        if (simpleType.enumeration != null && simpleType.enumeration.length > 0) {
            enumeration = new String[simpleType.enumeration.length];
            System.arraycopy(simpleType.enumeration, 0, enumeration, 0, enumeration.length);
        }
        else {
            enumeration = null;
        }
        list = simpleType.list;
        defaultType = simpleType.defaultType;
        defaultValue = simpleType.defaultValue;
        nonNormalizedDefaultValue = simpleType.nonNormalizedDefaultValue;
        
    } // setValues(XMLSimpleType)
    
    /**
     * clear
     */
    public void clear() {
        this.type              = -1;
        this.name              = null;
        this.enumeration       = null;
        this.list              = false;
        this.defaultType       = -1;
        this.defaultValue      = null;
        this.nonNormalizedDefaultValue = null;
    }
    
} // class XMLSimpleType
