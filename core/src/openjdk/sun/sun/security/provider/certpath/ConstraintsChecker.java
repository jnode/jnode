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

package sun.security.provider.certpath;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.CertPathValidatorException;
import sun.security.util.Debug;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.NameConstraintsExtension;
import sun.security.x509.X509CertImpl;

/**
 * ConstraintsChecker is a <code>PKIXCertPathChecker</code> that checks
 * constraints information on a PKIX certificate, namely basic constraints
 * and name constraints.
 *
 * @since	1.4
 * @author	Yassir Elley
 */
class ConstraintsChecker extends PKIXCertPathChecker {
 
    private static final Debug debug = Debug.getInstance("certpath");
    /* length of cert path */
    private final int certPathLength;
    /* current maximum path length (as defined in PKIX) */
    private int maxPathLength;
    /* current index of cert */
    private int i;
    private NameConstraintsExtension prevNC;

    private static Set<String> supportedExts;
    
    /**
     * Creates a ConstraintsChecker.
     *
     * @param certPathLength the length of the certification path
     * @throws CertPathValidatorException if the checker cannot be initialized
     */
    ConstraintsChecker(int certPathLength) throws CertPathValidatorException {
	this.certPathLength = certPathLength;
	init(false);
    }
    
    public void init(boolean forward) throws CertPathValidatorException {
	if (!forward) {
	    i = 0;
	    maxPathLength = certPathLength;
	    prevNC = null;
	} else {
	    throw new CertPathValidatorException
		("forward checking not supported");
	}
    }

    public boolean isForwardCheckingSupported() {
	return false;
    }

    public Set<String> getSupportedExtensions() {
	if (supportedExts == null) {
	    supportedExts = new HashSet<String>();
	    supportedExts.add(PKIXExtensions.BasicConstraints_Id.toString());
	    supportedExts.add(PKIXExtensions.NameConstraints_Id.toString());
	    supportedExts = Collections.unmodifiableSet(supportedExts);
	}
        return supportedExts;
    }

    /**
     * Performs the basic constraints and name constraints
     * checks on the certificate using its internal state. 
     *
     * @param cert the <code>Certificate</code> to be checked
     * @param unresCritExts a <code>Collection</code> of OID strings 
     * representing the current set of unresolved critical extensions
     * @throws CertPathValidatorException if the specified certificate 
     * does not pass the check
     */
    public void check(Certificate cert, Collection<String> unresCritExts) 
        throws CertPathValidatorException
    {
	X509Certificate currCert = (X509Certificate) cert;
	
	i++;
        // MUST run NC check second, since it depends on BC check to
        // update remainingCerts
	checkBasicConstraints(currCert);
	verifyNameConstraints(currCert);

	if (unresCritExts != null && !unresCritExts.isEmpty()) {
	    unresCritExts.remove(PKIXExtensions.BasicConstraints_Id.toString());
	    unresCritExts.remove(PKIXExtensions.NameConstraints_Id.toString());
	}
    }

    /**
     * Internal method to check the name constraints against a cert
     */
    private void verifyNameConstraints(X509Certificate currCert) 
        throws CertPathValidatorException
    {
	String msg = "name constraints";
	if (debug != null) {
	    debug.println("---checking " + msg + "...");
	}

        // check name constraints only if there is a previous name constraint
        // and either the currCert is the final cert or the currCert is not
        // self-issued
        if (prevNC != null && ((i == certPathLength) ||
		!X509CertImpl.isSelfIssued(currCert))) {
            if (debug != null) {
                debug.println("prevNC = " + prevNC);
                debug.println("currDN = " + currCert.getSubjectX500Principal());
            }

            try {
                if (!prevNC.verify(currCert)) {
                    throw new CertPathValidatorException(msg + " check failed");
                }
            } catch (IOException ioe) {
                throw new CertPathValidatorException(ioe);
            }
        }
         
        // merge name constraints regardless of whether cert is self-issued
        prevNC = mergeNameConstraints(currCert, prevNC);
        	
	if (debug != null)
	    debug.println(msg + " verified.");	
    }

    /**
     * Helper to fold sets of name constraints together
     */
    static NameConstraintsExtension 
        mergeNameConstraints(X509Certificate currCert, 
            NameConstraintsExtension prevNC) throws CertPathValidatorException 
    {
	X509CertImpl currCertImpl;
	try {
	    currCertImpl = X509CertImpl.toImpl(currCert);
	} catch (CertificateException ce) {
	    throw new CertPathValidatorException(ce);
	}

	NameConstraintsExtension newConstraints = 
	    currCertImpl.getNameConstraintsExtension();
	
	if (debug != null) {
	    debug.println("prevNC = " + prevNC);
	    debug.println("newNC = " + String.valueOf(newConstraints));
	}
	
	// if there are no previous name constraints, we just return the
	// new name constraints.
	if (prevNC == null) {
	    if (debug != null) {
	    	debug.println("mergedNC = " + String.valueOf(newConstraints));
	    }
	    if (newConstraints == null) {
		return newConstraints;
 	    } else {
 		// Make sure we do a clone here, because we're probably
 		// going to modify this object later and we don't want to
 		// be sharing it with a Certificate object!
 		return (NameConstraintsExtension) newConstraints.clone();
 	    }
	} else {
	    try {
		// after merge, prevNC should contain the merged constraints
		prevNC.merge(newConstraints);
	    } catch (IOException ioe) {
		throw new CertPathValidatorException(ioe);
	    }
	    if (debug != null) {
	    	debug.println("mergedNC = " + prevNC);
	    }
	    return prevNC;
	}
    }

    /**
     * Internal method to check that a given cert meets basic constraints.
     */
    private void checkBasicConstraints(X509Certificate currCert) 
        throws CertPathValidatorException
    {
	String msg = "basic constraints";
	if (debug != null) {
	    debug.println("---checking " + msg + "...");
	    debug.println("i = " + i);
	    debug.println("maxPathLength = " + maxPathLength);	
	}

        /* check if intermediate cert */
	if (i < certPathLength) {
	    int pathLenConstraint = currCert.getBasicConstraints();
	    if (pathLenConstraint == -1) {
		throw new CertPathValidatorException(msg + " check failed: " 
		    + "this is not a CA certificate");
	    }
 
	    if (!X509CertImpl.isSelfIssued(currCert)) {
		if (maxPathLength <= 0) {
	    	   throw new CertPathValidatorException
			(msg + " check failed: pathLenConstraint violated - "
			 + "this cert must be the last cert in the "
			 + "certification path");
		}
	        maxPathLength--;
	    }
	    if (pathLenConstraint < maxPathLength)
		maxPathLength = pathLenConstraint;
	}

	if (debug != null) {
	    debug.println("after processing, maxPathLength = " + maxPathLength);
	    debug.println(msg + " verified.");	
	}
    }

    /**
     * Merges the specified maxPathLength with the pathLenConstraint 
     * obtained from the certificate.
     *
     * @param cert the <code>X509Certificate</code> 
     * @param maxPathLength the previous maximum path length
     * @return the new maximum path length constraint (-1 means no more 
     * certificates can follow, Integer.MAX_VALUE means path length is
     * unconstrained)
     */
    static int mergeBasicConstraints(X509Certificate cert, int maxPathLength) {

	int pathLenConstraint = cert.getBasicConstraints();

        if (!X509CertImpl.isSelfIssued(cert)) {
            maxPathLength--;
	}

        if (pathLenConstraint < maxPathLength) {
            maxPathLength = pathLenConstraint;
	}

	return maxPathLength;
    }
}
