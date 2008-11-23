/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All Rights Reserved.
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

package sun.management;

/**
 * Configuration Error thrown by a management agent.
 */
public class AgentConfigurationError extends Error {
    public static final String AGENT_EXCEPTION = 
        "agent.err.exception";
    public static final String CONFIG_FILE_NOT_FOUND    = 
        "agent.err.configfile.notfound";
    public static final String CONFIG_FILE_OPEN_FAILED  =
        "agent.err.configfile.failed";
    public static final String CONFIG_FILE_CLOSE_FAILED =
        "agent.err.configfile.closed.failed";
    public static final String CONFIG_FILE_ACCESS_DENIED =
        "agent.err.configfile.access.denied";
    public static final String EXPORT_ADDRESS_FAILED =
	"agent.err.exportaddress.failed";
    public static final String AGENT_CLASS_NOT_FOUND =
        "agent.err.agentclass.notfound";
    public static final String AGENT_CLASS_FAILED =
        "agent.err.agentclass.failed";
    public static final String AGENT_CLASS_PREMAIN_NOT_FOUND =
        "agent.err.premain.notfound";
    public static final String AGENT_CLASS_ACCESS_DENIED =
        "agent.err.agentclass.access.denied";
    public static final String AGENT_CLASS_INVALID =
        "agent.err.invalid.agentclass";
    public static final String INVALID_JMXREMOTE_PORT =
        "agent.err.invalid.jmxremote.port";
    public static final String PASSWORD_FILE_NOT_SET =
        "agent.err.password.file.notset";
    public static final String PASSWORD_FILE_NOT_READABLE =
        "agent.err.password.file.not.readable";
    public static final String PASSWORD_FILE_READ_FAILED =
        "agent.err.password.file.read.failed";
    public static final String PASSWORD_FILE_NOT_FOUND =
        "agent.err.password.file.notfound";
    public static final String ACCESS_FILE_NOT_SET =
        "agent.err.access.file.notset";
    public static final String ACCESS_FILE_NOT_READABLE =
        "agent.err.access.file.not.readable";
    public static final String ACCESS_FILE_READ_FAILED =
        "agent.err.access.file.read.failed";
    public static final String ACCESS_FILE_NOT_FOUND =
        "agent.err.access.file.notfound";
    public static final String PASSWORD_FILE_ACCESS_NOT_RESTRICTED =
        "agent.err.password.file.access.notrestricted";
    public static final String FILE_ACCESS_NOT_RESTRICTED =
        "agent.err.file.access.not.restricted";
    public static final String FILE_NOT_FOUND =
        "agent.err.file.not.found";
    public static final String FILE_NOT_READABLE =
        "agent.err.file.not.readable";
    public static final String FILE_NOT_SET =
        "agent.err.file.not.set";
    public static final String FILE_READ_FAILED =
        "agent.err.file.read.failed";
    public static final String CONNECTOR_SERVER_IO_ERROR =
        "agent.err.connector.server.io.error";
    public static final String INVALID_OPTION =
	"agent.err.invalid.option";
    public static final String INVALID_SNMP_PORT =
        "agent.err.invalid.snmp.port";
    public static final String INVALID_SNMP_TRAP_PORT =
        "agent.err.invalid.snmp.trap.port";
    public static final String UNKNOWN_SNMP_INTERFACE =
        "agent.err.unknown.snmp.interface";
    public static final String SNMP_ACL_FILE_NOT_SET =
        "agent.err.acl.file.notset";
    public static final String SNMP_ACL_FILE_NOT_FOUND =
        "agent.err.acl.file.notfound";
    public static final String SNMP_ACL_FILE_NOT_READABLE =
        "agent.err.acl.file.not.readable";
    public static final String SNMP_ACL_FILE_READ_FAILED =
        "agent.err.acl.file.read.failed";
    public static final String SNMP_ACL_FILE_ACCESS_NOT_RESTRICTED =
        "agent.err.acl.file.access.notrestricted";
    public static final String SNMP_ADAPTOR_START_FAILED =
        "agent.err.snmp.adaptor.start.failed";
    public static final String SNMP_MIB_INIT_FAILED =
        "agent.err.snmp.mib.init.failed";

    private final String error;
    private final String[] params;

    public AgentConfigurationError(String error) {
        super();
        this.error = error;
        this.params = null;
    }

    public AgentConfigurationError(String error, Throwable cause) {
        super(cause);
        this.error = error;
        this.params = null;
    }

    public AgentConfigurationError(String error, String... params) {
        super();
        this.error = error;
        this.params = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            this.params[i] = params[i];
        }
    }

    public AgentConfigurationError(String error, Throwable cause, String... params) {
        super(cause);
        this.error = error;
        this.params = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            this.params[i] = params[i];
        }
    }

    public String getError() {
        return error;
    }

    public String[] getParams() {
        return params;
    }
}
