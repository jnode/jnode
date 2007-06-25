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

package com.sun.org.apache.xerces.internal.impl.xs.opti;

import com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import com.sun.org.apache.xerces.internal.xni.XMLString;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Vector;
import java.util.Enumeration;

/**
 * @xerces.internal  
 * 
 * @author Rahul Srivastava, Sun Microsystems Inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id: SchemaDOM.java,v 1.2.6.1 2005/09/08 10:33:57 sunithareddy Exp $
 */
public class SchemaDOM extends DefaultDocument {
    
    static final int relationsRowResizeFactor = 15;
    static final int relationsColResizeFactor = 10;
    
    NodeImpl[][] relations;
    // parent must be an element in this scheme
    ElementImpl parent;
    int currLoc;
    int nextFreeLoc;
    boolean hidden;
    boolean inCDATA;
    
    // for annotation support:
    StringBuffer fAnnotationBuffer = null;
    
    public SchemaDOM() {
        reset();
    }
    
    
    public void startElement(QName element, XMLAttributes attributes,
            int line, int column, int offset) {
        ElementImpl node = new ElementImpl(line, column, offset);
        processElement(element, attributes, node);
        // now the current node added, becomes the parent
        parent = node;
    }
    
    
    public void emptyElement(QName element, XMLAttributes attributes,
            int line, int column, int offset) {
        ElementImpl node = new ElementImpl(line, column, offset);
        processElement(element, attributes, node);
    }
    
    public void startElement(QName element, XMLAttributes attributes,
            int line, int column) {
        startElement(element, attributes, line, column, -1);
    }
    
    
    public void emptyElement(QName element, XMLAttributes attributes,
            int line, int column) {
        emptyElement(element, attributes, line, column, -1);
    }
    
    
    private void processElement(QName element, XMLAttributes attributes, ElementImpl node) {
        
        // populate node
        node.prefix = element.prefix;
        node.localpart = element.localpart;
        node.rawname = element.rawname;
        node.uri = element.uri;
        node.schemaDOM = this;
        
        // set the attributes
        Attr[] attrs = new Attr[attributes.getLength()];
        for (int i=0; i<attributes.getLength(); i++) {
            attrs[i] = new AttrImpl(null, 
                    attributes.getPrefix(i), 
                    attributes.getLocalName(i), 
                    attributes.getQName(i), 
                    attributes.getURI(i), 
                    attributes.getValue(i));
        }
        node.attrs = attrs;
        
        // check if array needs to be resized
        if (nextFreeLoc == relations.length) {
            resizeRelations();
        }
        
        // store the current parent
        //if (relations[currLoc][0] == null || relations[currLoc][0] != parent) {
        if (relations[currLoc][0] != parent) {
            relations[nextFreeLoc][0] = parent;
            currLoc = nextFreeLoc++;
        }
        
        // add the current node as child of parent
        boolean foundPlace = false;
        int i = 1;
        for (i = 1; i<relations[currLoc].length; i++) {
            if (relations[currLoc][i] == null) {
                foundPlace = true;
                break;
            }
        }
        
        if (!foundPlace) {
            resizeRelations(currLoc);
        }
        relations[currLoc][i] = node;
        
        parent.parentRow = currLoc;
        node.row = currLoc;
        node.col = i;
    }
    
    
    public void endElement()  {
        // the parent of current parent node becomes the parent
        // for the next node.
        currLoc = parent.row;
        parent = (ElementImpl)relations[currLoc][0];
    }
    
    // note that this will only be called within appinfo/documentation
    void comment(XMLString text) {
        fAnnotationBuffer.append("<!--").append(text.toString()).append("-->");
    }
    
    // note that this will only be called within appinfo/documentation
    void processingInstruction(String target, String data) {
        fAnnotationBuffer.append("<?").append(target).append(" ").append(data).append("?>");
    }
    
    // note that this will only be called within appinfo/documentation
    void characters(XMLString text ) {
        
        // escape characters if necessary
        if (!inCDATA) {   
            for (int i = text.offset; i < text.offset+text.length; ++i ) {
                char ch = text.ch[i];
                if (ch == '&') {
                    fAnnotationBuffer.append("&amp;");
                } 
                else if (ch == '<') {
                    fAnnotationBuffer.append("&lt;");
                }
                // character sequence "]]>" cannot appear in content, 
                // therefore we should escape '>'.
                else if (ch == '>') {
                    fAnnotationBuffer.append("&gt;");
                }
                // If CR is part of the document's content, it
                // must not be printed as a literal otherwise
                // it would be normalized to LF when the document
                // is reparsed.
                else if (ch == '\r') {
                    fAnnotationBuffer.append("&#xD;");
                }
                else {
                    fAnnotationBuffer.append(ch);
                }
            }
        }
        else {
            fAnnotationBuffer.append(text.ch, text.offset, text.length);
        }
    }
    
    void endAnnotationElement(QName elemName, boolean complete) {
        if(complete) {
            fAnnotationBuffer.append("\n</").append(elemName.rawname).append(">");
            // note that this is always called after endElement on <annotation>'s
            // child and before endElement on annotation.
            // hence, we must make this the child of the current
            // parent's only child.
            ElementImpl child = (ElementImpl)relations[currLoc][1];
            
            // check if array needs to be resized
            if (nextFreeLoc == relations.length) {
                resizeRelations();
            }
            int newRow = child.parentRow = nextFreeLoc++; 
            
            // now find the place to insert this node
            boolean foundPlace = false;
            int i = 1;
            for (; i<relations[newRow].length; i++) {
                if (relations[newRow][i] == null) {
                    foundPlace = true;
                    break;
                }
            }
            
            if (!foundPlace) {
                resizeRelations(newRow);
            }
            relations[newRow][i] = new TextImpl(fAnnotationBuffer, this, newRow, i);
            // apparently, there is no sensible way of resetting
            // these things
            fAnnotationBuffer = null;
        } else      //capturing character calls
            fAnnotationBuffer.append("</").append(elemName.rawname).append(">");
    }
    
    void endSyntheticAnnotationElement(QName elemName, boolean complete) {
        if(complete) {
            fAnnotationBuffer.append("\n</").append(elemName.rawname).append(">");
            // note that this is always called after endElement on <annotation>'s
            // child and before endElement on annotation.
            // hence, we must make this the child of the current
            // parent's only child.
            parent.fSyntheticAnnotation = fAnnotationBuffer.toString();
            
            // apparently, there is no sensible way of resetting
            // these things
            fAnnotationBuffer = null;
        } else      //capturing character calls
            fAnnotationBuffer.append("</").append(elemName.rawname).append(">");
    }
    
    void startAnnotationCDATA() {
        inCDATA = true;
        fAnnotationBuffer.append("<![CDATA[");
    }
    
    void endAnnotationCDATA() {
        fAnnotationBuffer.append("]]>");
        inCDATA = false;
    }
    
    private void resizeRelations() {
        NodeImpl[][] temp = new NodeImpl[relations.length+relationsRowResizeFactor][];
        System.arraycopy(relations, 0, temp, 0, relations.length);
        for (int i = relations.length ; i < temp.length ; i++) {
            temp[i] = new NodeImpl[relationsColResizeFactor];
        }
        relations = temp;
    }
    
    private void resizeRelations(int i) {
        NodeImpl[] temp = new NodeImpl[relations[i].length+relationsColResizeFactor];
        System.arraycopy(relations[i], 0, temp, 0, relations[i].length);
        relations[i] = temp;
    }
    
    
    public void reset() {
        
        // help out the garbage collector
        if(relations != null) 
            for(int i=0; i<relations.length; i++) 
                for(int j=0; j<relations[i].length; j++) 
                    relations[i][j] = null;
        relations = new NodeImpl[relationsRowResizeFactor][];
        parent = new ElementImpl(0, 0, 0);
        parent.rawname = "DOCUMENT_NODE";
        currLoc = 0;
        nextFreeLoc = 1;
        inCDATA = false;
        for (int i=0; i<relationsRowResizeFactor; i++) {
            relations[i] = new NodeImpl[relationsColResizeFactor];
        }
        relations[currLoc][0] = parent;
    }
    
    
    public void printDOM() {
        /*
         for (int i=0; i<relations.length; i++) {
         if (relations[i][0] != null) {
         for (int j=0; j<relations[i].length; j++) {
         if (relations[i][j] != null) {
         System.out.print(relations[i][j].nodeType+"-"+relations[i][j].parentRow+"  ");
         }
         }
         System.out.println("");
         }
         }
         */
        //traverse(getDocumentElement(), 0);
    }
    
    
    // debug methods
    
    public static void traverse(Node node, int depth) {
        indent(depth);
        System.out.print("<"+node.getNodeName());
        
        if (node.hasAttributes()) {
            NamedNodeMap attrs = node.getAttributes();
            for (int i=0; i<attrs.getLength(); i++) {
                System.out.print("  "+((Attr)attrs.item(i)).getName()+"=\""+((Attr)attrs.item(i)).getValue()+"\"");
            }
        }
        
        if (node.hasChildNodes()) {
            System.out.println(">");
            depth+=4;
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                traverse(child, depth);
            }
            depth-=4;
            indent(depth);
            System.out.println("</"+node.getNodeName()+">");
        }
        else {
            System.out.println("/>");
        }
    }
    
    public static void indent(int amount) {
        for (int i = 0; i < amount; i++) {
            System.out.print(' ');
        }
    }
    
    // org.w3c.dom methods
    public Element getDocumentElement() {
        // this returns a parent node, known to be an ElementImpl
        return (ElementImpl)relations[0][1];
    }
    
    // commence the serialization of an annotation
    void startAnnotation(QName elemName, XMLAttributes attributes,
            NamespaceContext namespaceContext) {
        if(fAnnotationBuffer == null) fAnnotationBuffer = new StringBuffer(256);
        fAnnotationBuffer.append("<").append(elemName.rawname).append(" ");
        
        // attributes are a bit of a pain.  To get this right, we have to keep track
        // of the namespaces we've seen declared, then examine the namespace context
        // for other namespaces so that we can also include them.
        // optimized for simplicity and the case that not many
        // namespaces are declared on this annotation...
        Vector namespaces = new Vector();
        for (int i = 0; i < attributes.getLength(); ++i) {
            String aValue = attributes.getValue(i);
            String aPrefix = attributes.getPrefix(i);
            String aQName = attributes.getQName(i);
            // if it's xmlns:* or xmlns, must be a namespace decl
            if (aPrefix == XMLSymbols.PREFIX_XMLNS || aQName == XMLSymbols.PREFIX_XMLNS) {
                namespaces.addElement(aPrefix == XMLSymbols.PREFIX_XMLNS ? 
                        attributes.getLocalName(i) : XMLSymbols.EMPTY_STRING);
            }
            fAnnotationBuffer.append(aQName).append("=\"").append(processAttValue(aValue)).append("\" ");
        }
        // now we have to look through currently in-scope namespaces to see what
        // wasn't declared here
        Enumeration currPrefixes = namespaceContext.getAllPrefixes();
        while(currPrefixes.hasMoreElements()) {
            String prefix = (String)currPrefixes.nextElement();
            String uri = namespaceContext.getURI(prefix);
            if (uri == null) {
                uri = XMLSymbols.EMPTY_STRING;
            }
            if (!namespaces.contains(prefix)) {
                // have to declare this one
                if(prefix == XMLSymbols.EMPTY_STRING) {
                    fAnnotationBuffer.append("xmlns").append("=\"").append(processAttValue(uri)).append("\" ");
                }
                else {
                    fAnnotationBuffer.append("xmlns:").append(prefix).append("=\"").append(processAttValue(uri)).append("\" ");
                }
            }
        }
        fAnnotationBuffer.append(">\n");
    }
    void startAnnotationElement(QName elemName, XMLAttributes attributes) {
        fAnnotationBuffer.append("<").append(elemName.rawname);
        for(int i=0; i<attributes.getLength(); i++) {
            String aValue = attributes.getValue(i);
            fAnnotationBuffer.append(" ").append(attributes.getQName(i)).append("=\"").append(processAttValue(aValue)).append("\"");
        }
        fAnnotationBuffer.append(">");
    }
    
    private static String processAttValue(String original) {
        final int length = original.length();
        // normally, nothing will happen
        for (int i = 0; i < length; ++i) {
            char currChar = original.charAt(i);
            if (currChar == '"' || currChar == '<' || currChar == '&' ||
                    currChar == 0x09 || currChar == 0x0A || currChar == 0x0D) {
                return escapeAttValue(original, i);
            }
        }
        return original;
    }
    
    private static String escapeAttValue(String original, int from) {
        int i;
        final int length = original.length();
        StringBuffer newVal = new StringBuffer(length);
        newVal.append(original.substring(0, from));
        for (i = from; i < length; ++i) {
            char currChar = original.charAt(i);
            if (currChar == '"') {
                newVal.append("&quot;");
            } 
            else if (currChar == '<') {
                newVal.append("&lt;");
            }
            else if (currChar == '&') {
                newVal.append("&amp;");
            }
            // Must escape 0x09, 0x0A and 0x0D if they appear in attribute
            // value so that they may be round-tripped. They would otherwise
            // be transformed to a 0x20 during attribute value normalization.
            else if (currChar == 0x09) {
                newVal.append("&#x9;");
            }
            else if (currChar == 0x0A) {
                newVal.append("&#xA;");
            }
            else if (currChar == 0x0D) {
                newVal.append("&#xD;");
            }
            else {
                newVal.append(currChar);
            }
        }
        return newVal.toString();
    }
}
