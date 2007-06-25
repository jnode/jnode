/*
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
package org.omg.CORBA;


/**
* The interface for <tt>IRObject</tt>.  For more information on 
* Operations interfaces, see <a href="doc-files/generatedfiles.html#operations">
* "Generated Files: Operations files"</a>.
*/

/*
 tempout/org/omg/CORBA/IRObjectOperations.java
 Generated by the IBM IDL-to-Java compiler, version 1.0
 from ../../Lib/ir.idl
 Thursday, February 25, 1999 2:11:21 o'clock PM PST
*/

/**
 * This is the Operations interface for the mapping from <tt>IRObject</tt>.
 * Several interfaces are used as base interfaces for objects in 
 * the Interface Repository (IR). These base interfaces are not instantiable.
 * A common set of operations is used to locate objects within the 
 * Interface Repository. Some of these operations are defined in 
 * the IRObject. All IR objects inherit from the IRObject interface, 
 * which provides an operation for identifying the actual type of 
 * the object. (The IDL base interface IRObject represents the most 
 * generic interface from which all other Interface Repository interfaces 
 * are derived, even the Repository itself.) All java implementations of 
 * IR objects must implement the IRObjectOperations interface.
 * @see IDLTypeOperations
 * @see IDLType
 * @see IRObject
 */
public interface IRObjectOperations 
{

    // read interface
    /**
     * Returns the <code>DefinitionKind</code> corresponding to this Interface Repository object.
     * @return the <code>DefinitionKind</code> corresponding to this Interface Repository object.
     */
    org.omg.CORBA.DefinitionKind def_kind ();

    // write interface
    /**
     * Destroys this object. If the object is a Container,
     * this method is applied to all its contents. If the object contains an IDLType
     * attribute for an anonymous type, that IDLType is destroyed.
     * If the object is currently contained in some other object, it is removed.
     * If the method is invoked on a <code>Repository</code> or on a <code>PrimitiveDef</code>
     * then the <code>BAD_INV_ORDER</code> exception is raised with minor value 2.
     * An attempt to destroy an object that would leave the repository in an
     * incoherent state causes <code>BAD_INV_ORDER</code> exception to be raised
     * with the minor code 1.
     * @exception BAD_INV_ORDER if this method is invoked on a repository or
     *            <code>PrimitiveDef</code>, or if an attempt to destroy an
     *            object would leave the repository in an incoherent state
     */
    void destroy ();
} // interface IRObjectOperations
