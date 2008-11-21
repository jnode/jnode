/*
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

/*
 * Copyright (c) 2004 by BEA Systems, Inc. All Rights Reserved.
 */

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
