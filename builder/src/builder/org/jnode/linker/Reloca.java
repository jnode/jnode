/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.linker;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Reloca extends Reloc {

	private final long r_addend;

	/**
	 * @param elf
	 * @param in
	 * @throws IOException
	 */
	public Reloca(Elf elf, InputStream in) throws IOException {
		super(elf, in);
		this.r_addend = LoadUtil.loadXword(in, elf.e_ident);
	}

	public final boolean hasAddEnd() {
		return true;
	}
	
	public final long getAddEnd() {
		return r_addend;
	}
}
