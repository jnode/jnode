/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.net.arp;


public enum ARPOperation {
	ARP_REQUEST (1),
    ARP_REPLY (2),
    RARP_REQUEST (3),
    RARP_REPLY (4);
	
	private int id;
	
	private ARPOperation(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public static ARPOperation getType(int id){
		for(ARPOperation t : ARPOperation.values()){
			return t;
		}
		return null;
	}
}
