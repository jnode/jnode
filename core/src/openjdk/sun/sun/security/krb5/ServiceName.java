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
 *  (C) Copyright IBM Corp. 1999 All Rights Reserved.
 *  Copyright 1997 The Open Group Research Institute.  All rights reserved.
 */

package sun.security.krb5;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServiceName extends PrincipalName {

    public ServiceName(String name, int type) throws RealmException {
        super(name, type);

    }
    public ServiceName(String name) throws RealmException {
	this(name, PrincipalName.KRB_NT_UNKNOWN);
    }

    public ServiceName(String name, String realm) throws RealmException {
	this(name, PrincipalName.KRB_NT_UNKNOWN);
	setRealm(realm);
    }

    public ServiceName (String service, String instance, String realm)
        throws KrbException
    {
        super(service, instance, realm, PrincipalName.KRB_NT_SRV_INST);
    }

}
