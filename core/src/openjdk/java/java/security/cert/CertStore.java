/*
 * Copyright 2000-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.security.cert;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.util.Collection;

import sun.security.jca.*;
import sun.security.jca.GetInstance.Instance;

/**
 * A class for retrieving <code>Certificate</code>s and <code>CRL</code>s
 * from a repository.
 * <p>
 * This class uses a provider-based architecture.
 * To create a <code>CertStore</code>, call one of the static
 * <code>getInstance</code> methods, passing in the type of
 * <code>CertStore</code> desired, any applicable initialization parameters 
 * and optionally the name of the provider desired. 
 * <p>
 * Once the <code>CertStore</code> has been created, it can be used to 
 * retrieve <code>Certificate</code>s and <code>CRL</code>s by calling its
 * {@link #getCertificates(CertSelector selector) getCertificates} and
 * {@link #getCRLs(CRLSelector selector) getCRLs} methods.
 * <p>
 * Unlike a {@link java.security.KeyStore KeyStore}, which provides access
 * to a cache of private keys and trusted certificates, a 
 * <code>CertStore</code> is designed to provide access to a potentially
 * vast repository of untrusted certificates and CRLs. For example, an LDAP 
 * implementation of <code>CertStore</code> provides access to certificates
 * and CRLs stored in one or more directories using the LDAP protocol and the
 * schema as defined in the RFC service attribute. See Appendix A in the
 * <a href= "../../../../technotes/guides/security/certpath/CertPathProgGuide.html#AppA"> 
 * Java Certification Path API Programmer's Guide</a> for more information about
 * standard <code>CertStore</code> types.
 * <p>
 * <b>Concurrent Access</b>
 * <p>
 * All public methods of <code>CertStore</code> objects must be thread-safe. 
 * That is, multiple threads may concurrently invoke these methods on a
 * single <code>CertStore</code> object (or more than one) with no
 * ill effects. This allows a <code>CertPathBuilder</code> to search for a
 * CRL while simultaneously searching for further certificates, for instance.
 * <p>
 * The static methods of this class are also guaranteed to be thread-safe.
 * Multiple threads may concurrently invoke the static methods defined in
 * this class with no ill effects.
 *
 * @since	1.4
 * @author	Sean Mullan, Steve Hanna
 */
public class CertStore {
    /*
     * Constant to lookup in the Security properties file to determine
     * the default certstore type. In the Security properties file, the 
     * default certstore type is given as:
     * <pre>
     * certstore.type=LDAP
     * </pre>
     */
    private static final String CERTSTORE_TYPE = "certstore.type";
    private CertStoreSpi storeSpi;
    private Provider provider;
    private String type;
    private CertStoreParameters params;

    /**
     * Creates a <code>CertStore</code> object of the given type, and
     * encapsulates the given provider implementation (SPI object) in it.
     *
     * @param storeSpi the provider implementation
     * @param provider the provider
     * @param type the type
     * @param params the initialization parameters (may be <code>null</code>)
     */
    protected CertStore(CertStoreSpi storeSpi, Provider provider, 
			String type, CertStoreParameters params) {
        this.storeSpi = storeSpi;
        this.provider = provider;
        this.type = type;
	if (params != null)
	    this.params = (CertStoreParameters) params.clone();
    }

    /**
     * Returns a <code>Collection</code> of <code>Certificate</code>s that
     * match the specified selector. If no <code>Certificate</code>s
     * match the selector, an empty <code>Collection</code> will be returned.
     * <p>
     * For some <code>CertStore</code> types, the resulting
     * <code>Collection</code> may not contain <b>all</b> of the
     * <code>Certificate</code>s that match the selector. For instance,
     * an LDAP <code>CertStore</code> may not search all entries in the
     * directory. Instead, it may just search entries that are likely to
     * contain the <code>Certificate</code>s it is looking for. 
     * <p>
     * Some <code>CertStore</code> implementations (especially LDAP 
     * <code>CertStore</code>s) may throw a <code>CertStoreException</code> 
     * unless a non-null <code>CertSelector</code> is provided that 
     * includes specific criteria that can be used to find the certificates.
     * Issuer and/or subject names are especially useful criteria.
     *
     * @param selector A <code>CertSelector</code> used to select which
     *  <code>Certificate</code>s should be returned. Specify <code>null</code>
     *  to return all <code>Certificate</code>s (if supported).
     * @return A <code>Collection</code> of <code>Certificate</code>s that
     *         match the specified selector (never <code>null</code>)
     * @throws CertStoreException if an exception occurs 
     */
    public final Collection<? extends Certificate> getCertificates
	    (CertSelector selector) throws CertStoreException {
        return storeSpi.engineGetCertificates(selector);
    }
   
    /**
     * Returns a <code>Collection</code> of <code>CRL</code>s that
     * match the specified selector. If no <code>CRL</code>s
     * match the selector, an empty <code>Collection</code> will be returned.
     * <p>
     * For some <code>CertStore</code> types, the resulting
     * <code>Collection</code> may not contain <b>all</b> of the
     * <code>CRL</code>s that match the selector. For instance,
     * an LDAP <code>CertStore</code> may not search all entries in the
     * directory. Instead, it may just search entries that are likely to
     * contain the <code>CRL</code>s it is looking for. 
     * <p>
     * Some <code>CertStore</code> implementations (especially LDAP 
     * <code>CertStore</code>s) may throw a <code>CertStoreException</code> 
     * unless a non-null <code>CRLSelector</code> is provided that 
     * includes specific criteria that can be used to find the CRLs.
     * Issuer names and/or the certificate to be checked are especially useful.
     *
     * @param selector A <code>CRLSelector</code> used to select which
     *  <code>CRL</code>s should be returned. Specify <code>null</code>
     *  to return all <code>CRL</code>s (if supported).
     * @return A <code>Collection</code> of <code>CRL</code>s that
     *         match the specified selector (never <code>null</code>)
     * @throws CertStoreException if an exception occurs 
     */
    public final Collection<? extends CRL> getCRLs(CRLSelector selector)
	    throws CertStoreException {
        return storeSpi.engineGetCRLs(selector);
    }
   
    /**
     * Returns a <code>CertStore</code> object that implements the specified
     * <code>CertStore</code> type and is initialized with the specified
     * parameters.
     *
     * <p> This method traverses the list of registered security Providers,
     * starting with the most preferred Provider.
     * A new CertStore object encapsulating the
     * CertStoreSpi implementation from the first
     * Provider that supports the specified type is returned.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * <p>The <code>CertStore</code> that is returned is initialized with the 
     * specified <code>CertStoreParameters</code>. The type of parameters 
     * needed may vary between different types of <code>CertStore</code>s.
     * Note that the specified <code>CertStoreParameters</code> object is 
     * cloned.
     * 
     * @param type the name of the requested <code>CertStore</code> type.
     *  See Appendix A in the <a href=
     *  "../../../../technotes/guides/security/certpath/CertPathProgGuide.html#AppA">
     *  Java Certification Path API Programmer's Guide </a>
     *  for information about standard types.
     *
     * @param params the initialization parameters (may be <code>null</code>).
     *
     * @return a <code>CertStore</code> object that implements the specified
     *		<code>CertStore</code> type.
     *
     * @throws NoSuchAlgorithmException if no Provider supports a
     *		CertStoreSpi implementation for the specified type.
     *
     * @throws InvalidAlgorithmParameterException if the specified
     *		initialization parameters are inappropriate for this
     *		<code>CertStore</code>.
     *
     * @see java.security.Provider
     */
    public static CertStore getInstance(String type, CertStoreParameters params)
	    throws InvalidAlgorithmParameterException, 
	    NoSuchAlgorithmException {
	try {
	    Instance instance = GetInstance.getInstance("CertStore",
	    	CertStoreSpi.class, type, params);
	    return new CertStore((CertStoreSpi)instance.impl, 
	    	instance.provider, type, params);
	} catch (NoSuchAlgorithmException e) {
	    return handleException(e);
	}
    }
    
    private static CertStore handleException(NoSuchAlgorithmException e) 
	    throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
	Throwable cause = e.getCause();
	if (cause instanceof InvalidAlgorithmParameterException) {
	    throw (InvalidAlgorithmParameterException)cause;
	}
	throw e;
    }
    
    /**
     * Returns a <code>CertStore</code> object that implements the specified
     * <code>CertStore</code> type.
     *
     * <p> A new CertStore object encapsulating the
     * CertStoreSpi implementation from the specified provider
     * is returned.  The specified provider must be registered
     * in the security provider list.
     *
     * <p> Note that the list of registered providers may be retrieved via
     * the {@link Security#getProviders() Security.getProviders()} method.
     *
     * <p>The <code>CertStore</code> that is returned is initialized with the 
     * specified <code>CertStoreParameters</code>. The type of parameters 
     * needed may vary between different types of <code>CertStore</code>s.
     * Note that the specified <code>CertStoreParameters</code> object is 
     * cloned.
     *
     * @param type the requested <code>CertStore</code> type.
     *  See Appendix A in the <a href=
     *  "../../../../technotes/guides/security/certpath/CertPathProgGuide.html#AppA">
     *  Java Certification Path API Programmer's Guide </a>
     *  for information about standard types.
     *
     * @param params the initialization parameters (may be <code>null</code>).
     *
     * @param provider the name of the provider.
     *
     * @return a <code>CertStore</code> object that implements the
     *		specified type.
     *
     * @throws NoSuchAlgorithmException if a CertStoreSpi
     *		implementation for the specified type is not
     *		available from the specified provider.
     *
     * @throws InvalidAlgorithmParameterException if the specified
     *		initialization parameters are inappropriate for this 
     *		<code>CertStore</code>.
     *
     * @throws NoSuchProviderException if the specified provider is not
     *		registered in the security provider list.
     *
     * @exception IllegalArgumentException if the <code>provider</code> is
     *		null or empty.
     *
     * @see java.security.Provider
     */
    public static CertStore getInstance(String type, 
	    CertStoreParameters params, String provider)
	    throws InvalidAlgorithmParameterException, 
	    NoSuchAlgorithmException, NoSuchProviderException {
	try {
	    Instance instance = GetInstance.getInstance("CertStore",
	    	CertStoreSpi.class, type, params, provider);
	    return new CertStore((CertStoreSpi)instance.impl, 
	    	instance.provider, type, params);
	} catch (NoSuchAlgorithmException e) {
	    return handleException(e);
	}
    }
    
    /**
     * Returns a <code>CertStore</code> object that implements the specified
     * <code>CertStore</code> type.
     *
     * <p> A new CertStore object encapsulating the
     * CertStoreSpi implementation from the specified Provider
     * object is returned.  Note that the specified Provider object
     * does not have to be registered in the provider list.
     *
     * <p>The <code>CertStore</code> that is returned is initialized with the 
     * specified <code>CertStoreParameters</code>. The type of parameters 
     * needed may vary between different types of <code>CertStore</code>s.
     * Note that the specified <code>CertStoreParameters</code> object is 
     * cloned.
     *
     * @param type the requested <code>CertStore</code> type.
     *  See Appendix A in the <a href=
     *  "../../../../technotes/guides/security/certpath/CertPathProgGuide.html#AppA">
     *  Java Certification Path API Programmer's Guide </a>
     *  for information about standard types.
     *
     * @param params the initialization parameters (may be <code>null</code>).
     *
     * @param provider the provider.
     *
     * @return a <code>CertStore</code> object that implements the
     *		specified type.
     *
     * @exception NoSuchAlgorithmException if a CertStoreSpi
     *		implementation for the specified type is not available
     *		from the specified Provider object.
     *
     * @throws InvalidAlgorithmParameterException if the specified
     *		initialization parameters are inappropriate for this 
     *		<code>CertStore</code>
     *
     * @exception IllegalArgumentException if the <code>provider</code> is
     *		null.
     *
     * @see java.security.Provider
     */
    public static CertStore getInstance(String type, CertStoreParameters params,
	    Provider provider) throws NoSuchAlgorithmException, 
	    InvalidAlgorithmParameterException {
	try {
	    Instance instance = GetInstance.getInstance("CertStore",
	    	CertStoreSpi.class, type, params, provider);
	    return new CertStore((CertStoreSpi)instance.impl,
	    	instance.provider, type, params);
	} catch (NoSuchAlgorithmException e) {
	    return handleException(e);
	}
    }
    
    /**
     * Returns the parameters used to initialize this <code>CertStore</code>.
     * Note that the <code>CertStoreParameters</code> object is cloned before 
     * it is returned.
     *
     * @return the parameters used to initialize this <code>CertStore</code>
     * (may be <code>null</code>)
     */
    public final CertStoreParameters getCertStoreParameters() {
        return (params == null ? null : (CertStoreParameters) params.clone());
    }

    /**
     * Returns the type of this <code>CertStore</code>.
     *
     * @return the type of this <code>CertStore</code>
     */
    public final String getType() {
        return this.type;
    }

    /**
     * Returns the provider of this <code>CertStore</code>.
     *
     * @return the provider of this <code>CertStore</code>
     */
    public final Provider getProvider() {
        return this.provider;
    }

    /**
     * Returns the default <code>CertStore</code> type as specified in the 
     * Java security properties file, or the string &quot;LDAP&quot; if no 
     * such property exists. The Java security properties file is located in 
     * the file named &lt;JAVA_HOME&gt;/lib/security/java.security.
     * &lt;JAVA_HOME&gt; refers to the value of the java.home system property,
     * and specifies the directory where the JRE is installed.
     *
     * <p>The default <code>CertStore</code> type can be used by applications 
     * that do not want to use a hard-coded type when calling one of the
     * <code>getInstance</code> methods, and want to provide a default 
     * <code>CertStore</code> type in case a user does not specify its own.
     *
     * <p>The default <code>CertStore</code> type can be changed by setting 
     * the value of the "certstore.type" security property (in the Java 
     * security properties file) to the desired type.
     *
     * @return the default <code>CertStore</code> type as specified in the
     * Java security properties file, or the string &quot;LDAP&quot;
     * if no such property exists.
     */
    public final static String getDefaultType() {
        String cstype;
        cstype = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty(CERTSTORE_TYPE);
            }
        });
        if (cstype == null) {
            cstype = "LDAP";
        }
        return cstype;
    }
}
