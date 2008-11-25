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

//Copyright Sun Microsystems Inc. 2004 - 2005.

package javax.annotation;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * The PreDestroy annotation is used on methods as a callback notification to 
 * signal that the instance is in the process of being removed by the 
 * container. The method annotated with PreDestroy is typically used to 
 * release resources that it has been holding. This annotation MUST be 
 * supported by all container managed objects that support PostConstruct 
 * except the application client container in Java EE 5. The method on which 
 * the PreDestroy annotation is applied MUST fulfill all of the following 
 * criteria - 
 * - The method MUST NOT have any parameters except in the case of EJB 
 * interceptors in which case it takes an InvocationContext object as defined 
 * by the EJB specification.
 * - The return type of the method MUST be void.
 * - The method MUST NOT throw a checked exception.
 * - The method on which PreDestroy is applied MAY be public, protected, 
 * package private or private.
 * - The method MUST NOT be static.
 * - The method MAY be final.
 * - If the method throws an unchecked exception it is ignored except in the 
 * case of EJBs where the EJB can handle exceptions.
 *
 * @see javax.annotation.PostConstruct
 * @see javax.annotation.Resource
 * @since Common Annotations 1.0
 */

@Documented
@Retention (RUNTIME)
@Target(METHOD)
public @interface PreDestroy {
}
