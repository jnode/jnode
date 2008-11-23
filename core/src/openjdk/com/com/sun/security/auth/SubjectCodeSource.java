/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.security.auth;

import java.net.URL;
import java.util.*;
import java.security.CodeSource;
import java.security.Principal;
import java.security.cert.Certificate;
import java.lang.reflect.Constructor;

import javax.security.auth.Subject;

/**
 * <p> This <code>SubjectCodeSource</code> class contains
 * a <code>URL</code>, signer certificates, and either a <code>Subject</code>
 * (that represents the <code>Subject</code> in the current
 * <code>AccessControlContext</code>),
 * or a linked list of Principals/PrincipalComparators
 * (that represent a "subject" in a <code>Policy</code>).
 *
 */
class SubjectCodeSource extends CodeSource implements java.io.Serializable {

    private static final long serialVersionUID = 6039418085604715275L;

    private static final java.util.ResourceBundle rb =
	java.security.AccessController.doPrivileged
	(new java.security.PrivilegedAction<java.util.ResourceBundle>() {
	    public java.util.ResourceBundle run() {
		return (java.util.ResourceBundle.getBundle
			("sun.security.util.AuthResources"));
            }
	});

    private Subject subject;
    private LinkedList<PolicyParser.PrincipalEntry> principals;
    private static final Class[] PARAMS = { String.class };
    private static final sun.security.util.Debug debug =
	sun.security.util.Debug.getInstance("auth", "\t[Auth Access]");
    private ClassLoader sysClassLoader;

    /**
     * Creates a new <code>SubjectCodeSource</code>
     * with the given <code>Subject</code>, principals, <code>URL</code>,
     * and signers (Certificates).  The <code>Subject</code>
     * represents the <code>Subject</code> associated with the current
     * <code>AccessControlContext</code>.
     * The Principals are given as a <code>LinkedList</code>
     * of <code>PolicyParser.PrincipalEntry</code> objects.
     * Typically either a <code>Subject</code> will be provided,
     * or a list of <code>principals</code> will be provided
     * (not both).
     *
     * <p>
     *
     * @param subject the <code>Subject</code> associated with this
     *			<code>SubjectCodeSource</code> <p>
     *
     * @param url the <code>URL</code> associated with this
     *			<code>SubjectCodeSource</code> <p>
     *
     * @param certs the signers associated with this
     *			<code>SubjectCodeSource</code> <p>
     */
    SubjectCodeSource(Subject subject,
	LinkedList<PolicyParser.PrincipalEntry> principals,
	URL url, Certificate[] certs) {

	super(url, certs);
	this.subject = subject;
	this.principals = (principals == null ?
		new LinkedList<PolicyParser.PrincipalEntry>() :
		new LinkedList<PolicyParser.PrincipalEntry>(principals));
	sysClassLoader = java.security.AccessController.doPrivileged
	(new java.security.PrivilegedAction<ClassLoader>() {
	    public ClassLoader run() {
		    return ClassLoader.getSystemClassLoader();
	    }
	});
    }

    /**
     * Get the Principals associated with this <code>SubjectCodeSource</code>.
     * The Principals are retrieved as a <code>LinkedList</code>
     * of <code>PolicyParser.PrincipalEntry</code> objects.
     *
     * <p>
     *
     * @return the Principals associated with this
     *		<code>SubjectCodeSource</code> as a <code>LinkedList</code>
     *		of <code>PolicyParser.PrincipalEntry</code> objects.
     */
    LinkedList<PolicyParser.PrincipalEntry> getPrincipals() {
	return principals;
    }

    /**
     * Get the <code>Subject</code> associated with this
     * <code>SubjectCodeSource</code>.  The <code>Subject</code>
     * represents the <code>Subject</code> associated with the
     * current <code>AccessControlContext</code>.
     *
     * <p>
     *
     * @return the <code>Subject</code> associated with this
     *		<code>SubjectCodeSource</code>.
     */
    Subject getSubject() {
	return subject;
    }

    /**
     * Returns true if this <code>SubjectCodeSource</code> object "implies"
     * the specified <code>CodeSource</code>.
     * More specifically, this method makes the following checks.
     * If any fail, it returns false.  If they all succeed, it returns true.
     *
     * <p> 
     * <ol>
     * <li> The provided codesource must not be <code>null</code>.
     * <li> codesource must be an instance of <code>SubjectCodeSource</code>.
     * <li> super.implies(codesource) must return true.
     * <li> for each principal in this codesource's principal list:
     * <ol>
     * <li>	if the principal is an instanceof
     *		<code>PrincipalComparator</code>, then the principal must
     *		imply the provided codesource's <code>Subject</code>.
     * <li>	if the principal is not an instanceof
     *		<code>PrincipalComparator</code>, then the provided
     *		codesource's <code>Subject</code> must have an
     *		associated <code>Principal</code>, <i>P</i>, where
     *		P.getClass().getName equals principal.principalClass,
     *		and P.getName() equals principal.principalName.
     * </ol>
     * </ol>
     *
     * <p>
     *
     * @param codesource the <code>CodeSource</code> to compare against.
     *
     * @return true if this <code>SubjectCodeSource</code> implies the
     *		the specified <code>CodeSource</code>.
     */
    public boolean implies(CodeSource codesource) {

	LinkedList<PolicyParser.PrincipalEntry> subjectList = null;

	if (codesource == null ||
	    !(codesource instanceof SubjectCodeSource) ||
	    !(super.implies(codesource))) {

	    if (debug != null)
		debug.println("\tSubjectCodeSource.implies: FAILURE 1");
	    return false;
	}

	SubjectCodeSource that = (SubjectCodeSource)codesource;

	// if the principal list in the policy "implies"
	// the Subject associated with the current AccessControlContext,
	// then return true

	if (this.principals == null) {
	    if (debug != null)
		debug.println("\tSubjectCodeSource.implies: PASS 1");
	    return true;
	}

	if (that.getSubject() == null ||
	    that.getSubject().getPrincipals().size() == 0) {
	    if (debug != null)
		debug.println("\tSubjectCodeSource.implies: FAILURE 2");
	    return false;
	}

	ListIterator<PolicyParser.PrincipalEntry> li =
		this.principals.listIterator(0);
	while (li.hasNext()) {
	    PolicyParser.PrincipalEntry pppe = li.next();
	    try {

		// handle PrincipalComparators

		Class principalComparator = Class.forName(pppe.principalClass,
							true,
							sysClassLoader);
		Constructor c = principalComparator.getConstructor(PARAMS);
		PrincipalComparator pc =
			(PrincipalComparator)c.newInstance
			(new Object[] { pppe.principalName });

		if (!pc.implies(that.getSubject())) {
		    if (debug != null)
			debug.println("\tSubjectCodeSource.implies: FAILURE 3");
		    return false;
		} else {
		    if (debug != null)
			debug.println("\tSubjectCodeSource.implies: PASS 2");
		    return true;
		}
	    } catch (Exception e) {

		// no PrincipalComparator, simply compare Principals

		if (subjectList == null) {

		    if (that.getSubject() == null) {
			if (debug != null)
			    debug.println("\tSubjectCodeSource.implies: " +
					"FAILURE 4");
			return false;
		    }
		    Iterator<Principal> i =
				that.getSubject().getPrincipals().iterator();

		    subjectList = new LinkedList<PolicyParser.PrincipalEntry>();
		    while (i.hasNext()) {
			Principal p = i.next();
			PolicyParser.PrincipalEntry spppe =
				new PolicyParser.PrincipalEntry
				(p.getClass().getName(), p.getName());
			subjectList.add(spppe);
		    }
		}
		
		if (!subjectListImpliesPrincipalEntry(subjectList, pppe)) {
		    if (debug != null)
			debug.println("\tSubjectCodeSource.implies: FAILURE 5");
		    return false;
		}
	    }
	}

	if (debug != null)
	    debug.println("\tSubjectCodeSource.implies: PASS 3");
	return true;
    }

    /**
     * This method returns, true, if the provided <i>subjectList</i>
     * "contains" the <code>Principal</code> specified
     * in the provided <i>pppe</i> argument.
     *
     * Note that the provided <i>pppe</i> argument may have
     * wildcards (*) for the <code>Principal</code> class and name,
     * which need to be considered.
     *
     * <p>
     * 
     * @param subjectList a list of PolicyParser.PrincipalEntry objects
     *		that correspond to all the Principals in the Subject currently
     *		on this thread's AccessControlContext. <p>
     *
     * @param pppe the Principals specified in a grant entry.
     *
     * @return true if the provided <i>subjectList</i> "contains"
     *		the <code>Principal</code> specified in the provided
     *		<i>pppe</i> argument.
     */
    private boolean subjectListImpliesPrincipalEntry(
		LinkedList<PolicyParser.PrincipalEntry> subjectList,
		PolicyParser.PrincipalEntry pppe) {

	ListIterator<PolicyParser.PrincipalEntry> li =
					subjectList.listIterator(0);
	while (li.hasNext()) {
	    PolicyParser.PrincipalEntry listPppe = li.next();

	    if (pppe.principalClass.equals
			(PolicyParser.PrincipalEntry.WILDCARD_CLASS) ||
		pppe.principalClass.equals
			(listPppe.principalClass)) {

		if (pppe.principalName.equals
			(PolicyParser.PrincipalEntry.WILDCARD_NAME) ||
		    pppe.principalName.equals
			(listPppe.principalName))
		    return true;
	    }
	}
	return false;
    }

    /**
     * Tests for equality between the specified object and this
     * object. Two <code>SubjectCodeSource</code> objects are considered equal
     * if their locations are of identical value, if the two sets of
     * Certificates are of identical values, and if the
     * Subjects are equal, and if the PolicyParser.PrincipalEntry values
     * are of identical values.  It is not required that
     * the Certificates or PolicyParser.PrincipalEntry values
     * be in the same order.
     *
     * <p>
     *
     * @param obj the object to test for equality with this object.
     *
     * @return true if the objects are considered equal, false otherwise.
     */
    public boolean equals(Object obj) {

	if (obj == this)
	    return true;

	if (super.equals(obj) == false)
	    return false;

	if (!(obj instanceof SubjectCodeSource))
	    return false;

	SubjectCodeSource that = (SubjectCodeSource)obj;

	// the principal lists must match
	try {
	    if (this.getSubject() != that.getSubject())
		return false;
	} catch (SecurityException se) {
	    return false;
	}

	if ((this.principals == null && that.principals != null) ||
	    (this.principals != null && that.principals == null))
	    return false;
	
	if (this.principals != null && that.principals != null) {
	    if (!this.principals.containsAll(that.principals) ||
		!that.principals.containsAll(this.principals))

		return false;
	}

	return true;
    }

    /**
     * Return a hashcode for this <code>SubjectCodeSource</code>.
     *
     * <p>
     *
     * @return a hashcode for this <code>SubjectCodeSource</code>.
     */
    public int hashCode() {
	return super.hashCode();
    }

    /**
     * Return a String representation of this <code>SubjectCodeSource</code>.
     *
     * <p>
     *
     * @return a String representation of this <code>SubjectCodeSource</code>.
     */
    public String toString() {
	String returnMe = super.toString();
	if (getSubject() != null) {
	    if (debug != null) {
		final Subject finalSubject = getSubject();
		returnMe = returnMe + "\n" +
			java.security.AccessController.doPrivileged
				(new java.security.PrivilegedAction<String>() {
				public String run() {
				    return finalSubject.toString();
				}
			});
	    } else {
		returnMe = returnMe + "\n" + getSubject().toString();
	    }
	}
	if (principals != null) {
	    ListIterator<PolicyParser.PrincipalEntry> li =
					principals.listIterator();
	    while (li.hasNext()) {
		PolicyParser.PrincipalEntry pppe = li.next();
		returnMe = returnMe + rb.getString("\n") +
			pppe.principalClass + " " +
			pppe.principalName;
	    }
	}
	return returnMe;
    }
}
