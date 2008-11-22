/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.tools.doclets.formats.html;

import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.javadoc.*;

import java.io.*;

/**
 * Writes annotation type optional member documentation in HTML format.
 *
 * @author Jamie Ho
 */
public class AnnotationTypeOptionalMemberWriterImpl extends 
        AnnotationTypeRequiredMemberWriterImpl 
    implements AnnotationTypeOptionalMemberWriter, MemberSummaryWriter {
    
    /**
     * Construct a new AnnotationTypeOptionalMemberWriterImpl.
     * 
     * @param writer         the writer that will write the output.
     * @param annotationType the AnnotationType that holds this member.
     */   
    public AnnotationTypeOptionalMemberWriterImpl(SubWriterHolderWriter writer, 
        AnnotationTypeDoc annotationType) {
        super(writer, annotationType);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeMemberSummaryHeader(ClassDoc classDoc) {
        writer.println("<!-- =========== ANNOTATION TYPE OPTIONAL MEMBER SUMMARY =========== -->"); 
        writer.println();
        writer.printSummaryHeader(this, classDoc);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeDefaultValueInfo(MemberDoc member) {
        writer.dl();
        writer.dt();
        writer.bold(ConfigurationImpl.getInstance().
            getText("doclet.Default"));
        writer.dd();
        writer.print(((AnnotationTypeElementDoc) member).defaultValue());
        writer.ddEnd();
        writer.dlEnd();
    }
    
    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        writer.close();
    }
    
    /**
     * {@inheritDoc}
     */
    public void printSummaryLabel(ClassDoc cd) {
        writer.boldText("doclet.Annotation_Type_Optional_Member_Summary");
    }
    
    /**
     * {@inheritDoc}
     */
    public void printSummaryAnchor(ClassDoc cd) {
        writer.anchor("annotation_type_optional_element_summary");
    }
    
    /**
     * {@inheritDoc}
     */
    protected void printNavSummaryLink(ClassDoc cd, boolean link) {
        if (link) {
            writer.printHyperLink("", "annotation_type_optional_element_summary",
                    configuration().getText("doclet.navAnnotationTypeOptionalMember"));        
        } else {
            writer.printText("doclet.navAnnotationTypeOptionalMember");
        }
    }
}
