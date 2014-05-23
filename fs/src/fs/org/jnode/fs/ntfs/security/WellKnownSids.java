/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.ntfs.security;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A list of well known SIDs or {@link SecurityIdentifier}s.
 *
 * @author Luke Quinane
 */
public class WellKnownSids {
    /**
     * A map of SID to friendly name.
     */
    private static final Map<SecurityIdentifier, String> nameMap = new LinkedHashMap<SecurityIdentifier, String>();

    // Global SIDs
    public static final SecurityIdentifier NULL = register("Null SID", "S-1-0");
    public static final SecurityIdentifier WORLD = register("World SID", "S-1-1");
    public static final SecurityIdentifier LOCAL = register("Local SID", "S-1-2");
    public static final SecurityIdentifier CREATOR = register("Creator SID", "S-1-3");
    public static final SecurityIdentifier CREATOR_GROUP = register("Creator Group", "S-1-3-1");
    public static final SecurityIdentifier CREATOR_OWNER_SERVER = register("Creator Owner Server", "S-1-3-2");
    public static final SecurityIdentifier CREATOR_GROUP_SERVER = register("Creator Group Server", "S-1-3-3");
    public static final SecurityIdentifier OWNDER_RIGHTS = register("Owner Rights", "S-1-3-4");
    public static final SecurityIdentifier NON_UNIQUE = register("Non-unique SID", "S-1-4");

    // NT Authority SIDs
    public static final SecurityIdentifier NT_AUTHORITY = register("NT Authority", "S-1-5");
    public static final SecurityIdentifier DIALUP = register("Dialup", "S-1-5-1");
    public static final SecurityIdentifier NETWORK = register("Network", "S-1-5-2");
    public static final SecurityIdentifier BATCH = register("Batch", "S-1-5-3");
    public static final SecurityIdentifier INTERACTIVE = register("Interactive", "S-1-5-4");
    public static final SecurityIdentifier SERVICE = register("Service", "S-1-5-6");
    public static final SecurityIdentifier ANONYMOUS = register("Anonymous (Null Logon)", "S-1-5-7");
    public static final SecurityIdentifier PROXY = register("Proxy", "S-1-5-8");
    public static final SecurityIdentifier SERVER_LOGON = register("Server Logon (Domain Controller)", "S-1-5-9");
    public static final SecurityIdentifier SELF = register("Self", "S-1-5-10");
    public static final SecurityIdentifier AUTHENTICATED_USER = register("Authenticated User", "S-1-5-11");
    public static final SecurityIdentifier RESTRICTED_CODE = register("Restricted Code", "S-1-5-12");
    public static final SecurityIdentifier TERMINAL_SERVER = register("Terminal Server", "S-1-5-13");
    public static final SecurityIdentifier REMOTE_INTERACTIVE_LOGON = register("Remote Interactive Logon", "S-1-5-14");
    public static final SecurityIdentifier THIS_ORGANIZATION = register("This Organisation", "S-1-5-15");
    public static final SecurityIdentifier IUSR = register("IIS User", "S-1-5-17");
    public static final SecurityIdentifier LOCAL_SYSTEM = register("Local System", "S-1-5-18");
    public static final SecurityIdentifier AUTHENTICATION_AUTHORITY_ASSERTED_IDENTITY =
        register("Authentication Authority Asserted Identity", "S-1-5-18-1");
    public static final SecurityIdentifier SERVICE_ASSERTED_IDENTITY =
        register("Service Asserted Identity", "S-1-5-18-2");
    public static final SecurityIdentifier LOCAL_SERVICE = register("Local Service", "S-1-5-19");
    public static final SecurityIdentifier NETWORK_SERVICE = register("Network Service", "S-1-5-20");
    public static final SecurityIdentifier COMPOUNDED_AUTHENTICATION =
        register("Compound Authentication", "S-1-5-21-0-0-0-496");
    public static final SecurityIdentifier BUILT_IN_DOMAIN = register("Built-in Domain", "S-1-5-32");

    // Local domain users
    public static final SecurityIdentifier LOCAL_ADMIN = register("Local Admin", "S-1-5-32-500");
    public static final SecurityIdentifier LOCAL_GUEST = register("Local Guest", "S-1-5-32-501");
    public static final SecurityIdentifier LOCAL_KERBEROS_TARGET = register("Local Kerberos Tager", "S-1-5-32-502");

    // Local domain groups
    public static final SecurityIdentifier LOCAL_ADMINS = register("Local Admins", "S-1-5-32-512");
    public static final SecurityIdentifier LOCAL_USERS = register("Local Users", "S-1-5-32-513");
    public static final SecurityIdentifier LOCAL_GUESTS = register("Local Guests", "S-1-5-32-514");
    public static final SecurityIdentifier LOCAL_COMPUTERS = register("Local Computers", "S-1-5-32-515");
    public static final SecurityIdentifier LOCAL_CONTROLLERS = register("Local Controllers", "S-1-5-32-516");
    public static final SecurityIdentifier LOCAL_CERT_ADMINS = register("Local Cert Admins", "S-1-5-32-517");
    public static final SecurityIdentifier LOCAL_SCHEMA_ADMINS = register("Local Schema Admins", "S-1-5-32-518");
    public static final SecurityIdentifier LOCAL_ENTERPRISE_ADMINS =
        register("Local Enterprise Admins", "S-1-5-32-519");
    public static final SecurityIdentifier LOCAL_POLICY_ADMINS = register("Local Policy Admins", "S-1-5-32-520");

    // Local domain aliases
    public static final SecurityIdentifier LOCAL_ADMINS_ALIAS = register("Local Admins", "S-1-5-32-544");
    public static final SecurityIdentifier LOCAL_USERS_ALIAS = register("Local Users", "S-1-5-32-545");
    public static final SecurityIdentifier LOCAL_GUESTs_ALIAS = register("Local Guests", "S-1-5-32-546");
    public static final SecurityIdentifier LOCAL_POWER_USERS_ALIAS = register("Local Power Users", "S-1-5-32-547");
    public static final SecurityIdentifier LOCAL_ACCOUNT_OPS_ALIAS = register("Local Account Ops", "S-1-5-32-548");
    public static final SecurityIdentifier LOCAL_SYSTEM_OPS_ALIAS = register("Local System Ops", "S-1-5-32-549");
    public static final SecurityIdentifier LOCAL_PRINT_OPS_ALIAS = register("Local Print Ops", "S-1-5-32-550");
    public static final SecurityIdentifier LOCAL_BACKUP_OPS_ALIAS = register("Local Backup Ops", "S-1-5-32-551");
    public static final SecurityIdentifier LOCAL_REPLICATOR_ALIAS = register("Local Replicator", "S-1-5-32-552");
    public static final SecurityIdentifier LOCAL_RAS_SERVERS_ALIAS = register("Local RAS Servers", "S-1-5-32-553");
    public static final SecurityIdentifier LOCAL_PRE_W2K_ALIAS = register("Local Pre-W2K Comp Access", "S-1-5-32-554");

    public static final SecurityIdentifier REMOTE_DESKTOP = register("Remote Desktop", "S-1-5-32-555");

    public static final SecurityIdentifier NT_SERVICE = register("NT Service", "S-1-5-80");
    public static final SecurityIdentifier LOCAL_ACCOUNT = register("Local Account", "S-1-5-113");

    private WellKnownSids() {
        // Prevent instantiation
    }

    /**
     * Registers a SID with the set of known SIDs.
     *
     * @param name    the friendly name for the SID.
     * @param sidText the SID text.
     * @return the SID.
     */
    private static SecurityIdentifier register(String name, String sidText) {
        SecurityIdentifier sid = SecurityIdentifier.fromString(sidText);
        if (nameMap.put(sid, name) != null) {
            throw new IllegalStateException("Duplicate key! " + sidText);
        }
        return sid;
    }

    /**
     * Checks if a given SID is well known.
     *
     * @param sid the SID to check.
     * @return {@code true} if well known.
     */
    public static boolean containsSid(SecurityIdentifier sid) {
        return nameMap.containsKey(sid);
    }

    /**
     * Gets the friendly name for a SID if it is known.
     *
     * @param sid the SID to look up.
     * @return the friendly name or {@code null} if the SID was not well known.
     */
    public static String getName(SecurityIdentifier sid) {
        return nameMap.get(sid);
    }
}
