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

import java.io.ByteArrayInputStream;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * An immutable sequence of certificates (a certification path).
 * <p>
 * This is an abstract class that defines the methods common to all
 * <code>CertPath</code>s. Subclasses can handle different kinds of
 * certificates (X.509, PGP, etc.).
 * <p>
 * All <code>CertPath</code> objects have a type, a list of 
 * <code>Certificate</code>s, and one or more supported encodings. Because the 
 * <code>CertPath</code> class is immutable, a <code>CertPath</code> cannot 
 * change in any externally visible way after being constructed. This 
 * stipulation applies to all public fields and methods of this class and any 
 * added or overridden by subclasses.
 * <p>
 * The type is a <code>String</code> that identifies the type of
 * <code>Certificate</code>s in the certification path. For each
 * certificate <code>cert</code> in a certification path <code>certPath</code>,
 * <code>cert.getType().equals(certPath.getType())</code> must be
 * <code>true</code>.
 * <p>
 * The list of <code>Certificate</code>s is an ordered <code>List</code> of
 * zero or more <code>Certificate</code>s. This <code>List</code> and all
 * of the <code>Certificate</code>s contained in it must be immutable.
 * <p>
 * Each <code>CertPath</code> object must support one or more encodings
 * so that the object can be translated into a byte array for storage or
 * transmission to other parties. Preferably, these encodings should be
 * well-documented standards (such as PKCS#7). One of the encodings supported
 * by a <code>CertPath</code> is considered the default encoding. This
 * encoding is used if no encoding is explicitly requested (for the
 * {@link #getEncoded() getEncoded()} method, for instance).
 * <p>
 * All <code>CertPath</code> objects are also <code>Serializable</code>. 
 * <code>CertPath</code> objects are resolved into an alternate 
 * {@link CertPathRep CertPathRep} object during serialization. This allows
 * a <code>CertPath</code> object to be serialized into an equivalent
 * representation regardless of its underlying implementation.
 * <p>
 * <code>CertPath</code> objects can be created with a
 * <code>CertificateFactory</code> or they can be returned by other classes,
 * such as a <code>CertPathBuilder</code>.
 * <p>
 * By convention, X.509 <code>CertPath</code>s (consisting of
 * <code>X509Certificate</code>s), are ordered starting with the target 
 * certificate and ending with a certificate issued by the trust anchor. That 
 * is, the issuer of one certificate is the subject of the following one. The 
 * certificate representing the {@link TrustAnchor TrustAnchor} should not be 
 * included in the certification path. Unvalidated X.509 <code>CertPath</code>s 
 * may not follow these conventions. PKIX <code>CertPathValidator</code>s will 
 * detect any departure from these conventions that cause the certification 
 * path to be invalid and throw a <code>CertPathValidatorException</code>.
 * <p>
 * <b>Concurrent Access</b>
 * <p>
 * All <code>CertPath</code> objects must be thread-safe. That is, multiple
 * threads may concurrently invoke the methods defined in this class on a
 * single <code>CertPath</code> object (or more than one) with no
 * ill effects. This is also true for the <code>List</code> returned by
 * <code>CertPath.getCertificates</code>.
 * <p>
 * Requiring <code>CertPath</code> objects to be immutable and thread-safe
 * allows them to be passed around to various pieces of code without worrying
 * about coordinating access.  Providing this thread-safety is
 * generally not difficult, since the <code>CertPath</code> and
 * <code>List</code> objects in question are immutable.
 *
 * @see CertificateFactory
 * @see CertPathBuilder
 *
 * @author	Yassir Elley
 * @since	1.4
 */
public abstract class CertPath implements Serializable {

    private static final long serialVersionUID = 6068470306649138683L;
   
    private String type;        // the type of certificates in this chain

    /**
     * Creates a <code>CertPath</code> of the specified type.
     * <p>
     * This constructor is protected because most users should use a
     * <code>CertificateFactory</code> to create <code>CertPath</code>s.
     *
     * @param type the standard name of the type of
     * <code>Certificate</code>s in this path
     */
    protected CertPath(String type) {
        this.type = type;
    }

    /**
     * Returns the type of <code>Certificate</code>s in this certification
     * path. This is the same string that would be returned by
     * {@link java.security.cert.Certificate#getType() cert.getType()}
     * for all <code>Certificate</code>s in the certification path.
     *
     * @return the type of <code>Certificate</code>s in this certification
     * path (never null)
     */
    public String getType() {
        return type;
    }

    /**
     * Returns an iteration of the encodings supported by this certification 
     * path, with the default encoding first. Attempts to modify the returned
     * <code>Iterator</code> via its <code>remove</code> method result in an
     * <code>UnsupportedOperationException</code>.
     *
     * @return an <code>Iterator</code> over the names of the supported
     *         encodings (as Strings)
     */
    public abstract Iterator<String> getEncodings();

    /**
     * Compares this certification path for equality with the specified
     * object. Two <code>CertPath</code>s are equal if and only if their
     * types are equal and their certificate <code>List</code>s (and by
     * implication the <code>Certificate</code>s in those <code>List</code>s)
     * are equal. A <code>CertPath</code> is never equal to an object that is
     * not a <code>CertPath</code>.
     * <p>
     * This algorithm is implemented by this method. If it is overridden,
     * the behavior specified here must be maintained.
     *
     * @param other the object to test for equality with this certification path
     * @return true if the specified object is equal to this certification path,
     * false otherwise
     */
    public boolean equals(Object other) {
        if (this == other)
            return true;
	
        if (! (other instanceof CertPath))
            return false;

        CertPath otherCP = (CertPath) other;
        if (! otherCP.getType().equals(type))
            return false;

        List<? extends Certificate> thisCertList = this.getCertificates();
        List<? extends Certificate> otherCertList = otherCP.getCertificates();
        return(thisCertList.equals(otherCertList));
    }

    /**
     * Returns the hashcode for this certification path. The hash code of
     * a certification path is defined to be the result of the following
     * calculation:
     * <pre><code>
     *  hashCode = path.getType().hashCode();
     *  hashCode = 31*hashCode + path.getCertificates().hashCode();
     * </code></pre>
     * This ensures that <code>path1.equals(path2)</code> implies that
     * <code>path1.hashCode()==path2.hashCode()</code> for any two certification
     * paths, <code>path1</code> and <code>path2</code>, as required by the
     * general contract of <code>Object.hashCode</code>.
     *
     * @return the hashcode value for this certification path
     */
    public int hashCode() {
        int hashCode = type.hashCode();
        hashCode = 31*hashCode + getCertificates().hashCode();
        return hashCode;
    }

    /**
     * Returns a string representation of this certification path.
     * This calls the <code>toString</code> method on each of the
     * <code>Certificate</code>s in the path.
     *
     * @return a string representation of this certification path
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator<? extends Certificate> stringIterator =
					getCertificates().iterator();

        sb.append("\n" + type + " Cert Path: length = " 
	    + getCertificates().size() + ".\n");
        sb.append("[\n");
        int i = 1;
        while (stringIterator.hasNext()) {
            sb.append("=========================================="
		+ "===============Certificate " + i + " start.\n");
            Certificate stringCert = stringIterator.next();
            sb.append(stringCert.toString());
            sb.append("\n========================================"
		+ "=================Certificate " + i + " end.\n\n\n");
            i++;
        }

        sb.append("\n]");	 
        return sb.toString();
    }

    /**
     * Returns the encoded form of this certification path, using the default
     * encoding.
     *
     * @return the encoded bytes
     * @exception CertificateEncodingException if an encoding error occurs
     */
    public abstract byte[] getEncoded()
        throws CertificateEncodingException;

    /**
     * Returns the encoded form of this certification path, using the
     * specified encoding.
     *
     * @param encoding the name of the encoding to use
     * @return the encoded bytes
     * @exception CertificateEncodingException if an encoding error occurs or
     *   the encoding requested is not supported
     */
    public abstract byte[] getEncoded(String encoding)
        throws CertificateEncodingException;

    /**
     * Returns the list of certificates in this certification path.
     * The <code>List</code> returned must be immutable and thread-safe.
     *
     * @return an immutable <code>List</code> of <code>Certificate</code>s
     *         (may be empty, but not null)
     */
    public abstract List<? extends Certificate> getCertificates();

    /**
     * Replaces the <code>CertPath</code> to be serialized with a 
     * <code>CertPathRep</code> object.
     *
     * @return the <code>CertPathRep</code> to be serialized
     *
     * @throws ObjectStreamException if a <code>CertPathRep</code> object 
     * representing this certification path could not be created
     */
    protected Object writeReplace() throws ObjectStreamException {
        try {
            return new CertPathRep(type, getEncoded());
        } catch (CertificateException ce) {
	    NotSerializableException nse = 
		new NotSerializableException
		    ("java.security.cert.CertPath: " + type);
	    nse.initCause(ce);
	    throw nse;
        }
    }

    /**
     * Alternate <code>CertPath</code> class for serialization.
     * @since 1.4
     */
    protected static class CertPathRep implements Serializable {

	private static final long serialVersionUID = 3015633072427920915L;

        /** The Certificate type */
        private String type;
        /** The encoded form of the cert path */
        private byte[] data;

        /**
         * Creates a <code>CertPathRep</code> with the specified 
         * type and encoded form of a certification path.
         *
         * @param type the standard name of a <code>CertPath</code> type
         * @param data the encoded form of the certification path
         */
        protected CertPathRep(String type, byte[] data) {
            this.type = type;
            this.data = data;
        }

        /**
         * Returns a <code>CertPath</code> constructed from the type and data.
         *
         * @return the resolved <code>CertPath</code> object
         *
         * @throws ObjectStreamException if a <code>CertPath</code> could not
         * be constructed
         */
        protected Object readResolve() throws ObjectStreamException {
            try {
                CertificateFactory cf = CertificateFactory.getInstance(type);
                return cf.generateCertPath(new ByteArrayInputStream(data));
            } catch (CertificateException ce) {
	        NotSerializableException nse = 
		    new NotSerializableException
		        ("java.security.cert.CertPath: " + type);
	        nse.initCause(ce);
	        throw nse;
            }
        }
    }
}
