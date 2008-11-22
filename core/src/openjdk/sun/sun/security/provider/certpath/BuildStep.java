/*
 * Copyright 2000-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

import sun.security.util.Debug;
import java.security.cert.X509Certificate;

/**
 * Describes one step of a certification path build, consisting of a
 * <code>Vertex</code> state description, a certificate, a possible throwable,
 * and a result code.
 *
 * @author 	Anne Anderson
 * @since	1.4
 * @see sun.security.provider.certpath.Vertex
 */
public class BuildStep {
 
    private static final Debug debug = Debug.getInstance("certpath");
    private Vertex          vertex;
    private X509Certificate cert;
    private Throwable       throwable;
    private int             result;

    /**
     * result code associated with a certificate that may continue a path from
     * the current certificate.
     */
    public static final int POSSIBLE = 1;

    /**
     * result code associated with a certificate that was tried, but that
     * represents an unsuccessful path, so the certificate has been backed out
     * to allow backtracking to the next possible path.
     */
    public static final int BACK = 2;

    /**
     * result code associated with a certificate that successfully continues the
     * current path, but does not yet reach the target.
     */
    public static final int FOLLOW = 3;

    /**
     * result code associated with a certificate that represents the end of the
     * last possible path, where no path successfully reached the target.
     */
    public static final int FAIL = 4;

    /**
     * result code associated with a certificate that represents the end of a
     * path that successfully reaches the target.
     */
    public static final int SUCCEED = 5;

    /**
     * construct a BuildStep
     *
     * @param vtx description of the vertex at this step
     * @param res result, where result is one of POSSIBLE, BACK,
     *            FOLLOW, FAIL, SUCCEED
     */
    public BuildStep(Vertex vtx, int res) {
	vertex = vtx;
	if (vertex != null) {
	    cert = (X509Certificate)vertex.getCertificate();
	    throwable = vertex.getThrowable();
	}
	result = res;
    }

    /**
     * return vertex description for this build step
     *
     * @returns Vertex
     */
    public Vertex getVertex() {
	return vertex;
    }

    /**
     * return the certificate associated with this build step
     *
     * @returns X509Certificate
     */
    public X509Certificate getCertificate() {
	return cert;
    }

    /**
     * return string form of issuer name from certificate associated with this
     * build step
     *
     * @returns String form of issuer name or null, if no certificate.
     */
    public String getIssuerName() {
	return (cert == null ? null : cert.getIssuerX500Principal().toString());
    }

    /**
     * return string form of issuer name from certificate associated with this
     * build step, or a default name if no certificate associated with this
     * build step, or if issuer name could not be obtained from the certificate.
     *
     * @param defaultName name to use as default if unable to return an issuer
     * name from the certificate, or if no certificate.
     * @returns String form of issuer name or defaultName, if no certificate or
     * exception received while trying to extract issuer name from certificate.
     */
    public String getIssuerName(String defaultName) {
	return (cert == null ? defaultName 
			     : cert.getIssuerX500Principal().toString());
    }

    /**
     * return string form of subject name from certificate associated with this
     * build step.
     *
     * @returns String form of subject name or null, if no certificate.
     */
    public String getSubjectName() {
	return (cert == null ? null : cert.getSubjectX500Principal().toString());
    }

    /**
     * return string form of subject name from certificate associated with this
     * build step, or a default name if no certificate associated with this
     * build step, or if subject name could not be obtained from the 
     * certificate.
     *
     * @param defaultName name to use as default if unable to return a subject
     * name from the certificate, or if no certificate.
     * @returns String form of subject name or defaultName, if no certificate or
     * if an exception was received while attempting to extract the subject name
     * from the certificate.
     */
    public String getSubjectName(String defaultName) {
	return (cert == null ? defaultName 
			     : cert.getSubjectX500Principal().toString());
    }

    /**
     * return the exception associated with this build step.
     *
     * @returns Throwable
     */
    public Throwable getThrowable() {
	return throwable;
    }

    /**
     * return the result code associated with this build step.  The result codes
     * are POSSIBLE, FOLLOW, BACK, FAIL, SUCCEED.
     *
     * @returns int result code
     */
    public int getResult() {
	return result;
    }

    /**
     * return a string representing the meaning of the result code associated
     * with this build step.
     *
     * @param   res    result code
     * @returns String string representing meaning of the result code
     */
    public String resultToString(int res) {
	String resultString = "";
	switch (res) {
	    case BuildStep.POSSIBLE:
		resultString = "Certificate to be tried.\n";
	        break;
	    case BuildStep.BACK:
		resultString = "Certificate backed out since path does not "
		    + "satisfy build requirements.\n";
		break;
	    case BuildStep.FOLLOW:
		resultString = "Certificate satisfies conditions.\n";
		break;
	    case BuildStep.FAIL:
		resultString = "Certificate backed out since path does not "
		    + "satisfy conditions.\n";
		break;
	    case BuildStep.SUCCEED:
		resultString = "Certificate satisfies conditions.\n";
		break;
	    default:
		resultString = "Internal error: Invalid step result value.\n";
	}
	return resultString;
    }

    /**
     * return a string representation of this build step, showing minimal
     * detail.
     *
     * @returns String
     */
    public String toString() {
	String out = "Internal Error\n";
	switch (result) {
	case BACK:
	case FAIL:
	    out = resultToString(result);
	    out = out + vertex.throwableToString();
	    break;
	case FOLLOW:
	case SUCCEED:
	case POSSIBLE:
	    out = resultToString(result);
	    break;
	default:
	    out = "Internal Error: Invalid step result\n";
	}
	return out;
    }

    /**
     * return a string representation of this build step, showing all detail of
     * the vertex state appropriate to the result of this build step, and the
     * certificate contents.
     *
     * @returns String
     */
    public String verboseToString() {
	String out = resultToString(getResult());
	switch (result) {
	case BACK:
	case FAIL:
	    out = out + vertex.throwableToString();
	    break;
	case FOLLOW:
	case SUCCEED:
	    out = out + vertex.moreToString();
	    break;
	case POSSIBLE:
	    break;
	default:
	    break;
	}
	out = out + "Certificate contains:\n" + vertex.certToString();
	return out;
    }

    /**
     * return a string representation of this build step, including all possible
     * detail of the vertex state, but not including the certificate contents.
     *
     * @returns String
     */
    public String fullToString() {
	String out = resultToString(getResult());
	out = out + vertex.toString();
	return out;
    }
}
