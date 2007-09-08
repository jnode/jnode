/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.jmx.remote.security;

import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.Principal;
import java.security.PrivilegedAction;
import javax.security.auth.Subject;

import javax.management.remote.SubjectDelegationPermission;

import com.sun.jmx.remote.util.CacheMap;

public class SubjectDelegator {
    private static final int PRINCIPALS_CACHE_SIZE = 10;
    private static final int ACC_CACHE_SIZE = 10;

    private CacheMap<Subject, Principal[]> principalsCache;
    private CacheMap<Subject, AccessControlContext> accCache;

    /* Return the AccessControlContext appropriate to execute an
       operation on behalf of the delegatedSubject.  If the
       authenticatedAccessControlContext does not have permission to
       delegate to that subject, throw SecurityException.  */
    public synchronized AccessControlContext
	delegatedContext(AccessControlContext authenticatedACC,
			 Subject delegatedSubject,
			 boolean removeCallerContext)
	    throws SecurityException {

	if (principalsCache == null || accCache == null) {
	    principalsCache =
                    new CacheMap<Subject, Principal[]>(PRINCIPALS_CACHE_SIZE);
	    accCache =
                    new CacheMap<Subject, AccessControlContext>(ACC_CACHE_SIZE);
	}

	// Retrieve the principals for the given
	// delegated subject from the cache
	//
	Principal[] delegatedPrincipals = principalsCache.get(delegatedSubject);

	// Convert the set of principals stored in the
	// delegated subject into an array of principals
	// and store it in the cache
	//
	if (delegatedPrincipals == null) {
	    delegatedPrincipals =
		delegatedSubject.getPrincipals().toArray(new Principal[0]);
	    principalsCache.put(delegatedSubject, delegatedPrincipals);
	}

	// Retrieve the access control context for the
	// given delegated subject from the cache
	//
	AccessControlContext delegatedACC = accCache.get(delegatedSubject);

	// Build the access control context to be used
	// when executing code as the delegated subject
	// and store it in the cache
	//
	if (delegatedACC == null) {
	    if (removeCallerContext) {
		delegatedACC =
                    JMXSubjectDomainCombiner.getDomainCombinerContext(
                                                              delegatedSubject);
	    } else {
		delegatedACC =
		    JMXSubjectDomainCombiner.getContext(delegatedSubject);
	    }
	    accCache.put(delegatedSubject, delegatedACC);
	}

	// Check if the subject delegation permission allows the
	// authenticated subject to assume the identity of each
	// principal in the delegated subject
	//
	final Principal[] dp = delegatedPrincipals;
	PrivilegedAction<Void> action =
	    new PrivilegedAction<Void>() {
		public Void run() {
		    for (int i = 0 ; i < dp.length ; i++) {
			final String pname =
			    dp[i].getClass().getName() + "." + dp[i].getName();
			Permission sdp =
			    new SubjectDelegationPermission(pname);
			AccessController.checkPermission(sdp);
		    }
		    return null;
		}
	    };
	AccessController.doPrivileged(action, authenticatedACC);

	return delegatedACC;
    }

    /**
     * Check if the connector server creator can assume the identity of each
     * principal in the authenticated subject, i.e. check if the connector
     * server creator codebase contains a subject delegation permission for
     * each principal present in the authenticated subject.
     *
     * @return {@code true} if the connector server creator can delegate to all
     * the authenticated principals in the subject. Otherwise, {@code false}.
     */
    public static synchronized boolean
        checkRemoveCallerContext(Subject subject) {
        try {
            final Principal[] dp =
                subject.getPrincipals().toArray(new Principal[0]);
            for (int i = 0 ; i < dp.length ; i++) {
                final String pname =
                    dp[i].getClass().getName() + "." + dp[i].getName();
                final Permission sdp =
                    new SubjectDelegationPermission(pname);
                AccessController.checkPermission(sdp);
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }
}
