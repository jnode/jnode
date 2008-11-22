/*
 * Copyright 1998-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

import com.sun.tools.doclets.internal.toolkit.util.*;
import com.sun.javadoc.*;
import java.io.*;
import java.util.*;

/**
 * Generate class usage information.
 *
 * @author Robert G. Field
 */
public class ClassUseWriter extends SubWriterHolderWriter {
    
    final ClassDoc classdoc;
    Set pkgToPackageAnnotations = null;
    final Map pkgToClassTypeParameter;
    final Map pkgToClassAnnotations;
    final Map pkgToMethodTypeParameter;
    final Map pkgToMethodArgTypeParameter;
    final Map pkgToMethodReturnTypeParameter;
    final Map pkgToMethodAnnotations;
    final Map pkgToMethodParameterAnnotations;
    final Map pkgToFieldTypeParameter;
    final Map pkgToFieldAnnotations;
    final Map pkgToSubclass;
    final Map pkgToSubinterface;
    final Map pkgToImplementingClass;
    final Map pkgToField;
    final Map pkgToMethodReturn;
    final Map pkgToMethodArgs;
    final Map pkgToMethodThrows;
    final Map pkgToConstructorAnnotations;
    final Map pkgToConstructorParameterAnnotations;
    final Map pkgToConstructorArgs;
    final Map pkgToConstructorArgTypeParameter;
    final Map pkgToConstructorThrows;
    final SortedSet pkgSet;
    final MethodWriterImpl methodSubWriter;
    final ConstructorWriterImpl constrSubWriter;
    final FieldWriterImpl fieldSubWriter;
    final NestedClassWriterImpl classSubWriter;
    
    
    /**
     * Constructor.
     *
     * @param filename the file to be generated.
     * @throws IOException
     * @throws DocletAbortException
     */
    public ClassUseWriter(ConfigurationImpl configuration,
                          ClassUseMapper mapper, String path,
                          String filename, String relpath,
                          ClassDoc classdoc) throws IOException {
        super(configuration, path, filename, relpath);
        this.classdoc = classdoc;
        if (mapper.classToPackageAnnotations.containsKey(classdoc.qualifiedName()))
        	pkgToPackageAnnotations = new HashSet((List) mapper.classToPackageAnnotations.get(classdoc.qualifiedName()));
        configuration.currentcd = classdoc;
        this.pkgSet = new TreeSet();
        this.pkgToClassTypeParameter = pkgDivide(mapper.classToClassTypeParam);
        this.pkgToClassAnnotations = pkgDivide(mapper.classToClassAnnotations);
        this.pkgToMethodTypeParameter = pkgDivide(mapper.classToExecMemberDocTypeParam);
        this.pkgToMethodArgTypeParameter = pkgDivide(mapper.classToExecMemberDocArgTypeParam);
        this.pkgToFieldTypeParameter = pkgDivide(mapper.classToFieldDocTypeParam);
        this.pkgToFieldAnnotations = pkgDivide(mapper.annotationToFieldDoc);
        this.pkgToMethodReturnTypeParameter = pkgDivide(mapper.classToExecMemberDocReturnTypeParam);
        this.pkgToMethodAnnotations = pkgDivide(mapper.classToExecMemberDocAnnotations);
        this.pkgToMethodParameterAnnotations = pkgDivide(mapper.classToExecMemberDocParamAnnotation);
        this.pkgToSubclass = pkgDivide(mapper.classToSubclass);
        this.pkgToSubinterface = pkgDivide(mapper.classToSubinterface);
        this.pkgToImplementingClass = pkgDivide(mapper.classToImplementingClass);
        this.pkgToField = pkgDivide(mapper.classToField);
        this.pkgToMethodReturn = pkgDivide(mapper.classToMethodReturn);
        this.pkgToMethodArgs = pkgDivide(mapper.classToMethodArgs);
        this.pkgToMethodThrows = pkgDivide(mapper.classToMethodThrows);
        this.pkgToConstructorAnnotations = pkgDivide(mapper.classToConstructorAnnotations);
        this.pkgToConstructorParameterAnnotations = pkgDivide(mapper.classToConstructorParamAnnotation);
        this.pkgToConstructorArgs = pkgDivide(mapper.classToConstructorArgs);
        this.pkgToConstructorArgTypeParameter = pkgDivide(mapper.classToConstructorDocArgTypeParam);
        this.pkgToConstructorThrows = pkgDivide(mapper.classToConstructorThrows);
        //tmp test
        if (pkgSet.size() > 0 && 
            mapper.classToPackage.containsKey(classdoc.qualifiedName()) &&
            !pkgSet.equals(mapper.classToPackage.get(classdoc.qualifiedName()))) {
            configuration.root.printWarning("Internal error: package sets don't match: " + pkgSet + " with: " +
                                   mapper.classToPackage.get(classdoc.qualifiedName()));
        }
        methodSubWriter = new MethodWriterImpl(this);
        constrSubWriter = new ConstructorWriterImpl(this);
        fieldSubWriter = new FieldWriterImpl(this);
        classSubWriter = new NestedClassWriterImpl(this);
    }
    
    /**
     * Write out class use pages.
     * @throws DocletAbortException
     */
    public static void generate(ConfigurationImpl configuration,
                                ClassTree classtree)  {
        ClassUseMapper mapper = new ClassUseMapper(configuration.root, classtree);
        ClassDoc[] classes = configuration.root.classes();
        for (int i = 0; i < classes.length; i++) {
            ClassUseWriter.generate(configuration, mapper, classes[i]);
        }
        PackageDoc[] pkgs = configuration.packages;
        for (int i = 0; i < pkgs.length; i++) {
            PackageUseWriter.generate(configuration, mapper, pkgs[i]);
        }
    }
    
    private Map pkgDivide(Map classMap) {
        Map map = new HashMap();
        List list= (List)classMap.get(classdoc.qualifiedName());
        if (list != null) {
            Collections.sort(list);
            Iterator it = list.iterator();
            while (it.hasNext()) {
                ProgramElementDoc doc = (ProgramElementDoc)it.next();
                PackageDoc pkg = doc.containingPackage();
                pkgSet.add(pkg);
                List inPkg = (List)map.get(pkg.name());
                if (inPkg == null) {
                    inPkg = new ArrayList();
                    map.put(pkg.name(), inPkg);
                }
                inPkg.add(doc);
            }
        }
        return map;
    }
    
    /**
     * Generate a class page.
     */
    public static void generate(ConfigurationImpl configuration,
                                ClassUseMapper mapper, ClassDoc classdoc) {
        ClassUseWriter clsgen;
        String path = DirectoryManager.getDirectoryPath(classdoc.
                                                            containingPackage());
        if (path.length() > 0) {
            path += File.separator;
        }
        path += "class-use";
        String filename = classdoc.name() + ".html";
        String pkgname = classdoc.containingPackage().name();
        pkgname += (pkgname.length() > 0)? ".class-use": "class-use";
        String relpath = DirectoryManager.getRelativePath(pkgname);
        try {
            clsgen = new ClassUseWriter(configuration,
                                        mapper, path, filename,
                                        relpath, classdoc);
            clsgen.generateClassUseFile();
            clsgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.
                error("doclet.exception_encountered",
                      exc.toString(), filename);
            throw new DocletAbortException();
        }
    }
    
    /**
     * Print the class use list.
     */
    protected void generateClassUseFile() throws IOException {
        
        printClassUseHeader();
        
        if (pkgSet.size() > 0) {
            generateClassUse();
        } else {
            printText("doclet.ClassUse_No.usage.of.0",
                      classdoc.qualifiedName());
            p();
        }
        
        printClassUseFooter();
    }
    
    protected void generateClassUse() throws IOException {
        if (configuration.packages.length > 1) {
            generatePackageList();
            generatePackageAnnotationList();
        }
        generateClassList();
    }
    
    protected void generatePackageList() throws IOException {
        tableIndexSummary();
        tableHeaderStart("#CCCCFF");
        printText("doclet.ClassUse_Packages.that.use.0",
            getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc, 
                false)));
        tableHeaderEnd();
        
        for (Iterator it = pkgSet.iterator(); it.hasNext();) {
            PackageDoc pkg = (PackageDoc)it.next();
            generatePackageUse(pkg);
        }
        tableEnd();
        space();
        p();
    }
    
    protected void generatePackageAnnotationList() throws IOException {
        if ((! classdoc.isAnnotationType()) || 
               pkgToPackageAnnotations == null || 
               pkgToPackageAnnotations.size() == 0)
            return;
        tableIndexSummary();
        tableHeaderStart("#CCCCFF");
        printText("doclet.ClassUse_PackageAnnotation",
            getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc, 
                false)));
        tableHeaderEnd();
        for (Iterator it = pkgToPackageAnnotations.iterator(); it.hasNext();) {
            PackageDoc pkg = (PackageDoc)it.next();
            trBgcolorStyle("white", "TableRowColor");
            summaryRow(0);
            //Just want an anchor here.
            printPackageLink(pkg, pkg.name(), true);
            summaryRowEnd();
            summaryRow(0);
            printSummaryComment(pkg);
            space();
            summaryRowEnd();
            trEnd();
        }
        tableEnd();
        space();
        p();
    }
    
    protected void generateClassList() throws IOException {
        for (Iterator it = pkgSet.iterator(); it.hasNext();) {
            PackageDoc pkg = (PackageDoc)it.next();
            anchor(pkg.name());
            tableIndexSummary();
            tableHeaderStart("#CCCCFF");
            printText("doclet.ClassUse_Uses.of.0.in.1",
                getLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, 
                    classdoc, false)),
                getPackageLink(pkg, Util.getPackageName(pkg), false));
            tableHeaderEnd();
            tableEnd();
            space();
            p();
            generateClassUse(pkg);
        }
    }
    
    /**
     * Print the package use list.
     */
    protected void generatePackageUse(PackageDoc pkg) throws IOException {
        trBgcolorStyle("white", "TableRowColor");
        summaryRow(0);
        //Just want an anchor here.
        printHyperLink("", pkg.name(), Util.getPackageName(pkg), true);
        summaryRowEnd();
        summaryRow(0);
        printSummaryComment(pkg);
        space();
        summaryRowEnd();
        trEnd();
    }
    
    /**
     * Print the class use list.
     */
    protected void generateClassUse(PackageDoc pkg) throws IOException {
        String classLink = getLink(new LinkInfoImpl(
            LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc, false));
        String pkgLink = getPackageLink(pkg, Util.getPackageName(pkg), false);
        classSubWriter.printUseInfo(pkgToClassAnnotations.get(pkg.name()),
            configuration.getText("doclet.ClassUse_Annotation", classLink, 
            pkgLink));
        
        classSubWriter.printUseInfo(pkgToClassTypeParameter.get(pkg.name()),
            configuration.getText("doclet.ClassUse_TypeParameter", classLink, 
            pkgLink));
        classSubWriter.printUseInfo(pkgToSubclass.get(pkg.name()),
            configuration.getText("doclet.ClassUse_Subclass", classLink, 
            pkgLink));
        classSubWriter.printUseInfo(pkgToSubinterface.get(pkg.name()),
                                    configuration.getText("doclet.ClassUse_Subinterface",
                                            classLink,
                                            pkgLink));
        classSubWriter.printUseInfo(pkgToImplementingClass.get(pkg.name()),
                                    configuration.getText("doclet.ClassUse_ImplementingClass",
                                            classLink,
                                            pkgLink));
        fieldSubWriter.printUseInfo(pkgToField.get(pkg.name()),
                                    configuration.getText("doclet.ClassUse_Field",
                                            classLink,
                                            pkgLink));
        fieldSubWriter.printUseInfo(pkgToFieldAnnotations.get(pkg.name()),
            configuration.getText("doclet.ClassUse_FieldAnnotations",
            classLink,
            pkgLink));
        fieldSubWriter.printUseInfo(pkgToFieldTypeParameter.get(pkg.name()),
            configuration.getText("doclet.ClassUse_FieldTypeParameter",
            classLink,
            pkgLink));
        methodSubWriter.printUseInfo(pkgToMethodAnnotations.get(pkg.name()),
            configuration.getText("doclet.ClassUse_MethodAnnotations", classLink, 
            pkgLink));
        methodSubWriter.printUseInfo(pkgToMethodParameterAnnotations.get(pkg.name()),
            configuration.getText("doclet.ClassUse_MethodParameterAnnotations", classLink, 
            pkgLink));
        methodSubWriter.printUseInfo(pkgToMethodTypeParameter.get(pkg.name()),
            configuration.getText("doclet.ClassUse_MethodTypeParameter", classLink, 
            pkgLink));      
        methodSubWriter.printUseInfo(pkgToMethodReturn.get(pkg.name()),
                                     configuration.getText("doclet.ClassUse_MethodReturn",
                                             classLink,
                                             pkgLink));
        methodSubWriter.printUseInfo(pkgToMethodReturnTypeParameter.get(pkg.name()),
            configuration.getText("doclet.ClassUse_MethodReturnTypeParameter", classLink, 
            pkgLink));
        methodSubWriter.printUseInfo(pkgToMethodArgs.get(pkg.name()),
                                     configuration.getText("doclet.ClassUse_MethodArgs",
                                             classLink,
                                             pkgLink));
        methodSubWriter.printUseInfo(pkgToMethodArgTypeParameter.get(pkg.name()),
            configuration.getText("doclet.ClassUse_MethodArgsTypeParameters",
            classLink,
            pkgLink));
        methodSubWriter.printUseInfo(pkgToMethodThrows.get(pkg.name()),
                                     configuration.getText("doclet.ClassUse_MethodThrows",
                                             classLink,
                                             pkgLink));        
        constrSubWriter.printUseInfo(pkgToConstructorAnnotations.get(pkg.name()),
            configuration.getText("doclet.ClassUse_ConstructorAnnotations",
                classLink,
                pkgLink));
        constrSubWriter.printUseInfo(pkgToConstructorParameterAnnotations.get(pkg.name()),
            configuration.getText("doclet.ClassUse_ConstructorParameterAnnotations",
                classLink,
                pkgLink));
        constrSubWriter.printUseInfo(pkgToConstructorArgs.get(pkg.name()),
                                     configuration.getText("doclet.ClassUse_ConstructorArgs",
                                             classLink,
                                             pkgLink));
        constrSubWriter.printUseInfo(pkgToConstructorArgTypeParameter.get(pkg.name()),
            configuration.getText("doclet.ClassUse_ConstructorArgsTypeParameters",
            classLink,
            pkgLink));
        constrSubWriter.printUseInfo(pkgToConstructorThrows.get(pkg.name()),
                                     configuration.getText("doclet.ClassUse_ConstructorThrows",
                                             classLink,
                                             pkgLink));
    }
    
    /**
     * Print the header for the class use Listing.
     */
    protected void printClassUseHeader() {
        String cltype = configuration.getText(classdoc.isInterface()?
                                    "doclet.Interface":
                                    "doclet.Class");
        String clname = classdoc.qualifiedName();
        printHtmlHeader(configuration.getText("doclet.Window_ClassUse_Header",
                            cltype, clname), null, true);
        printTop();
        navLinks(true);
        hr();
        center();
        h2();
        boldText("doclet.ClassUse_Title", cltype, clname);
        h2End();
        centerEnd();
    }
    
    /**
     * Print the footer for the class use Listing.
     */
    protected void printClassUseFooter() {
        hr();
        navLinks(false);
        printBottom();
        printBodyHtmlEnd();
    }
    
    
    /**
     * Print this package link
     */
    protected void navLinkPackage() {
        navCellStart();
        printHyperLink("../package-summary.html", "",
                       configuration.getText("doclet.Package"), true, "NavBarFont1");
        navCellEnd();
    }
    
    /**
     * Print class page indicator
     */
    protected void navLinkClass() {
        navCellStart();
        printLink(new LinkInfoImpl(LinkInfoImpl.CONTEXT_CLASS_USE_HEADER, classdoc, "", 
            configuration.getText("doclet.Class"), true, "NavBarFont1"));
        navCellEnd();
    }
    
    /**
     * Print class use link
     */
    protected void navLinkClassUse() {
        navCellRevStart();
        fontStyle("NavBarFont1Rev");
        boldText("doclet.navClassUse");
        fontEnd();
        navCellEnd();
    }
    
    protected void navLinkTree() {
        navCellStart();
        if (classdoc.containingPackage().isIncluded()) {
            printHyperLink("../package-tree.html", "", 
                configuration.getText("doclet.Tree"), true, "NavBarFont1");
        } else {
            printHyperLink(relativePath + "overview-tree.html", "",
                configuration.getText("doclet.Tree"), true, "NavBarFont1");
        }
        navCellEnd();
    }
    
}
