/*
 * Copyright 2000-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.IOException;
import java.util.*;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathChecker;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.cert.X509CertSelector;
import javax.security.auth.x500.X500Principal;

import sun.security.util.Debug;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.PKIXExtensions;
import sun.security.x509.PolicyMappingsExtension;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;

/** 
 * This class represents a forward builder, which is able to retrieve
 * matching certificates from CertStores and verify a particular certificate
 * against a ForwardState.
 *
 * @since	1.4
 * @author      Yassir Elley
 * @author      Sean Mullan
 */
class ForwardBuilder extends Builder {
 
    private static final Debug debug = Debug.getInstance("certpath");
    private final Set<X509Certificate> trustedCerts;
    private final Set<X500Principal> trustedSubjectDNs;
    private final Set<TrustAnchor> trustAnchors;
    private X509CertSelector eeSelector;
    private X509CertSelector caSelector;
    private X509CertSelector caTargetSelector;
    TrustAnchor trustAnchor;
    private Comparator<X509Certificate> comparator;
    private boolean searchAllCertStores = true;

    /**
     * Initialize the builder with the input parameters.
     *
     * @param params the parameter set used to build a certification path
     */
    ForwardBuilder(PKIXBuilderParameters buildParams, 
	X500Principal targetSubjectDN, boolean searchAllCertStores) 
    {
	super(buildParams, targetSubjectDN);

	// populate sets of trusted certificates and subject DNs
	trustAnchors = buildParams.getTrustAnchors();
	trustedCerts = new HashSet<X509Certificate>(trustAnchors.size());
	trustedSubjectDNs = new HashSet<X500Principal>(trustAnchors.size());
        for (TrustAnchor anchor : trustAnchors) {
	    X509Certificate trustedCert = anchor.getTrustedCert();
	    if (trustedCert != null) {
		trustedCerts.add(trustedCert);
		trustedSubjectDNs.add(trustedCert.getSubjectX500Principal());
	    } else {
		trustedSubjectDNs.add(anchor.getCA());
	    }
	}
	comparator = new PKIXCertComparator(trustedSubjectDNs);
	this.searchAllCertStores = searchAllCertStores;
    }

    /**
     * Retrieves all certs from the specified CertStores that satisfy the
     * requirements specified in the parameters and the current
     * PKIX state (name constraints, policy constraints, etc).
     *
     * @param currentState the current state. 
     *        Must be an instance of <code>ForwardState</code>
     * @param certStores list of CertStores
     */
    Collection<X509Certificate> getMatchingCerts
	(State currentState, List<CertStore> certStores)
	throws CertStoreException, CertificateException, IOException
    {
	if (debug != null) {
	    debug.println("ForwardBuilder.getMatchingCerts()...");
	}

        ForwardState currState = (ForwardState) currentState;

	/*
         * We store certs in a Set because we don't want duplicates.
         * As each cert is added, it is sorted based on the PKIXCertComparator
         * algorithm.
	 */
	Set<X509Certificate> certs = new TreeSet<X509Certificate>(comparator);
	
	/*
	 * Only look for EE certs if search has just started.
	 */
        if (currState.isInitial()) {
            getMatchingEECerts(currState, certStores, certs);
	}
        getMatchingCACerts(currState, certStores, certs);

	return certs;
    }

    /*
     * Retrieves all end-entity certificates which satisfy constraints
     * and requirements specified in the parameters and PKIX state.
     */
    private void getMatchingEECerts(ForwardState currentState, 
	List<CertStore> certStores, Collection<X509Certificate> eeCerts) 
	throws IOException {

	if (debug != null) {
	    debug.println("ForwardBuilder.getMatchingEECerts()...");
	}
	/* 
	 * Compose a certificate matching rule to filter out 
         * certs which don't satisfy constraints
	 *
         * First, retrieve clone of current target cert constraints,
         * and then add more selection criteria based on current validation 
	 * state. Since selector never changes, cache local copy & reuse.
         */
	if (eeSelector == null) {
	    eeSelector = (X509CertSelector) targetCertConstraints.clone();

 	    /*
	     * Match on certificate validity date
	     */
	    eeSelector.setCertificateValid(date);

	    /*
	     * Policy processing optimizations
	     */
	    if (buildParams.isExplicitPolicyRequired()) {
	        eeSelector.setPolicy(getMatchingPolicies());
	    }
  	    /*
	     * Require EE certs
	     */
	    eeSelector.setBasicConstraints(-2);
	}
	
	/* Retrieve matching EE certs from CertStores */
	addMatchingCerts(eeSelector, certStores, eeCerts, searchAllCertStores);
    }

    /**
     * Retrieves all CA certificates which satisfy constraints
     * and requirements specified in the parameters and PKIX state.
     */
    private void getMatchingCACerts(ForwardState currentState, 
	List<CertStore> certStores, Collection<X509Certificate> caCerts) 
	throws IOException {

	if (debug != null) {
	    debug.println("ForwardBuilder.getMatchingCACerts()...");
	}
	int initialSize = caCerts.size();

	/*
	 * Compose a CertSelector to filter out 
         * certs which do not satisfy requirements.
	 */
	X509CertSelector sel = null;

	if (currentState.isInitial()) {
	    /* This means a CA is the target, so match on same stuff as 
             * getMatchingEECerts 
             */
	    if (debug != null) {
	        debug.println("ForwardBuilder.getMatchingCACerts(): ca is target");
	    }

	    if (caTargetSelector == null) {
	        caTargetSelector = (X509CertSelector) 
		    targetCertConstraints.clone();

	        /*
	         * Match on certificate validity date
	         */
	        caTargetSelector.setCertificateValid(date);

	        /*
	         * Policy processing optimizations
	         */
	        if (buildParams.isExplicitPolicyRequired())
	            caTargetSelector.setPolicy(getMatchingPolicies());
	    }

            /*
             * Require CA certs with a pathLenConstraint that allows
             * at least as many CA certs that have already been traversed
	     */
            caTargetSelector.setBasicConstraints(currentState.traversedCACerts);
	    sel = caTargetSelector;

        } else {

	    if (caSelector == null) {
		caSelector = new X509CertSelector();

	        /*
	         * Match on certificate validity date.
	         */
	        caSelector.setCertificateValid(date);

	        /*
	         * Policy processing optimizations
	         */
	        if (buildParams.isExplicitPolicyRequired())
	            caSelector.setPolicy(getMatchingPolicies());
	    }

	    /* 
    	     * Match on subject (issuer of previous cert)
	     */
	    caSelector.setSubject(currentState.issuerDN);
	    
	    /*
	     * Match on subjectNamesTraversed (both DNs and AltNames)
	     * (checks that current cert's name constraints permit it
	     * to certify all the DNs and AltNames that have been traversed)
	     */
	    CertPathHelper.setPathToNames
		(caSelector, currentState.subjectNamesTraversed);

	    /*
             * Require CA certs with a pathLenConstraint that allows
             * at least as many CA certs that have already been traversed
	     */
            caSelector.setBasicConstraints(currentState.traversedCACerts);
	    sel = caSelector;
	}

	/*
	 * Check if any of the trusted certs could be a match.
         * Since we are not validating the trusted cert, we can't
         * re-use the selector we've built up (sel) - we need
         * to use a new selector (trustedSel)
	 */
	X509CertSelector trustedSel = null;
        if (currentState.isInitial()) {
            trustedSel = targetCertConstraints;
        } else {
	    trustedSel = new X509CertSelector();
            trustedSel.setSubject(currentState.issuerDN);
        }

	boolean foundMatchingCert = false;
        for (X509Certificate trustedCert : trustedCerts) {
            if (trustedSel.match(trustedCert)) {
                if (debug != null) {
	            debug.println("ForwardBuilder.getMatchingCACerts: "
		        + "found matching trust anchor");
		}
                if (caCerts.add(trustedCert) && !searchAllCertStores) {
	    	    return;
		}
	    }
	}


	/*
	 * If we have already traversed as many CA certs as the maxPathLength
	 * will allow us to, then we don't bother looking through these
	 * certificate pairs. If maxPathLength has a value of -1, this
	 * means it is unconstrained, so we always look through the 
	 * certificate pairs.
	 */ 
	if (currentState.isInitial() ||
	   (buildParams.getMaxPathLength() == -1) ||
	   (buildParams.getMaxPathLength() > currentState.traversedCACerts)) 
	{
	    if (addMatchingCerts(sel, certStores, caCerts, searchAllCertStores)
	        && !searchAllCertStores) {
	        return;
	    }
	}

	if (!currentState.isInitial() && Builder.USE_AIA) {
 	    // check for AuthorityInformationAccess extension
            AuthorityInfoAccessExtension aiaExt = 
                currentState.cert.getAuthorityInfoAccessExtension();
            if (aiaExt != null) {
		getCerts(aiaExt, caCerts);
	    }
	}
	
	if (debug != null) {
	    int numCerts = caCerts.size() - initialSize;
	    debug.println("ForwardBuilder.getMatchingCACerts: found " + 
		numCerts + " CA certs");
	} 
    }

    /**
     * Download Certificates from the given AIA and add them to the
     * specified Collection.
     */
    private boolean getCerts(AuthorityInfoAccessExtension aiaExt,
        Collection<X509Certificate> certs) {
	if (Builder.USE_AIA == false) { 
            return false;
        } 
        List<AccessDescription> adList = aiaExt.getAccessDescriptions();
        if (adList == null || adList.isEmpty()) {
            return false;
        }

        boolean add = false;
        for (AccessDescription ad : adList) {
	    CertStore cs = URICertStore.getInstance(ad);
	    try {
                if (certs.addAll((Collection<X509Certificate>) 
		    cs.getCertificates(caSelector))) {
                    add = true;
                    if (!searchAllCertStores) {
                        return true;
                    }
                }
	    } catch (CertStoreException cse) {
		if (debug != null) {
		    debug.println("exception getting certs from CertStore:");
		    cse.printStackTrace();
		}
		continue;
	    }
        }
        return add;
    }

    /**
     * This inner class compares 2 PKIX certificates according to which
     * should be tried first when building a path from the target.
     * The preference order is as follows:
     *
     * Given trusted certificate(s):
     *    Subject:ou=D,ou=C,o=B,c=A
     * 
     * Preference order for current cert:
     *
     * 1) Issuer matches a trusted subject
     *    Issuer: ou=D,ou=C,o=B,c=A
     * 
     * 2) Issuer is a descendant of a trusted subject (in order of
     *    number of links to the trusted subject)
     *    a) Issuer: ou=E,ou=D,ou=C,o=B,c=A        [links=1]
     *    b) Issuer: ou=F,ou=E,ou=D,ou=C,ou=B,c=A  [links=2]
     * 
     * 3) Issuer is an ancestor of a trusted subject (in order of number of
     *    links to the trusted subject)
     *    a) Issuer: ou=C,o=B,c=A [links=1]
     *    b) Issuer: o=B,c=A      [links=2]
     * 
     * 4) Issuer is in the same namespace as a trusted subject (in order of
     *    number of links to the trusted subject)
     *    a) Issuer: ou=G,ou=C,o=B,c=A  [links=2]
     *    b) Issuer: ou=H,o=B,c=A       [links=3]
     * 
     * 5) Issuer is an ancestor of certificate subject (in order of number
     *    of links to the certificate subject)
     *    a) Issuer:  ou=K,o=J,c=A
     *       Subject: ou=L,ou=K,o=J,c=A
     *    b) Issuer:  o=J,c=A
     *       Subject: ou=L,ou=K,0=J,c=A
     * 
     * 6) Any other certificates
     */
    static class PKIXCertComparator implements Comparator<X509Certificate> {
	
	final static String METHOD_NME = "PKIXCertComparator.compare()";
	
	private final Set<X500Principal> trustedSubjectDNs;

	PKIXCertComparator(Set<X500Principal> trustedSubjectDNs) {
	    this.trustedSubjectDNs = trustedSubjectDNs;
	}
 
	/**
	 * @param oCert1 First X509Certificate to be compared
	 * @param oCert2 Second X509Certificate to be compared
	 * @return -1 if oCert1 is preferable to oCert2, or
	 *            if oCert1 and oCert2 are equally preferable (in this
	 *	      case it doesn't matter which is preferable, but we don't
	 *	      return 0 because the comparator would behave strangely
	 *	      when used in a SortedSet).
	 *          1 if oCert2 is preferable to oCert1
	 *          0 if oCert1.equals(oCert2). We only return 0 if the
	 *	    certs are equal so that this comparator behaves 
	 *	    correctly when used in a SortedSet.
	 * @throws ClassCastException if either argument is not of type 
	 * X509Certificate
	 */
	public int compare(X509Certificate oCert1, X509Certificate oCert2) {

	    // if certs are the same, return 0
	    if (oCert1.equals(oCert2)) return 0;

	    X500Principal cIssuer1 = oCert1.getIssuerX500Principal();
	    X500Principal cIssuer2 = oCert2.getIssuerX500Principal();
	    X500Name cIssuer1Name = X500Name.asX500Name(cIssuer1);
	    X500Name cIssuer2Name = X500Name.asX500Name(cIssuer2);

	    if (debug != null) {
	    	debug.println(METHOD_NME + " o1 Issuer:  " + cIssuer1);
	    	debug.println(METHOD_NME + " o2 Issuer:  " + cIssuer2);
	    }

	    /* If one cert's issuer matches a trusted subject, then it is
	     * preferable.
	     */
	    if (debug != null) {
	    	debug.println(METHOD_NME + " MATCH TRUSTED SUBJECT TEST...");
	    }
	    
	    boolean m1 = trustedSubjectDNs.contains(cIssuer1);
	    boolean m2 = trustedSubjectDNs.contains(cIssuer2);
	    if (debug != null) {
		debug.println(METHOD_NME + " m1: " + m1);
		debug.println(METHOD_NME + " m2: " + m2);
	    }
	    if (m1 && m2) {
	        return -1;
	    } else if (m1) {
	        return -1;
	    } else if (m2) {
	        return 1;
	    }
	    
	    /* If one cert's issuer is a naming descendant of a trusted subject,
	     * then it is preferable, in order of increasing naming distance.
	     */
	    if (debug != null) {
	    	debug.println(METHOD_NME + " NAMING DESCENDANT TEST...");
	    }
	    for (X500Principal tSubject : trustedSubjectDNs) {
		X500Name tSubjectName = X500Name.asX500Name(tSubject);
		int distanceTto1 = 
		    Builder.distance(tSubjectName, cIssuer1Name, -1);
	        int distanceTto2 = 
		    Builder.distance(tSubjectName, cIssuer2Name, -1);
		if (debug != null) {
		    debug.println(METHOD_NME +" distanceTto1: " + distanceTto1);
		    debug.println(METHOD_NME +" distanceTto2: " + distanceTto2);
		}
		if (distanceTto1 > 0 || distanceTto2 > 0) {
		    if (distanceTto1 == distanceTto2) {
			return -1;
		    } else if (distanceTto1 > 0 && distanceTto2 <= 0) {
			return -1;
		    } else if (distanceTto1 <= 0 && distanceTto2 > 0) {
			return 1;
		    } else if (distanceTto1 < distanceTto2) {
			return -1;
		    } else {    // distanceTto1 > distanceTto2
			return 1;
		    }
		}
	    }

	    /* If one cert's issuer is a naming ancestor of a trusted subject,
	     * then it is preferable, in order of increasing naming distance.
	     */
	    if (debug != null) {
	    	debug.println(METHOD_NME + " NAMING ANCESTOR TEST...");
	    }
	    for (X500Principal tSubject : trustedSubjectDNs) {
		X500Name tSubjectName = X500Name.asX500Name(tSubject);

		int distanceTto1 = Builder.distance
		    (tSubjectName, cIssuer1Name, Integer.MAX_VALUE);
	        int distanceTto2 = Builder.distance
		    (tSubjectName, cIssuer2Name, Integer.MAX_VALUE);
		if (debug != null) {
		    debug.println(METHOD_NME +" distanceTto1: " + distanceTto1);
		    debug.println(METHOD_NME +" distanceTto2: " + distanceTto2);
		}
		if (distanceTto1 < 0 || distanceTto2 < 0) {
		    if (distanceTto1 == distanceTto2) {
			return -1;
		    } else if (distanceTto1 < 0 && distanceTto2 >= 0) {
			return -1;
		    } else if (distanceTto1 >= 0 && distanceTto2 < 0) {
			return 1;
		    } else if (distanceTto1 > distanceTto2) {
			return -1;
		    } else {
			return 1;
		    }
		}
	    }

	    /* If one cert's issuer is in the same namespace as a trusted 
	     * subject, then it is preferable, in order of increasing naming 
	     * distance.
	     */
	    if (debug != null) {
	    	debug.println(METHOD_NME +" SAME NAMESPACE AS TRUSTED TEST...");
	    }
	    for (X500Principal tSubject : trustedSubjectDNs) {
		X500Name tSubjectName = X500Name.asX500Name(tSubject);
		X500Name tAo1 = tSubjectName.commonAncestor(cIssuer1Name);
		X500Name tAo2 = tSubjectName.commonAncestor(cIssuer2Name);
		if (debug != null) {
		    debug.println(METHOD_NME +" tAo1: " + String.valueOf(tAo1));
		    debug.println(METHOD_NME +" tAo2: " + String.valueOf(tAo2));
		}
		if (tAo1 != null || tAo2 != null) {
		    if (tAo1 != null && tAo2 != null) {
			int hopsTto1 = Builder.hops
			    (tSubjectName, cIssuer1Name, Integer.MAX_VALUE);
			int hopsTto2 = Builder.hops
			    (tSubjectName, cIssuer2Name, Integer.MAX_VALUE);
			if (debug != null) {
			    debug.println(METHOD_NME +" hopsTto1: " + hopsTto1);
			    debug.println(METHOD_NME +" hopsTto2: " + hopsTto2);
			}
			if (hopsTto1 == hopsTto2) {
			} else if (hopsTto1 > hopsTto2) {
			    return 1;
			} else {  // hopsTto1 < hopsTto2
			    return -1;
			}
		    } else if (tAo1 == null) {
			return 1;
		    } else {
			return -1;
		    }
		}
	    }
		

	    /* If one cert's issuer is an ancestor of that cert's subject,
	     * then it is preferable, in order of increasing naming distance.
	     */
	    if (debug != null) {
	    	debug.println(METHOD_NME+" CERT ISSUER/SUBJECT COMPARISON TEST...");
	    }
	    X500Principal cSubject1 = oCert1.getSubjectX500Principal();
	    X500Principal cSubject2 = oCert2.getSubjectX500Principal();
	    X500Name cSubject1Name = X500Name.asX500Name(cSubject1);
	    X500Name cSubject2Name = X500Name.asX500Name(cSubject2);

	    if (debug != null) {
	    	debug.println(METHOD_NME + " o1 Subject: " + cSubject1);
	    	debug.println(METHOD_NME + " o2 Subject: " + cSubject2);
	    }
	    int distanceStoI1 = Builder.distance
		(cSubject1Name, cIssuer1Name, Integer.MAX_VALUE);
	    int distanceStoI2 = Builder.distance
		(cSubject2Name, cIssuer2Name, Integer.MAX_VALUE);
	    if (debug != null) {
	    	debug.println(METHOD_NME + " distanceStoI1: " + distanceStoI1);
	    	debug.println(METHOD_NME + " distanceStoI2: " + distanceStoI2);
	    }
	    if (distanceStoI2 > distanceStoI1) {
		return -1;
	    } else if (distanceStoI2 < distanceStoI1) {
		return 1;
	    }

	    /* Otherwise, certs are equally preferable.
	     */
	    if (debug != null) {
	    	debug.println(METHOD_NME + " no tests matched; RETURN 0");
	    }
	    return -1;
	}
    }

    /**
     * Verifies a matching certificate. 
     *
     * This method executes the validation steps in the PKIX path 
     * validation algorithm <draft-ietf-pkix-new-part1-08.txt> which were 
     * not satisfied by the selection criteria used by getCertificates()
     * to find the certs and only the steps that can be executed in a 
     * forward direction (target to trust anchor). Those steps that can
     * only be executed in a reverse direction are deferred until the
     * complete path has been built. 
     *
     * Trust anchor certs are not validated, but are used to verify the
     * signature and revocation status of the previous cert.
     *
     * If the last certificate is being verified (the one whose subject
     * matches the target subject, then steps in 6.1.4 of the PKIX
     * Certification Path Validation algorithm are NOT executed,
     * regardless of whether or not the last cert is an end-entity
     * cert or not. This allows callers to certify CA certs as
     * well as EE certs.
     *
     * @param cert the certificate to be verified
     * @param currentState the current state against which the cert is verified
     * @param certPathList the certPathList generated thus far
     */
    void verifyCert(X509Certificate cert, State currentState, 
	List<X509Certificate> certPathList) throws GeneralSecurityException 
    {
	if (debug != null) {
	    debug.println("ForwardBuilder.verifyCert(SN: " 
		+ Debug.toHexString(cert.getSerialNumber()) 
		+ "\n  Issuer: " + cert.getIssuerX500Principal() + ")"
		+ "\n  Subject: " + cert.getSubjectX500Principal() + ")");
	}

        ForwardState currState = (ForwardState) currentState;

	/* 
	 * check for looping - abort a loop if 
	 * ((we encounter the same certificate twice) AND
	 * ((policyMappingInhibited = true) OR (no policy mapping
	 * extensions can be found between the occurences of the same
	 * certificate)))
	 */
	if (certPathList != null) {
	    boolean policyMappingFound = false;
	    for (X509Certificate cpListCert : certPathList) {
		X509CertImpl cpListCertImpl = X509CertImpl.toImpl(cpListCert);
		PolicyMappingsExtension policyMappingsExt 
		    = cpListCertImpl.getPolicyMappingsExtension();
		if (policyMappingsExt != null) {
		    policyMappingFound = true;
		}
		if (debug != null) {
		    debug.println("policyMappingFound = " + policyMappingFound);
		}
		if (cert.equals(cpListCert)) {
		    if ((buildParams.isPolicyMappingInhibited()) ||
			(!policyMappingFound)) {
			if (debug != null) {
			    debug.println("loop detected!!");
			}
			throw new CertPathValidatorException("loop detected");
		    }
		}
	    }
	}

	/* check if trusted cert */
	boolean isTrustedCert = trustedCerts.contains(cert);

        /* we don't perform any validation of the trusted cert */
        if (!isTrustedCert) {
	    /*
	     * Check CRITICAL private extensions for user checkers that
	     * support forward checking (forwardCheckers) and remove
	     * ones we know how to check.
	     */
            Set<String> unresCritExts = cert.getCriticalExtensionOIDs();
	    if (unresCritExts == null) {
	        unresCritExts = Collections.<String>emptySet();
	    }
	    for (PKIXCertPathChecker checker : currState.forwardCheckers) {
	        checker.check(cert, unresCritExts);
	    }
	
	    /* 
	     * Remove extensions from user checkers that don't support
	     * forward checking. After this step, we will have removed
	     * all extensions that all user checkers are capable of
	     * processing.
	     */
	    for (PKIXCertPathChecker checker : buildParams.getCertPathCheckers()) {
	        if (!checker.isForwardCheckingSupported()) {
		    Set<String> supportedExts = checker.getSupportedExtensions();
		    if (supportedExts != null) {
		        unresCritExts.removeAll(supportedExts);
		    }
	        }
	    }
	
	    /*
	     * Look at the remaining extensions and remove any ones we know how
             * to check. If there are any left, throw an exception!
	     */
	    if (!unresCritExts.isEmpty()) {
	        unresCritExts.remove(
		    PKIXExtensions.BasicConstraints_Id.toString());
	        unresCritExts.remove(
		    PKIXExtensions.NameConstraints_Id.toString());
	        unresCritExts.remove(
		    PKIXExtensions.CertificatePolicies_Id.toString());
	        unresCritExts.remove(
		    PKIXExtensions.PolicyMappings_Id.toString());
	        unresCritExts.remove(
		    PKIXExtensions.PolicyConstraints_Id.toString());
                unresCritExts.remove(
		    PKIXExtensions.InhibitAnyPolicy_Id.toString());
	        unresCritExts.remove(
		    PKIXExtensions.SubjectAlternativeName_Id.toString());
	        unresCritExts.remove(PKIXExtensions.KeyUsage_Id.toString());
	        unresCritExts.remove(
		    PKIXExtensions.ExtendedKeyUsage_Id.toString());

	        if (!unresCritExts.isEmpty())
	            throw new CertificateException("Unrecognized critical "
			+ "extension(s)");
	    }
        }

        /* 
         * if this is the target certificate (init=true), then we are
         * not able to do any more verification, so just return
         */
        if (currState.isInitial()) {
            return;
        }

        /* we don't perform any validation of the trusted cert */
        if (!isTrustedCert) {
            /* Make sure this is a CA cert */
            if (cert.getBasicConstraints() == -1) {
                throw new CertificateException("cert is NOT a CA cert");
	    }

            /*
             * Check keyUsage extension 
             */
            KeyChecker.verifyCAKeyUsage(cert);
        }

        /* 
         * the following checks are performed even when the cert
         * is a trusted cert, since we are only extracting the
         * subjectDN, and publicKey from the cert
         * in order to verify a previous cert
         */
        
        /*
         * Check revocation for the previous cert
         */
        if (buildParams.isRevocationEnabled()) {
            
            // first off, see if this cert can authorize revocation...
            if (CrlRevocationChecker.certCanSignCrl(cert)) {
                // And then check to be sure no key requiring key parameters 
		// has been encountered
                if (!currState.keyParamsNeeded())
 		    // If all that checks out, we can check the
 		    // revocation status of the cert. Otherwise,
 		    // we'll just wait until the end.
                    currState.crlChecker.check(currState.cert,
                                               cert.getPublicKey(),
                                               true);
            }
        }

        /*
         * Check signature only if no key requiring key parameters has been 
         * encountered.
         */
        if (!currState.keyParamsNeeded()) {
            (currState.cert).verify(cert.getPublicKey(),
                                    buildParams.getSigProvider());
        }
    }

    /**
     * Verifies whether the input certificate completes the path.
     * Checks the cert against each trust anchor that was specified, in order,
     * and returns true as soon as it finds a valid anchor.
     * Returns true if the cert matches a trust anchor specified as a 
     * certificate or if the cert verifies with a trust anchor that
     * was specified as a trusted {pubkey, caname} pair. Returns false if none
     * of the trust anchors are valid for this cert.
     * 
     * @param cert the certificate to test 
     * @return a boolean value indicating whether the cert completes the path.
     */
    boolean isPathCompleted(X509Certificate cert) {
	for (TrustAnchor anchor : trustAnchors) {
	    if (anchor.getTrustedCert() != null) {
	        if (cert.equals(anchor.getTrustedCert())) {
		    this.trustAnchor = anchor;
	            return true;
	        } else {
		    continue;
		}
	    }
	
	    X500Principal trustedCAName = anchor.getCA();
	    
	    /* Check subject/issuer name chaining */
	    if (!trustedCAName.equals(cert.getIssuerX500Principal())) {
	        continue;
	    }

	    /* Check revocation if it is enabled */
	    if (buildParams.isRevocationEnabled()) {
		try {
		    CrlRevocationChecker crlChecker =
			new CrlRevocationChecker(anchor, buildParams);
		    crlChecker.check(cert, anchor.getCAPublicKey(), true);
                } catch (CertPathValidatorException cpve) {
		    if (debug != null) {
		    	debug.println("ForwardBuilder.isPathCompleted() cpve");
		    	cpve.printStackTrace();
		    }
		    continue;
		}
	    }
	    
	    /*
	     * Check signature  
	     */
	    try {
                // NOTE: the DSA public key in the buildParams may lack
                // parameters, yet there is no key to inherit the parameters
                // from.  This is probably such a rare case that it is not worth
                // trying to detect the situation earlier.
                cert.verify(anchor.getCAPublicKey(),
                            buildParams.getSigProvider());
            } catch (InvalidKeyException ike) { 
                if (debug != null) { 
                    debug.println("ForwardBuilder.isPathCompleted() invalid " 
                        + "DSA key found"); 
                } 
                continue;
	    } catch (Exception e){
		if (debug != null) {
		    debug.println("ForwardBuilder.isPathCompleted() " +
			"unexpected exception");
		    e.printStackTrace();
		}
		continue;
	    }
	    
	    this.trustAnchor = anchor;
	    return true;
	}

	return false;
    }

    /** Adds the certificate to the certPathList
     *
     * @param cert the certificate to be added
     * @param certPathList the certification path list
     */
    void addCertToPath(X509Certificate cert, 
	LinkedList<X509Certificate> certPathList) {
	certPathList.addFirst(cert);
    }

    /** Removes final certificate from the certPathList
     *
     * @param certPathList the certification path list
     */
    void removeFinalCertFromPath(LinkedList<X509Certificate> certPathList) {
	certPathList.removeFirst();
    }
}
