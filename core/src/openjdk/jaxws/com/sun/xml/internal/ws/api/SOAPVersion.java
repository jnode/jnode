/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.xml.internal.ws.api;

import com.sun.xml.internal.ws.encoding.soap.SOAP12Constants;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPBinding;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Version of SOAP (1.1 and 1.2).
 *
 * <p>
 * This class defines various constants for SOAP 1.1 and SOAP 1.2,
 * and also defines convenience methods to simplify the processing
 * of multiple SOAP versions.
 *
 * <p>
 * This constant alows you to do:
 *
 * <pre>
 * SOAPVersion version = ...;
 * version.someOp(...);
 * </pre>
 *
 * As opposed to:
 *
 * <pre>
 * if(binding is SOAP11) {
 *   doSomeOp11(...);
 * } else {
 *   doSomeOp12(...);
 * }
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
public enum SOAPVersion {
    SOAP_11(SOAPBinding.SOAP11HTTP_BINDING,
            com.sun.xml.internal.ws.encoding.soap.SOAPConstants.URI_ENVELOPE,
            "text/xml",
            SOAPConstants.URI_SOAP_ACTOR_NEXT, "actor",
            javax.xml.soap.SOAPConstants.SOAP_1_1_PROTOCOL,
            new QName(com.sun.xml.internal.ws.encoding.soap.SOAPConstants.URI_ENVELOPE, "MustUnderstand"),
            "Client",
            "Server",
            Collections.singleton(SOAPConstants.URI_SOAP_ACTOR_NEXT)),

    SOAP_12(SOAPBinding.SOAP12HTTP_BINDING,
            SOAP12Constants.URI_ENVELOPE,
            "application/soap+xml",
            SOAPConstants.URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER, "role",
            javax.xml.soap.SOAPConstants.SOAP_1_2_PROTOCOL,
            new QName(com.sun.xml.internal.ws.encoding.soap.SOAP12Constants.URI_ENVELOPE, "MustUnderstand"),
            "Sender",
            "Receiver",
            new HashSet<String>(Arrays.asList(SOAPConstants.URI_SOAP_1_2_ROLE_NEXT,SOAPConstants.URI_SOAP_1_2_ROLE_ULTIMATE_RECEIVER)));

    /**
     * Binding ID for SOAP/HTTP binding of this SOAP version.
     *
     * <p>
     * Either {@link SOAPBinding#SOAP11HTTP_BINDING} or
     *  {@link SOAPBinding#SOAP12HTTP_BINDING}
     */
    public final String httpBindingId;

    /**
     * SOAP envelope namespace URI.
     */
    public final String nsUri;

    /**
     * Content-type. Either "text/xml" or "application/soap+xml".
     */
    public final String contentType;

    /**
     * SOAP MustUnderstand FaultCode for this SOAP version
     */
    public final QName faultCodeMustUnderstand;

    /**
     * SAAJ {@link MessageFactory} for this SOAP version.
     */
    public final MessageFactory saajMessageFactory;

    /**
     * SAAJ {@link SOAPFactory} for this SOAP version.
     */
    public final SOAPFactory saajSoapFactory;

    /**
     * If the actor/role attribute is absent, this SOAP version assumes this value.
     */
    public final String implicitRole;

    /**
     * Singleton set that contains {@link #implicitRole}.
     */
    public final Set<String> implicitRoleSet;

    /**
     * This represents the roles required to be assumed by SOAP binding implementation.
     */
    public final Set<String> requiredRoles;

    /**
     * "role" (SOAP 1.2) or "actor" (SOAP 1.1)
     */
    public final String roleAttributeName;

    /**
     * "{nsUri}Client" or "{nsUri}Sender"
     */
    public final QName faultCodeClient;

    /**
     * "{nsUri}Server" or "{nsUri}Receiver"
     */
    public final QName faultCodeServer;


    private SOAPVersion(String httpBindingId, String nsUri, String contentType, String implicitRole, String roleAttributeName,
                        String saajFactoryString, QName faultCodeMustUnderstand, String faultCodeClientLocalName,
                        String faultCodeServerLocalName,Set<String> requiredRoles) {
        this.httpBindingId = httpBindingId;
        this.nsUri = nsUri;
        this.contentType = contentType;
        this.implicitRole = implicitRole;
        this.implicitRoleSet = Collections.singleton(implicitRole);
        this.roleAttributeName = roleAttributeName;
        try {
            saajMessageFactory = MessageFactory.newInstance(saajFactoryString);
            saajSoapFactory = SOAPFactory.newInstance(saajFactoryString);
        } catch (SOAPException e) {
            throw new Error(e);
        }
        this.faultCodeMustUnderstand = faultCodeMustUnderstand;
        this.requiredRoles = requiredRoles;
        this.faultCodeClient = new QName(nsUri,faultCodeClientLocalName);
        this.faultCodeServer = new QName(nsUri,faultCodeServerLocalName);
    }


    public String toString() {
        return httpBindingId;
    }

    /**
     * Returns {@link SOAPVersion} whose {@link #httpBindingId} equals to
     * the given string.
     *
     * This method does not perform input string validation.
     *
     * @param binding
     *      for historical reason, we treat null as {@link #SOAP_11},
     *      but you really shouldn't be passing null.
     * @return always non-null.
     */
    public static SOAPVersion fromHttpBinding(String binding) {
        if(binding==null)
            return SOAP_11;

        if(binding.equals(SOAP_12.httpBindingId))
            return SOAP_12;
        else
            return SOAP_11;
    }

    /**
     * Returns {@link SOAPVersion} whose {@link #nsUri} equals to
     * the given string.
     *
     * This method does not perform input string validation.
     *
     * @param nsUri
     *      must not be null.
     * @return always non-null.
     */
    public static SOAPVersion fromNsUri(String nsUri) {
        if(nsUri.equals(SOAP_12.nsUri))
            return SOAP_12;
        else
            return SOAP_11;
    }
}
