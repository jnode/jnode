/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.Vector;

import com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import com.sun.org.apache.xerces.internal.impl.dv.XSFacets;
import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.impl.validation.ValidationState;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import com.sun.org.apache.xerces.internal.impl.xs.XSAnnotationImpl;
import com.sun.org.apache.xerces.internal.impl.xs.XSAttributeGroupDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSAttributeUseImpl;
import com.sun.org.apache.xerces.internal.impl.xs.XSComplexTypeDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSElementDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSParticleDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;
import com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import com.sun.org.apache.xerces.internal.impl.xs.util.XSObjectListImpl;
import com.sun.org.apache.xerces.internal.util.DOMUtil;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.xni.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Class <code>XSDAbstractTraverser</code> serves as the base class for all
 * other <code>XSD???Traverser</code>s. It holds the common data and provide
 * a unified way to initialize these data.
 *
 * @xerces.internal 
 *
 * @author Elena Litani, IBM
 * @author Rahul Srivastava, Sun Microsystems Inc.
 * @author Neeraj Bajaj, Sun Microsystems Inc.
 *
 * @version $Id: XSDAbstractTraverser.java,v 1.2.6.1 2005/09/08 12:01:12 sunithareddy Exp $
 */
abstract class XSDAbstractTraverser {
    
    protected static final String NO_NAME      = "(no name)";
    
    // Flags for checkOccurrences to indicate any special
    // restrictions on minOccurs and maxOccurs relating to "all".
    //    NOT_ALL_CONTEXT    - not processing an <all>
    //    PROCESSING_ALL_EL  - processing an <element> in an <all>
    //    GROUP_REF_WITH_ALL - processing <group> reference that contained <all>
    //    CHILD_OF_GROUP     - processing a child of a model group definition
    //    PROCESSING_ALL_GP  - processing an <all> group itself
    
    protected static final int NOT_ALL_CONTEXT    = 0;
    protected static final int PROCESSING_ALL_EL  = 1;
    protected static final int GROUP_REF_WITH_ALL = 2;
    protected static final int CHILD_OF_GROUP     = 4;
    protected static final int PROCESSING_ALL_GP  = 8;
    
    //Shared data
    protected XSDHandler            fSchemaHandler = null;
    protected SymbolTable           fSymbolTable = null;
    protected XSAttributeChecker    fAttrChecker = null;
    protected boolean               fValidateAnnotations = false;
    
    // used to validate default/fixed attribute values
    ValidationState fValidationState = new ValidationState();
    
    XSDAbstractTraverser (XSDHandler handler,
            XSAttributeChecker attrChecker) {
        fSchemaHandler = handler;
        fAttrChecker = attrChecker;
    }
    
    void reset(SymbolTable symbolTable, boolean validateAnnotations) {
        fSymbolTable = symbolTable;
        fValidateAnnotations = validateAnnotations;
        fValidationState.setExtraChecking(false);
        fValidationState.setSymbolTable(symbolTable);
    }
    
    // traverse the annotation declaration
    // REVISIT: how to pass the parentAttrs? as DOM attributes?
    //          as name/value pairs (string)? in parsed form?
    // @return XSAnnotationImpl object
    XSAnnotationImpl traverseAnnotationDecl(Element annotationDecl, Object[] parentAttrs,
            boolean isGlobal, XSDocumentInfo schemaDoc) {
        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(annotationDecl, isGlobal, schemaDoc);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        
        String contents = null;
        Element child = DOMUtil.getFirstChildElement(annotationDecl);
        if (child != null) {
            do {
                String name = DOMUtil.getLocalName(child);
                
                // the only valid children of "annotation" are
                // "appinfo" and "documentation"
                if (!((name.equals(SchemaSymbols.ELT_APPINFO)) ||
                        (name.equals(SchemaSymbols.ELT_DOCUMENTATION)))) {
                    reportSchemaError("src-annotation", new Object[]{name}, child);
                } else { // the annotation, as we currently know it, is a Text child
                    Node textContent = child.getFirstChild();
                    if(textContent != null && textContent.getNodeType() == Node.TEXT_NODE) {
                        contents = ((Text)textContent).getData();
                    }
                }
                
                // General Attribute Checking
                // There is no difference between global or local appinfo/documentation,
                // so we assume it's always global.
                attrValues = fAttrChecker.checkAttributes(child, true, schemaDoc);
                fAttrChecker.returnAttrArray(attrValues, schemaDoc);
                
                child = DOMUtil.getNextSiblingElement(child);
            }
            while (child != null);
        }
        // REVISIT: When an annotation has no <documentation> or
        // <appinfo> children the text child is stored on the first child of its
        // parent. Only if the annotation is the first child will we find the
        // text node there. See SchemaDOM. We need to store the string representation
        // in a consistent place so it can be reliably retrieved, perhaps as 
        // user data. -- mrglavas
        else {
            Node textContent = annotationDecl.getFirstChild();
            if(textContent != null && textContent.getNodeType() == Node.TEXT_NODE) {
                contents = ((Text)textContent).getData();
            }
        }
        // if contents was null, must have been some kind of error;
        // nothing to contribute to PSVI
        if (contents == null) return null;
        
        // find the grammar; fSchemaHandler must be known!
        SchemaGrammar grammar = fSchemaHandler.getGrammar(schemaDoc.fTargetNamespace);
        // fish out local attributes passed from parent
        Vector annotationLocalAttrs = (Vector)parentAttrs[XSAttributeChecker.ATTIDX_NONSCHEMA];
        // optimize for case where there are no local attributes
        if(annotationLocalAttrs != null && !annotationLocalAttrs.isEmpty()) {
            StringBuffer localStrBuffer = new StringBuffer(64);
            localStrBuffer.append(" ");
            // Vector should contain rawname value pairs
            int i = 0;
            while (i < annotationLocalAttrs.size()) {
                String rawname = (String)annotationLocalAttrs.elementAt(i++);
                int colonIndex = rawname.indexOf(':');
                String prefix, localpart;
                if (colonIndex == -1) {
                    prefix = "";
                    localpart = rawname;
                }
                else {
                    prefix = rawname.substring(0,colonIndex);
                    localpart = rawname.substring(colonIndex+1);
                }
                String uri = schemaDoc.fNamespaceSupport.getURI(prefix.intern());
                if (!annotationDecl.getAttributeNS(uri, localpart).equals("")) {
                    i++; // skip the next value, too
                    continue;
                }
                localStrBuffer.append(rawname)
                .append("=\"");
                String value = (String)annotationLocalAttrs.elementAt(i++);
                // search for pesky "s and >s within attr value:
                value = processAttValue(value);
                localStrBuffer.append(value)
                .append("\" ");
            }
            // and now splice it into place; immediately after the annotation token, for simplicity's sake
            StringBuffer contentBuffer = new StringBuffer(contents.length() + localStrBuffer.length());
            int annotationTokenEnd = contents.indexOf(SchemaSymbols.ELT_ANNOTATION);
            // annotation must occur somewhere or we're in big trouble...
            if(annotationTokenEnd == -1) return null;
            annotationTokenEnd += SchemaSymbols.ELT_ANNOTATION.length();
            contentBuffer.append(contents.substring(0,annotationTokenEnd));
            contentBuffer.append(localStrBuffer.toString());
            contentBuffer.append(contents.substring(annotationTokenEnd, contents.length()));
            final String annotation = contentBuffer.toString();
            if (fValidateAnnotations) {
                schemaDoc.addAnnotation(new XSAnnotationInfo(annotation, annotationDecl));
            }
            return new XSAnnotationImpl(annotation, grammar);
        } else {
            if (fValidateAnnotations) {
                schemaDoc.addAnnotation(new XSAnnotationInfo(contents, annotationDecl));
            }
            return new XSAnnotationImpl(contents, grammar);
        }
        
    }
    
    XSAnnotationImpl traverseSyntheticAnnotation(Element annotationParent, String initialContent,
            Object[] parentAttrs, boolean isGlobal, XSDocumentInfo schemaDoc) {
        
        String contents = initialContent;
        
        // find the grammar; fSchemaHandler must be known!
        SchemaGrammar grammar = fSchemaHandler.getGrammar(schemaDoc.fTargetNamespace);
        // fish out local attributes passed from parent
        Vector annotationLocalAttrs = (Vector)parentAttrs[XSAttributeChecker.ATTIDX_NONSCHEMA];
        // optimize for case where there are no local attributes
        if (annotationLocalAttrs != null && !annotationLocalAttrs.isEmpty()) {
            StringBuffer localStrBuffer = new StringBuffer(64);
            localStrBuffer.append(" ");
            // Vector should contain rawname value pairs
            int i = 0;
            while (i < annotationLocalAttrs.size()) {
                String rawname = (String)annotationLocalAttrs.elementAt(i++);
                int colonIndex = rawname.indexOf(':');
                String prefix, localpart;
                if (colonIndex == -1) {
                    prefix = "";
                    localpart = rawname;
                }
                else {
                    prefix = rawname.substring(0,colonIndex);
                    localpart = rawname.substring(colonIndex+1);
                }
                String uri = schemaDoc.fNamespaceSupport.getURI(prefix.intern());
                localStrBuffer.append(rawname)
                .append("=\"");
                String value = (String)annotationLocalAttrs.elementAt(i++);
                // search for pesky "s and >s within attr value:
                value = processAttValue(value);
                localStrBuffer.append(value)
                .append("\" ");
            }
            // and now splice it into place; immediately after the annotation token, for simplicity's sake
            StringBuffer contentBuffer = new StringBuffer(contents.length() + localStrBuffer.length());
            int annotationTokenEnd = contents.indexOf(SchemaSymbols.ELT_ANNOTATION);
            // annotation must occur somewhere or we're in big trouble...
            if(annotationTokenEnd == -1) return null;
            annotationTokenEnd += SchemaSymbols.ELT_ANNOTATION.length();
            contentBuffer.append(contents.substring(0,annotationTokenEnd));
            contentBuffer.append(localStrBuffer.toString());
            contentBuffer.append(contents.substring(annotationTokenEnd, contents.length()));
            final String annotation = contentBuffer.toString();
            if (fValidateAnnotations) {
                schemaDoc.addAnnotation(new XSAnnotationInfo(annotation, annotationParent));
            }
            return new XSAnnotationImpl(annotation, grammar);
        } else {
            if (fValidateAnnotations) {
                schemaDoc.addAnnotation(new XSAnnotationInfo(contents, annotationParent));
            }
            return new XSAnnotationImpl(contents, grammar);
        }
    }
    
    // the QName simple type used to resolve qnames
    private static final XSSimpleType fQNameDV = (XSSimpleType)SchemaGrammar.SG_SchemaNS.getGlobalTypeDecl(SchemaSymbols.ATTVAL_QNAME);
    // Temp data structures to be re-used in traversing facets
    private StringBuffer fPattern = new StringBuffer();
    private final XSFacets xsFacets = new XSFacets();
    
    class FacetInfo {
        XSFacets facetdata;
        Element nodeAfterFacets;
        short fPresentFacets;
        short fFixedFacets;
    }
    
    FacetInfo traverseFacets(Element content,
            XSSimpleType baseValidator,
            XSDocumentInfo schemaDoc) {
        
        short facetsPresent = 0 ;
        short facetsFixed = 0; // facets that have fixed="true"        
        String facet;
        boolean hasQName = containsQName(baseValidator);
        Vector enumData = null;
        XSObjectListImpl enumAnnotations = null;
        XSObjectListImpl patternAnnotations = null;
        Vector enumNSDecls = hasQName ? new Vector() : null;       
        int currentFacet = 0;
        xsFacets.reset();
        while (content != null) {           
            // General Attribute Checking
            Object[] attrs = null;
            facet = DOMUtil.getLocalName(content);
            if (facet.equals(SchemaSymbols.ELT_ENUMERATION)) {
                attrs = fAttrChecker.checkAttributes(content, false, schemaDoc, hasQName);
                String enumVal = (String)attrs[XSAttributeChecker.ATTIDX_VALUE];
                NamespaceSupport nsDecls = (NamespaceSupport)attrs[XSAttributeChecker.ATTIDX_ENUMNSDECLS];
                
                // for NOTATION types, need to check whether there is a notation
                // declared with the same name as the enumeration value.
                if (baseValidator.getVariety() == XSSimpleType.VARIETY_ATOMIC &&
                        baseValidator.getPrimitiveKind() == XSSimpleType.PRIMITIVE_NOTATION) {
                    // need to use the namespace context returned from checkAttributes
                    schemaDoc.fValidationContext.setNamespaceSupport(nsDecls);
                    try{
                        QName temp = (QName)fQNameDV.validate(enumVal, schemaDoc.fValidationContext, null);
                        // try to get the notation decl. if failed, getGlobalDecl
                        // reports an error, so we don't need to report one again.
                        fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.NOTATION_TYPE, temp, content);
                    }catch(InvalidDatatypeValueException ex){
                        reportSchemaError(ex.getKey(), ex.getArgs(), content);
                    }
                    // restore to the normal namespace context
                    schemaDoc.fValidationContext.setNamespaceSupport(schemaDoc.fNamespaceSupport);
                }
                if (enumData == null){
                    enumData = new Vector();
                    enumAnnotations = new XSObjectListImpl();
                }
                enumData.addElement(enumVal);
                enumAnnotations.add(null);
                if (hasQName)
                    enumNSDecls.addElement(nsDecls);
                Element child = DOMUtil.getFirstChildElement( content );
                
                if (child != null) {
                    // traverse annotation if any
                    
                    if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        enumAnnotations.add(enumAnnotations.getLength()-1,traverseAnnotationDecl(child, attrs, false, schemaDoc));
                        child = DOMUtil.getNextSiblingElement(child);
                    }
                    else {
                        String text = DOMUtil.getSyntheticAnnotation(content);
                        if (text != null) {
                            enumAnnotations.add(enumAnnotations.getLength()-1, traverseSyntheticAnnotation(content, text, attrs, false, schemaDoc));
                        }
                    }
                    if (child !=null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        reportSchemaError("s4s-elt-must-match.1", new Object[]{"enumeration", "(annotation?)", DOMUtil.getLocalName(child)}, child);
                    }
                }
                else {
                    String text = DOMUtil.getSyntheticAnnotation(content);
                    if (text != null) {
                        enumAnnotations.add(enumAnnotations.getLength() - 1, traverseSyntheticAnnotation(content, text, attrs, false, schemaDoc));
                    }
                }
            }
            else if (facet.equals(SchemaSymbols.ELT_PATTERN)) {
                attrs = fAttrChecker.checkAttributes(content, false, schemaDoc);
                if (fPattern.length() == 0) {
                    fPattern.append((String)attrs[XSAttributeChecker.ATTIDX_VALUE]);
                } else {
                    // ---------------------------------------------
                    //datatypes: 5.2.4 pattern: src-multiple-pattern
                    // ---------------------------------------------
                    fPattern.append("|");
                    fPattern.append((String)attrs[XSAttributeChecker.ATTIDX_VALUE]);
                }
                Element child = DOMUtil.getFirstChildElement( content );
                if (child != null) {
                    // traverse annotation if any
                    if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        if (patternAnnotations == null){
                            patternAnnotations = new XSObjectListImpl();
                        }
                        patternAnnotations.add(traverseAnnotationDecl(child, attrs, false, schemaDoc));
                        child = DOMUtil.getNextSiblingElement(child);
                    }
                    if (child !=null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        reportSchemaError("s4s-elt-must-match.1", new Object[]{"pattern", "(annotation?)", DOMUtil.getLocalName(child)}, child);
                    }
                }
                
            }
            else {
                if (facet.equals(SchemaSymbols.ELT_MINLENGTH)) {
                    currentFacet = XSSimpleType.FACET_MINLENGTH;
                }
                else if (facet.equals(SchemaSymbols.ELT_MAXLENGTH)) {
                    currentFacet = XSSimpleType.FACET_MAXLENGTH;
                }
                else if (facet.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                    currentFacet = XSSimpleType.FACET_MAXEXCLUSIVE;
                }
                else if (facet.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                    currentFacet = XSSimpleType.FACET_MAXINCLUSIVE;
                }
                else if (facet.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                    currentFacet = XSSimpleType.FACET_MINEXCLUSIVE;
                }
                else if (facet.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                    currentFacet = XSSimpleType.FACET_MININCLUSIVE;
                }
                else if (facet.equals(SchemaSymbols.ELT_TOTALDIGITS)) {
                    currentFacet = XSSimpleType.FACET_TOTALDIGITS;
                }
                else if (facet.equals(SchemaSymbols.ELT_FRACTIONDIGITS)) {
                    currentFacet = XSSimpleType.FACET_FRACTIONDIGITS;
                }
                else if (facet.equals(SchemaSymbols.ELT_WHITESPACE)) {
                    currentFacet = XSSimpleType.FACET_WHITESPACE;
                }
                else if (facet.equals(SchemaSymbols.ELT_LENGTH)) {
                    currentFacet = XSSimpleType.FACET_LENGTH;
                }
                else {
                    break;   // a non-facet
                }
                
                attrs = fAttrChecker.checkAttributes(content, false, schemaDoc);
                
                // check for duplicate facets
                if ((facetsPresent & currentFacet) != 0) {
                    reportSchemaError("src-single-facet-value", new Object[]{facet}, content);
                } else if (attrs[XSAttributeChecker.ATTIDX_VALUE] != null) {
                    facetsPresent |= currentFacet;
                    // check for fixed facet
                    if (((Boolean)attrs[XSAttributeChecker.ATTIDX_FIXED]).booleanValue()) {
                        facetsFixed |= currentFacet;
                    }
                    switch (currentFacet) {
                    case XSSimpleType.FACET_MINLENGTH:
                        xsFacets.minLength = ((XInt)attrs[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                    break;
                    case XSSimpleType.FACET_MAXLENGTH:
                        xsFacets.maxLength = ((XInt)attrs[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                    break;
                    case XSSimpleType.FACET_MAXEXCLUSIVE:
                        xsFacets.maxExclusive = (String)attrs[XSAttributeChecker.ATTIDX_VALUE];
                    break;
                    case XSSimpleType.FACET_MAXINCLUSIVE:
                        xsFacets.maxInclusive = (String)attrs[XSAttributeChecker.ATTIDX_VALUE];
                    break;
                    case XSSimpleType.FACET_MINEXCLUSIVE:
                        xsFacets.minExclusive = (String)attrs[XSAttributeChecker.ATTIDX_VALUE];
                    break;
                    case XSSimpleType.FACET_MININCLUSIVE:
                        xsFacets.minInclusive = (String)attrs[XSAttributeChecker.ATTIDX_VALUE];
                    break;
                    case XSSimpleType.FACET_TOTALDIGITS:
                        xsFacets.totalDigits = ((XInt)attrs[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                    break;
                    case XSSimpleType.FACET_FRACTIONDIGITS:
                        xsFacets.fractionDigits = ((XInt)attrs[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                    break;
                    case XSSimpleType.FACET_WHITESPACE:
                        xsFacets.whiteSpace = ((XInt)attrs[XSAttributeChecker.ATTIDX_VALUE]).shortValue();
                    break;
                    case XSSimpleType.FACET_LENGTH:
                        xsFacets.length = ((XInt)attrs[XSAttributeChecker.ATTIDX_VALUE]).intValue();
                    break;
                    }
                }
                
                Element child = DOMUtil.getFirstChildElement( content );
                if (child != null) {
                    // traverse annotation if any
                    if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        XSAnnotationImpl annotation = traverseAnnotationDecl(child, attrs, false, schemaDoc);
                        switch (currentFacet) {
                        case XSSimpleType.FACET_MINLENGTH:
                            xsFacets.minLengthAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_MAXLENGTH:
                            xsFacets.maxLengthAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_MAXEXCLUSIVE:
                            xsFacets.maxExclusiveAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_MAXINCLUSIVE:
                            xsFacets.maxInclusiveAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_MINEXCLUSIVE:
                            xsFacets.minExclusiveAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_MININCLUSIVE:
                            xsFacets.minInclusiveAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_TOTALDIGITS:
                            xsFacets.totalDigitsAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_FRACTIONDIGITS:
                            xsFacets.fractionDigitsAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_WHITESPACE:
                            xsFacets.whiteSpaceAnnotation = annotation;
                        break;
                        case XSSimpleType.FACET_LENGTH:
                            xsFacets.lengthAnnotation = annotation;
                        break;
                        }
                        
                        
                        child = DOMUtil.getNextSiblingElement(child);
                    }
                    else {
                        String text = DOMUtil.getSyntheticAnnotation(content);
                        if (text != null) {
                            XSAnnotationImpl annotation = traverseSyntheticAnnotation(content, text, attrs, false, schemaDoc);
                            switch (currentFacet) {
                                case XSSimpleType.FACET_MINLENGTH:
                                    xsFacets.minLengthAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_MAXLENGTH:
                                    xsFacets.maxLengthAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_MAXEXCLUSIVE:
                                    xsFacets.maxExclusiveAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_MAXINCLUSIVE:
                                    xsFacets.maxInclusiveAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_MINEXCLUSIVE:
                                    xsFacets.minExclusiveAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_MININCLUSIVE:
                                    xsFacets.minInclusiveAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_TOTALDIGITS:
                                    xsFacets.totalDigitsAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_FRACTIONDIGITS:
                                    xsFacets.fractionDigitsAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_WHITESPACE:
                                    xsFacets.whiteSpaceAnnotation = annotation;
                                    break;
                                case XSSimpleType.FACET_LENGTH:
                                    xsFacets.lengthAnnotation = annotation;
                                    break;
                            }
                        }
                    }
                    if (child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                        reportSchemaError("s4s-elt-must-match.1", new Object[]{facet, "(annotation?)", DOMUtil.getLocalName(child)}, child);
                    }
                }
            }
            fAttrChecker.returnAttrArray (attrs, schemaDoc);
            content = DOMUtil.getNextSiblingElement(content);
        }
        if (enumData !=null) {
            facetsPresent |= XSSimpleType.FACET_ENUMERATION;
            xsFacets.enumeration = enumData;
            xsFacets.enumNSDecls = enumNSDecls;
            xsFacets.enumAnnotations = enumAnnotations;
        }
        if (fPattern.length() != 0) {
            facetsPresent |= XSSimpleType.FACET_PATTERN;
            xsFacets.pattern = fPattern.toString();
            xsFacets.patternAnnotations = patternAnnotations;
        }
        
        fPattern.setLength(0);
        
        FacetInfo fi = new FacetInfo();
        fi.facetdata = xsFacets;
        fi.nodeAfterFacets = content;
        fi.fPresentFacets = facetsPresent;
        fi.fFixedFacets = facetsFixed;
        return fi;
    }
    
    
    // return whether QName/NOTATION is part of the given type
    private boolean containsQName(XSSimpleType type) {
        if (type.getVariety() == XSSimpleType.VARIETY_ATOMIC) {
            short primitive = type.getPrimitiveKind();
            return (primitive == XSSimpleType.PRIMITIVE_QNAME ||
                    primitive == XSSimpleType.PRIMITIVE_NOTATION);
        }
        else if (type.getVariety() == XSSimpleType.VARIETY_LIST) {
            return containsQName((XSSimpleType)type.getItemType());
        }
        else if (type.getVariety() == XSSimpleType.VARIETY_UNION) {
            XSObjectList members = type.getMemberTypes();
            for (int i = 0; i < members.getLength(); i++) {
                if (containsQName((XSSimpleType)members.item(i)))
                    return true;
            }
        }
        return false;
    }
    
    //
    // Traverse a set of attribute and attribute group elements
    // Needed by complexType and attributeGroup traversal
    // This method will return the first non-attribute/attrgrp found
    //
    Element traverseAttrsAndAttrGrps(Element firstAttr, XSAttributeGroupDecl attrGrp,
            XSDocumentInfo schemaDoc, SchemaGrammar grammar,
            XSComplexTypeDecl enclosingCT) {
        
        Element child=null;
        XSAttributeGroupDecl tempAttrGrp = null;
        XSAttributeUseImpl tempAttrUse = null;
        String childName;
        
        for (child=firstAttr; child!=null; child=DOMUtil.getNextSiblingElement(child)) {
            childName = DOMUtil.getLocalName(child);
            if (childName.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                tempAttrUse = fSchemaHandler.fAttributeTraverser.traverseLocal(child,
                        schemaDoc,
                        grammar,
                        enclosingCT);
                if (tempAttrUse == null) break;
                if (attrGrp.getAttributeUse(tempAttrUse.fAttrDecl.getNamespace(),
                        tempAttrUse.fAttrDecl.getName())==null) {
                    String idName = attrGrp.addAttributeUse(tempAttrUse);
                    if (idName != null) {
                        String code = (enclosingCT == null) ? "ag-props-correct.3" : "ct-props-correct.5";
                        String name = (enclosingCT == null) ? attrGrp.fName : enclosingCT.getName();
                        reportSchemaError(code, new Object[]{name, tempAttrUse.fAttrDecl.getName(), idName}, child);
                    }
                }
                else {
                    // REVISIT: what if one of the attribute uses is "prohibited"
                    String code = (enclosingCT == null) ? "ag-props-correct.2" : "ct-props-correct.4";
                    String name = (enclosingCT == null) ? attrGrp.fName : enclosingCT.getName();
                    reportSchemaError(code, new Object[]{name, tempAttrUse.fAttrDecl.getName()}, child);
                }
            }
            else if (childName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                //REVISIT: do we need to save some state at this point??
                tempAttrGrp = fSchemaHandler.fAttributeGroupTraverser.traverseLocal(
                        child, schemaDoc, grammar);
                if(tempAttrGrp == null ) break;
                XSObjectList attrUseS = tempAttrGrp.getAttributeUses();
                XSAttributeUseImpl existingAttrUse = null, oneAttrUse;
                int attrCount = attrUseS.getLength();
                for (int i=0; i<attrCount; i++) {
                    oneAttrUse = (XSAttributeUseImpl)attrUseS.item(i);
                    if (existingAttrUse == attrGrp.getAttributeUse(oneAttrUse.fAttrDecl.getNamespace(),
                            oneAttrUse.fAttrDecl.getName())) {
                        String idName = attrGrp.addAttributeUse(oneAttrUse);
                        if (idName != null) {
                            String code = (enclosingCT == null) ? "ag-props-correct.3" : "ct-props-correct.5";
                            String name = (enclosingCT == null) ? attrGrp.fName : enclosingCT.getName();
                            reportSchemaError(code, new Object[]{name, oneAttrUse.fAttrDecl.getName(), idName}, child);
                        }
                    }
                    else {
                        // REVISIT: what if one of the attribute uses is "prohibited"
                        String code = (enclosingCT == null) ? "ag-props-correct.2" : "ct-props-correct.4";
                        String name = (enclosingCT == null) ? attrGrp.fName : enclosingCT.getName();
                        reportSchemaError(code, new Object[]{name, oneAttrUse.fAttrDecl.getName()}, child);
                    }
                }
                
                if (tempAttrGrp.fAttributeWC != null) {
                    if (attrGrp.fAttributeWC == null) {
                        attrGrp.fAttributeWC = tempAttrGrp.fAttributeWC;
                    }
                    // perform intersection of attribute wildcard
                    else {
                        attrGrp.fAttributeWC = attrGrp.fAttributeWC.
                        performIntersectionWith(tempAttrGrp.fAttributeWC, attrGrp.fAttributeWC.fProcessContents);
                        if (attrGrp.fAttributeWC == null) {
                            String code = (enclosingCT == null) ? "src-attribute_group.2" : "src-ct.4";
                            String name = (enclosingCT == null) ? attrGrp.fName : enclosingCT.getName();
                            reportSchemaError(code, new Object[]{name}, child);
                        }
                    }
                }
            }
            else
                break;
        } // for
        
        if (child != null) {
            childName = DOMUtil.getLocalName(child);
            if (childName.equals(SchemaSymbols.ELT_ANYATTRIBUTE)) {
                XSWildcardDecl tempAttrWC = fSchemaHandler.fWildCardTraverser.
                traverseAnyAttribute(child, schemaDoc, grammar);
                if (attrGrp.fAttributeWC == null) {
                    attrGrp.fAttributeWC = tempAttrWC;
                }
                // perform intersection of attribute wildcard
                else {
                    attrGrp.fAttributeWC = tempAttrWC.
                    performIntersectionWith(attrGrp.fAttributeWC, tempAttrWC.fProcessContents);
                    if (attrGrp.fAttributeWC == null) {
                        String code = (enclosingCT == null) ? "src-attribute_group.2" : "src-ct.4";
                        String name = (enclosingCT == null) ? attrGrp.fName : enclosingCT.getName();
                        reportSchemaError(code, new Object[]{name}, child);
                    }
                }
                child = DOMUtil.getNextSiblingElement(child);
            }
        }
        
        // Success
        return child;
        
    }
    
    void reportSchemaError (String key, Object[] args, Element ele) {
        fSchemaHandler.reportSchemaError(key, args, ele);
    }
    
    /**
     * Element/Attribute traversers call this method to check whether
     * the type is NOTATION without enumeration facet
     */
    void checkNotationType(String refName, XSTypeDefinition typeDecl, Element elem) {
        if (typeDecl.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE &&
                ((XSSimpleType)typeDecl).getVariety() == XSSimpleType.VARIETY_ATOMIC &&
                ((XSSimpleType)typeDecl).getPrimitiveKind() == XSSimpleType.PRIMITIVE_NOTATION) {
            if ((((XSSimpleType)typeDecl).getDefinedFacets() & XSSimpleType.FACET_ENUMERATION) == 0) {
                reportSchemaError("enumeration-required-notation", new Object[]{typeDecl.getName(), refName, DOMUtil.getLocalName(elem)}, elem);
            }
        }
    }
    
    // Checks constraints for minOccurs, maxOccurs
    protected XSParticleDecl checkOccurrences(XSParticleDecl particle,
            String particleName, Element parent,
            int allContextFlags,
            long defaultVals) {
        
        int min = particle.fMinOccurs;
        int max = particle.fMaxOccurs;
        boolean defaultMin = (defaultVals & (1 << XSAttributeChecker.ATTIDX_MINOCCURS)) != 0;
        boolean defaultMax = (defaultVals & (1 << XSAttributeChecker.ATTIDX_MAXOCCURS)) != 0;
        
        boolean processingAllEl = ((allContextFlags & PROCESSING_ALL_EL) != 0);
        boolean processingAllGP = ((allContextFlags & PROCESSING_ALL_GP) != 0);
        boolean groupRefWithAll = ((allContextFlags & GROUP_REF_WITH_ALL) != 0);
        boolean isGroupChild    = ((allContextFlags & CHILD_OF_GROUP) != 0);
        
        // Neither minOccurs nor maxOccurs may be specified
        // for the child of a model group definition.
        if (isGroupChild) {
            if (!defaultMin) {
                Object[] args = new Object[]{particleName, "minOccurs"};
                reportSchemaError("s4s-att-not-allowed", args, parent);
                min = 1;
            }
            if (!defaultMax) {
                Object[] args = new Object[]{particleName, "maxOccurs"};
                reportSchemaError("s4s-att-not-allowed", args, parent);
                max = 1;
            }
        }
        
        // If minOccurs=maxOccurs=0, no component is specified
        if (min == 0 && max== 0) {
            particle.fType = XSParticleDecl.PARTICLE_EMPTY;
            return null;
        }
        
        // For the elements referenced in an <all>, minOccurs attribute
        // must be zero or one, and maxOccurs attribute must be one.
        // For a complex type definition that contains an <all> or a
        // reference a <group> whose model group is an all model group,
        // minOccurs and maxOccurs must be one.
        if (processingAllEl) {
            if (max != 1) {
                reportSchemaError("cos-all-limited.2", new Object[]{new Integer(max),
                        ((XSElementDecl)particle.fValue).getName()}, parent);
                max = 1;
                if (min > 1)
                    min = 1;
            }
        }
        else if (processingAllGP || groupRefWithAll) {
            if (max != 1) {
                reportSchemaError("cos-all-limited.1.2", null, parent);
                if (min > 1)
                    min = 1;
                max = 1;
            }
        }
        
        particle.fMaxOccurs = min;
        particle.fMaxOccurs = max;
        
        return particle;
    }
    
    // this is not terribly performant!
    private static String processAttValue(String original) {
        // normally, nothing will happen
        StringBuffer newVal = new StringBuffer(original.length());
        for(int i=0; i<original.length(); i++) {
            char currChar = original.charAt(i);
            if(currChar == '"') {
                newVal.append("&quot;");
            } else if (currChar == '>') {
                newVal.append("&gt;");
            } else if (currChar == '&') {
                newVal.append("&amp;");
            } else {
                newVal.append(currChar);
            }
        }
        return newVal.toString();
    }
}
