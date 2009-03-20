/*
 * Copyright 2001-2007 Sun Microsystems, Inc.  All Rights Reserved.
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


package sun.net.www.protocol.https;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.CookieHandler;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.Principal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.security.AccessController;

import javax.security.auth.x500.X500Principal;
import javax.security.auth.kerberos.KerberosPrincipal;

import javax.net.ssl.*;
import sun.security.x509.X500Name;
import sun.misc.Regexp;
import sun.misc.RegexpPool;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.net.www.http.HttpClient;
import sun.security.action.*;

import sun.security.util.HostnameChecker;
import sun.security.ssl.SSLSocketImpl;


/**
 * This class provides HTTPS client URL support, building on the standard
 * "sun.net.www" HTTP protocol handler.  HTTPS is the same protocol as HTTP,
 * but differs in the transport layer which it uses:  <UL>
 *
 *      <LI>There's a <em>Secure Sockets Layer</em> between TCP
 *      and the HTTP protocol code.
 *
 *      <LI>It uses a different default TCP port.
 *
 *      <LI>It doesn't use application level proxies, which can see and
 *      manipulate HTTP user level data, compromising privacy.  It uses
 *      low level tunneling instead, which hides HTTP protocol and data
 *      from all third parties.  (Traffic analysis is still possible).
 *
 *      <LI>It does basic server authentication, to protect
 *      against "URL spoofing" attacks.  This involves deciding
 *      whether the X.509 certificate chain identifying the server
 *      is trusted, and verifying that the name of the server is
 *      found in the certificate.  (The application may enable an
 *      anonymous SSL cipher suite, and such checks are not done
 *      for anonymous ciphers.)
 *
 *      <LI>It exposes key SSL session attributes, specifically the
 *      cipher suite in use and the server's X509 certificates, to
 *      application software which knows about this protocol handler.
 *
 *      </UL>
 *
 * <P> System properties used include:  <UL>
 *
 *      <LI><em>https.proxyHost</em> ... the host supporting SSL
 *      tunneling using the conventional CONNECT syntax
 *
 *      <LI><em>https.proxyPort</em> ... port to use on proxyHost
 *
 *      <LI><em>https.cipherSuites</em> ... comma separated list of
 *      SSL cipher suite names to enable.
 *
 *      <LI><em>http.nonProxyHosts</em> ...
 *
 *      </UL>
 *
 * @author David Brownell
 * @author Bill Foote
 */

// final for export control reasons (access to APIs); remove with care
final class HttpsClient extends HttpClient
    implements HandshakeCompletedListener
{
    // STATIC STATE and ACCESSORS THERETO

    // HTTPS uses a different default port number than HTTP.
    private static final int    httpsPortNumber = 443;

    /** Returns the default HTTPS port (443) */
    protected int getDefaultPort() { return httpsPortNumber; }

    private HostnameVerifier hv;
    private SSLSocketFactory sslSocketFactory;

    // HttpClient.proxyDisabled will always be false, because we don't
    // use an application-level HTTP proxy.  We might tunnel through
    // our http proxy, though.


    // INSTANCE DATA

    // last negotiated SSL session
    private SSLSession  session;

    private String [] getCipherSuites() {
        //
        // If ciphers are assigned, sort them into an array.
        //
        String ciphers [];
        String cipherString = AccessController.doPrivileged(
                new GetPropertyAction("https.cipherSuites"));

        if (cipherString == null || "".equals(cipherString)) {
            ciphers = null;
        } else {
            StringTokenizer     tokenizer;
            Vector<String>      v = new Vector<String>();

            tokenizer = new StringTokenizer(cipherString, ",");
            while (tokenizer.hasMoreTokens())
                v.addElement(tokenizer.nextToken());
            ciphers = new String [v.size()];
            for (int i = 0; i < ciphers.length; i++)
                ciphers [i] = v.elementAt(i);
        }
        return ciphers;
    }

    private String [] getProtocols() {
        //
        // If protocols are assigned, sort them into an array.
        //
        String protocols [];
        String protocolString = AccessController.doPrivileged(
                new GetPropertyAction("https.protocols"));

        if (protocolString == null || "".equals(protocolString)) {
            protocols = null;
        } else {
            StringTokenizer     tokenizer;
            Vector<String>      v = new Vector<String>();

            tokenizer = new StringTokenizer(protocolString, ",");
            while (tokenizer.hasMoreTokens())
                v.addElement(tokenizer.nextToken());
            protocols = new String [v.size()];
            for (int i = 0; i < protocols.length; i++) {
                protocols [i] = v.elementAt(i);
            }
        }
        return protocols;
    }

    private String getUserAgent() {
        String userAgent = java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("https.agent"));
        if (userAgent == null || userAgent.length() == 0) {
            userAgent = "JSSE";
        }
        return userAgent;
    }

    // should remove once HttpClient.newHttpProxy is putback
    private static Proxy newHttpProxy(String proxyHost, int proxyPort) {
        InetSocketAddress saddr = null;
        final String phost = proxyHost;
        final int pport = proxyPort < 0 ? httpsPortNumber : proxyPort;
        try {
            saddr = java.security.AccessController.doPrivileged(new
                java.security.PrivilegedExceptionAction<InetSocketAddress>() {
                public InetSocketAddress run() {
                    return new InetSocketAddress(phost, pport);
                }});
        } catch (java.security.PrivilegedActionException pae) {
        }
        return new Proxy(Proxy.Type.HTTP, saddr);
    }

    // CONSTRUCTOR, FACTORY


    /**
     * Create an HTTPS client URL.  Traffic will be tunneled through any
     * intermediate nodes rather than proxied, so that confidentiality
     * of data exchanged can be preserved.  However, note that all the
     * anonymous SSL flavors are subject to "person-in-the-middle"
     * attacks against confidentiality.  If you enable use of those
     * flavors, you may be giving up the protection you get through
     * SSL tunneling.
     *
     * Use New to get new HttpsClient. This constructor is meant to be
     * used only by New method. New properly checks for URL spoofing.
     *
     * @param URL https URL with which a connection must be established
     */
    private HttpsClient(SSLSocketFactory sf, URL url)
    throws IOException
    {
        // HttpClient-level proxying is always disabled,
        // because we override doConnect to do tunneling instead.
        this(sf, url, (String)null, -1);
    }

    /**
     *  Create an HTTPS client URL.  Traffic will be tunneled through
     * the specified proxy server.
     */
    HttpsClient(SSLSocketFactory sf, URL url, String proxyHost, int proxyPort)
        throws IOException {
        this(sf, url, proxyHost, proxyPort, -1);
    }

    /**
     *  Create an HTTPS client URL.  Traffic will be tunneled through
     * the specified proxy server, with a connect timeout
     */
    HttpsClient(SSLSocketFactory sf, URL url, String proxyHost, int proxyPort,
                int connectTimeout)
        throws IOException {
        this(sf, url,
             (proxyHost == null? null:
                HttpsClient.newHttpProxy(proxyHost, proxyPort)),
                connectTimeout);
    }

    /**
     *  Same as previous constructor except using a Proxy
     */
    HttpsClient(SSLSocketFactory sf, URL url, Proxy proxy,
                int connectTimeout)
        throws IOException {
        this.proxy = proxy;
        setSSLSocketFactory(sf);
        this.proxyDisabled = true;

        this.host = url.getHost();
        this.url = url;
        port = url.getPort();
        if (port == -1) {
            port = getDefaultPort();
        }
        setConnectTimeout(connectTimeout);
        // get the cookieHandler if there is any
        cookieHandler = java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<CookieHandler>() {
                public CookieHandler run() {
                    return CookieHandler.getDefault();
                }
            });
        openServer();
    }


    // This code largely ripped off from HttpClient.New, and
    // it uses the same keepalive cache.

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv)
            throws IOException {
        return HttpsClient.New(sf, url, hv, true);
    }

    /** See HttpClient for the model for this method. */
    static HttpClient New(SSLSocketFactory sf, URL url,
            HostnameVerifier hv, boolean useCache) throws IOException {
        return HttpsClient.New(sf, url, hv, (String)null, -1, useCache);
    }

    /**
     * Get a HTTPS client to the URL.  Traffic will be tunneled through
     * the specified proxy server.
     */
    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv,
                           String proxyHost, int proxyPort) throws IOException {
        return HttpsClient.New(sf, url, hv, proxyHost, proxyPort, true);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv,
                           String proxyHost, int proxyPort, boolean useCache)
        throws IOException {
        return HttpsClient.New(sf, url, hv, proxyHost, proxyPort, useCache, -1);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv,
                          String proxyHost, int proxyPort, boolean useCache,
                          int connectTimeout)
        throws IOException {

        return HttpsClient.New(sf, url, hv,
                               (proxyHost == null? null :
                                HttpsClient.newHttpProxy(proxyHost, proxyPort)),
                               useCache, connectTimeout);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv,
                          Proxy p, boolean useCache,
                          int connectTimeout)
        throws IOException {
        HttpsClient ret = null;
        if (useCache) {
            /* see if one's already around */
            ret = (HttpsClient) kac.get(url, sf);
            if (ret != null) {
                ret.cachedHttpClient = true;
            }
        }
        if (ret == null) {
            ret = new HttpsClient(sf, url, p, connectTimeout);
        } else {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(url.getHost(), url.getPort());
            }
            ret.url = url;
        }
        ret.setHostnameVerifier(hv);

        return ret;
    }

    // METHODS
    void setHostnameVerifier(HostnameVerifier hv) {
        this.hv = hv;
    }

    void setSSLSocketFactory(SSLSocketFactory sf) {
        sslSocketFactory = sf;
    }

    SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

    public boolean needsTunneling() {
        return (proxy != null && proxy.type() != Proxy.Type.DIRECT
                && proxy.type() != Proxy.Type.SOCKS);
    }

    public void afterConnect() throws IOException, UnknownHostException {
        if (!isCachedConnection()) {
            SSLSocket s = null;
            SSLSocketFactory factory = sslSocketFactory;
            try {
                if (!(serverSocket instanceof SSLSocket)) {
                    s = (SSLSocket)factory.createSocket(serverSocket,
                                                        host, port, true);
                } else {
                    s = (SSLSocket)serverSocket;
                }
            } catch (IOException ex) {
                // If we fail to connect through the tunnel, try it
                // locally, as a last resort.  If this doesn't work,
                // throw the original exception.
                try {
                    s = (SSLSocket)factory.createSocket(host, port);
                } catch (IOException ignored) {
                    throw ex;
                }
            }

            //
            // Force handshaking, so that we get any authentication.
            // Register a handshake callback so our session state tracks any
            // later session renegotiations.
            //
            String [] protocols = getProtocols();
            String [] ciphers = getCipherSuites();
            if (protocols != null) {
                s.setEnabledProtocols(protocols);
            }
            if (ciphers != null) {
                s.setEnabledCipherSuites(ciphers);
            }
            s.addHandshakeCompletedListener(this);

            // if the HostnameVerifier is not set, try to enable endpoint
            // identification during handshaking
            boolean enabledIdentification = false;
            if (hv instanceof DefaultHostnameVerifier &&
                (s instanceof SSLSocketImpl) &&
                ((SSLSocketImpl)s).trySetHostnameVerification("HTTPS")) {
                enabledIdentification = true;
            }

            s.startHandshake();
            session = s.getSession();
            // change the serverSocket and serverOutput
            serverSocket = s;
            try {
                serverOutput = new PrintStream(
                    new BufferedOutputStream(serverSocket.getOutputStream()),
                    false, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new InternalError(encoding+" encoding not found");
            }

            // check URL spoofing if it has not been checked under handshaking
            if (!enabledIdentification) {
                checkURLSpoofing(hv);
            }
        } else {
            // if we are reusing a cached https session,
            // we don't need to do handshaking etc. But we do need to
            // set the ssl session
            session = ((SSLSocket)serverSocket).getSession();
        }
    }

    // Server identity checking is done according to RFC 2818: HTTP over TLS
    // Section 3.1 Server Identity
    private void checkURLSpoofing(HostnameVerifier hostnameVerifier)
            throws IOException
    {
        //
        // Get authenticated server name, if any
        //
        boolean done = false;
        String host = url.getHost();

        // if IPv6 strip off the "[]"
        if (host != null && host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length()-1);
        }

        Certificate[] peerCerts = null;
        try {
            HostnameChecker checker = HostnameChecker.getInstance(
                                                HostnameChecker.TYPE_TLS);

            Principal principal = getPeerPrincipal();
            if (principal instanceof KerberosPrincipal) {
                if (!checker.match(host, (KerberosPrincipal)principal)) {
                    throw new SSLPeerUnverifiedException("Hostname checker" +
                                " failed for Kerberos");
                }
            } else {
                // get the subject's certificate
                peerCerts = session.getPeerCertificates();

                X509Certificate peerCert;
                if (peerCerts[0] instanceof
                        java.security.cert.X509Certificate) {
                    peerCert = (java.security.cert.X509Certificate)peerCerts[0];
                } else {
                    throw new SSLPeerUnverifiedException("");
                }
                checker.match(host, peerCert);
            }

            // if it doesn't throw an exception, we passed. Return.
            return;

        } catch (SSLPeerUnverifiedException e) {

            //
            // client explicitly changed default policy and enabled
            // anonymous ciphers; we can't check the standard policy
            //
            // ignore
        } catch (java.security.cert.CertificateException cpe) {
            // ignore
        }

        String cipher = session.getCipherSuite();
        if ((cipher != null) && (cipher.indexOf("_anon_") != -1)) {
            return;
        } else if ((hostnameVerifier != null) &&
                   (hostnameVerifier.verify(host, session))) {
            return;
        }

        serverSocket.close();
        session.invalidate();

        throw new IOException("HTTPS hostname wrong:  should be <"
                              + url.getHost() + ">");
    }

    protected void putInKeepAliveCache() {
        kac.put(url, sslSocketFactory, this);
    }

    /**
     * Returns the cipher suite in use on this connection.
     */
    String getCipherSuite() {
        return session.getCipherSuite();
    }

    /**
     * Returns the certificate chain the client sent to the
     * server, or null if the client did not authenticate.
     */
    public java.security.cert.Certificate [] getLocalCertificates() {
        return session.getLocalCertificates();
    }

    /**
     * Returns the certificate chain with which the server
     * authenticated itself, or throw a SSLPeerUnverifiedException
     * if the server did not authenticate.
     */
    java.security.cert.Certificate [] getServerCertificates()
            throws SSLPeerUnverifiedException
    {
        return session.getPeerCertificates();
    }

    /**
     * Returns the X.509 certificate chain with which the server
     * authenticated itself, or null if the server did not authenticate.
     */
    javax.security.cert.X509Certificate [] getServerCertificateChain()
            throws SSLPeerUnverifiedException
    {
        return session.getPeerCertificateChain();
    }

    /**
     * Returns the principal with which the server authenticated
     * itself, or throw a SSLPeerUnverifiedException if the
     * server did not authenticate.
     */
    Principal getPeerPrincipal()
            throws SSLPeerUnverifiedException
    {
        Principal principal;
        try {
            principal = session.getPeerPrincipal();
        } catch (AbstractMethodError e) {
            // if the provider does not support it, fallback to peer certs.
            // return the X500Principal of the end-entity cert.
            java.security.cert.Certificate[] certs =
                        session.getPeerCertificates();
            principal = (X500Principal)
                ((X509Certificate)certs[0]).getSubjectX500Principal();
        }
        return principal;
    }

    /**
     * Returns the principal the client sent to the
     * server, or null if the client did not authenticate.
     */
    Principal getLocalPrincipal()
    {
        Principal principal;
        try {
            principal = session.getLocalPrincipal();
        } catch (AbstractMethodError e) {
            principal = null;
            // if the provider does not support it, fallback to local certs.
            // return the X500Principal of the end-entity cert.
            java.security.cert.Certificate[] certs =
                        session.getLocalCertificates();
            if (certs != null) {
                principal = (X500Principal)
                    ((X509Certificate)certs[0]).getSubjectX500Principal();
            }
        }
        return principal;
    }

    /**
     * This method implements the SSL HandshakeCompleted callback,
     * remembering the resulting session so that it may be queried
     * for the current cipher suite and peer certificates.  Servers
     * sometimes re-initiate handshaking, so the session in use on
     * a given connection may change.  When sessions change, so may
     * peer identities and cipher suites.
     */
    public void handshakeCompleted(HandshakeCompletedEvent event)
    {
        session = event.getSession();
    }

    /**
     * @return the proxy host being used for this client, or null
     *          if we're not going through a proxy
     */
    public String getProxyHostUsed() {
        if (!needsTunneling()) {
            return null;
        } else {
            return ((InetSocketAddress)proxy.address()).getHostName();
        }
    }

    /**
     * @return the proxy port being used for this client.  Meaningless
     *          if getProxyHostUsed() gives null.
     */
    public int getProxyPortUsed() {
        return (proxy == null || proxy.type() == Proxy.Type.DIRECT ||
                proxy.type() == Proxy.Type.SOCKS)? -1:
            ((InetSocketAddress)proxy.address()).getPort();
    }
}
