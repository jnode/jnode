package javax.jws;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Associates the Web Service with an externally defined handler chain.  This annotation is typically used in scenarios
 * where embedding the handler configuration directly in the Java source is not appropriate; for example, where the
 * handler configuration needs to be shared across multiple Web Services, or where the handler chain consists of
 * handlers for multiple transports.
 *
 * It is an error to combine this annotation with the @SOAPMessageHandlers annotation.
 *
 * @author Copyright (c) 2004 by BEA Systems, Inc. All Rights Reserved.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface HandlerChain {

    /**
     * Location of the handler chain file.
     * <p>
     * The location supports 2 formats:
     * <ol>
     * <li>An absolute java.net.URL in externalForm (ex: http://myhandlers.foo.com/handlerfile1.xml).
     * <li>A relative path from the source file or class file (ex: bar/handlerfile1.xml).
     * </ol>
     */
    String file();

    /**
     * Name of the handler chain in the configuration file
     *
     * @deprecated As of JSR-181 2.0 with no replacement.
     */
    @Deprecated String name() default "";
};
