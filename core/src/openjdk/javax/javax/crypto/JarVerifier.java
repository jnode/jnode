/*
 * Copyright 2007 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.crypto;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;
import javax.crypto.CryptoPolicyParser.ParsingException;

/**
 * This class verifies JAR files (and any supporting JAR files), and
 * determines whether they may be used in this implementation.
 *
 * The JCE in OpenJDK has an open cryptographic interface, meaning it
 * does not restrict which providers can be used.  Compliance with
 * United States export controls and with local law governing the
 * import/export of products incorporating the JCE in the OpenJDK is
 * the responsibility of the licensee.
 *
 * @since 1.7
 */
final class JarVerifier {

    // The URL for the JAR file we want to verify.
    private URL jarURL;
    private boolean savePerms;
    private CryptoPermissions appPerms = null;

    /**
     * Creates a JarVerifier object to verify the given URL.
     *
     * @param jarURL the JAR file to be verified.
     * @param savePerms if true, save the permissions allowed by the
     *          exemption mechanism
     */
    JarVerifier(URL jarURL, boolean savePerms) {
        this.jarURL = jarURL;
        this.savePerms = savePerms;
    }

    /**
     * Verify the JAR file is signed by an entity which has a certificate
     * issued by a trusted CA.
     *
     * In OpenJDK, we just need to examine the "cryptoperms" file to see
     * if any permissions were bundled together with this jar file.
     */
    void verify() throws JarException, IOException {

        // Short-circuit.  If we weren't asked to save any, we're done.
        if (!savePerms) {
            return;
        }

        // If the protocol of jarURL isn't "jar", we should
        // construct a JAR URL so we can open a JarURLConnection
        // for verifying this provider.
        final URL url = jarURL.getProtocol().equalsIgnoreCase("jar")?
                        jarURL : new URL("jar:" + jarURL.toString() + "!/");

        JarFile jf = null;
        try {

            // Get a link to the Jarfile to search.
            try {
                jf = (JarFile)
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws Exception {
                                JarURLConnection conn =
                                    (JarURLConnection) url.openConnection();
                                // You could do some caching here as
                                // an optimization.
                                conn.setUseCaches(false);
                                return conn.getJarFile();
                            }
                        });
            } catch (java.security.PrivilegedActionException pae) {
                SecurityException se = new SecurityException(
                    "Cannot load " + url.toString());
                se.initCause(pae);
                throw se;
            }

            if (jf != null) {
                JarEntry je = jf.getJarEntry("cryptoPerms");
                if (je == null) {
                    throw new JarException(
                        "Can not find cryptoPerms");
                }
                try {
                    appPerms = new CryptoPermissions();
                    appPerms.load(jf.getInputStream(je));
                } catch (Exception ex) {
                    JarException jex =
                        new JarException("Cannot load/parse" +
                            jarURL.toString());
                    jex.initCause(ex);
                    throw jex;
                }
            }
        } finally {
            // Only call close() when caching is not enabled.
            // Otherwise, exceptions will be thrown for all
            // subsequent accesses of this cached jar.
            if (jf != null) {
                jf.close();
            }
        }
    }

    /**
     * Verify that the provided JarEntry was indeed signed by the
     * framework signing certificate.
     *
     * @param je the URL of the jar entry to be checked.
     * @throws Exception if the jar entry was not signed by
     *          the proper certificate
     */
    static void verifyFrameworkSigned(URL je) throws Exception {
    }

    /**
     * Verify that the provided certs include the
     * framework signing certificate.
     *
     * @param certs the list of certs to be checked.
     * @throws Exception if the list of certs did not contain
     *          the framework signing certificate
     */
    static void verifyPolicySigned(java.security.cert.Certificate[] certs)
            throws Exception {
    }

    /**
     * Returns the permissions which are bundled with the JAR file,
     * aka the "cryptoperms" file.
     *
     * NOTE: if this JarVerifier instance is constructed with "savePerms"
     * equal to false, then this method would always return null.
     */
    CryptoPermissions getPermissions() {
        return appPerms;
    }
}
