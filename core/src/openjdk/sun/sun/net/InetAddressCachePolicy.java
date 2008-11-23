/*
 * Copyright 1998-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.net;

import java.net.InetAddress;
import java.security.PrivilegedAction;
import java.security.Security;

public final class InetAddressCachePolicy {

    // Controls the cache policy for successful lookups only
    private static final String cachePolicyProp = "networkaddress.cache.ttl";
    private static final String cachePolicyPropFallback =
	"sun.net.inetaddr.ttl";

    // Controls the cache policy for negative lookups only
    private static final String negativeCachePolicyProp = 
	"networkaddress.cache.negative.ttl";
    private static final String negativeCachePolicyPropFallback = 
	"sun.net.inetaddr.negative.ttl";

    public static final int FOREVER = -1;
    public static final int NEVER = 0;
    
    /* default value for positive lookups */
    public static final int DEFAULT_POSITIVE = 30; 

    /* The Java-level namelookup cache policy for successful lookups:
     * 
     * -1: caching forever
     * any positive value: the number of seconds to cache an address for
     *
     * default value is forever (FOREVER), as we let the platform do the
     * caching. For security reasons, this caching is made forever when
     * a security manager is set.
     */
    private static int cachePolicy;
    
    /* The Java-level namelookup cache policy for negative lookups:
     * 
     * -1: caching forever
     * any positive value: the number of seconds to cache an address for
     *
     * default value is 0. It can be set to some other value for
     * performance reasons.
     */
    private static int negativeCachePolicy;

    /* 
     * Whether or not the cache policy for successful lookups was set
     * using a property (cmd line).
     */
    private static boolean set = false;

    /* 
     * Whether or not the cache policy for negative lookups was set
     * using a property (cmd line).
     */
    private static boolean negativeSet = false;

    /*
     * Initialize
     */
    static {

	set = false;
	negativeSet = false;

	cachePolicy = FOREVER;
	negativeCachePolicy =  0;

	Integer tmp = null;

	try {
	    tmp = new Integer(
              java.security.AccessController.doPrivileged (
                new PrivilegedAction<String>() {
                  public String run() {
		      return Security.getProperty(cachePolicyProp);
		  }
	      }));
	} catch (NumberFormatException e) {
	    // ignore
	}
	if (tmp != null) {
	    cachePolicy = tmp.intValue();
	    if (cachePolicy < 0) {
		cachePolicy = FOREVER;
	    }
	    set = true;
	} else {
            tmp = java.security.AccessController.doPrivileged
		(new sun.security.action.GetIntegerAction(cachePolicyPropFallback));
	    if (tmp != null) {
		cachePolicy = tmp.intValue();
		if (cachePolicy < 0) {
		    cachePolicy = FOREVER;
		}
		set = true;
	    }
	}

	try {
	    tmp = new Integer(
              java.security.AccessController.doPrivileged (
                new PrivilegedAction<String>() {
                  public String run() {
		      return Security.getProperty(negativeCachePolicyProp);
		  }
	      }));
	} catch (NumberFormatException e) {
	    // ignore
	}

	if (tmp != null) {
	    negativeCachePolicy = tmp.intValue();
	    if (negativeCachePolicy < 0) {
		negativeCachePolicy = FOREVER;
	    }
	    negativeSet = true;
	} else {
            tmp = java.security.AccessController.doPrivileged
		(new sun.security.action.GetIntegerAction(negativeCachePolicyPropFallback));
	    if (tmp != null) {
		negativeCachePolicy = tmp.intValue();
		if (negativeCachePolicy < 0) {
		    negativeCachePolicy = FOREVER;
		}
		negativeSet = true;
	    }
	}
    }

    public static synchronized int get() {
	if (!set && System.getSecurityManager() == null) {
	    return DEFAULT_POSITIVE;
	} else {
	    return cachePolicy;
	}
    }

    public static synchronized int getNegative() {
	return negativeCachePolicy;
    }

    /**
     * Sets the cache policy for successful lookups if the user has not
     * already specified a cache policy for it using a
     * command-property.
     * @param newPolicy the value in seconds for how long the lookup
     * should be cached
     */
    public static synchronized void setIfNotSet(int newPolicy) {
	
	/* 
	 * When setting the new value we may want to signal that the
	 * cache should be flushed, though this doesn't seem strictly
	 * necessary.
	 */
	
	if (!set) {
	    checkValue(newPolicy, cachePolicy);
	    cachePolicy = newPolicy;
	}
	
    }


    /**
     * Sets the cache policy for negative lookups if the user has not
     * already specified a cache policy for it using a
     * command-property.
     * @param newPolicy the value in seconds for how long the lookup
     * should be cached
     */
    public static synchronized void setNegativeIfNotSet(int newPolicy) {
	
	/* 
	 * When setting the new value we may want to signal that the
	 * cache should be flushed, though this doesn't seem strictly
	 * necessary.
	 */
	
	if (!negativeSet) {
	    // Negative caching does not seem to have any security
	    // implications.
 	    // checkValue(newPolicy, negativeCachePolicy);
	    negativeCachePolicy = newPolicy;
	}
    }

    private static void checkValue(int newPolicy, int oldPolicy) {

	/*
	 * If malicious code gets a hold of this method, prevent
	 * setting the cache policy to something laxer or some
	 * invalid negative value.
	 */

	if (newPolicy == FOREVER)
	    return;

	if ((oldPolicy == FOREVER) ||
	    (newPolicy < oldPolicy) ||
	    (newPolicy < FOREVER)) {
	    
	    throw new 
		SecurityException("can't make InetAddress cache more lax");
	    
	}
    }
}
