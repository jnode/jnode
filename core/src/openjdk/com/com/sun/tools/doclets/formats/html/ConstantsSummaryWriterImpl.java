/*
 * Copyright 2001-2005 Sun Microsystems, Inc.  All Rights Reserved.
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
import com.sun.javadoc.*;
import java.io.*;
import java.util.*;

/**
 * Write the Constants Summary Page in HTML format.
 *
 * @author Jamie Ho
 * @since 1.4
 */
public class ConstantsSummaryWriterImpl extends HtmlDocletWriter
        implements ConstantsSummaryWriter {
    
    /**
     * The configuration used in this run of the standard doclet.
     */
    ConfigurationImpl configuration;
    
    /**
     * The current class being documented.
     */
    private ClassDoc currentClassDoc;
    
    /**
     * Construct a ConstantsSummaryWriter.
     * @param configuration the configuration used in this run
     *        of the standard doclet.
     */
    public ConstantsSummaryWriterImpl(ConfigurationImpl configuration)
            throws IOException {
        super(configuration, ConfigurationImpl.CONSTANTS_FILE_NAME);
        this.configuration = configuration;
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeHeader() {
        printHtmlHeader(configuration.getText("doclet.Constants_Summary"),
            null, true);
        printTop();
        navLinks(true);
        hr();
        
        center();
        h1(); printText("doclet.Constants_Summary"); h1End();
        centerEnd();
        
        hr(4, "noshade");
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeFooter() {
        hr();
        navLinks(false);
        printBottom();
        printBodyHtmlEnd();
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeContentsHeader() {
        bold(configuration.getText("doclet.Contents"));
        ul();
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeContentsFooter() {
        ulEnd();
        println();
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeLinkToPackageContent(PackageDoc pkg, String parsedPackageName, Set printedPackageHeaders) {
        String packageName = pkg.name();
        //add link to summary
        li();
        if (packageName.length() == 0) {
            printHyperLink("#" + DocletConstants.UNNAMED_PACKAGE_ANCHOR,
                           DocletConstants.DEFAULT_PACKAGE_NAME);
        } else {
            printHyperLink("#" + parsedPackageName, parsedPackageName + ".*");
            printedPackageHeaders.add(parsedPackageName);
        }
        println();
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeConstantMembersHeader(ClassDoc cd) {
        //generate links backward only to public classes.
        String classlink = (cd.isPublic() || cd.isProtected())?
            getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CONSTANT_SUMMARY, cd, 
                false)) :
            cd.qualifiedName();
        String name = cd.containingPackage().name();
        if (name.length() > 0) {
            writeClassName(name + "." + classlink);
        } else {
            writeClassName(classlink);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void writeConstantMembersFooter(ClassDoc cd) {
        tableFooter(false);
        p();
    }
    
    /**
     * Print the class name in the table heading.
     * @param classStr the heading to print.
     */
    protected void writeClassName(String classStr) {
        table(1, 3, 0);
        trBgcolorStyle("#EEEEFF", "TableSubHeadingColor");
        thAlignColspan("left", 3);
        write(classStr);
        thEnd();
        trEnd();
    }
    
    private void tableFooter(boolean isHeader) {
        fontEnd();
        if (isHeader) {
            thEnd();
        } else {
            tdEnd();
        }        
        trEnd(); 
        tableEnd();
        p();
    }
    
    /**
     * {@inheritDoc}
     */
    public void writePackageName(PackageDoc pkg, String parsedPackageName) {
        String pkgname;
        if (parsedPackageName.length() == 0) {
            anchor(DocletConstants.UNNAMED_PACKAGE_ANCHOR);
            pkgname = DocletConstants.DEFAULT_PACKAGE_NAME;
        } else {
            anchor(parsedPackageName);
            pkgname = parsedPackageName;
        }
        table(1, "100%", 3, 0);
        trBgcolorStyle("#CCCCFF", "TableHeadingColor");
        thAlign("left");
        font("+2");
        write(pkgname + ".*");
        tableFooter(true);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeConstantMembers(ClassDoc cd, List fields) {
        currentClassDoc = cd;
        for (int i = 0; i < fields.size(); ++i) {
            writeConstantMember((FieldDoc)(fields.get(i)));
        }
    }
        
    private void writeConstantMember(FieldDoc member) {
        trBgcolorStyle("white", "TableRowColor");
        anchor(currentClassDoc.qualifiedName() + "." + member.name());
        writeTypeColumn(member);
        writeNameColumn(member);
        writeValue(member);
        trEnd();
    }
    
    private void writeTypeColumn(FieldDoc member) {
        tdAlign("right");
        font("-1");
        code();
        StringTokenizer mods = new StringTokenizer(member.modifiers());
        while(mods.hasMoreTokens()) {
            print(mods.nextToken() + "&nbsp;");
        }
        printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CONSTANT_SUMMARY, 
            member.type()));
        codeEnd();
        fontEnd();
        tdEnd();
    }
    
    private void writeNameColumn(FieldDoc member) {
        tdAlign("left");
        code();
        printDocLink(LinkInfoImpl.CONTEXT_CONSTANT_SUMMARY, member, 
            member.name(), false);
        codeEnd();
        tdEnd();
    }
    
    private void writeValue(FieldDoc member) {
        tdAlign("right");
        code();
        print(Util.escapeHtmlChars(member.constantValueExpression()));
        codeEnd();
        tdEnd();
    }
}
