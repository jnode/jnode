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

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.build.BuildException;

/**
* Build the boot image from an assembler compiled bootstrap (in ELF format)
* combined with the precompiled Java classes.
**/
public class ElfLinker {
	private X86BinaryAssembler os;
	private int baseAddr;

	public ElfLinker(X86BinaryAssembler os) {
		this.os = os;
		baseAddr = (int)os.getBaseAddr();
	}

	/**
	 * Load an ELF object file with a given name and link it into the native
	 * stream.
	 */
	public void loadElfObject(String name) throws BuildException {
		Elf elf;
		try {
			elf = Elf.newFromFile(name);
			loadElfObject(elf);
		} catch (IOException ex) {
			throw new BuildException(ex);
		}
	}


	/**
	 * Load an ELF object file with a given name and link it into the native
	 * stream.
	 */
	public void loadElfObject(Elf elf) throws BuildException {
		if (!elf.isRel()) {
			throw new BuildException("Elf object is not relocatable");
		}

		final Section text = elf.getSectionByName(".text");
		if (text == null) {
			throw new BuildException(".text section not found");
		}

		// Write the code
		final int start = os.getLength();
		final byte[] tdata = text.getBody();
		os.write(tdata, 0, tdata.length);

		// Add all resolved symbols
		int cnt = elf.getNoSymbols();
		for (int i = 1; i < cnt; i++) {
			Symbol sym = elf.getSymbol(i);
			Section sec = sym.getSection();
			if (sec == text) {
				X86BinaryAssembler.X86ObjectRef ref = (X86BinaryAssembler.X86ObjectRef)os.getObjectRef(new Label(sym.getName()));
				ref.setPublic();
				if (!sym.isUndef()) {
					//System.out.println("Defined symbol at " + sym.getValue() + " [" + sym.getName() + "]");
					ref.setOffset((int)sym.getValue() + start);
				} else {
					System.out.println("Undefined symbol: " + sym.getName());
				}
			} else if ((sec != null) && !sym.isUndef()){
				System.out.println("Symbol '"+ sym.getName() + "' refers to unknown section '" + sec.getName() + "'");
			}
		}

		// Add all relocation items
		Section rels = elf.getSectionByName(".rel.text");
		if (rels != null) {
			cnt = rels.getNoRelocs();
			for (int i = 0; i < cnt; i++) {
				Reloc r = rels.getReloc(i);
				if (!(r.isPcRel() || r.isAbs())) {
					throw new BuildException("Only PC relative and ABS relocations are supported");
				}
				final long addr = r.getAddress() + start;
				if (r.isAbs() && (r.getSymbol().getName().length() == 0)) {
					//System.out.print("Abs reloc at "+ addr + "=" + os.get32(addr));
					if (elf.isClass32()) {
						os.set32((int)addr, os.get32((int)addr) + start + baseAddr);
					} else {
						throw new BuildException("64-bit not implemented yet");
					}
					//System.out.println(" base=" + baseAddr + " start=" + start);
				} else {
					X86BinaryAssembler.X86ObjectRef ref = (X86BinaryAssembler.X86ObjectRef)os.getObjectRef(new Label(r.getSymbol().getName()));
					if (ref.isResolved()) {
						//System.out.println("Resolved reloc " + ref.getObject());
						if (r.isPcRel()) {
							if (elf.isClass32()) {
								os.set32((int)addr, (int)(ref.getOffset() - (addr + 4)));
							} else {
								throw new BuildException("64-bit not implemented yet");								
							}
						} else if (r.isAbs()) {
							if (elf.isClass32()) {
								os.set32((int)addr, ref.getOffset() + baseAddr);
							} else {
								throw new BuildException("64-bit not implemented yet");								
							}
						} else {
							throw new BuildException("Unknown relocation type " + r);
						}
					} else {
						//System.out.println("Unresolved reloc " + ref.getObject() + " at address " + r.getAddress());
						if (r.isAbs()) {
							if (elf.isClass32()) {
								os.set32((int)addr, -baseAddr);
								ref.addUnresolvedLink((int)addr);
							} else {
								throw new BuildException("64-bit not implemented yet");																
							}
						} else if (r.isPcRel()) { 
							if (elf.isClass32()) {
								ref.addUnresolvedLink((int)addr);
							} else {
								throw new BuildException("64-bit not implemented yet");																
							}
						} else {
							throw new BuildException("Unknown relocation type " + r);
						}						
					}
				}
			}
		}
	}

}
