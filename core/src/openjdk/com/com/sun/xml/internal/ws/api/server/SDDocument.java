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

package com.sun.xml.internal.ws.api.server;

import com.sun.istack.internal.Nullable;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Represents an individual document that forms a {@link ServiceDefinition}.
 *
 * <pre>
 * TODO:
 *      how does those documents refer to each other?
 *
 * </pre>
 *
 * @author Jitendra Kotamraju
 */
public interface SDDocument {

    /**
     * Gets the root tag name of this document.
     *
     * <p>
     * This can be used to identify a kind of document quickly
     * (such as schema, WSDL, ...)
     *
     * @return
     *      always non-null.
     */
    QName getRootName();

    /**
     * Returns true if this document is WSDL.
     */
    boolean isWSDL();

    /**
     * Returns true if this document is schema.
     */
    boolean isSchema();

    /**
     * Gets the system ID of the document where it's taken from. Generated documents
     * use a fake URL that can be used to resolve relative URLs. So donot use this URL
     * for reading or writing.
     */
    URL getURL();

    /**
     * Writes the document to the given {@link OutputStream}.
     *
     * <p>
     * Since {@link ServiceDefinition} doesn't know which endpoint address
     * {@link Adapter} is serving to, (and often it serves multiple URLs
     * simultaneously), this method takes the PortAddressResolver as a parameter,
     * so that it can produce the corret address information in the generated WSDL.
     *
     * @param portAddressResolver
     *      An endpoint address resolver that gives endpoint address for a WSDL
     *      port. Can be null.
     * @param resolver
     *      Used to resolve relative references among documents.
     * @param os
     *      The {@link OutputStream} that receives the generated document.
     *
     * @throws IOException
     *      if there was a failure reported from the {@link OutputStream}.
     */
    void writeTo(@Nullable PortAddressResolver portAddressResolver,
            DocumentAddressResolver resolver, OutputStream os) throws IOException;

    /**
     * Writes the document to the given {@link XMLStreamWriter}.
     *
     * <p>
     * The same as {@link #writeTo(PortAddressResolver,DocumentAddressResolver,OutputStream)} except
     * it writes to an {@link XMLStreamWriter}.
     *
     * <p>
     * The implementation must not call {@link XMLStreamWriter#writeStartDocument()}
     * nor {@link XMLStreamWriter#writeEndDocument()}. Those are the caller's
     * responsibility.
     *
     * @throws XMLStreamException
     *      if the {@link XMLStreamWriter} reports an error.
     */
    void writeTo(PortAddressResolver portAddressResolver,
            DocumentAddressResolver resolver, XMLStreamWriter out) throws XMLStreamException, IOException;

    /**
     * {@link SDDocument} that represents an XML Schema.
     */
    interface Schema extends SDDocument {
        /**
         * Gets the target namepsace of this schema.
         */
        String getTargetNamespace();
    }

    /**
     * {@link SDDocument} that represents a WSDL.
     */
    interface WSDL extends SDDocument {
        /**
         * Gets the target namepsace of this schema.
         */
        String getTargetNamespace();

        /**
         * This WSDL has a portType definition
         * that matches what {@link WSEndpoint} is serving.
         *
         * TODO: does this info needs to be exposed?
         */
        boolean hasPortType();

        /**
         * This WSDL has a service definition
         * that matches the {@link WSEndpoint}.
         *
         * TODO: does this info need to be exposed?
         */
        boolean hasService();
    }
}
