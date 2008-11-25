/*
 * Copyright 2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.doclets.internal.toolkit;

import com.sun.tools.doclets.internal.toolkit.util.*;
import com.sun.javadoc.*;

/**
 * The interface for a factory creates writers.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 * 
 * @author Jamie Ho
 * @since 1.4
 */

public interface WriterFactory {
    
    /**
     * Return the writer for the constant summary.
     *
     * @return the writer for the constant summary.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract ConstantsSummaryWriter getConstantsSummaryWriter()
        throws Exception;
    
    /**
     * Return the writer for the package summary.
     *
     * @param packageDoc the package being documented.
     * @param prevPkg the previous package that was documented.
     * @param nextPkg the next package being documented.
     * @return the writer for the package summary.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract PackageSummaryWriter getPackageSummaryWriter(PackageDoc
        packageDoc, PackageDoc prevPkg, PackageDoc nextPkg) 
    throws Exception;
    
    /**
     * Return the writer for a class.
     *
     * @param classDoc the class being documented.
     * @param prevClass the previous class that was documented.
     * @param nextClass the next class being documented.
     * @param classTree the class tree.
     * @return the writer for the class.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract ClassWriter getClassWriter(ClassDoc classDoc,
        ClassDoc prevClass, ClassDoc nextClass, ClassTree classTree)
            throws Exception;
            
    /**
     * Return the writer for an annotation type.
     *
     * @param annotationType the type being documented.
     * @param prevType the previous type that was documented.
     * @param nextType the next type being documented.
     * @return the writer for the annotation type.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract AnnotationTypeWriter getAnnotationTypeWriter(
        AnnotationTypeDoc annotationType, Type prevType, Type nextType)
            throws Exception;
    
    /**
     * Return the method writer for a given class.
     *
     * @param classWriter the writer for the class being documented.
     * @return the method writer for the give class.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract MethodWriter getMethodWriter(ClassWriter classWriter)
            throws Exception;
    
    /**
     * Return the annotation type optional member writer for a given annotation 
     * type.
     *
     * @param annotationTypeWriter the writer for the annotation type
     *        being documented.
     * @return the member writer for the given annotation type.  Return null if 
     *         this writer is not supported by the doclet.
     */
    public abstract AnnotationTypeOptionalMemberWriter 
            getAnnotationTypeOptionalMemberWriter(
        AnnotationTypeWriter annotationTypeWriter) throws Exception;
        
    /**
     * Return the annotation type required member writer for a given annotation type.
     *
     * @param annotationTypeWriter the writer for the annotation type
     *        being documented.
     * @return the member writer for the given annotation type.  Return null if 
     *         this writer is not supported by the doclet.
     */
    public abstract AnnotationTypeRequiredMemberWriter 
            getAnnotationTypeRequiredMemberWriter(
        AnnotationTypeWriter annotationTypeWriter) throws Exception;
    
    /**
     * Return the enum constant writer for a given class.
     *
     * @param classWriter the writer for the class being documented.
     * @return the enum constant writer for the give class.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract EnumConstantWriter getEnumConstantWriter(
        ClassWriter classWriter) throws Exception;
    
    /**
     * Return the field writer for a given class.
     *
     * @param classWriter the writer for the class being documented.
     * @return the field writer for the give class.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract FieldWriter getFieldWriter(ClassWriter classWriter)
            throws Exception;
    
    /**
     * Return the constructor writer for a given class.
     *
     * @param classWriter the writer for the class being documented.
     * @return the method writer for the give class.  Return null if this
     * writer is not supported by the doclet.
     */
    public abstract ConstructorWriter getConstructorWriter(
        ClassWriter classWriter) 
    throws Exception;
    
    /**
     * Return the specified member summary writer for a given class.
     *
     * @param classWriter the writer for the class being documented.
     * @param memberType  the {@link VisibleMemberMap} member type indicating
     *                    the type of member summary that should be returned.
     * @return the summary writer for the give class.  Return null if this
     * writer is not supported by the doclet.
     *
     * @see VisibleMemberMap
     * @throws IllegalArgumentException if memberType is unknown.
     */
    public abstract MemberSummaryWriter getMemberSummaryWriter(
        ClassWriter classWriter, int memberType)
    throws Exception;
    
    /**
     * Return the specified member summary writer for a given annotation type.
     *
     * @param annotationTypeWriter the writer for the annotation type being 
     *                             documented.
     * @param memberType  the {@link VisibleMemberMap} member type indicating
     *                    the type of member summary that should be returned.
     * @return the summary writer for the give class.  Return null if this
     * writer is not supported by the doclet.
     *
     * @see VisibleMemberMap
     * @throws IllegalArgumentException if memberType is unknown.
     */
    public abstract MemberSummaryWriter getMemberSummaryWriter(
        AnnotationTypeWriter annotationTypeWriter, int memberType)
    throws Exception;
    
    /**
     * Return the writer for the serialized form.
     *
     * @return the writer for the serialized form.
     */
    public SerializedFormWriter getSerializedFormWriter() throws Exception;
}
