package javax.jws;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Customizes the mapping of the return value to a WSDL part and XML element.
 *
 * @author Copyright (c) 2004 by BEA Systems, Inc. All Rights Reserved.
 */
@Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.METHOD})
    public @interface WebResult
{

    /**
     * Name of return value.
     * <p>
     * If the operation is rpc style and @WebResult.partName has not been specified, this is the name of the wsdl:part
     * representing the return value.
     * <br>
     * If the operation is document style or the return value maps to a header, this is the local name of the XML
     * element representing the return value.
     *
     * @specdefault
     *   If the operation is document style and the parameter style is BARE, {@code @WebParam.operationName}+"Response".<br>
     *   Otherwise, "return."
     */
    String name() default "";

    /**
     * The name of the wsdl:part representing this return value.
     * <p>
     * This is only used if the operation is rpc style, or if the operation is document style and the parameter style
     * is BARE.
     *
     * @specdefault {@code @WebResult.name}
     *
     * @since 2.0
     */
    String partName() default "";

    /**
     * The XML namespace for the return value.
     * <p>
     * Only used if the operation is document style or the return value maps to a header.
     * If the target namespace is set to "", this represents the empty namespace.
     *
     * @specdefault
     *   If the operation is document style, the parameter style is WRAPPED, and the return value does not map to a
     *   header, the empty namespace.<br>
     *   Otherwise, the targetNamespace for the Web Service.
     */
    String targetNamespace() default "";

    /**
     * If true, the result is pulled from a message header rather then the message body.
     *
     * @since 2.0
     */
    boolean header() default false;
}
