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

import com.sun.javadoc.*;
import java.io.*;

/**
 * The interface for writing serialized form output.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 * 
 * @author Jamie Ho
 * @since 1.5
 */

public interface SerializedFormWriter {
    
    /**
     * Write the given header.
     *
     * @param header the header to write.
     */
    public void writeHeader(String header);
    
    /**
     * Write the given package header.
     *
     * @param packageName the package header to write.
     */
    public void writePackageHeader(String packageName);
    
    /**
     * Write the heading for the serializable class.
     *
     * @param classDoc the class being processed.
     */
    public void writeClassHeader(ClassDoc classDoc);
    
    /**
     * Write the serial UID info.
     *
     * @param header the header that will show up before the UID.
     * @param serialUID the serial UID to print.
     */
    public void writeSerialUIDInfo(String header, String serialUID);
    
    /**
     * Return an instance of a SerialFieldWriter.
     *
     * @return an instance of a SerialFieldWriter.
     */
    public SerialFieldWriter getSerialFieldWriter(ClassDoc classDoc);
    
    /**
     * Return an instance of a SerialMethodWriter.
     *
     * @return an instance of a SerialMethodWriter.
     */
    public SerialMethodWriter getSerialMethodWriter(ClassDoc classDoc);
    
    /**
     * Close the writer.
     */
    public abstract void close() throws IOException;
    
    /**
     * Write the footer.
     */
    public void writeFooter();
    
    /**
     * Write the serialized form for a given field.
     */
    public interface SerialFieldWriter {
        
        /**
         * Write the given heading.
         *
         * @param heading the heading to write.
         */
        public void writeHeader(String heading);
        
        /**
         * Write the deprecated information for this member.
         *
         * @param field the field to document.
         */
        public void writeMemberDeprecatedInfo(FieldDoc field);
        
        /**
         * Write the description text for this member.
         *
         * @param field the field to document.
         */
        public void writeMemberDescription(FieldDoc field);
        
        /**
         * Write the description text for this member represented by the tag.
         *
         * @param serialFieldTag the field to document (represented by tag).
         */
        public void writeMemberDescription(SerialFieldTag serialFieldTag);
        
        /**
         * Write the tag information for this member.
         *
         * @param field the field to document.
         */
        public void writeMemberTags(FieldDoc field);
        
        /**
         * Write the member header.
         *
         * @param fieldType the type of the field.
         * @param fieldTypeStr the type of the field in string format.  We will
         * print this out if we can't link to the type.
         * @param fieldDimensions the dimensions of the field.
         * @param fieldName the name of the field.
         */
        public void writeMemberHeader(ClassDoc fieldType, String fieldTypeStr,
            String fieldDimensions, String fieldName);
        
        /**
         * Write the footer.
         *
         * @param member the member to write the header for.
         */
        public void writeMemberFooter(FieldDoc member);
    }
    
    /**
     * Write the serialized form for a given field.
     */
    public interface SerialMethodWriter {
        
        /**
         * Write the given heading.
         *
         * @param heading the heading to write.
         */
        public void writeHeader(String heading);
        
        /**
         * Write a warning that no serializable methods exist.
         *
         * @param msg the warning to print.
         */
        public void writeNoCustomizationMsg(String msg);
        
        /**
         * Write the header.
         *
         * @param member the member to write the header for.
         */
        public void writeMemberHeader(MethodDoc member);
        
        /**
         * Write the footer.
         *
         * @param member the member to write the header for.
         */
        public void writeMemberFooter(MethodDoc member);
        
        /**
         * Write the deprecated information for this member.
         */
        public void writeDeprecatedMemberInfo(MethodDoc member);
        
        /**
         * Write the description for this member.
         */
        public void writeMemberDescription(MethodDoc member);
        
        /**
         * Write the tag information for this member.
         */
        public void writeMemberTags(MethodDoc member);
    }
}
