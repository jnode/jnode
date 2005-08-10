/*
 * $Id$
 */
package org.jnode.vm.annotation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used to hint the compiler to inline the method on which
 * the annotation is applied.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Documented
@Retention(CLASS)
@Target({CONSTRUCTOR, METHOD})
public @interface Inline {

}
