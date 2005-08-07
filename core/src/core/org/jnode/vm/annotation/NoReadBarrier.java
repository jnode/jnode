/*
 * $Id$
 */
package org.jnode.vm.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * When a methods throws this exception, no read barriers will
 * be called upon array access.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Documented
@Retention(CLASS)
@Target(METHOD)
public @interface NoReadBarrier {

}
