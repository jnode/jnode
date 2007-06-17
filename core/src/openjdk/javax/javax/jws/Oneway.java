package javax.jws;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Indicates that the given @WebMethod has only an input message and no output.  Typically, a oneway method returns
 * the thread of control to the calling application prior to executing the actual business method.  A 181 processor
 * should report an error if an operation marked @Oneway has a return value or Holder parameters, or declares any
 * checked exceptions.
 *
 * @author Copyright (c) 2004 by BEA Systems, Inc. All Rights Reserved.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
public @interface Oneway {
}
