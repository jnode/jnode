/*
 * Copyright 1999-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.corba.se.spi.servicecontext;

import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.servicecontext.ServiceContext ;

public class UnknownServiceContext extends ServiceContext {
    public UnknownServiceContext( int id, byte[] data ) 
    {
	this.id = id ;
	this.data = data ;
    }

    public UnknownServiceContext( int id, InputStream is ) 
    {
	this.id = id ;

	int len = is.read_long();
	data = new byte[len];
	is.read_octet_array(data,0,len);
    }

    public int getId() { return id ; }

    public void writeData( OutputStream os ) throws SystemException
    {
    }

    public void write( OutputStream os , GIOPVersion gv) 
        throws SystemException 
    {
	os.write_long( id ) ;
	os.write_long( data.length ) ;
	os.write_octet_array( data, 0, data.length ) ;
    }

    public byte[] getData() 
    {
	return data ;
    }

    private int id = -1 ;
    private byte[] data = null ;
}
