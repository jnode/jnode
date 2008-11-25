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
package com.sun.xml.internal.ws.resources;

import com.sun.xml.internal.ws.util.localization.Localizable;
import com.sun.xml.internal.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.internal.ws.util.localization.Localizer;


/**
 * Defines string formatting method for each constant in the resource file
 * 
 */
public final class ServerMessages {

    private final static LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.xml.internal.ws.resources.server");
    private final static Localizer localizer = new Localizer();

    public static Localizable localizableRUNTIME_PARSER_MISSING_ATTRIBUTE_NO_LINE() {
        return messageFactory.getMessage("runtime.parser.missing.attribute.no.line");
    }

    /**
     * missing attribute "{2}" in element "{1}" of runtime descriptor
     * 
     */
    public static String RUNTIME_PARSER_MISSING_ATTRIBUTE_NO_LINE() {
        return localizer.localize(localizableRUNTIME_PARSER_MISSING_ATTRIBUTE_NO_LINE());
    }

    public static Localizable localizableSTATEFUL_COOKIE_HEADER_INCORRECT(Object arg0, Object arg1) {
        return messageFactory.getMessage("stateful.cookie.header.incorrect", arg0, arg1);
    }

    /**
     * Invalid/expired {0} header value: {1}
     * 
     */
    public static String STATEFUL_COOKIE_HEADER_INCORRECT(Object arg0, Object arg1) {
        return localizer.localize(localizableSTATEFUL_COOKIE_HEADER_INCORRECT(arg0, arg1));
    }

    public static Localizable localizableNOT_IMPLEMENT_PROVIDER(Object arg0) {
        return messageFactory.getMessage("not.implement.provider", arg0);
    }

    /**
     * "{0}" doesn't implement Provider
     * 
     */
    public static String NOT_IMPLEMENT_PROVIDER(Object arg0) {
        return localizer.localize(localizableNOT_IMPLEMENT_PROVIDER(arg0));
    }

    public static Localizable localizableSTATEFUL_REQURES_ADDRESSING(Object arg0) {
        return messageFactory.getMessage("stateful.requres.addressing", arg0);
    }

    /**
     * Stateful web service {0} requires the WS-Addressing support to be enabled. Perhaps you are missing @Addressing
     * 
     */
    public static String STATEFUL_REQURES_ADDRESSING(Object arg0) {
        return localizer.localize(localizableSTATEFUL_REQURES_ADDRESSING(arg0));
    }

    public static Localizable localizableSOAPDECODER_ERR() {
        return messageFactory.getMessage("soapdecoder.err");
    }

    /**
     * Error in decoding SOAP Message
     * 
     */
    public static String SOAPDECODER_ERR() {
        return localizer.localize(localizableSOAPDECODER_ERR());
    }

    public static Localizable localizableRUNTIME_PARSER_INVALID_READER_STATE(Object arg0) {
        return messageFactory.getMessage("runtime.parser.invalidReaderState", arg0);
    }

    /**
     * error parsing runtime descriptor: {0}
     * 
     */
    public static String RUNTIME_PARSER_INVALID_READER_STATE(Object arg0) {
        return localizer.localize(localizableRUNTIME_PARSER_INVALID_READER_STATE(arg0));
    }

    public static Localizable localizableGENERATE_NON_STANDARD_WSDL() {
        return messageFactory.getMessage("generate.non.standard.wsdl");
    }

    /**
     * Generating non-standard WSDL for the specified binding
     * 
     */
    public static String GENERATE_NON_STANDARD_WSDL() {
        return localizer.localize(localizableGENERATE_NON_STANDARD_WSDL());
    }

    public static Localizable localizableDISPATCH_CANNOT_FIND_METHOD(Object arg0, Object arg1) {
        return messageFactory.getMessage("dispatch.cannotFindMethod", arg0, arg1);
    }

    /**
     * Cannot find dispatch method for {0} using "{1}"
     * 
     */
    public static String DISPATCH_CANNOT_FIND_METHOD(Object arg0, Object arg1) {
        return localizer.localize(localizableDISPATCH_CANNOT_FIND_METHOD(arg0, arg1));
    }

    public static Localizable localizableNO_CONTENT_TYPE() {
        return messageFactory.getMessage("no.contentType");
    }

    /**
     * Request doesn't have a Content-Type
     * 
     */
    public static String NO_CONTENT_TYPE() {
        return localizer.localize(localizableNO_CONTENT_TYPE());
    }

    public static Localizable localizableRUNTIME_PARSER_INVALID_VERSION_NUMBER() {
        return messageFactory.getMessage("runtime.parser.invalidVersionNumber");
    }

    /**
     * unsupported runtime descriptor version: {2}
     * 
     */
    public static String RUNTIME_PARSER_INVALID_VERSION_NUMBER() {
        return localizer.localize(localizableRUNTIME_PARSER_INVALID_VERSION_NUMBER());
    }

    public static Localizable localizablePROVIDER_INVALID_PARAMETER_TYPE(Object arg0, Object arg1) {
        return messageFactory.getMessage("provider.invalid.parameterType", arg0, arg1);
    }

    /**
     * "{0}" implements Provider but its type parameter {1} is incorrect
     * 
     */
    public static String PROVIDER_INVALID_PARAMETER_TYPE(Object arg0, Object arg1) {
        return localizer.localize(localizablePROVIDER_INVALID_PARAMETER_TYPE(arg0, arg1));
    }

    public static Localizable localizableWRONG_NO_PARAMETERS(Object arg0) {
        return messageFactory.getMessage("wrong.no.parameters", arg0);
    }

    /**
     * Incorrect no of arguments for method "{0}"
     * 
     */
    public static String WRONG_NO_PARAMETERS(Object arg0) {
        return localizer.localize(localizableWRONG_NO_PARAMETERS(arg0));
    }

    public static Localizable localizableANNOTATION_ONLY_ONCE(Object arg0) {
        return messageFactory.getMessage("annotation.only.once", arg0);
    }

    /**
     * Only one method should have the annotation "{0}"
     * 
     */
    public static String ANNOTATION_ONLY_ONCE(Object arg0) {
        return localizer.localize(localizableANNOTATION_ONLY_ONCE(arg0));
    }

    public static Localizable localizableALREADY_HTTPS_SERVER(Object arg0) {
        return messageFactory.getMessage("already.https.server", arg0);
    }

    /**
     * There is already a HTTPS server at : {0}
     * 
     */
    public static String ALREADY_HTTPS_SERVER(Object arg0) {
        return localizer.localize(localizableALREADY_HTTPS_SERVER(arg0));
    }

    public static Localizable localizableRUNTIME_PARSER_XML_READER(Object arg0) {
        return messageFactory.getMessage("runtime.parser.xmlReader", arg0);
    }

    /**
     * error parsing runtime descriptor: {0}
     * 
     */
    public static String RUNTIME_PARSER_XML_READER(Object arg0) {
        return localizer.localize(localizableRUNTIME_PARSER_XML_READER(arg0));
    }

    public static Localizable localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("runtime.parser.wsdl.incorrectserviceport", arg0, arg1, arg2);
    }

    /**
     * could not get binding from WSDL! service: {0} or port {1} not found in the WSDL {2}.
     * It could be because service and port names do not match WSDL''s wsdl:service and wsdl:port names:
     *  1. service and port names are not there in deployment descriptor OR
     *  2. Either there is a typo in deployment descriptor''s service and port names OR
     *  3. The computed names from @WebService do not match wsdl:service and wsdl:port names
     * Suggest doing the following:
     *  1. Add/Correct entries for service and port names in deployment descriptor OR 
     *  2. Specify targetNamespace, serviceName, portName in @WebService on the endpoint class
     * 
     */
    public static String RUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableRUNTIME_PARSER_WSDL_INCORRECTSERVICEPORT(arg0, arg1, arg2));
    }

    public static Localizable localizableSERVER_RT_ERR(Object arg0) {
        return messageFactory.getMessage("server.rt.err", arg0);
    }

    /**
     * Server Runtime Error: {0}
     * 
     */
    public static String SERVER_RT_ERR(Object arg0) {
        return localizer.localize(localizableSERVER_RT_ERR(arg0));
    }

    public static Localizable localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("runtime.parser.invalidAttributeValue", arg0, arg1, arg2);
    }

    /**
     * invalid value for attribute "{2}" of element "{1}" in runtime descriptor (line {0})
     * 
     */
    public static String RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(arg0, arg1, arg2));
    }

    public static Localizable localizableNO_CURRENT_PACKET() {
        return messageFactory.getMessage("no.current.packet");
    }

    /**
     * This thread is not currently processing any web service request.
     * 
     */
    public static String NO_CURRENT_PACKET() {
        return localizer.localize(localizableNO_CURRENT_PACKET());
    }

    public static Localizable localizableRUNTIME_PARSER_UNEXPECTED_CONTENT(Object arg0) {
        return messageFactory.getMessage("runtime.parser.unexpectedContent", arg0);
    }

    /**
     * unexpected content in runtime descriptor (line {0})
     * 
     */
    public static String RUNTIME_PARSER_UNEXPECTED_CONTENT(Object arg0) {
        return localizer.localize(localizableRUNTIME_PARSER_UNEXPECTED_CONTENT(arg0));
    }

    public static Localizable localizableSTATEFUL_COOKIE_HEADER_REQUIRED(Object arg0) {
        return messageFactory.getMessage("stateful.cookie.header.required", arg0);
    }

    /**
     * This is a stateful web service and {0} header is required.
     * 
     */
    public static String STATEFUL_COOKIE_HEADER_REQUIRED(Object arg0) {
        return localizer.localize(localizableSTATEFUL_COOKIE_HEADER_REQUIRED(arg0));
    }

    public static Localizable localizableNULL_IMPLEMENTOR() {
        return messageFactory.getMessage("null.implementor");
    }

    /**
     * Implementor cannot be null
     * 
     */
    public static String NULL_IMPLEMENTOR() {
        return localizer.localize(localizableNULL_IMPLEMENTOR());
    }

    public static Localizable localizableRUNTIME_PARSER_WSDL(Object arg0) {
        return messageFactory.getMessage("runtime.parser.wsdl", arg0);
    }

    /**
     * exception during WSDL parsing: {0}
     * 
     */
    public static String RUNTIME_PARSER_WSDL(Object arg0) {
        return localizer.localize(localizableRUNTIME_PARSER_WSDL(arg0));
    }

    public static Localizable localizableSOAPENCODER_ERR() {
        return messageFactory.getMessage("soapencoder.err");
    }

    /**
     * Error in encoding SOAP Message
     * 
     */
    public static String SOAPENCODER_ERR() {
        return localizer.localize(localizableSOAPENCODER_ERR());
    }

    public static Localizable localizableWSDL_REQUIRED() {
        return messageFactory.getMessage("wsdl.required");
    }

    /**
     * wsdl is required
     * 
     */
    public static String WSDL_REQUIRED() {
        return localizer.localize(localizableWSDL_REQUIRED());
    }

    public static Localizable localizableNOT_HTTP_CONTEXT_TYPE(Object arg0) {
        return messageFactory.getMessage("not.HttpContext.type", arg0);
    }

    /**
     * Required com.sun.net.httpserver.HttpContext. Got : {0}
     * 
     */
    public static String NOT_HTTP_CONTEXT_TYPE(Object arg0) {
        return localizer.localize(localizableNOT_HTTP_CONTEXT_TYPE(arg0));
    }

    public static Localizable localizablePORT_NAME_REQUIRED() {
        return messageFactory.getMessage("port.name.required");
    }

    /**
     * Port QName is not found
     * 
     */
    public static String PORT_NAME_REQUIRED() {
        return localizer.localize(localizablePORT_NAME_REQUIRED());
    }

    public static Localizable localizableWRONG_TNS_FOR_PORT(Object arg0) {
        return messageFactory.getMessage("wrong.tns.for.port", arg0);
    }

    /**
     * Port namespace {0} doesn't match Service namespace {1}
     * 
     */
    public static String WRONG_TNS_FOR_PORT(Object arg0) {
        return localizer.localize(localizableWRONG_TNS_FOR_PORT(arg0));
    }

    public static Localizable localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("runtime.parser.wsdl.multiplebinding", arg0, arg1, arg2);
    }

    /**
     * multiple bindings found for binding ID {0} for service {1} in WSDL {2}
     * 
     */
    public static String RUNTIME_PARSER_WSDL_MULTIPLEBINDING(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableRUNTIME_PARSER_WSDL_MULTIPLEBINDING(arg0, arg1, arg2));
    }

    public static Localizable localizableALREADY_HTTP_SERVER(Object arg0) {
        return messageFactory.getMessage("already.http.server", arg0);
    }

    /**
     * There is already a HTTP server at : {0} 
     * 
     */
    public static String ALREADY_HTTP_SERVER(Object arg0) {
        return localizer.localize(localizableALREADY_HTTP_SERVER(arg0));
    }

    public static Localizable localizableCAN_NOT_GENERATE_WSDL(Object arg0) {
        return messageFactory.getMessage("can.not.generate.wsdl", arg0);
    }

    /**
     * Cannot generate WSDL for binding "{0}"
     * 
     */
    public static String CAN_NOT_GENERATE_WSDL(Object arg0) {
        return localizer.localize(localizableCAN_NOT_GENERATE_WSDL(arg0));
    }

    public static Localizable localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object arg0, Object arg1) {
        return messageFactory.getMessage("runtime.parser.invalid.attribute.value", arg0, arg1);
    }

    /**
     * invalid attribute value "{1}" in runtime descriptor (line {0})
     * 
     */
    public static String RUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(Object arg0, Object arg1) {
        return localizer.localize(localizableRUNTIME_PARSER_INVALID_ATTRIBUTE_VALUE(arg0, arg1));
    }

    public static Localizable localizableRUNTIME_PARSER_WRONG_ELEMENT(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("runtime.parser.wrong.element", arg0, arg1, arg2);
    }

    /**
     * found element "{1}", expected "{2}" in runtime descriptor (line {0})
     * 
     */
    public static String RUNTIME_PARSER_WRONG_ELEMENT(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableRUNTIME_PARSER_WRONG_ELEMENT(arg0, arg1, arg2));
    }

    public static Localizable localizableRUNTIME_PARSER_WSDL_NOSERVICE() {
        return messageFactory.getMessage("runtime.parser.wsdl.noservice");
    }

    /**
     * can't apply binding! service {0} not found in the WSDL {1}
     * 
     */
    public static String RUNTIME_PARSER_WSDL_NOSERVICE() {
        return localizer.localize(localizableRUNTIME_PARSER_WSDL_NOSERVICE());
    }

    public static Localizable localizableSERVICE_NAME_REQUIRED() {
        return messageFactory.getMessage("service.name.required");
    }

    /**
     * Service QName is not found
     * 
     */
    public static String SERVICE_NAME_REQUIRED() {
        return localizer.localize(localizableSERVICE_NAME_REQUIRED());
    }

    public static Localizable localizablePROVIDER_NOT_PARAMETERIZED(Object arg0) {
        return messageFactory.getMessage("provider.not.parameterized", arg0);
    }

    /**
     * "{0}" implements Provider but doesn't specify the type parameter
     * 
     */
    public static String PROVIDER_NOT_PARAMETERIZED(Object arg0) {
        return localizer.localize(localizablePROVIDER_NOT_PARAMETERIZED(arg0));
    }

    public static Localizable localizableRUNTIME_WSDL_PATCHER() {
        return messageFactory.getMessage("runtime.wsdl.patcher");
    }

    /**
     * error while patching WSDL related document
     * 
     */
    public static String RUNTIME_WSDL_PATCHER() {
        return localizer.localize(localizableRUNTIME_WSDL_PATCHER());
    }

    public static Localizable localizableRUNTIME_SAXPARSER_EXCEPTION(Object arg0, Object arg1) {
        return messageFactory.getMessage("runtime.saxparser.exception", arg0, arg1);
    }

    /**
     * {0}
     * {1}
     * 
     */
    public static String RUNTIME_SAXPARSER_EXCEPTION(Object arg0, Object arg1) {
        return localizer.localize(localizableRUNTIME_SAXPARSER_EXCEPTION(arg0, arg1));
    }

    public static Localizable localizableRUNTIME_PARSER_WSDL_NOT_FOUND(Object arg0) {
        return messageFactory.getMessage("runtime.parser.wsdl.not.found", arg0);
    }

    /**
     * {0} is not found in the WAR file. Package it in the WAR file or correct it in sun-jaxws.xml.
     * 
     */
    public static String RUNTIME_PARSER_WSDL_NOT_FOUND(Object arg0) {
        return localizer.localize(localizableRUNTIME_PARSER_WSDL_NOT_FOUND(arg0));
    }

    public static Localizable localizableWRONG_PARAMETER_TYPE(Object arg0) {
        return messageFactory.getMessage("wrong.parameter.type", arg0);
    }

    /**
     * Incorrect argument types for method "{0}"
     * 
     */
    public static String WRONG_PARAMETER_TYPE(Object arg0) {
        return localizer.localize(localizableWRONG_PARAMETER_TYPE(arg0));
    }

    public static Localizable localizableRUNTIME_PARSER_CLASS_NOT_FOUND(Object arg0) {
        return messageFactory.getMessage("runtime.parser.classNotFound", arg0);
    }

    /**
     * class not found in runtime descriptor: {0}
     * 
     */
    public static String RUNTIME_PARSER_CLASS_NOT_FOUND(Object arg0) {
        return localizer.localize(localizableRUNTIME_PARSER_CLASS_NOT_FOUND(arg0));
    }

    public static Localizable localizableSTATIC_RESOURCE_INJECTION_ONLY(Object arg0, Object arg1) {
        return messageFactory.getMessage("static.resource.injection.only", arg0, arg1);
    }

    /**
     * Static resource {0} cannot be injected to non-static "{1}"
     * 
     */
    public static String STATIC_RESOURCE_INJECTION_ONLY(Object arg0, Object arg1) {
        return localizer.localize(localizableSTATIC_RESOURCE_INJECTION_ONLY(arg0, arg1));
    }

    public static Localizable localizableNOT_ZERO_PARAMETERS(Object arg0) {
        return messageFactory.getMessage("not.zero.parameters", arg0);
    }

    /**
     * Method "{0}" shouldn''t have any arguments
     * 
     */
    public static String NOT_ZERO_PARAMETERS(Object arg0) {
        return localizer.localize(localizableNOT_ZERO_PARAMETERS(arg0));
    }

    public static Localizable localizableDUPLICATE_PRIMARY_WSDL(Object arg0) {
        return messageFactory.getMessage("duplicate.primary.wsdl", arg0);
    }

    /**
     * Metadata has more than one WSDL that has Service definiton for the endpoint. WSDL={0} is one such WSDL.
     * 
     */
    public static String DUPLICATE_PRIMARY_WSDL(Object arg0) {
        return localizer.localize(localizableDUPLICATE_PRIMARY_WSDL(arg0));
    }

    public static Localizable localizableDUPLICATE_ABSTRACT_WSDL(Object arg0) {
        return messageFactory.getMessage("duplicate.abstract.wsdl", arg0);
    }

    /**
     * Metadata has more than one WSDL that has PortType definiton for the endpoint. WSDL={0} is one such WSDL.
     * 
     */
    public static String DUPLICATE_ABSTRACT_WSDL(Object arg0) {
        return localizer.localize(localizableDUPLICATE_ABSTRACT_WSDL(arg0));
    }

    public static Localizable localizableSTATEFUL_INVALID_WEBSERVICE_CONTEXT(Object arg0) {
        return messageFactory.getMessage("stateful.invalid.webservice.context", arg0);
    }

    /**
     * Not a WebServiceContext from JAX-WS RI: {0}
     * 
     */
    public static String STATEFUL_INVALID_WEBSERVICE_CONTEXT(Object arg0) {
        return localizer.localize(localizableSTATEFUL_INVALID_WEBSERVICE_CONTEXT(arg0));
    }

    public static Localizable localizableRUNTIME_PARSER_WSDL_NOBINDING() {
        return messageFactory.getMessage("runtime.parser.wsdl.nobinding");
    }

    /**
     * can't apply binding! no binding found for binding ID {0] for service {1} in WSDL {2}
     * 
     */
    public static String RUNTIME_PARSER_WSDL_NOBINDING() {
        return localizer.localize(localizableRUNTIME_PARSER_WSDL_NOBINDING());
    }

    public static Localizable localizableRUNTIME_PARSER_INVALID_ELEMENT(Object arg0, Object arg1) {
        return messageFactory.getMessage("runtime.parser.invalidElement", arg0, arg1);
    }

    /**
     * invalid element "{1}" in runtime descriptor (line {0})
     * 
     */
    public static String RUNTIME_PARSER_INVALID_ELEMENT(Object arg0, Object arg1) {
        return localizer.localize(localizableRUNTIME_PARSER_INVALID_ELEMENT(arg0, arg1));
    }

    public static Localizable localizableRUNTIME_PARSER_MISSING_ATTRIBUTE(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("runtime.parser.missing.attribute", arg0, arg1, arg2);
    }

    /**
     * missing attribute "{2}" in element "{1}" of runtime descriptor (line {0})
     * 
     */
    public static String RUNTIME_PARSER_MISSING_ATTRIBUTE(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableRUNTIME_PARSER_MISSING_ATTRIBUTE(arg0, arg1, arg2));
    }

    public static Localizable localizableWRONG_FIELD_TYPE(Object arg0) {
        return messageFactory.getMessage("wrong.field.type", arg0);
    }

    /**
     * Incorrect type for field "{0}"
     * 
     */
    public static String WRONG_FIELD_TYPE(Object arg0) {
        return localizer.localize(localizableWRONG_FIELD_TYPE(arg0));
    }

    public static Localizable localizableDUPLICATE_PORT_KNOWN_HEADER(Object arg0) {
        return messageFactory.getMessage("duplicate.portKnownHeader", arg0);
    }

    /**
     * Received SOAP message contains duplicate header: {0} for a bound parameter
     * 
     */
    public static String DUPLICATE_PORT_KNOWN_HEADER(Object arg0) {
        return localizer.localize(localizableDUPLICATE_PORT_KNOWN_HEADER(arg0));
    }

    public static Localizable localizableUNSUPPORTED_CONTENT_TYPE(Object arg0, Object arg1) {
        return messageFactory.getMessage("unsupported.contentType", arg0, arg1);
    }

    /**
     * Unsupported Content-Type: {0} Supported ones are: {1}
     * 
     */
    public static String UNSUPPORTED_CONTENT_TYPE(Object arg0, Object arg1) {
        return localizer.localize(localizableUNSUPPORTED_CONTENT_TYPE(arg0, arg1));
    }

    public static Localizable localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object arg0, Object arg1, Object arg2) {
        return messageFactory.getMessage("failed.to.instantiate.instanceResolver", arg0, arg1, arg2);
    }

    /**
     * Unable to instantiate {0} (which is specified in {1} on {2})
     * 
     */
    public static String FAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(Object arg0, Object arg1, Object arg2) {
        return localizer.localize(localizableFAILED_TO_INSTANTIATE_INSTANCE_RESOLVER(arg0, arg1, arg2));
    }

    public static Localizable localizableDD_MTOM_CONFLICT(Object arg0, Object arg1) {
        return messageFactory.getMessage("dd.mtom.conflict", arg0, arg1);
    }

    /**
     * Error in Deployment Descriptor : MTOM Configuration in binding {0} conflicts with enable-mtom attribute value {1}
     * 
     */
    public static String DD_MTOM_CONFLICT(Object arg0, Object arg1) {
        return localizer.localize(localizableDD_MTOM_CONFLICT(arg0, arg1));
    }

}
