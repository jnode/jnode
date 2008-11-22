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

package com.sun.xml.internal.ws.transport.http;


import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.ws.api.PropertySet;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import com.sun.xml.internal.ws.api.message.Message;
import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.pipe.Codec;
import com.sun.xml.internal.ws.api.pipe.ContentType;
import com.sun.xml.internal.ws.api.server.AbstractServerAsyncTransport;
import com.sun.xml.internal.ws.api.server.Adapter;
import com.sun.xml.internal.ws.api.server.DocumentAddressResolver;
import com.sun.xml.internal.ws.api.server.PortAddressResolver;
import com.sun.xml.internal.ws.api.server.SDDocument;
import com.sun.xml.internal.ws.api.server.ServiceDefinition;
import com.sun.xml.internal.ws.api.server.TransportBackChannel;
import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.internal.ws.resources.WsservletMessages;
import com.sun.xml.internal.ws.server.ServerRtException;
import com.sun.xml.internal.ws.server.UnsupportedMediaException;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;

import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Adapter} that receives messages in HTTP.
 *
 * <p>
 * This object also assigns unique query string (such as "xsd=1") to
 * each {@link SDDocument} so that they can be served by HTTP GET requests.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public class HttpAdapter extends Adapter<HttpAdapter.HttpToolkit> {

    /**
     * {@link SDDocument}s keyed by the query string like "?abc".
     * Used for serving documents via HTTP GET.
     *
     * Empty if the endpoint doesn't have {@link ServiceDefinition}.
     * Read-only.
     */
    public final Map<String,SDDocument> wsdls;

    /**
     * Reverse map of {@link #wsdls}. Read-only.
     */
    public final Map<SDDocument,String> revWsdls;

    public final HttpAdapterList<? extends HttpAdapter> owner;

    /**
     * Creates a lone {@link HttpAdapter} that does not know of any other
     * {@link HttpAdapter}s.
     *
     * This is convenient for creating an {@link HttpAdapter} for an environment
     * where they don't know each other (such as JavaSE deployment.)
     *
     * @param endpoint web service endpoint
     * @return singe adapter to process HTTP messages
     */
    public static HttpAdapter createAlone(WSEndpoint endpoint) {
        return new DummyList().createAdapter("","",endpoint);
    }

    protected HttpAdapter(WSEndpoint endpoint, HttpAdapterList<? extends HttpAdapter> owner) {
        super(endpoint);
        this.owner = owner;

        // fill in WSDL map
        ServiceDefinition sdef = this.endpoint.getServiceDefinition();
        if(sdef==null) {
            wsdls = Collections.emptyMap();
            revWsdls = Collections.emptyMap();
        } else {
            wsdls = new HashMap<String, SDDocument>();  // wsdl=1 --> Doc
            // Sort WSDL, Schema documents based on SystemId so that the same
            // document gets wsdl=x mapping
            Map<String, SDDocument> systemIds = new TreeMap<String, SDDocument>();
            for (SDDocument sdd : sdef) {
                if (sdd == sdef.getPrimary()) { // No sorting for Primary WSDL
                    wsdls.put("wsdl", sdd);     
                    wsdls.put("WSDL", sdd);
                } else {
                    systemIds.put(sdd.getURL().toString(), sdd);
                }
            }
            
            int wsdlnum = 1;
            int xsdnum = 1;
            for (Map.Entry<String, SDDocument> e : systemIds.entrySet()) {
                SDDocument sdd = e.getValue();
                if (sdd.isWSDL()) {
                    wsdls.put("wsdl="+(wsdlnum++),sdd);
                }
                if (sdd.isSchema()) {
                    wsdls.put("xsd="+(xsdnum++),sdd);
                }
            }

            revWsdls = new HashMap<SDDocument,String>();    // Doc --> wsdl=1
            for (Entry<String,SDDocument> e : wsdls.entrySet()) {
                if (!e.getKey().equals("WSDL")) {           // map Doc --> wsdl, not WSDL
                    revWsdls.put(e.getValue(),e.getKey());
                }
            }
        }
    }

    protected HttpToolkit createToolkit() {
        return new HttpToolkit();
    }

    /**
     * Receives the incoming HTTP connection and dispatches
     * it to JAX-WS. This method returns when JAX-WS completes
     * processing the request and the whole reply is written
     * to {@link WSHTTPConnection}.
     *
     * <p>
     * This method is invoked by the lower-level HTTP stack,
     * and "connection" here is an HTTP connection.
     *
     * <p>
     * To populate a request {@link Packet} with more info,
     * define {@link PropertySet.Property properties} on
     * {@link WSHTTPConnection}.
     *
     * @param connection to receive/send HTTP messages for web service endpoints
     * @throws IOException when I/O errors happen
     */
    public void handle(@NotNull WSHTTPConnection connection) throws IOException {
        HttpToolkit tk = pool.take();
        try {
            tk.handle(connection);
        } finally {
            pool.recycle(tk);
        }
    }

    /**
     *
     * @param con
     * @param codec
     * @return
     * @throws IOException
     *         ExceptionHasMessage exception that contains particular fault message
     *         UnsupportedMediaException to indicate to send 415 error code
     */
    private Packet decodePacket(@NotNull WSHTTPConnection con, @NotNull Codec codec) throws IOException {
        String ct = con.getRequestHeader("Content-Type");
        InputStream in = con.getInput();
        Packet packet = new Packet();
        packet.soapAction = con.getRequestHeader("SOAPAction");
        packet.wasTransportSecure = con.isSecure();
        packet.acceptableMimeTypes = con.getRequestHeader("Accept");
        packet.addSatellite(con);
        packet.transportBackChannel = new Oneway(con);
        packet.webServiceContextDelegate = con.getWebServiceContextDelegate();

        if (dump) {
            ByteArrayBuffer buf = new ByteArrayBuffer();
            buf.write(in);
            dump(buf, "HTTP request", con.getRequestHeaders());
            in = buf.newInputStream();
        }
        codec.decode(in, ct, packet);
        return packet;
    }




    private void encodePacket(@NotNull Packet packet, @NotNull WSHTTPConnection con, @NotNull Codec codec) throws IOException {
        if (con.isClosed()) {
            return;                 // Connection is already closed
        }
        Message responseMessage = packet.getMessage();
        if (responseMessage == null) {
            if (!con.isClosed()) {
                // set the response code if not already set
                // for example, 415 may have been set earlier for Unsupported Content-Type
                if (con.getStatus() == 0)
                    con.setStatus(WSHTTPConnection.ONEWAY);
                // close the response channel now
                try {
                    con.getOutput().close(); // no payload
                } catch (IOException e) {
                    throw new WebServiceException(e);
                }
            }
        } else {
            if (con.getStatus() == 0) {
                // if the appliation didn't set the status code,
                // set the default one.
                con.setStatus(responseMessage.isFault()
                        ? HttpURLConnection.HTTP_INTERNAL_ERROR
                        : HttpURLConnection.HTTP_OK);
            }

            ContentType contentType = codec.getStaticContentType(packet);
            if (contentType != null) {
                con.setContentTypeResponseHeader(contentType.getContentType());
                OutputStream os = con.getProtocol().contains("1.1") ? con.getOutput() : new Http10OutputStream(con);
                if (dump) {
                    ByteArrayBuffer buf = new ByteArrayBuffer();
                    codec.encode(packet, buf);
                    dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                    buf.writeTo(os);
                } else {
                    codec.encode(packet, os);
                }
                os.close();
            } else {
                ByteArrayBuffer buf = new ByteArrayBuffer();
                contentType = codec.encode(packet, buf);
                con.setContentTypeResponseHeader(contentType.getContentType());
                if (dump) {
                    dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                }
                OutputStream os = con.getOutput();
                buf.writeTo(os);
                os.close();
            }
        }
    }

    public void invokeAsync(final WSHTTPConnection con) throws IOException {
        final HttpToolkit tk = pool.take();
        final Packet request;
        try {
            request = decodePacket(con, tk.codec);
        } catch(ExceptionHasMessage e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            Packet response = new Packet();
            response.setMessage(e.getFaultMessage());
            encodePacket(response, con, tk.codec);
            pool.recycle(tk);
            con.close();
            return;
        } catch(UnsupportedMediaException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            Packet response = new Packet();
            con.setStatus(WSHTTPConnection.UNSUPPORTED_MEDIA);
            encodePacket(response, con, tk.codec);
            pool.recycle(tk);
            con.close();
            return;
        }

        endpoint.schedule(request, new WSEndpoint.CompletionCallback() {
            public void onCompletion(@NotNull Packet response) {
                try {
                    try {
                        encodePacket(response, con, tk.codec);
                    } catch(IOException ioe) {
                        LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
                    }
                    pool.recycle(tk);
                } finally{
                    con.close();
                }
            }
        });
    }

    final class AsyncTransport extends AbstractServerAsyncTransport<WSHTTPConnection> {

        public AsyncTransport() {
            super(endpoint);
        }

        public void handleAsync(WSHTTPConnection con) throws IOException {
            super.handle(con);
        }

        protected void encodePacket(WSHTTPConnection con, @NotNull Packet packet, @NotNull Codec codec) throws IOException {
            HttpAdapter.this.encodePacket(packet, con, codec);
        }

        protected @Nullable String getAcceptableMimeTypes(WSHTTPConnection con) {
            return null;
        }

        protected @Nullable TransportBackChannel getTransportBackChannel(WSHTTPConnection con) {
            return new Oneway(con);
        }

        protected @NotNull
        PropertySet getPropertySet(WSHTTPConnection con) {
            return con;
        }

        protected @NotNull WebServiceContextDelegate getWebServiceContextDelegate(WSHTTPConnection con) {
            return con.getWebServiceContextDelegate();
        }
    }

    final class Oneway implements TransportBackChannel {
        WSHTTPConnection con;
        Oneway(WSHTTPConnection con) {
            this.con = con;
        }
        public void close() {
            if(!con.isClosed()) {
                // close the response channel now
                con.setStatus(WSHTTPConnection.ONEWAY);
                try {
                    con.getOutput().close(); // no payload
                } catch (IOException e) {
                    throw new WebServiceException(e);
                }
                con.close();
            }
        }
    }

    final class HttpToolkit extends Adapter.Toolkit {
        public void handle(WSHTTPConnection con) throws IOException {
            try {
                Packet packet = new Packet();
                try {
                    packet = decodePacket(con, codec);
                } catch(ExceptionHasMessage e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    packet.setMessage(e.getFaultMessage());
                } catch(UnsupportedMediaException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    con.setStatus(WSHTTPConnection.UNSUPPORTED_MEDIA);
                } catch(ServerRtException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                if (packet.getMessage() != null && !packet.getMessage().isFault()) {
                    try {
                        packet = head.process(packet, con.getWebServiceContextDelegate(),
                                packet.transportBackChannel);
                    } catch(Exception e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        if (!con.isClosed()) {
                            writeInternalServerError(con);
                        }
                        return;
                    }
                }
               encodePacket(packet, con, codec);
            } finally {
                if (!con.isClosed()) {
                    con.close();
                }
            }
        }
    }

    /**
     * Returns true if the given query string is for metadata request.
     *
     * @param query
     *      String like "xsd=1" or "perhaps=some&amp;unrelated=query".
     *      Can be null.
     * @return true for metadata requests
     *         false for web service requests
     */
    public final boolean isMetadataQuery(String query) {
        // we intentionally return true even if documents don't exist,
        // so that they get 404.
        return query != null && (query.equals("WSDL") || query.startsWith("wsdl") || query.startsWith("xsd="));
    }

    /**
     * Sends out the WSDL (and other referenced documents)
     * in response to the GET requests to URLs like "?wsdl" or "?xsd=2".
     *
     * @param con
     *      The connection to which the data will be sent. Must not be null.
     * @param baseAddress
     *      The requested base URL (such as "http://myhost:2045/foo/bar").
     *      Used to reference other resoures. Must not be null.
     * @param queryString
     *      The query string given by the client (which indicates
     *      what document to serve.) Can be null (but it causes an 404 not found.)
     *
     * @throws IOException when I/O errors happen
     */
    public void publishWSDL(WSHTTPConnection con, final String baseAddress, String queryString) throws IOException {
        // Workaround for a bug in http server. Read and close InputStream
        // TODO remove once the bug is fixed in http server
        InputStream in = con.getInput();
        while(in.read() != -1);
        in.close();

        SDDocument doc = wsdls.get(queryString);
        if (doc == null) {
            writeNotFoundErrorPage(con,"Invalid Request");
            return;
        }

        con.setStatus(HttpURLConnection.HTTP_OK);
        con.setContentTypeResponseHeader("text/xml;charset=utf-8");

        OutputStream os = con.getProtocol().contains("1.1") ? con.getOutput() : new Http10OutputStream(con);

        final PortAddressResolver portAddressResolver = owner.createPortAddressResolver(baseAddress);
        final String address = portAddressResolver.getAddressFor(endpoint.getServiceName(), endpoint.getPortName().getLocalPart());
        assert address != null;
        DocumentAddressResolver resolver = new DocumentAddressResolver() {
            public String getRelativeAddressFor(@NotNull SDDocument current, @NotNull SDDocument referenced) {
                // the map on endpoint should account for all SDDocument
                assert revWsdls.containsKey(referenced);
                return address+'?'+ revWsdls.get(referenced);
            }
        };

        doc.writeTo(portAddressResolver, resolver, os);
        os.close();
    }

    /**
     * HTTP/1.0 connections require Content-Length. So just buffer to find out
     * the length.
     */
    private final static class Http10OutputStream extends ByteArrayBuffer {
        private final WSHTTPConnection con;

        Http10OutputStream(WSHTTPConnection con) {
            this.con = con;
        }

        @Override
        public void close() throws IOException {
            super.close();
            con.setContentLengthResponseHeader(size());
            OutputStream os = con.getOutput();
            writeTo(os);
            os.close();
        }
    }

    private void writeNotFoundErrorPage(WSHTTPConnection con, String message) throws IOException {
        con.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        con.setContentTypeResponseHeader("text/html; charset=UTF-8");

        PrintWriter out = new PrintWriter(new OutputStreamWriter(con.getOutput(),"UTF-8"));
        out.println("<html>");
        out.println("<head><title>");
        out.println(WsservletMessages.SERVLET_HTML_TITLE());
        out.println("</title></head>");
        out.println("<body>");
        out.println(WsservletMessages.SERVLET_HTML_NOT_FOUND(message));
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    private void writeInternalServerError(WSHTTPConnection con) throws IOException {
        con.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        con.getOutput().close();        // Sets the status code
    }

    private static final class DummyList extends HttpAdapterList<HttpAdapter> {
        @Override
        protected HttpAdapter createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
            return new HttpAdapter(endpoint, this);
        }
    }

    private void dump(ByteArrayBuffer buf, String caption, Map<String, List<String>> headers) throws IOException {
        System.out.println("---["+caption +"]---");
        if (headers != null) {
            for (Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getValue().isEmpty()) {
                    // I don't think this is legal, but let's just dump it,
                    // as the point of the dump is to uncover problems.
                    System.out.println(header.getValue());
                } else {
                    for (String value : header.getValue()) {
                        System.out.println(header.getKey() + ": " + value);
                    }
                }
            }
        }
        buf.writeTo(System.out);
        System.out.println("--------------------");
    }

    /**
     * Dumps what goes across HTTP transport.
     */
    public static boolean dump;

    static {
        boolean b;
        try {
            b = Boolean.getBoolean(HttpAdapter.class.getName()+".dump");
        } catch( Throwable t ) {
            b = false;
        }
        dump = b;
    }

    private static final Logger LOGGER = Logger.getLogger(HttpAdapter.class.getName());

}
