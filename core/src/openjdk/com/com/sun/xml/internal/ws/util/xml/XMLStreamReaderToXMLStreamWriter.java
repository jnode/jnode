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

package com.sun.xml.internal.ws.util.xml;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.XMLConstants;

/**
 * Reads a sub-tree from {@link XMLStreamReader} and writes to {@link XMLStreamWriter}
 * as-is.
 *
 * <p>
 * This class can be sub-classed to implement a simple transformation logic.
 *
 * @author Kohsuke Kawaguchi
 * @author Ryan Shoemaker
 */
public class XMLStreamReaderToXMLStreamWriter {

    protected XMLStreamReader in;
    protected XMLStreamWriter out;

    /**
     * Reads one subtree and writes it out.
     *
     * <p>
     * The {@link XMLStreamWriter} never receives a start/end document event.
     * Those need to be written separately by the caller.
     */
    public void bridge(XMLStreamReader in, XMLStreamWriter out) throws XMLStreamException {
        assert in!=null && out!=null;
        this.in = in;
        this.out = out;

        // remembers the nest level of elements to know when we are done.
        int depth=0;

        // if the parser is at the start tag, proceed to the first element
        int event = in.getEventType();
        if(event == XMLStreamConstants.START_DOCUMENT) {
            // nextTag doesn't correctly handle DTDs
            while( !in.isStartElement() ) {
                event = in.next();
                if (event == XMLStreamConstants.COMMENT)
                    handleComment();
            }
        }


        if( event!=XMLStreamConstants.START_ELEMENT)
            throw new IllegalStateException("The current event is not START_ELEMENT\n but " + event);

        do {
            // These are all of the events listed in the javadoc for
            // XMLEvent.
            // The spec only really describes 11 of them.
            switch (event) {
                case XMLStreamConstants.START_ELEMENT :
                    depth++;
                    handleStartElement();
                    break;
                case XMLStreamConstants.END_ELEMENT :
                    handleEndElement();
                    depth--;
                    if(depth==0)
                        return;
                    break;
                case XMLStreamConstants.CHARACTERS :
                    handleCharacters();
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE :
                    handleEntityReference();
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION :
                    handlePI();
                    break;
                case XMLStreamConstants.COMMENT :
                    handleComment();
                    break;
                case XMLStreamConstants.DTD :
                    handleDTD();
                    break;
                case XMLStreamConstants.CDATA :
                    handleCDATA();
                    break;
                case XMLStreamConstants.SPACE :
                    handleSpace();
                    break;
                default :
                    throw new InternalError("processing event: " + event);
            }

            event=in.next();
        } while (depth!=0);
    }

    protected void handlePI() throws XMLStreamException {
        out.writeProcessingInstruction(
            in.getPITarget(),
            in.getPIData());
    }

    protected void handleCharacters() throws XMLStreamException {
        out.writeCharacters(
            in.getTextCharacters(),
            in.getTextStart(),
            in.getTextLength() );
    }

    protected void handleEndElement() throws XMLStreamException {
        out.writeEndElement();
    }

    protected void handleStartElement() throws XMLStreamException {
        String nsUri = in.getNamespaceURI();
        if(nsUri==null)
            out.writeStartElement(in.getLocalName());
        else
            out.writeStartElement(
                fixNull(in.getPrefix()),
                in.getLocalName(),
                nsUri
            );

        // start namespace bindings
        int nsCount = in.getNamespaceCount();
        for (int i = 0; i < nsCount; i++) {
            out.writeNamespace(
                in.getNamespacePrefix(i),
                fixNull(in.getNamespaceURI(i)));    // zephyr doesn't like null, I don't know what is correct, so just fix null to "" for now
        }

        // write attributes
        int attCount = in.getAttributeCount();
        for (int i = 0; i < attCount; i++) {
            handleAttribute(i);
        }
    }

    /**
     * Writes out the {@code i}-th attribute of the current element.
     *
     * <p>
     * Used from {@link #handleStartElement()}.
     */
    protected void handleAttribute(int i) throws XMLStreamException {
        String nsUri = in.getAttributeNamespace(i);
        String prefix = in.getAttributePrefix(i);
         if (fixNull(nsUri).equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
             //Its a namespace decl, ignore as it is already written.
             return;
         }

        if(nsUri==null || prefix == null || prefix.equals("")) {
            out.writeAttribute(
                in.getAttributeLocalName(i),
                in.getAttributeValue(i)
            );
        } else {
            out.writeAttribute(
                prefix,
                nsUri,
                in.getAttributeLocalName(i),
                in.getAttributeValue(i)
            );
        }
    }

    protected void handleDTD() throws XMLStreamException {
        out.writeDTD(in.getText());
    }

    protected void handleComment() throws XMLStreamException {
        out.writeComment(in.getText());
    }

    protected void handleEntityReference() throws XMLStreamException {
        out.writeEntityRef(in.getText());
    }

    protected void handleSpace() throws XMLStreamException {
        handleCharacters();
    }

    protected void handleCDATA() throws XMLStreamException {
        out.writeCData(in.getText());
    }

    private static String fixNull(String s) {
        if(s==null)     return "";
        else            return s;
    }
}
