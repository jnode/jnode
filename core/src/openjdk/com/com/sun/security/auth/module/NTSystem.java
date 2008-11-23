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

package com.sun.security.auth.module;

import javax.security.auth.login.LoginException;

/**
 * <p> This class implementation retrieves and makes available NT
 * security information for the current user.
 * 
 */
public class NTSystem {
    
    private native void getCurrent(boolean debug);
    
    private String userName;
    private String domain;
    private String domainSID;
    private String userSID;
    private String groupIDs[];
    private String primaryGroupID;
    private long   impersonationToken;
    
    /**
     * Instantiate an <code>NTSystem</code> and load
     * the native library to access the underlying system information.
     */
    public NTSystem() {
	this(false);
    }

    /**
     * Instantiate an <code>NTSystem</code> and load
     * the native library to access the underlying system information.
     */
    NTSystem(boolean debug) {
	loadNative();
	getCurrent(debug);
    }
    
    /**
     * Get the username for the current NT user.
     *
     * <p>
     *
     * @return the username for the current NT user.
     */
    public String getName() {
        return userName;
    }
    
    /**
     * Get the domain for the current NT user.
     *
     * <p>
     *
     * @return the domain for the current NT user.
     */
    public String getDomain() {
        return domain;
    }
    
    /**
     * Get a printable SID for the current NT user's domain.
     *
     * <p>
     *
     * @return a printable SID for the current NT user's domain.
     */
    public String getDomainSID() {
        return domainSID;
    }
        
    /**
     * Get a printable SID for the current NT user.
     *
     * <p>
     *
     * @return a printable SID for the current NT user.
     */
    public String getUserSID() {
        return userSID;
    }
    
    /**
     * Get a printable primary group SID for the current NT user.
     *
     * <p>
     *
     * @return the primary group SID for the current NT user.
     */
    public String getPrimaryGroupID() {
        return primaryGroupID;
    }
    
    /**
     * Get the printable group SIDs for the current NT user.
     *
     * <p>
     *
     * @return the group SIDs for the current NT user.
     */
    public String[] getGroupIDs() {
        return groupIDs;
    }
    
    /**
     * Get an impersonation token for the current NT user.
     *
     * <p>
     *
     * @return an impersonation token for the current NT user.
     */
    public long getImpersonationToken() {
        return impersonationToken;
    }
    
    private void loadNative() {
	System.loadLibrary("jaas_nt");
    }
}
