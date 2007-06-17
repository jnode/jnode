package javax.jws.soap;

/**
 * An initialization parameter
 *
 * @deprecated  As of JSR-181 2.0 with no replacement.
 * 
 * @author Copyright (c) 2004 by BEA Systems, Inc. All Rights Reserved.
 */
@Deprecated public @interface InitParam {

    /**
     * Name of the initialization parameter
     */
    String name();

    /**
     * Value of the initialization parameter
     */
    String value();
};

