/*
 * Copyright 1997-2003 Sun Microsystems, Inc.  All Rights Reserved.
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
import com.sun.tools.doclets.internal.toolkit.util.*;
import com.sun.tools.doclets.internal.toolkit.taglets.*;
import com.sun.javadoc.*;
import java.util.*;
import java.io.*;

/**
 * Writes constructor documentation.
 *
 * @author Robert Field
 * @author Atul M Dambalkar
 */
public class ConstructorWriterImpl extends AbstractExecutableMemberWriter
    implements ConstructorWriter, MemberSummaryWriter {    
    
    private boolean foundNonPubConstructor = false;
    private boolean printedSummaryHeader = false;
    
    /**
     * Construct a new ConstructorWriterImpl.
     *
     * @param writer The writer for the class that the constructors belong to.
     * @param classDoc the class being documented.
     */
    public ConstructorWriterImpl(SubWriterHolderWriter writer,
            ClassDoc classDoc) {
        super(writer, classDoc);
        VisibleMemberMap visibleMemberMap = new VisibleMemberMap(classDoc, 
            VisibleMemberMap.CONSTRUCTORS, configuration().nodeprecated);
        List constructors = new ArrayList(visibleMemberMap.getMembersFor(classDoc));
        for (int i = 0; i < constructors.size(); i++) {
            if (((ProgramElementDoc)(constructors.get(i))).isProtected() ||
                ((ProgramElementDoc)(constructors.get(i))).isPrivate()) {
                setFoundNonPubConstructor(true);                   
            }
        } 
    }
    
    /**
     * Construct a new ConstructorWriterImpl.
     *
     * @param writer The writer for the class that the constructors belong to.
     */
    public ConstructorWriterImpl(SubWriterHolderWriter writer) {
        super(writer);
    }
    
    /**
     * Write the constructors summary header for the given class.
     *
     * @param classDoc the class the summary belongs to.
     */
    public void writeMemberSummaryHeader(ClassDoc classDoc) {
        printedSummaryHeader = true;
        writer.println();
        writer.println("<!-- ======== CONSTRUCTOR SUMMARY ======== -->"); 
        writer.println();
        writer.printSummaryHeader(this, classDoc);
    }
    
    /**
     * Write the constructors summary footer for the given class.
     *
     * @param classDoc the class the summary belongs to.
     */
    public void writeMemberSummaryFooter(ClassDoc classDoc) {
        writer.printSummaryFooter(this, classDoc);        
    }
    
    /**
     * Write the header for the constructor documentation.
     *
     * @param classDoc the class that the constructors belong to.
     */
    public void writeHeader(ClassDoc classDoc, String header) {
        writer.println();
        writer.println("<!-- ========= CONSTRUCTOR DETAIL ======== -->"); 
        writer.println();
        writer.anchor("constructor_detail");
        writer.printTableHeadingBackground(header);
    }
    
    /**
     * Write the constructor header for the given constructor.
     *
     * @param constructor the constructor being documented.
     * @param isFirst the flag to indicate whether or not the constructor is the 
     *        first to be documented.
     */
    public void writeConstructorHeader(ConstructorDoc constructor, boolean isFirst) {
        if (! isFirst) {
            writer.printMemberHeader();            
        }
        writer.println();
        String erasureAnchor;
        if ((erasureAnchor = getErasureAnchor(constructor)) != null) {
            writer.anchor(erasureAnchor);
        } 
        writer.anchor(constructor);
        writer.h3();
        writer.print(constructor.name());
        writer.h3End();
    }
    
    /**
     * Write the signature for the given constructor.
     *
     * @param constructor the constructor being documented.
     */
    public void writeSignature(ConstructorDoc constructor) {
        writer.displayLength = 0;
        writer.pre();
        writer.writeAnnotationInfo(constructor);
        printModifiers(constructor);
        //printReturnType((ConstructorDoc)constructor);
        if (configuration().linksource) {
            writer.printSrcLink(constructor, constructor.name());
        } else {
            bold(constructor.name());
        }
        writeParameters(constructor);
        writeExceptions(constructor);
        writer.preEnd();
        writer.dl();
    }
    
    /**
     * Write the deprecated output for the given constructor.
     *
     * @param constructor the constructor being documented.
     */
    public void writeDeprecated(ConstructorDoc constructor) {
        String output = ((TagletOutputImpl)
            (new DeprecatedTaglet()).getTagletOutput(constructor, 
            writer.getTagletWriterInstance(false))).toString();
        if (output != null && output.trim().length() > 0) {            
            writer.print(output);
        }
    }
    
    /**
     * Write the comments for the given constructor.
     *
     * @param constructor the constructor being documented.
     */
    public void writeComments(ConstructorDoc constructor) {
        if (constructor.inlineTags().length > 0) {
            writer.dd();
            writer.printInlineComment(constructor);
        }
    }
    
    /**
     * Write the tag output for the given constructor.
     *
     * @param constructor the constructor being documented.
     */
    public void writeTags(ConstructorDoc constructor) {
        writer.printTags(constructor);
    }
    
    /**
     * Write the constructor footer.
     */
    public void writeConstructorFooter() {
        writer.dlEnd();
    }
    
    /**
     * Write the footer for the constructor documentation.
     *
     * @param classDoc the class that the constructors belong to.
     */
    public void writeFooter(ClassDoc classDoc) {
        //No footer to write for constructor documentation
    }
    
    /**
     * Close the writer.
     */
    public void close() throws IOException {
        writer.close();
    }
    
    /**
     * Let the writer know whether a non public constructor was found.
     *
     * @param foundNonPubConstructor true if we found a non public constructor.
     */
    public void setFoundNonPubConstructor(boolean foundNonPubConstructor) {
        this.foundNonPubConstructor = foundNonPubConstructor;
    }
    
    public void printSummaryLabel(ClassDoc cd) {
        writer.boldText("doclet.Constructor_Summary");
    }
    
    public void printSummaryAnchor(ClassDoc cd) {
        writer.anchor("constructor_summary");
    }
    
    public void printInheritedSummaryAnchor(ClassDoc cd) {
    }   // no such
    
    public void printInheritedSummaryLabel(ClassDoc cd) {
        // no such
    }
    
    public int getMemberKind() {
        return VisibleMemberMap.CONSTRUCTORS;
    }
    
    protected void navSummaryLink(List members) {
        printNavSummaryLink(classdoc,
                members.size() > 0? true: false);
    }
    
    protected void printNavSummaryLink(ClassDoc cd, boolean link) {
        if (link) {
            writer.printHyperLink("", "constructor_summary",
                    ConfigurationImpl.getInstance().getText("doclet.navConstructor"));
        } else {
            writer.printText("doclet.navConstructor");
        }
    }
    
    protected void printNavDetailLink(boolean link) {
        if (link) {
            writer.printHyperLink("", "constructor_detail",
                    ConfigurationImpl.getInstance().getText("doclet.navConstructor"));
        } else {
            writer.printText("doclet.navConstructor");
        }
    }
    
    protected void printSummaryType(ProgramElementDoc member) {
        if (foundNonPubConstructor) {
            writer.printTypeSummaryHeader();
            if (member.isProtected()) {
                print("protected ");
            } else if (member.isPrivate()) {
                print("private ");
            } else if (member.isPublic()) {
                writer.space();
            } else {
                writer.printText("doclet.Package_private");
            }
            writer.printTypeSummaryFooter();
        }
    }
    
    /**
     * Write the inherited member summary header for the given class.
     *
     * @param classDoc the class the summary belongs to.
     */
    public void writeInheritedMemberSummaryHeader(ClassDoc classDoc) {
        if(! printedSummaryHeader){
            //We don't want inherited summary to not be under heading.
            writeMemberSummaryHeader(classDoc);
            writeMemberSummaryFooter(classDoc);
            printedSummaryHeader = true;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeInheritedMemberSummary(ClassDoc classDoc, 
        ProgramElementDoc member, boolean isFirst, boolean isLast) {}
    
    /**
     * Write the inherited member summary footer for the given class.
     *
     * @param classDoc the class the summary belongs to.
     */
    public void writeInheritedMemberSummaryFooter(ClassDoc classDoc) {}
}
