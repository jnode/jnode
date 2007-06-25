/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.org.apache.xerces.internal.impl;

import com.sun.xml.internal.stream.events.AttributeImpl;
import com.sun.xml.internal.stream.events.NamespaceImpl;
import java.util.ArrayList;
import javax.xml.XMLConstants;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import javax.xml.stream.events.XMLEvent;
import com.sun.org.apache.xerces.internal.util.NamespaceContextWrapper;
import com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import com.sun.org.apache.xerces.internal.util.XMLChar;


/**
 *
 * @author  K.Venugopal@sun.com
 */

public class XMLStreamFilterImpl implements javax.xml.stream.XMLStreamReader {
    
    private StreamFilter fStreamFilter = null;
    private XMLStreamReader fStreamReader = null;
    private int fCurrentEventType = -1;
    private QName fElementName = null;
    private String fLocalName = null;
    private boolean fHasName = false;
    private boolean fReadNext = true;
    private boolean fHasMoreEvents = true;
    
    private boolean fReadFromCache = true;
    private ArrayList fCachedAttributes = null;
    private ArrayList fCachedNamespaceAttr = null;
    private NamespaceContextWrapper fCachedNamespaceContext = null;
    private String fCachedElementText = null;
    private int fCachedEventType = -1;
    private String fCachedVersion = null;
    private String fCachedEncoding = null;
    private boolean fCachedStandalone = false;
    private Location fCachedLocation = null;
    private String fCachedTextValue = null;
    private String fCachedPITarget = null;
    private String fCachedPIData = null;
    private String fCachedCharEncoding = null;
    private static boolean DEBUG = false;
    
    /** Creates a new instance of XMLStreamFilterImpl */
    
    public XMLStreamFilterImpl(XMLStreamReader reader,StreamFilter filter){
        this.fStreamReader = reader;
        this.fStreamFilter = filter;
        fCachedAttributes = new ArrayList();
        fCachedNamespaceAttr = new ArrayList();
        try{
            if(!fStreamFilter.accept(fStreamReader)){
                next();
                cache();
            }
            
        }catch(XMLStreamException xs){
            System.err.println("Error while creating a stream Filter"+xs);
        }
        //fCachedEventType = fStreamReader.getEventType();
        fCurrentEventType = fStreamReader.getEventType();
        if(DEBUG)
            System.out.println("Cached Event"+fCachedEventType);
    }
    
    /**
     *
     * @param sf
     */
    protected void setStreamFilter(StreamFilter sf){
        this.fStreamFilter = sf;
    }
    
    /**
     *
     * @throws XMLStreamException
     * @return
     */
    public boolean hasNext() throws XMLStreamException {
        if(fReadNext){
            fReadNext = false;
            cache();
            if(DEBUG)
                System.out.println("Cached Event in hasNext"+fCachedEventType);
            return readNext();
            
        }
        return fHasMoreEvents;
    }
    
    /**
     *
     * @throws XMLStreamException
     */
    public void close() throws XMLStreamException {
        this.fStreamReader.close();
    }
    
    /**
     *
     * @return
     */
    public int getAttributeCount() {
        if(!fReadFromCache){
            return this.fStreamReader.getAttributeCount();
        }else{
            return fCachedAttributes.size();
        }
    }
    
    /**
     *
     * @param index
     * @return
     */
    public QName getAttributeName(int index) {
        if(!fReadFromCache){
            return this.fStreamReader.getAttributeName(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr != null)
                return attr.getName();
        }
        return null;
    }
    
    /**
     *
     * @param index
     * @return
     */
    public String getAttributeNamespace(int index) {
        if(!fReadFromCache){
            return fStreamReader.getAttributeNamespace(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr != null)
                return attr.getName().getNamespaceURI();
        }
        return null;
    }
    
    /**
     *
     * @param index
     * @return
     */
    public String getAttributePrefix(int index) {
        if(!fReadFromCache){
            return fStreamReader.getAttributePrefix(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr != null)
                return attr.getName().getPrefix();
        }
        return null;
    }
    
    /**
     *
     * @param index
     * @return
     */
    public String getAttributeType(int index) {
        if(!fReadFromCache){
            return fStreamReader.getAttributeType(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr != null)
                return attr.getDTDType();
            
        }
        return null;
    }
    
    /**
     *
     * @return
     * @param index
     */
    public String getAttributeValue(int index) {
        if(!fReadFromCache){
            return fStreamReader.getAttributeValue(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr != null)
                return attr.getValue();
            
        }
        return null;
    }
    
    /**
     *
     * @param namespaceURI
     * @param localName
     * @return
     */
    public String getAttributeValue(String namespaceURI, String localName) {
        if(!fReadFromCache){
            return fStreamReader.getAttributeValue(namespaceURI,localName);
        }else{
            if( fCachedEventType != XMLEvent.START_ELEMENT || fCachedEventType != XMLEvent.ATTRIBUTE)
                throw new IllegalStateException("Current event state is " + fCachedEventType );
            for(int i=0; i< fCachedAttributes.size();i++){
                AttributeImpl attr = (AttributeImpl)fCachedAttributes.get(i);
                if(attr != null && (attr.getName().getLocalPart().equals(localName)) &&
                (namespaceURI.equals(attr.getName().getNamespaceURI())))
                    return attr.getValue();
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getCharacterEncodingScheme() {
        if(!fReadFromCache){
            return fStreamReader.getCharacterEncodingScheme();
        }else{
            return fCachedCharEncoding;
        }
    }
    
    /**
     *
     * @throws XMLStreamException
     * @return
     */
    public String getElementText() throws XMLStreamException {
        if(!fReadFromCache){
            return fStreamReader.getElementText();
        }else{
            if(fCachedEventType != XMLEvent.START_ELEMENT) {
                throw new XMLStreamException(
                "parser must be on START_ELEMENT to read next text", getLocation());
            }
            return fCachedElementText;
        }
    }
    
    /**
     *
     * @return
     */
    public String getEncoding() {
        if(!fReadFromCache){
            return this.fStreamReader.getEncoding();
        }else{
            return fCachedEncoding;
        }
    }
    
    /**
     *
     * @return
     */
    public int getEventType() {
        if(!fReadFromCache){
            return fStreamReader.getEventType();
        }else {
            return fCachedEventType;
        }
    }
    
    /**
     *
     * @return
     */
    public String getLocalName() {
        if(!fReadFromCache){
            return fStreamReader.getLocalName();
        }else{
            return fLocalName;
        }
    }
    
    /**
     *
     * @return
     */
    public javax.xml.stream.Location getLocation() {
        if(!fReadFromCache){
            return fStreamReader.getLocation();
        }else{
            return fCachedLocation;
        }
    }
    
    /**
     *
     * @return
     */
    public javax.xml.namespace.QName getName() {
        if(!fReadFromCache){
            return fStreamReader.getName();
        }else{
            if(fCachedEventType == XMLEvent.START_ELEMENT || fCachedEventType == XMLEvent.END_ELEMENT){
                return fElementName;
            }
            else{
                throw new java.lang.IllegalArgumentException("Illegal to call getName() "+
                "when event type is "+fCachedEventType);
            }
        }
    }
    
    /**
     *
     * @return
     */
    public javax.xml.namespace.NamespaceContext getNamespaceContext() {
        if(!fReadFromCache){
            return fStreamReader.getNamespaceContext();
        }else{
            return fCachedNamespaceContext;
        }
    }
    
    /**
     *
     * @return
     */
    public int getNamespaceCount() {
        if(!fReadFromCache){
            return fStreamReader.getNamespaceCount();
        }else{
            if(fCachedEventType == XMLEvent.START_ELEMENT || fCachedEventType == XMLEvent.END_ELEMENT || fCachedEventType == XMLEvent.NAMESPACE){
                return fCachedNamespaceAttr.size();
            }else{
                throw new IllegalStateException("Current event state is " + fCachedEventType );
            }
        }
    }
    
    /**
     *
     * @param index
     * @return
     */
    public String getNamespacePrefix(int index) {
        if(!fReadFromCache){
            return fStreamReader.getNamespacePrefix(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr != null){
                return attr.getName().getPrefix();
            }
        }
        return null;
    }
    
    /**
     *
     * @return
     */
    public String getNamespaceURI() {
        if(!fReadFromCache){
            return fStreamReader.getNamespaceURI();
        }else{
            if((fCachedEventType == XMLEvent.START_ELEMENT || fCachedEventType == XMLEvent.END_ELEMENT) && (fElementName != null)) {
                return fElementName.getNamespaceURI();
            }
        }
        return null;
    }
    
    /**
     *
     * @param index
     * @return
     */
    public String getNamespaceURI(int index) {
        if(!fReadFromCache){
            return this.fStreamReader.getNamespaceURI(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr != null){
                return attr.getName().getNamespaceURI();
            }
        }
        return null;
    }
    
    /**
     *
     * @param prefix
     * @return
     */
    public String getNamespaceURI(String prefix) {
        if(!fReadFromCache){
            return this.fStreamReader.getNamespaceURI();
        }else{
            return fCachedNamespaceContext.getNamespaceURI(prefix);
        }
    }
    
    /**
     *
     * @return
     */
    public String getPIData() {
        if(!fReadFromCache){
            return this.fStreamReader.getPIData();
        }else{
            return fCachedPIData;
        }
    }
    
    /**
     *
     * @return
     */
    public String getPITarget() {
        if(!fReadFromCache){
            return this.fStreamReader.getPITarget();
        }else{
            return fCachedPITarget;
        }
    }
    
    /**
     *
     * @return
     */
    public String getPrefix() {
        if(!fReadFromCache){
            return this.fStreamReader.getPrefix();
        }else{
            if(fCachedEventType == XMLEvent.START_ELEMENT || fCachedEventType == XMLEvent.END_ELEMENT){
                return fElementName.getPrefix();
            }
        }
        return null;
    }
    
    /**
     *
     * @param name
     * @throws IllegalArgumentException
     * @return
     */
    public Object getProperty(java.lang.String name) throws java.lang.IllegalArgumentException {
        return this.fStreamReader.getProperty(name);
    }
    
    /**
     *
     * @return
     */
    public String getText() {
        if(!fReadFromCache){
            return fStreamReader.getText();
        }else{
            return fCachedTextValue;
        }
    }
    
    /**
     *
     * @return
     */
    public char[] getTextCharacters() {
        if(!fReadFromCache){
            return fStreamReader.getTextCharacters();
        }else{
            if(fCachedTextValue != null)
                return fCachedTextValue.toCharArray();
        }
        return null;
    }
    
    /**
     *
     * @param sourceStart
     * @param target
     * @param targetStart
     * @param length
     * @throws XMLStreamException
     * @return
     */
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        if(!fReadFromCache){
            return this.fStreamReader.getTextCharacters(sourceStart, target,targetStart,length);
        }else{
            if(target == null){
                throw new NullPointerException("target char array can't be null") ;
            }
            
            if(targetStart < 0 || length < 0 || sourceStart < 0 || targetStart >= target.length ||
            (targetStart + length ) > target.length) {
                throw new IndexOutOfBoundsException();
            }
            
            //getTextStart() + sourceStart should not be greater than the lenght of number of characters
            //present
            if(fCachedTextValue == null)
                return 0;
            int copiedLength = 0;
            //int presentDataLen = getTextLength() - (getTextStart()+sourceStart);
            int available = fCachedTextValue.length() - sourceStart;
            if(available < 0){
                throw new IndexOutOfBoundsException("sourceStart is greater than" +
                "number of characters associated with this event");
            }
            if(available < length){
                copiedLength = available;
            }
            else{
                copiedLength = length;
            }
            
            System.arraycopy(fCachedTextValue,  sourceStart , target, targetStart, copiedLength);
            return copiedLength;
            
        }
    }
    
    /**
     *
     * @return
     */
    public int getTextLength() {
        if(!fReadFromCache){
            return this.fStreamReader.getTextLength();
        }else{
            if(fCachedTextValue != null)
                return fCachedTextValue.length();
        }
        return 0;
    }
    
    /**
     *
     * @return
     */
    public int getTextStart() {
        if(!fReadFromCache){
            return this.fStreamReader.getTextStart();
        }else{
            return 0;
        }
    }
    
    /**
     *
     * @return
     */
    public String getVersion() {
        if(!fReadFromCache){
            return fStreamReader.getVersion();
        }else{
            return fCachedVersion;
        }
        
    }
    
    /**
     *
     * @return
     */
    public boolean hasName() {
        if(!fReadFromCache){
            return this.fStreamReader.hasName();
        }else{
            if(fCachedEventType == XMLEvent.START_ELEMENT || fCachedEventType == XMLEvent.END_ELEMENT
            || fCachedEventType == XMLEvent.ENTITY_REFERENCE || fCachedEventType == XMLEvent.PROCESSING_INSTRUCTION) {
                return true;
            }
        }
        return false;
    }
    
    /**
     *
     * @return
     */
    public boolean hasText() {
        if(!fReadFromCache){
            return this.fStreamReader.hasText();
        }else{
            if(fCachedTextValue != null)
                return true;
        }
        return false;
    }
    
    /**
     *
     * @return
     * @param index
     */
    public boolean isAttributeSpecified(int index) {
        if(!fReadFromCache){
            return this.fStreamReader.isAttributeSpecified(index);
        }else{
            AttributeImpl attr =getCachedAttribute(index);
            if(attr!=null)
                return attr.isSpecified();
        }
        return false;
    }
    
    /**
     *
     * @return
     */
    public boolean isCharacters() {
        if(!fReadFromCache){
            return this.fStreamReader.isCharacters();
        }else{
            return fCachedEventType == XMLEvent.CHARACTERS ;
        }
    }
    
    /**
     *
     * @return
     */
    public boolean isEndElement() {
        if(!fReadFromCache){
            return this.fStreamReader.isEndElement();
        }else{
            return  fCachedEventType == XMLEvent.END_ELEMENT;
        }
    }
    
    /**
     *
     * @return
     */
    public boolean isStandalone() {
        if(!fReadFromCache){
            return this.fStreamReader.isStandalone();
        }else{
            return fCachedStandalone;
        }
    }
    
    /**
     *
     * @return
     */
    public boolean isStartElement() {
        if(!fReadFromCache){
            return this.fStreamReader.isStartElement();
        }else{
            return fCachedEventType == XMLEvent.START_ELEMENT;
        }
    }
    
    /**
     *
     * @return
     */
    public boolean isWhiteSpace() {
        if(!fReadFromCache){
            return this.fStreamReader.isWhiteSpace();
        }else{
            if(isCharacters() || (fCachedEventType == XMLEvent.CDATA)){
                if(fCachedTextValue == null)
                    return false;
                char [] ch = fCachedTextValue.toCharArray();
                int start = 0;
                int length = fCachedTextValue.length();
                for (int i=start; i< length;i++){
                    if(!XMLChar.isSpace(ch[i])){
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }
    
    /**
     *
     * @return
     * @throws XMLStreamException
     */
    public int next() throws XMLStreamException {
        if(fReadNext){
            if(readNext()){
                fReadFromCache = false;
            }
        }else{
            fReadNext = true;
            fReadFromCache = false;
            //  return fCachedEventType;
        }
        return fCurrentEventType;
    }
    
    /**
     *
     * @throws XMLStreamException
     * @return
     */
    public int nextTag() throws XMLStreamException {
        if(fReadNext){
            if(readNextTag()){
                fReadFromCache = false;
            }
        }else{
            fReadNext = true;
            if( (fCurrentEventType != XMLEvent.START_ELEMENT) || (fCurrentEventType != XMLEvent.END_ELEMENT) ){
                fCurrentEventType = fStreamReader.nextTag();
                fReadFromCache = false;
            }
        }
        return fCurrentEventType;
    }
    
    /**
     *
     * @param type
     * @param namespaceURI
     * @param localName
     * @throws XMLStreamException
     */
    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        if(!fReadFromCache){
            fStreamReader.require(type,namespaceURI,localName);
        }else{
            if( type != fCachedEventType)
                throw new XMLStreamException("Event type " +XMLStreamReaderImpl.getEventTypeString(type)+" specified did not match with current parser event");
            if( namespaceURI != null && !namespaceURI.equals(getNamespaceURI()) )
                throw new XMLStreamException("Namespace URI " +namespaceURI+" specified did not match with current namespace URI");
            if(localName != null && !localName.equals(getLocalName()))
                throw new XMLStreamException("LocalName " +localName+" specified did not match with current local name");
            
        }
    }
    
    /**
     *
     * @return
     */
    public boolean standaloneSet() {
        if(!fReadFromCache){
            return fStreamReader.standaloneSet();
        }else{
            return fCachedStandalone;
        }
    }
    
    /**
     *
     * @param index
     * @return
     */
    public String getAttributeLocalName(int index){
        if(!fReadFromCache){
            return fStreamReader.getAttributeLocalName(index);
        }else{
            AttributeImpl attr = getCachedAttribute(index);
            if(attr!= null){
                attr.getName().getLocalPart();
            }
        }
        
        return null;
    }
    
    private void cache(){
        fReadFromCache = true;
        fCachedEventType = fCurrentEventType;
        clearCache();
		fCachedLocation = fStreamReader.getLocation();
        switch(fCurrentEventType){
            case XMLEvent.CHARACTERS :
            case XMLEvent.CDATA:
            case XMLEvent.SPACE:
            case XMLEvent.COMMENT:{
                fCachedTextValue = fStreamReader.getText();
                break;
            }
            case XMLEvent.DTD:{
                fCachedTextValue = fStreamReader.getText();
                break;
            }
            case XMLEvent.END_DOCUMENT:{
                break;
            }
            case XMLEvent.END_ELEMENT:{
                fElementName = fStreamReader.getName();
                fHasName = fStreamReader.hasName();
                fLocalName = fElementName.getLocalPart();
                cacheNamespaceContext();
                break;
            }
            case XMLEvent.ENTITY_DECLARATION:{
                break;
            }
            case XMLEvent.NOTATION_DECLARATION:{
                break;
            }
            case XMLEvent.ENTITY_REFERENCE:{
                fLocalName = fStreamReader.getLocalName();
                fCachedTextValue = fStreamReader.getText();
                break;
            }
            case XMLEvent.PROCESSING_INSTRUCTION:{
                fCachedPIData = fStreamReader.getPIData();
                fCachedPITarget = fStreamReader.getPITarget();
                break;
            }
            case XMLEvent.START_DOCUMENT:{
                fCachedVersion = fStreamReader.getVersion();
                fCachedEncoding = fStreamReader.getEncoding();
                fCachedStandalone = fStreamReader.isStandalone();
                fCachedCharEncoding = fStreamReader.getCharacterEncodingScheme();
                break;
            }
            case XMLEvent.START_ELEMENT:{
                try{
                    fElementName = fStreamReader.getName();
                    fHasName = fStreamReader.hasName();
                    fLocalName = fElementName.getLocalPart();
                    if(DEBUG){
                        System.out.println("Name is "+fLocalName);
                        System.out.println("Name is "+fElementName);
                    }
                    cacheAttributes();
                    cacheNamespaceAttributes();
                    cacheNamespaceContext();
                    if(fStreamReader.hasText())
                        fCachedElementText = fStreamReader.getElementText();
                }catch(Exception ex){
                    System.err.println("Error occurred while trying to cache START_ELEMENT"+ex.getMessage());
                }
                break;
            }
        }
    }
    
    private boolean readNext() throws XMLStreamException{
        while(fStreamReader.hasNext()){
            this.fStreamReader.next();
            fHasMoreEvents = fStreamFilter.accept(fStreamReader);
            if(fHasMoreEvents){
                fCurrentEventType = this.fStreamReader.getEventType();
                return true;
            }
        }

        fHasMoreEvents = false;
        return false;
    }
    
    private boolean readNextTag() throws XMLStreamException {
        while(fStreamReader.hasNext()){
            this.fStreamReader.nextTag();
            fHasMoreEvents = fStreamFilter.accept(fStreamReader);
            if(fHasMoreEvents){
                fCurrentEventType = this.fStreamReader.getEventType();
                return true;
            }
        }

        fHasMoreEvents = false;
        return false;
    }
    
    private void cacheAttributes(){
        int len = fStreamReader.getAttributeCount();
        QName qname = null;
        String prefix = null;
        String localpart = null;
        AttributeImpl attr = null;
        fCachedAttributes.clear();
        for(int i=0; i<len ;i++){
            qname = fStreamReader.getAttributeName(i);
            prefix = qname.getPrefix();
            localpart = qname.getLocalPart();
            attr = new AttributeImpl();
            attr.setName(qname);
            attr.setAttributeType(fStreamReader.getAttributeType(i));
            attr.setSpecified(fStreamReader.isAttributeSpecified(i));
            attr.setValue(fStreamReader.getAttributeValue(i));
            fCachedAttributes.add(attr);
        }
    }
    
    protected void cacheNamespaceAttributes(){
        int count = fStreamReader.getNamespaceCount();
        String uri = null;
        String prefix = null;
        NamespaceImpl attr = null;
        fCachedNamespaceAttr.clear();
        for(int i=0;i< count;i++){
            uri = fStreamReader.getNamespaceURI(i);
            prefix = fStreamReader.getNamespacePrefix(i);
            if(prefix == null){
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
            }
            attr = new NamespaceImpl(prefix,uri);
            fCachedNamespaceAttr.add(attr);
        }
    }
    
    private void cacheNamespaceContext(){
        NamespaceContextWrapper nc = (NamespaceContextWrapper) fStreamReader.getNamespaceContext();
        NamespaceSupport ns =  new NamespaceSupport(nc.getNamespaceContext());
        fCachedNamespaceContext = new NamespaceContextWrapper(ns);
    }
    
    private AttributeImpl getCachedAttribute(int index){
        if( fCachedEventType == XMLEvent.START_ELEMENT || fCachedEventType == XMLEvent.ATTRIBUTE) {
            if(index < fCachedAttributes.size()){
                return (AttributeImpl)fCachedAttributes.get(index);
            }
        }else{
            throw new IllegalStateException("Current event state is " + fCachedEventType );
        }
        return null;
    }
    
    private void clearCache(){
        fCachedAttributes.clear();
        fCachedNamespaceAttr.clear();
        fCachedNamespaceContext = null;
        fCachedElementText = null;
        fCachedVersion = null;
        fCachedEncoding = null;
        fCachedLocation = null;
        fCachedTextValue = null;
        fCachedPITarget = null;
        fCachedPIData = null;
        fCachedCharEncoding = null;
        fElementName = null;
        fHasName = false;
        fLocalName = null;
    }
}
