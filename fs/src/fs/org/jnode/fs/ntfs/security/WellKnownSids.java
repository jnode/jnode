/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 * {@see https://support.microsoft.com/en-us/kb/243330}, and
 * {@see https://msdn.microsoft.com/en-us/library/cc980032.aspx}
 *
 * @author Luke Quinane
 * @author Nicholas Klopfer-Webber
 */
public class WellKnownSids {
    /**
     * A map of SID to friendly name.
     */
    private static final Map<SecurityIdentifier, String> nameMap = new LinkedHashMap<SecurityIdentifier, String>();

    // Global SIDs
    public static final SecurityIdentifier NULL_AUTHORITY =           register("S-1-0", "Null Authority");
    public static final SecurityIdentifier NOBODY =                   register("S-1-0-0", "Nobody");
    public static final SecurityIdentifier WORLD_AUTHORITY =          register("S-1-1", "World Authority");
    public static final SecurityIdentifier EVERYONE =                 register("S-1-1-0", "Everyone");
    public static final SecurityIdentifier LOCAL_AUTHORITY =          register("S-1-2", "Local Authority");
    public static final SecurityIdentifier LOCAL =                    register("S-1-2-0", "Local");
    public static final SecurityIdentifier CONSOLE_LOGIN =            register("S-1-2-1", "Console Logon");
    public static final SecurityIdentifier CREATOR_AUTHORITY =        register("S-1-3", "Creator Authority");
    public static final SecurityIdentifier CREATOR_OWNER =            register("S-1-3-0", "Creator Owner");
    public static final SecurityIdentifier CREATOR_GROUP =            register("S-1-3-1", "Creator Group");
    public static final SecurityIdentifier CREATOR_OWNER_SERVER =     register("S-1-3-2", "Creator Owner Server");
    public static final SecurityIdentifier CREATOR_GROUP_SERVER =     register("S-1-3-3", "Creator Group Server");
    public static final SecurityIdentifier OWNDER_RIGHTS =            register("S-1-3-4", "Owner Rights");
    public static final SecurityIdentifier NON_UNIQUE_AUTHORITY =     register("S-1-4", "Non-unique Authority");

    // NT Authority SIDs
    public static final SecurityIdentifier NT_AUTHORITY =             register("S-1-5", "NT Authority");
    public static final SecurityIdentifier DIALUP =                   register("S-1-5-1", "Dialup");
    public static final SecurityIdentifier NETWORK =                  register("S-1-5-2", "Network");
    public static final SecurityIdentifier BATCH =                    register("S-1-5-3", "Batch");
    public static final SecurityIdentifier INTERACTIVE =              register("S-1-5-4", "Interactive");
    public static final SecurityIdentifier SERVICE =                  register("S-1-5-6", "Service");
    public static final SecurityIdentifier ANONYMOUS =                register("S-1-5-7", "Anonymous (Null Logon)");
    public static final SecurityIdentifier PROXY =                    register("S-1-5-8", "Proxy");
    public static final SecurityIdentifier SERVER_LOGON =             register("S-1-5-9", "Server Logon (Domain Controller)");
    public static final SecurityIdentifier PRINCIPAL_SELF =           register("S-1-5-10", "Principal Self");
    public static final SecurityIdentifier AUTHENTICATED_USER =       register("S-1-5-11", "Authenticated User");
    public static final SecurityIdentifier RESTRICTED_CODE =          register("S-1-5-12", "Restricted Code");
    public static final SecurityIdentifier TERMINAL_SERVER_USERS =    register("S-1-5-13", "Terminal Server Users");
    public static final SecurityIdentifier REMOTE_INTERACTIVE_LOGON = register("S-1-5-14", "Remote Interactive Logon");
    public static final SecurityIdentifier THIS_ORGANIZATION =        register("S-1-5-15", "This Organisation");
    public static final SecurityIdentifier IIS_USER =                 register("S-1-5-17", "IIS User");
    public static final SecurityIdentifier LOCAL_SYSTEM =             register("S-1-5-18", "Local System");
    public static final SecurityIdentifier AUTHENTICATION_AUTHORITY_ASSERTED_IDENTITY =
                                                                      register("S-1-5-18-1", "Authentication Authority Asserted Identity");
    public static final SecurityIdentifier SERVICE_ASSERTED_IDENTITY =register("S-1-5-18-2", "Service Asserted Identity");
    public static final SecurityIdentifier LOCAL_SERVICE =            register("S-1-5-19", "Local Service");
    public static final SecurityIdentifier NETWORK_SERVICE =          register("S-1-5-20", "Network Service");
    public static final SecurityIdentifier COMPOUNDED_AUTHENTICATION =register("S-1-5-21-0-0-0-496", "Compound Authentication");
    public static final SecurityIdentifier BUILT_IN_DOMAIN =          register("S-1-5-32", "Built-in Domain");

    // Local domain users
    public static final SecurityIdentifier LOCAL_ADMIN =              register("S-1-5-32-500", "Local Admin");
    public static final SecurityIdentifier LOCAL_GUEST =              register("S-1-5-32-501", "Local Guest");
    public static final SecurityIdentifier LOCAL_KERBEROS_TARGET =    register("S-1-5-32-502", "Local Kerberos Tager");

    // Local domain groups
    public static final SecurityIdentifier LOCAL_ADMINS =             register("S-1-5-32-512", "Local Admins");
    public static final SecurityIdentifier LOCAL_USERS =              register("S-1-5-32-513", "Local Users");
    public static final SecurityIdentifier LOCAL_GUESTS =             register("S-1-5-32-514", "Local Guests");
    public static final SecurityIdentifier LOCAL_COMPUTERS =          register("S-1-5-32-515", "Local Computers");
    public static final SecurityIdentifier LOCAL_CONTROLLERS =        register("S-1-5-32-516", "Local Controllers");
    public static final SecurityIdentifier LOCAL_CERT_ADMINS =        register("S-1-5-32-517", "Local Cert Admins");
    public static final SecurityIdentifier LOCAL_SCHEMA_ADMINS =      register("S-1-5-32-518", "Local Schema Admins");
    public static final SecurityIdentifier LOCAL_ENTERPRISE_ADMINS =  register("S-1-5-32-519", "Local Enterprise Admins");
    public static final SecurityIdentifier LOCAL_POLICY_ADMINS =      register("S-1-5-32-520", "Local Policy Admins");

    // Local domain aliases
    public static final SecurityIdentifier BUILTIN_ADMINS =           register("S-1-5-32-544", "BUILTIN\\Administrators");
    public static final SecurityIdentifier BUILTIN_USERS =            register("S-1-5-32-545", "BUILTIN\\Users");
    public static final SecurityIdentifier BUILTIN_GUESTS =           register("S-1-5-32-546", "BUILTIN\\Guests");
    public static final SecurityIdentifier BUILTIN_POWER_USERS =      register("S-1-5-32-547", "BUILTIN\\Power Users");
    public static final SecurityIdentifier BUILTIN_ACCOUNT_OPS =      register("S-1-5-32-548", "BUILTIN\\Account Operators");
    public static final SecurityIdentifier BUILTIN_SYSTEM_OPS =       register("S-1-5-32-549", "BUILTIN\\System Operators");
    public static final SecurityIdentifier BUILTIN_PRINT_OPS =        register("S-1-5-32-550", "BUILTIN\\Print Operators");
    public static final SecurityIdentifier BUILTIN_BACKUP_OPS =       register("S-1-5-32-551", "BUILTIN\\Backup Operators");
    public static final SecurityIdentifier BUILTIN_REPLICATOR =       register("S-1-5-32-552", "BUILTIN\\Replicator");
    public static final SecurityIdentifier BUILTIN_RAS_SERVERS =      register("S-1-5-32-553", "BUILTIN\\RAS Servers");

    public static final SecurityIdentifier LOCAL_PRE_W2K =            register("S-1-5-32-554", "Local Pre-Windows 2000 Compatible Access");
    public static final SecurityIdentifier REMOTE_DESKTOP_ALIAS =     register("S-1-5-32-555", "Remote Desktop Users Alias");
    public static final SecurityIdentifier NETWORK_CONFIGURATION_OPS =register("S-1-5-32-556", "Network Configuration Operators Alias");
    public static final SecurityIdentifier INCOMING_FOREST_TRUST_BUILDERS =
                                                                      register("S-1-5-32-557", "Incoming Forest Trust Builders Alias");
    public static final SecurityIdentifier PERFMON_USERS =            register("S-1-5-32-558", "Performance Monitor Users Alias");
    public static final SecurityIdentifier PERFLOG_USERS =            register("S-1-5-32-559", "Performance Log Users Alias");
    public static final SecurityIdentifier WINDOWS_AUTHORIZATION_ACCESS_GROUP =
                                                                      register("S-1-5-32-560", "Windows Authorization Access Group Alias");
    public static final SecurityIdentifier TERMINAL_SERVER_LICENSE_SERVERS =
                                                                      register("S-1-5-32-561", "Terminal Server License Servers Alias");
    public static final SecurityIdentifier DISTRIBUTED_COM_USERS =    register("S-1-5-32-562", "Distributed COM Users Alias");
    public static final SecurityIdentifier CRYPTOGRAPHIC_OPERATORS =  register("S-1-5-32-569", "BUILTIN\\Cryptographic Operators");
    public static final SecurityIdentifier EVENT_LOG_READERS =        register("S-1-5-32-573", "BUILTIN\\Event Log Readers");
    public static final SecurityIdentifier CERTIFICATE_SERVICE_DCOM_ACCESS =
                                                                      register("S-1-5-32-574", "BUILTIN\\Certificate Service DCOM Access");
    public static final SecurityIdentifier RDS_REMOTE_ACCESS_SERVERS =register("S-1-5-32-575", "RDS Remote Access Servers");
    public static final SecurityIdentifier RDS_ENDPOINT_SERVERS =     register("S-1-5-32-576", "RDS Endpoint Servers");
    public static final SecurityIdentifier RDS_MANAGEMENT_SERVERS =   register("S-1-5-32-577", "RDS Management Servers");
    public static final SecurityIdentifier HYPER_V_ADMINS =           register("S-1-5-32-578", "Hyper-V Administrators");
    public static final SecurityIdentifier ACCESS_CONTROL_ASSISTANCE_OPS =
                                                                      register("S-1-5-32-579", "Access Control Assistance Operators");
    public static final SecurityIdentifier REMOTE_MANAGEMENT_USERS =  register("S-1-5-32-580", "Remote Management Users");

    public static final SecurityIdentifier WRITE_RESTRICTED_CODE =    register("S-1-5-33", "Write Restricted Code");


    public static final SecurityIdentifier NTLM_AUTHENTICATION =      register("S-1-5-64-10", "NTLM Authentication");
    public static final SecurityIdentifier SCHANNEL_AUTHENTICATION =  register("S-1-5-64-14", "SChannel Authentication");
    public static final SecurityIdentifier DIGEST_AUTHENTICATION =    register("S-1-5-64-21", "Digest Authentication");


    public static final SecurityIdentifier NT_SERVICE =               register("S-1-5-80", "NT Service");
    public static final SecurityIdentifier ALL_NT_SERVICES =          register("S-1-5-80-0", "All Services");
    public static final SecurityIdentifier NT_VM_MACHINES =           register("S-1-5-83-0", "NT VIRTUAL MACHINE\\Virtual Machines");

    public static final SecurityIdentifier USER_MODE_DRIVERS =        register("S-1-5-84-0-0-0-0-0", "User-mode Driver Process");

    public static final SecurityIdentifier LOCAL_ACCOUNT =            register("S-1-5-113", "Local Account");

    public static final SecurityIdentifier ALL_APP_PACKAGES =         register("S-1-15-2-1", "All App Package Applications");

    public static final SecurityIdentifier ML_UNTRUSTED =             register("S-1-16-0", "Untrusted Mandatory Level");
    public static final SecurityIdentifier ML_LOW =                   register("S-1-16-4096", "Low Mandatory Level");
    public static final SecurityIdentifier ML_MEDIUM =                register("S-1-16-8192", "Medium Mandatory Level");
    public static final SecurityIdentifier ML_MEDIUM_PLUS =           register("S-1-16-8448", "Medium Plus Mandatory Level");
    public static final SecurityIdentifier ML_HIGH =                  register("S-1-16-12288", "High Mandatory Level");
    public static final SecurityIdentifier ML_SYSTEM =                register("S-1-16-16384", "System Mandatory Level");
    public static final SecurityIdentifier ML_PROTECTED_PROCESS =     register("S-1-16-20480", "Protected Process Mandatory Level");


    public static final SecurityIdentifier MANDATORY_PROCESS_SECURE = register("S-1-16-28672", "Secure Process Mandatory Level");


    /**
     * Well known SIDs for the sub-region beginning with 1-5-21 then a set of values and ending in the following.
     */
    private static final Map<String, String> WELL_KNOWN_1_5_21_MAP = new LinkedHashMap<String, String>() {{
        put("500", "Administrator");
        put("501", "Guest");
        put("502", "KRBTGT");
        put("512", "Domain Admins");
        put("513", "Domain Users");
        put("514", "Domain Guests");
        put("515", "Domain Computers");
        put("516", "Domain Controllers");
        put("517", "Cert Publishers");
        put("518", "Schema Admins");
        put("519", "Enterprise Admins");
        put("520", "Group Policy Creator Owners");
        put("553", "RAS and IAS Servers");
        put("498", "Enterprise Read-only Domain Controllers");
        put("521", "Read-only Domain Controllers");
        put("571", "Allowed RODC Password Replication Group");
        put("572", "Denied RODC Password Replication Group");
        put("522", "Cloneable Domain Controllers");
    }};


    /**
     * Sid for a login session in the format "S-1-5-5-*-*"
     */
    private static final String WELL_KNOWN_1_5_5_SID_NAME = "Logon Session";


    private WellKnownSids() {
        // Prevent instantiation
    }

    /**
     * Registers a SID with the set of known SIDs.
     *
     * @param name    the friendly name for the SID.
     * @param sidString the SID text.
     * @return the SID.
     */
    private static SecurityIdentifier register(String sidString, String name) {
        SecurityIdentifier sid = SecurityIdentifier.fromString(sidString);
        if (nameMap.put(sid, name) != null) {
            throw new IllegalStateException("Duplicate key! " + sidString);
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
        String name = nameMap.get(sid);
        if (name == null) {
            String sidString = sid.toSidString();
            if (sidString.startsWith("S-1-5-21-")) {
                String end = sidString.substring(sidString.lastIndexOf('-') + 1);
                name = WELL_KNOWN_1_5_21_MAP.get(end);
            }
            else if (sidString.startsWith("S-1-5-5-")) {
                name = WELL_KNOWN_1_5_5_SID_NAME;
            }

            if (name != null) {
                // If the name was in the well known subset then add the side on to the end.
                name = name + " (" + sidString + ")";
            }
        }

        return name;
    }
}
