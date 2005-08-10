/*
 * $Id$
 */
package org.jnode.vm.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to inform the compiler not to inline yield points.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Documented
@Retention(CLASS)
@Target({CONSTRUCTOR, METHOD, TYPE})
public @interface Uninterruptible {

}
