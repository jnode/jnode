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
import java.io.OutputStream;


public class Reloc {
	/**
	* Relocation item
	* typedef struct
	* {
	*   Elf32_Addr	r_address;		       relative to _start_ of section
	*	  Elf32_Word	r_symbol;		       ELF symbol info thingy
	* } Elf32_Reloc;
	**/
	public static final int R_386_32 = 1; /* ordinary absolute relocation */
	public static final int R_386_PC32 = 2; /* PC-relative relocation */
	public static final int R_386_GOT32 = 3; /* an offset into GOT */
	public static final int R_386_PLT32 = 4;
	/* a PC-relative offset into PLT */
	public static final int R_386_GOTOFF = 9; /* an offset from GOT base */
	public static final int R_386_GOTPC = 10;
	/* a PC-relative offset _to_ GOT */

	static final String[] typename =
		{
			null,
			"abs32",
			"pcrel32",
			"GOT32",
			"PLT32",
			null,
			null,
			null,
			null,
			"GOTOFF",
			"GOTPC" };

	private int r_address;
	private int r_symndx;
	private int r_type;
	private Elf elf;
	private Symbol symbol;

	public Reloc(Elf elf, InputStream in) {
		this.elf = elf;
		r_address = LoadUtil.little32(in);
		int v = LoadUtil.little32(in);
		r_symndx = v >> 8;
		r_type = v & 0xFF;
	}

	private Reloc(Elf elf, Symbol symbol, int address, int type) {
		this.elf = elf;
		this.r_address = address;
		this.r_type = type;
		this.symbol = symbol;
	}

	public static Reloc newAbsInstance(Elf elf, Symbol symbol, int address) {
		return new Reloc(elf, symbol, address, R_386_32);
	}

	public static Reloc newPcRelInstance(Elf elf, Symbol symbol, int address) {
		return new Reloc(elf, symbol, address, R_386_PC32);
	}

	public int store(OutputStream out) throws IOException {
		
		if (symbol != null) {
			r_symndx = elf.getIndexOfSymbol(symbol);
		}
		
		int v = (r_type & 0xFF) | (r_symndx << 8);
		int cnt = 0;
		cnt += StoreUtil.little32(out, r_address);
		cnt += StoreUtil.little32(out, v);
		return cnt;
	}

	public int getAddress() {
		return r_address;
	}
	public int getSymbolIndex() {
		if (symbol != null) {
			return elf.getIndexOfSymbol(symbol);
		} else {
			return r_symndx;
		}
	}
	public int getType() {
		return r_type;
	}
	public String getTypeName() {
		return typename[r_type];
	}
	public Symbol getSymbol() {
		if (symbol != null) {
			return symbol;
		} else {
			return elf.getSymbol(r_symndx);
		}
	}

	public boolean isAbs() {
		return (r_type == R_386_32);
	}
	public boolean isPcRel() {
		return (r_type == R_386_PC32);
	}

	public void print() {
		System.out.println("  ----- Reloc -----");
		System.out.println(
			"  r_address     : " + Integer.toHexString(r_address));
		System.out.println("  r_symbol      : " + getSymbol());
		System.out.println("  r_type        : " + getTypeName());
	}
}
