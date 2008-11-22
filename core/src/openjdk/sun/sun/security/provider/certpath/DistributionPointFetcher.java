/*
 * Copyright 2002-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.*;
import java.net.URI;
import java.util.*;
import java.security.*;
import java.security.cert.*;
import javax.security.auth.x500.X500Principal;

import sun.security.action.GetPropertyAction;
import sun.security.util.Debug;
import sun.security.x509.*;

/**
 * Class to obtain CRLs via the CRLDistributionPoints extension.
 * Note that the functionality of this class must be explicitly enabled
 * via a system property, see the USE_CRLDP variable below.
 *
 * This class uses the URICertStore class to fetch CRLs. The URICertStore
 * class also implements CRL caching: see the class description for more
 * information. 
 *
 * @author Andreas Sterbenz
 * @author Sean Mullan
 * @since 1.4.2
 */
class DistributionPointFetcher {
    
    private static final Debug debug = Debug.getInstance("certpath");

    private static final boolean[] ALL_REASONS = 
	{true, true, true, true, true, true, true, true, true};

    /**
     * Flag indicating whether support for the CRL distribution point
     * extension shall be enabled. Currently disabled by default for
     * compatibility and legal reasons.
     */
    private final static boolean USE_CRLDP = 
    	getBooleanProperty("com.sun.security.enableCRLDP", false);

    /**
     * Return the value of the boolean System property propName.
     */
    public static boolean getBooleanProperty(String propName,
	    boolean defaultValue) {
	// if set, require value of either true or false
        String b = AccessController.doPrivileged(
		new GetPropertyAction(propName));
	if (b == null) {
	    return defaultValue;
	} else if (b.equalsIgnoreCase("false")) {
	    return false;
	} else if (b.equalsIgnoreCase("true")) {
	    return true;
	} else {
	    throw new RuntimeException("Value of " + propName
	    + " must either be 'true' or 'false'");
	}
    }
	
    // singleton instance
    private static final DistributionPointFetcher INSTANCE = 
    	new DistributionPointFetcher();
    
    /** 
     * Private instantiation only.
     */
    private DistributionPointFetcher() {}
    
    /**
     * Return a DistributionPointFetcher instance.
     */
    static DistributionPointFetcher getInstance() {
	return INSTANCE;
    }
    
    /**
     * Return the X509CRLs matching this selector. The selector must be
     * an X509CRLSelector with certificateChecking set.
     *
     * If CRLDP support is disabled, this method always returns an
     * empty set.
     */
    Collection<X509CRL> getCRLs(X509CRLSelector selector, boolean signFlag,
	PublicKey prevKey, String provider, List<CertStore> certStores, 
	boolean[] reasonsMask, TrustAnchor anchor) throws CertStoreException 
    {
	if (USE_CRLDP == false) {
	    return Collections.emptySet();
	}
        X509Certificate cert = selector.getCertificateChecking();
	if (cert == null) {
	    return Collections.emptySet();
	}
	try {
	    X509CertImpl certImpl = X509CertImpl.toImpl(cert);
	    if (debug != null) {
		debug.println("DistributionPointFetcher.getCRLs: Checking "
			+ "CRLDPs for " + certImpl.getSubjectX500Principal());
	    }
	    CRLDistributionPointsExtension ext = 
	    	certImpl.getCRLDistributionPointsExtension();
	    if (ext == null) {
		if (debug != null) {
		    debug.println("No CRLDP ext");
		}
		return Collections.emptySet();
	    }
	    List<DistributionPoint> points = (List<DistributionPoint>)ext.get(
					CRLDistributionPointsExtension.POINTS);
	    Set<X509CRL> results = new HashSet<X509CRL>();
	    for (Iterator<DistributionPoint> t = points.iterator(); 
		 t.hasNext() && !Arrays.equals(reasonsMask, ALL_REASONS); ) {
		DistributionPoint point = t.next();
                Collection<X509CRL> crls = getCRLs(selector, certImpl,
		    point, reasonsMask, signFlag, prevKey, provider, 
		    certStores, anchor);
		results.addAll(crls);
	    }
	    if (debug != null) {
		debug.println("Returning " + results.size() + " CRLs");
	    }
	    return results;
	} catch (CertificateException e) {
	    return Collections.emptySet();
	} catch (IOException e) {
	    return Collections.emptySet();
	}
    }
    
    /**
     * Download CRLs from the given distribution point, verify and return them.
     * See the top of the class for current limitations.
     */
    private Collection<X509CRL> getCRLs(X509CRLSelector selector, 
	X509CertImpl certImpl, DistributionPoint point, boolean[] reasonsMask, 
	boolean signFlag, PublicKey prevKey, String provider, 
	List<CertStore> certStores, TrustAnchor anchor) 
    {
        // check for full name
        GeneralNames fullName = point.getFullName(); 
        if (fullName == null) { 
            // check for relative name
            RDN relativeName = point.getRelativeName();
            if (relativeName == null) {
            return Collections.emptySet();
        } 
            try {
                GeneralNames crlIssuers = point.getCRLIssuer();
                if (crlIssuers == null) {
                    fullName = getFullNames
                        ((X500Name) certImpl.getIssuerDN(), relativeName);
                } else {
                    // should only be one CRL Issuer
                    if (crlIssuers.size() != 1) {
                        return Collections.emptySet();
                    } else {
                        fullName = getFullNames
                            ((X500Name) crlIssuers.get(0).getName(), relativeName);
                    }
                }
            } catch (IOException ioe) {
                return Collections.emptySet();
            }
        }
	Collection<X509CRL> possibleCRLs = new ArrayList<X509CRL>();
	Collection<X509CRL> crls = new ArrayList<X509CRL>(2);
	for (Iterator<GeneralName> t = fullName.iterator(); t.hasNext(); ) {
	    GeneralName name = t.next();
	    if (name.getType() == GeneralNameInterface.NAME_DIRECTORY) {
		X500Name x500Name = (X500Name) name.getName();
		possibleCRLs.addAll(
		    getCRLs(x500Name, certImpl.getIssuerX500Principal(), 
			    certStores));
	    } else if (name.getType() == GeneralNameInterface.NAME_URI) {
	        URIName uriName = (URIName)name.getName();
		X509CRL crl = getCRL(uriName);
		if (crl != null) {
		    possibleCRLs.add(crl);
		}
	    }
	}

	for (X509CRL crl : possibleCRLs) {
	    try {
	        // make sure issuer is not set
		// we check the issuer in verifyCRLs method
	        selector.setIssuerNames(null);
	        if (selector.match(crl) && verifyCRL(certImpl, point, crl, 
	    		reasonsMask, signFlag, prevKey, provider, anchor, 
		    	certStores)) {
		    crls.add(crl);
	        }
	    } catch (Exception e) {
		// don't add the CRL
	        if (debug != null) {
		    debug.println("Exception verifying CRL: " + e.getMessage());
		    e.printStackTrace();
	        }
	    }
	}
	return crls;
    }

    /**
     * Download CRL from given URI.
     */ 
    private X509CRL getCRL(URIName name) {
        URI uri = name.getURI();
        if (debug != null) {
	    debug.println("Trying to fetch CRL from DP " + uri);
        }
        try {
	    CertStore ucs = URICertStore.getInstance
	        (new URICertStore.URICertStoreParameters(uri));
            Collection<? extends CRL> crls = ucs.getCRLs(null);
	    if (crls.isEmpty()) {
	        return null;
	    } else {
	        return (X509CRL) crls.iterator().next();
	    }
        } catch (Exception e) {
            if (debug != null) {
                debug.println("Exception getting CRL from CertStore: " + e);
                e.printStackTrace();
            }
        }
	return null;
    }

    /**
     * Fetch CRLs from certStores.
     */ 
    private Collection<X509CRL> getCRLs(X500Name name, 
	X500Principal certIssuer, List<CertStore> certStores) 
    {
        if (debug != null) {
	    debug.println("Trying to fetch CRL from DP " + name);
        }
	X509CRLSelector xcs = new X509CRLSelector();
	xcs.addIssuer(name.asX500Principal());
	xcs.addIssuer(certIssuer);
	Collection<X509CRL> crls = new ArrayList<X509CRL>();
	for (CertStore store : certStores) {
	    try {
		for (CRL crl : store.getCRLs(xcs)) {
		    crls.add((X509CRL)crl);
		}
	    } catch (CertStoreException cse) {
		// don't add the CRL
		if (debug != null) {
		    debug.println("Non-fatal exception while retrieving " +
			"CRLs: " + cse);
		    cse.printStackTrace();
		}
	    }
	}
	return crls;
    }

    /**
     * Verifies a CRL for the given certificate's Distribution Point to 
     * ensure it is appropriate for checking the revocation status.
     *
     * @param certImpl the certificate whose revocation status is being checked
     * @param point one of the distribution points of the certificate
     * @param crl the CRL
     * @param reasonsMask the interim reasons mask
     * @param signFlag true if prevKey can be used to verify the CRL
     * @param prevKey the public key that verifies the certificate's signature
     * @param provider the Signature provider to use
     * @return true if ok, false if not
     */
    boolean verifyCRL(X509CertImpl certImpl, DistributionPoint point, 
	X509CRL crl, boolean[] reasonsMask, boolean signFlag, 
	PublicKey prevKey, String provider, TrustAnchor anchor, 
	List<CertStore> certStores) throws CRLException, IOException {
	boolean indirectCRL = false;
        X509CRLImpl crlImpl = X509CRLImpl.toImpl(crl);
        IssuingDistributionPointExtension idpExt = 
	    crlImpl.getIssuingDistributionPointExtension();
        X500Name certIssuer = (X500Name) certImpl.getIssuerDN();
        X500Name crlIssuer = (X500Name) crlImpl.getIssuerDN();

	// if crlIssuer is set, verify that it matches the issuer of the 
	// CRL and the CRL contains an IDP extension with the indirectCRL 
	// boolean asserted. Otherwise, verify that the CRL issuer matches the 
	// certificate issuer.
	GeneralNames pointCrlIssuers = point.getCRLIssuer();
	X500Name pointCrlIssuer = null;
	if (pointCrlIssuers != null) {
	    if (idpExt == null || 
		((Boolean) idpExt.get
		    (IssuingDistributionPointExtension.INDIRECT_CRL)).equals
			(Boolean.FALSE)) {
		return false;
	    }
	    boolean match = false;
            for (Iterator<GeneralName> t = pointCrlIssuers.iterator(); 
		 !match && t.hasNext(); ) {
	        GeneralNameInterface name = t.next().getName();
	        if (crlIssuer.equals(name) == true) {
		    pointCrlIssuer = (X500Name) name;
		    match = true;
		}
	    }
	    if (match == false) {
	        return false;
	    }
	    indirectCRL = true;
	} else if (crlIssuer.equals(certIssuer) == false) {
	    if (debug != null) {
	        debug.println("crl issuer does not equal cert issuer");
	    }
	    return false;
	}

	if (!indirectCRL && !signFlag) {
	    // cert's key cannot be used to verify the CRL
	    return false;
	}

	if (idpExt != null) {
	    DistributionPointName idpPoint = (DistributionPointName)
	        idpExt.get(IssuingDistributionPointExtension.POINT);
	    if (idpPoint != null) {
                GeneralNames idpNames = idpPoint.getFullName();
	        if (idpNames == null) {
		    RDN relativeName = idpPoint.getRelativeName();
		    if (relativeName == null) {
			if (debug != null) {
		           debug.println("IDP must be relative or full DN");
			}
		        return false;
		    }
		    if (debug != null) {
			debug.println("IDP relativeName:" + relativeName);
		    }
		    idpNames = getFullNames(crlIssuer, relativeName);
		}
		// if the DP name is present in the IDP CRL extension and the
		// DP field is present in the DP, then verify that one of the
		// names in the IDP matches one of the names in the DP
		if (point.getFullName() != null ||
		    point.getRelativeName() != null) {
		    GeneralNames pointNames = point.getFullName();
		    if (pointNames == null) {
		        RDN relativeName = point.getRelativeName();
		        if (relativeName == null) {
			    if (debug != null) {
		                debug.println("DP must be relative or full DN");
			    }
		            return false;
		        }
		        if (debug != null) {
			    debug.println("DP relativeName:" + relativeName);
		        }
			if (indirectCRL) {
			    if (pointCrlIssuers.size() != 1) {
				// RFC 3280: there must be only 1 CRL issuer
				// name when relativeName is present
			        if (debug != null) {
			            debug.println("must only be one CRL " +
					"issuer when relative name present");
			        }
				return false;
			    }
		            pointNames = getFullNames
				(pointCrlIssuer, relativeName);
			} else {
		            pointNames = getFullNames(certIssuer, relativeName);
			}
		    }
		    boolean match = false;
       	            for (Iterator<GeneralName> i = idpNames.iterator(); 
		         !match && i.hasNext(); ) {
	        	GeneralNameInterface idpName = i.next().getName();
			if (debug != null) {
			    debug.println("idpName: " + idpName);
			}
       	                for (Iterator<GeneralName> p = pointNames.iterator(); 
		             !match && p.hasNext(); ) {
	        	    GeneralNameInterface pointName = p.next().getName();
			    if (debug != null) {
			        debug.println("pointName: " + pointName);
			    }
			    match = idpName.equals(pointName);
			}
		    }
		    if (!match) {
			if (debug != null) {
			    debug.println("IDP name does not match DP name");
			}
			return false;
		    }
		// if the DP name is present in the IDP CRL extension and the
		// DP field is absent from the DP, then verify that one of the
		// names in the IDP matches one of the names in the crlIssuer
		// field of the DP
		} else {
		    // verify that one of the names in the IDP matches one of
		    // the names in the cRLIssuer of the cert's DP
		    boolean match = false;
            	    for (Iterator<GeneralName> t = pointCrlIssuers.iterator(); 
			 !match && t.hasNext(); ) {
	        	GeneralNameInterface crlIssuerName = t.next().getName();
            	        for (Iterator<GeneralName> i = idpNames.iterator(); 
			     !match && i.hasNext(); ) {
	        	    GeneralNameInterface idpName = i.next().getName();
			    match = crlIssuerName.equals(idpName);
			}
		    }
		    if (!match) {
			return false;
		    }
		}
	    }

	    // if the onlyContainsUserCerts boolean is asserted, verify that the
	    // cert is not a CA cert
	    Boolean b = (Boolean) 
		idpExt.get(IssuingDistributionPointExtension.ONLY_USER_CERTS);
	    if (b.equals(Boolean.TRUE) && certImpl.getBasicConstraints() != -1) {
		if (debug != null) {
		    debug.println("cert must be a EE cert");
		}
		return false;
	    }

	    // if the onlyContainsCACerts boolean is asserted, verify that the
	    // cert is a CA cert
	    b = (Boolean) 
		idpExt.get(IssuingDistributionPointExtension.ONLY_CA_CERTS);
	    if (b.equals(Boolean.TRUE) && certImpl.getBasicConstraints() == -1) {
		if (debug != null) {
		    debug.println("cert must be a CA cert");
		}
		return false;
	    }

	    // verify that the onlyContainsAttributeCerts boolean is not
	    // asserted
	    b = (Boolean) idpExt.get 
		(IssuingDistributionPointExtension.ONLY_ATTRIBUTE_CERTS);
	    if (b.equals(Boolean.TRUE)) {
		if (debug != null) {
		    debug.println("cert must not be an AA cert");
		}
		return false;
	    }
	}

	// compute interim reasons mask
	boolean[] interimReasonsMask = new boolean[9];
	ReasonFlags reasons = null;
	if (idpExt != null) {
	    reasons = (ReasonFlags) 
	        idpExt.get(IssuingDistributionPointExtension.REASONS);
	}

	boolean[] pointReasonFlags = point.getReasonFlags();
	if (reasons != null) {
	    if (pointReasonFlags != null) {
	        // set interim reasons mask to the intersection of
	        // reasons in the DP and onlySomeReasons in the IDP
		boolean[] idpReasonFlags = reasons.getFlags();
		for (int i = 0; i < idpReasonFlags.length; i++) {
		    if (idpReasonFlags[i] && pointReasonFlags[i]) {
			interimReasonsMask[i] = true;
		    }
		} 
	    } else {
	        // set interim reasons mask to the value of
	        // onlySomeReasons in the IDP (and clone it since we may
	        // modify it)
                interimReasonsMask = reasons.getFlags().clone();
	    }
	} else if (idpExt == null || reasons == null) {
	    if (pointReasonFlags != null) {
		// set interim reasons mask to the value of DP reasons
                interimReasonsMask = pointReasonFlags.clone();
	    } else {
		// set interim reasons mask to the special value all-reasons
		interimReasonsMask = new boolean[9];
		Arrays.fill(interimReasonsMask, true);
	    }
	}

	// verify that interim reasons mask includes one or more reasons
	// not included in the reasons mask
	boolean oneOrMore = false;
	for (int i=0; i < interimReasonsMask.length && !oneOrMore; i++) {
	    if (!reasonsMask[i] && interimReasonsMask[i]) {
		oneOrMore = true;
	    }
	}
	if (!oneOrMore) {
	    return false;
	}

	// Obtain and validate the certification path for the complete 
	// CRL issuer (if indirect CRL). If a key usage extension is present 
	// in the CRL issuer's certificate, verify that the cRLSign bit is set.
	if (indirectCRL) {
	    X509CertSelector certSel = new X509CertSelector();
	    certSel.setSubject(crlIssuer.asX500Principal());
	    boolean[] crlSign = {false,false,false,false,false,false,true};
	    certSel.setKeyUsage(crlSign);
	    PKIXBuilderParameters params = null;
	    try {
	        params = new PKIXBuilderParameters
		    (Collections.singleton(anchor), certSel);
	    } catch (InvalidAlgorithmParameterException iape) {
		throw new CRLException(iape);
	    }
	    params.setCertStores(certStores);
	    params.setSigProvider(provider);
	    try {
	        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
	        PKIXCertPathBuilderResult result = 
		    (PKIXCertPathBuilderResult) builder.build(params);
	        prevKey = result.getPublicKey();
	    } catch (Exception e) {
		throw new CRLException(e);
	    }
	}

	// validate the signature on the CRL
        try {
            crl.verify(prevKey, provider);
        } catch (Exception e) {
            if (debug != null) {
                debug.println("CRL signature failed to verify");
            }
            return false;
        }

        // reject CRL if any unresolved critical extensions remain in the CRL.
        Set<String> unresCritExts = crl.getCriticalExtensionOIDs();
        // remove any that we have processed
	if (unresCritExts != null) {
            unresCritExts.remove
                (PKIXExtensions.IssuingDistributionPoint_Id.toString());
            if (!unresCritExts.isEmpty()) {
                if (debug != null) {
                    debug.println("Unrecognized critical extension(s) in CRL: " 
		        + unresCritExts);
                    Iterator<String> i = unresCritExts.iterator();
                    while (i.hasNext())
                        debug.println(i.next());
                }
	        return false;
	    }
        }

	// update reasonsMask
	for (int i=0; i < interimReasonsMask.length; i++) {
	    if (!reasonsMask[i] && interimReasonsMask[i]) {
		reasonsMask[i] = true;
	    }
	}
	return true;
    }
	
    /**
     * Append relative name to the issuer name and return a new
     * GeneralNames object.
     */
    private GeneralNames getFullNames(X500Name issuer, RDN rdn) 
	throws IOException {
        List<RDN> rdns = new ArrayList<RDN>(issuer.rdns());
        rdns.add(rdn);
        X500Name fullName = new X500Name(rdns.toArray(new RDN[0]));
        GeneralNames fullNames = new GeneralNames();
        fullNames.add(new GeneralName(fullName));
	return fullNames;
    }
}
