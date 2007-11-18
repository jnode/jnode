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

package javax.security.sasl;

import java.util.Map;
import javax.security.auth.callback.CallbackHandler;

/**
 * An interface for creating instances of <tt>SaslClient</tt>.
 * A class that implements this interface
 * must be thread-safe and handle multiple simultaneous 
 * requests. It must also have a public constructor that accepts no 
 * argument. 
 *<p>
 * This interface is not normally accessed directly by a client, which will use the
 * <tt>Sasl</tt> static methods
 * instead. However, a particular environment may provide and install a
 * new or different <tt>SaslClientFactory</tt>.
 * 
 * @since 1.5
 *
 * @see SaslClient
 * @see Sasl
 *
 * @author Rosanna Lee
 * @author Rob Weltman
 */
public abstract interface SaslClientFactory {
    /**
     * Creates a SaslClient using the parameters supplied.
     *
     * @param mechanisms The non-null list of mechanism names to try. Each is the
     * IANA-registered name of a SASL mechanism. (e.g. "GSSAPI", "CRAM-MD5").
     * @param authorizationId The possibly null protocol-dependent 
     * identification to be used for authorization.
     * If null or empty, the server derives an authorization 
     * ID from the client's authentication credentials.
     * When the SASL authentication completes successfully, 
     * the specified entity is granted access. 
     * @param protocol The non-null string name of the protocol for which
     * the authentication is being performed (e.g., "ldap").
     * @param serverName The non-null fully qualified host name 
     * of the server to authenticate to.
     * @param props The possibly null set of properties used to select the SASL
     * mechanism and to configure the authentication exchange of the selected
     * mechanism. See the <tt>Sasl</tt> class for a list of standard properties. 
     * Other, possibly mechanism-specific, properties can be included.
     * Properties not relevant to the selected mechanism are ignored,
     * including any map entries with non-String keys.
     * 
     * @param cbh The possibly null callback handler to used by the SASL
     * mechanisms to get further information from the application/library
     * to complete the authentication. For example, a SASL mechanism might
     * require the authentication ID, password and realm from the caller.
     * The authentication ID is requested by using a <tt>NameCallback</tt>.
     * The password is requested by using a <tt>PasswordCallback</tt>.
     * The realm is requested by using a <tt>RealmChoiceCallback</tt> if there is a list
     * of realms to choose from, and by using a <tt>RealmCallback</tt> if
     * the realm must be entered. 
     *
     *@return A possibly null <tt>SaslClient</tt> created using the parameters
     * supplied. If null, this factory cannot produce a <tt>SaslClient</tt>
     * using the parameters supplied.
     *@exception SaslException If cannot create a <tt>SaslClient</tt> because
     * of an error.
     */
    public abstract SaslClient createSaslClient(
	String[] mechanisms,
	String authorizationId,
	String protocol,
	String serverName,
	Map<String,?> props,
	CallbackHandler cbh) throws SaslException;

    /**
     * Returns an array of names of mechanisms that match the specified
     * mechanism selection policies.
     * @param props The possibly null set of properties used to specify the
     * security policy of the SASL mechanisms. For example, if <tt>props</tt>
     * contains the <tt>Sasl.POLICY_NOPLAINTEXT</tt> property with the value
     * <tt>"true"</tt>, then the factory must not return any SASL mechanisms
     * that are susceptible to simple plain passive attacks.
     * See the <tt>Sasl</tt> class for a complete list of policy properties.
     * Non-policy related properties, if present in <tt>props</tt>, are ignored,
     * including any map entries with non-String keys.
     * @return A non-null array containing a IANA-registered SASL mechanism names.
     */
    public abstract String[] getMechanismNames(Map<String,?> props);
}
