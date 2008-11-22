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

package com.sun.xml.internal.ws.encoding.xml;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ContentType;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import com.sun.xml.internal.ws.api.SOAPVersion;
import com.sun.xml.internal.ws.api.message.Attachment;
import com.sun.xml.internal.ws.api.message.AttachmentSet;
import com.sun.xml.internal.ws.api.message.HeaderList;
import com.sun.xml.internal.ws.api.message.Message;
import com.sun.xml.internal.ws.api.message.Messages;
import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.internal.ws.api.pipe.Codec;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;
import com.sun.xml.internal.ws.encoding.MimeMultipartParser;
import com.sun.xml.internal.ws.encoding.XMLHTTPBindingCodec;
import com.sun.xml.internal.ws.message.AbstractMessageImpl;
import com.sun.xml.internal.ws.message.EmptyMessageImpl;
import com.sun.xml.internal.ws.util.xml.XMLStreamReaderToXMLStreamWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.activation.DataSource;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Jitendra Kotamraju
 */
public final class XMLMessage {

    // So that SAAJ registers DCHs for MIME types
    static {
        new com.sun.xml.internal.messaging.saaj.soap.AttachmentPartImpl();
    }

    private static final int PLAIN_XML_FLAG      = 1;       // 00001
    private static final int MIME_MULTIPART_FLAG = 2;       // 00010
    private static final int FI_ENCODED_FLAG     = 16;      // 10000

    
    /**
     * Finds if the stream has some content or not
     *
     * @return null if there is no data
     *         else stream to be used
     */
    private static InputStream hasSomeData(InputStream in) throws IOException {
        if (in != null) {
            if (in.available() < 1) {
                if (!in.markSupported()) {
                    in = new BufferedInputStream(in);
                }
                in.mark(1);
                if (in.read() != -1) {
                    in.reset();
                } else {
                    in = null;          // No data
                }
            }
        }
        return in;
    }
    

    /**
     * Construct a message given a content type and an input stream.
     */
    public static Message create(final String ct, InputStream in) {
        Message data;
        try {
            in = hasSomeData(in);
            if (in == null) {
                data = Messages.createEmpty(SOAPVersion.SOAP_11);
                return data;
            } 

            if (ct != null) {
                final ContentType contentType = new ContentType(ct);
                final int contentTypeId = identifyContentType(contentType);
                if ((contentTypeId & MIME_MULTIPART_FLAG) != 0) {
                    data = new XMLMultiPart(ct, in);
                } else if ((contentTypeId & PLAIN_XML_FLAG) != 0) {
                    data = Messages.createUsingPayload(new StreamSource(in),
                            SOAPVersion.SOAP_11);
                } else {
                    data = new UnknownContent(ct, in);
                }
            } else {
                data = Messages.createEmpty(SOAPVersion.SOAP_11);
            }
        } catch(Exception ex) {
            throw new WebServiceException(ex);
        }
        return data;
    }


    public static Message create(Source source) {
        return (source == null) ? 
            Messages.createEmpty(SOAPVersion.SOAP_11) : 
            Messages.createUsingPayload(source, SOAPVersion.SOAP_11);
    }

    public static Message create(DataSource ds) {
        try {
            return (ds == null) ? 
                Messages.createEmpty(SOAPVersion.SOAP_11) : 
                create(ds.getContentType(), ds.getInputStream());
        } catch(IOException ioe) {
            throw new WebServiceException(ioe);
        }
        }
        
    public static Message create(Exception e) {
        return new FaultMessage(SOAPVersion.SOAP_11);
    }
    
    /**
     * Get the content type ID from the content type.
     */
    private static int getContentId(String ct) {    
        try {
            final ContentType contentType = new ContentType(ct);
            return identifyContentType(contentType);
        } catch(Exception ex) {
            throw new WebServiceException(ex);
    }
    }
    
    /**
     * Return true if the content uses fast infoset.
     */
    public static boolean isFastInfoset(String ct) {    
        return (getContentId(ct) & FI_ENCODED_FLAG) != 0;
    }

    /**
     * Verify a contentType.
     *
     * @return
     * MIME_MULTIPART_FLAG | PLAIN_XML_FLAG
     * MIME_MULTIPART_FLAG | FI_ENCODED_FLAG;
     * PLAIN_XML_FLAG
     * FI_ENCODED_FLAG
     *
     */
    public static int identifyContentType(ContentType contentType) {
        String primary = contentType.getPrimaryType();
        String sub = contentType.getSubType();

        if (primary.equalsIgnoreCase("multipart") && sub.equalsIgnoreCase("related")) {
            String type = contentType.getParameter("type");
            if (type != null) {
                if (isXMLType(type)) {
                    return MIME_MULTIPART_FLAG | PLAIN_XML_FLAG;
                } else if (isFastInfosetType(type)) {
                    return MIME_MULTIPART_FLAG | FI_ENCODED_FLAG;
                }
            }
            return 0;
        } else if (isXMLType(primary, sub)) {
            return PLAIN_XML_FLAG;
        } else if (isFastInfosetType(primary, sub)) {
            return FI_ENCODED_FLAG;
        }
        return 0;
    }

    protected static boolean isXMLType(@NotNull String primary, @NotNull String sub) {
        return (primary.equalsIgnoreCase("text") && sub.equalsIgnoreCase("xml"))
                || (primary.equalsIgnoreCase("application") && sub.equalsIgnoreCase("xml"))
                || (primary.equalsIgnoreCase("application") && sub.toLowerCase().endsWith("+xml"));
    }

    protected static boolean isXMLType(String type) {
        String lowerType = type.toLowerCase();
        return lowerType.startsWith("text/xml")
                || lowerType.startsWith("application/xml")
                || (lowerType.startsWith("application/") && (lowerType.indexOf("+xml") != -1));
    }

    protected static boolean isFastInfosetType(String primary, String sub) {
        return primary.equalsIgnoreCase("application") && sub.equalsIgnoreCase("fastinfoset");
    }

    protected static boolean isFastInfosetType(String type) {
        return type.toLowerCase().startsWith("application/fastinfoset");
    }
    
        
        /**
     * Access a {@link Message} as a {@link DataSource}.
     * <p>
     * A {@link Message} implementation will implement this if the 
     * messages is to be access as data source.
     * <p>
     * TODO: consider putting as part of the API.
         */
    public static interface MessageDataSource {
        /**
         * Check if the data source has been consumed.
         * @return true of the data source has been consumed, otherwise false.
         */
        boolean hasUnconsumedDataSource();
        
        /**
         * Get the data source.
         * @return the data source.
         */
        DataSource getDataSource();
    }
    

    /**
     * Data represented as a multi-part MIME message. 
     * <p>
     * The root part may be an XML or an FI document.
     *
     * This class parses {@link MimeMultipart} lazily.
     */
    public static final class XMLMultiPart extends AbstractMessageImpl implements MessageDataSource {
        private final DataSource dataSource;
        private MimeMultipartParser mpp;
        
        public XMLMultiPart(final String contentType, final InputStream is) {
            super(SOAPVersion.SOAP_11);
            dataSource = createDataSource(contentType, is);
        }

        public XMLMultiPart(DataSource dataSource) {
            super(SOAPVersion.SOAP_11);
            this.dataSource = dataSource;
        }

        public DataSource getDataSource() {
            assert dataSource != null;
                return dataSource;
            }

        private void convertDataSourceToMessage() {
            if (mpp == null) {
            try {
                    mpp = new MimeMultipartParser(
                            dataSource.getInputStream(),
                            dataSource.getContentType());
                } catch(IOException ioe) {
                    throw new WebServiceException(ioe);
        }
        }
                }

        @Override
        public boolean isOneWay(@NotNull WSDLPort port) {
            return false;
                }

        public boolean isFault() {
            return false;
        }
        
        public boolean hasHeaders() {
            return false;
        }

        public HeaderList getHeaders() {
            return new HeaderList();
        }

        @Override
        public AttachmentSet getAttachments() {
            convertDataSourceToMessage();
            return new XMLAttachmentSet(mpp);
    }

        public String getPayloadLocalPart() {
            throw new UnsupportedOperationException();
        }

        public String getPayloadNamespaceURI() {
            throw new UnsupportedOperationException();
        }

        public boolean hasPayload() {
            return true;
        }

        public Source readPayloadAsSource() {
            convertDataSourceToMessage();
            return mpp.getRootPart().asSource();
        }

        public XMLStreamReader readPayload() throws XMLStreamException {
            convertDataSourceToMessage();
            return XMLStreamReaderFactory.create( null,
                    mpp.getRootPart().asInputStream(), true);
        }

        public void writePayloadTo(XMLStreamWriter sw) {
            XMLStreamReaderToXMLStreamWriter c = new XMLStreamReaderToXMLStreamWriter();
                    try {
                XMLStreamReader r = readPayload();
                c.bridge(r, sw);
                XMLStreamReaderFactory.recycle(r);
                    } catch(Exception e) {
                throw new RuntimeException(e);
                }
        }

        protected void writePayloadTo(ContentHandler contentHandler, 
                ErrorHandler errorHandler, boolean fragment){
            throw new UnsupportedOperationException();
                }

        public Message copy() {
            throw new UnsupportedOperationException();
                }

        public boolean hasUnconsumedDataSource() {
            return mpp == null;
                }

        }
        
    private static final class XMLAttachmentSet implements AttachmentSet {

        private final Map<String, Attachment> attMap;

        public XMLAttachmentSet(MimeMultipartParser mpp) {
            // TODO 
            attMap = new HashMap<String, Attachment>();
            attMap.putAll(mpp.getAttachmentParts());
    }

    /**
         * Gets the attachment by the content ID.
         *
         * @return null
         *         if no such attachment exist.
     */
        public Attachment get(String contentId) {
            return attMap.get(contentId);
        }

        public boolean isEmpty() {
            return attMap.isEmpty();
                }

        /**
         * Returns an iterator over a set of elements of type T.
         *
         * @return an Iterator.
         */
        public Iterator<Attachment> iterator() {
            return attMap.values().iterator();
                }

        public void add(Attachment att) {
            attMap.put(att.getContentId(), att);
                }

        }

    private static class FaultMessage extends EmptyMessageImpl {
        
        public FaultMessage(SOAPVersion version) {
            super(version);
        }

        @Override
        public boolean isFault() {
            return true;
        }
    }

    
    /**
     * Don't know about this content. It's conent-type is NOT the XML types
     * we recognize(text/xml, application/xml, multipart/related;text/xml etc).
     *
     * This could be used to represent image/jpeg etc
     */
    public static class UnknownContent extends AbstractMessageImpl implements MessageDataSource {
        private final DataSource ds;
        private final HeaderList headerList;

        public UnknownContent(final String ct, final InputStream in) {
            this(createDataSource(ct,in));
        }

        public UnknownContent(DataSource ds) {
            super(SOAPVersion.SOAP_11);
            this.ds = ds;
            this.headerList = new HeaderList();
        }
        
        /**
         * Copy constructor.
         */
        private UnknownContent(UnknownContent that) {
            super(that.soapVersion);
            this.ds = that.ds;
            this.headerList = HeaderList.copy(that.headerList);
                }

        public boolean hasUnconsumedDataSource() {
            return true;
                }

        public DataSource getDataSource() {
            assert ds != null;
            return ds;
                }

        protected void writePayloadTo(ContentHandler contentHandler, 
                ErrorHandler errorHandler, boolean fragment) throws SAXException {
            throw new UnsupportedOperationException();
        }

        public boolean hasHeaders() {
            return false;
        }
        
        public boolean isFault() {
            return false;
        }

        public HeaderList getHeaders() {
            return headerList;
        }

        public String getPayloadLocalPart() {
            throw new UnsupportedOperationException();
    }
    
        public String getPayloadNamespaceURI() {
            throw new UnsupportedOperationException();
        }

        public boolean hasPayload() {
            return false;
        }
        
        public Source readPayloadAsSource() {
            return null;
        }

        public XMLStreamReader readPayload() throws XMLStreamException {
            throw new WebServiceException("There isn't XML payload. Shouldn't come here.");
        }

        public void writePayloadTo(XMLStreamWriter sw) throws XMLStreamException {
            // No XML. Nothing to do
        }

        public Message copy() {
            return new UnknownContent(this);
        }

        }

    public static DataSource getDataSource(Message msg) {
        if (msg instanceof MessageDataSource) {
            return ((MessageDataSource)msg).getDataSource();
        } else {
            AttachmentSet atts = msg.getAttachments();
            if (atts != null && !atts.isEmpty()) {
                final ByteOutputStream bos = new ByteOutputStream();
                try {
                    Codec codec = new XMLHTTPBindingCodec();
                    com.sun.xml.internal.ws.api.pipe.ContentType ct = codec.getStaticContentType(new Packet(msg));
                    codec.encode(new Packet(msg), bos);
                    return createDataSource(ct.getContentType(), bos.newInputStream());
                } catch(IOException ioe) {
                    throw new WebServiceException(ioe);
        }

            } else {
                final ByteOutputStream bos = new ByteOutputStream();
                XMLStreamWriter writer = XMLStreamWriterFactory.create(bos);
                try {
                    msg.writePayloadTo(writer);
                    writer.flush();
                } catch (XMLStreamException e) {
                    throw new WebServiceException(e);
    }
                return XMLMessage.createDataSource("text/xml", bos.newInputStream());
        }
        }
        }

    public static DataSource createDataSource(final String contentType, final InputStream is) {
        return new DataSource() {
            public InputStream getInputStream() {
                return is;
        }

            public OutputStream getOutputStream() {
            return null;
        }

            public String getContentType() {
                return contentType;
        }

            public String getName() {
                return "";
        }
        };
    }
}
