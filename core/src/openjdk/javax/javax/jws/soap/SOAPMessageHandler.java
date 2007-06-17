package javax.jws.soap;

/**
 * A single SOAP message handler
 *
 * @deprecated  As of JSR-181 2.0 with no replacement.
 *
 * @author Copyright (c) 2004 by BEA Systems, Inc. All Rights Reserved.
 */
@Deprecated public @interface SOAPMessageHandler {

    /**
     * Name of the handler.  Defaults to the name of the handler class.
     */
    String name() default "";

    /**
     * Name of the handler class.
     */
    String className();

    /**
     * Array of name/value pairs that should be passed to the handler during initialization.
     */
    InitParam[] initParams() default {};

    /**
     * List of SOAP roles/actors implemented by the handler
     */
    String[] roles() default {};

    /**
     * List of SOAP headers processed by the handler.  Each element in this array contains a QName which defines the
     * header element processed by the handler.  The QNames are specified using the string notation described in the
     * documentation for javax.xml.namespace.QName.valueOf(String qNameAsString)
     */
    String[] headers() default {};
};


