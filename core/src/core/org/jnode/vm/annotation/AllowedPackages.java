/*
 * $Id$
 */
package org.jnode.vm.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface AllowedPackages {

    /**
     * Gets the packages in which it is allowed to use this annotation.
     * @return
     */
    String[] value();
    
}
