/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.linker;

import java.io.IOException;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.build.BuildException;

/**
 * Build the boot image from an assembler compiled bootstrap (in ELF format)
 * combined with the precompiled Java classes.
 */
public class ElfLinker {
    private X86BinaryAssembler os;

    private int start;

    private int baseAddr;

    public ElfLinker(X86BinaryAssembler os) {
        this.os = os;
        baseAddr = (int) os.getBaseAddr();
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
        start = os.getLength();
        final byte[] tdata = text.getBody();
        os.write(tdata, 0, tdata.length);

        // Add all resolved symbols
        final int symCnt = elf.getNoSymbols();
        for (int i = 1; i < symCnt; i++) {
            final Symbol sym = elf.getSymbol(i);
            final Section sec = sym.getSection();
            if (sec == text) {
                // System.out.println(sym);
                X86BinaryAssembler.X86ObjectRef ref = (X86BinaryAssembler.X86ObjectRef) os
                    .getObjectRef(new Label(sym.getName()));
                ref.setPublic();
                if (!sym.isUndef()) {
                    // System.out.println("Defined symbol at " + sym.getValue()
                    // + " [" + sym.getName() + "]");
                    ref.setOffset((int) sym.getValue() + start);
                } else {
                    System.out.println("Undefined symbol: " + sym.getName());
                }
            } else if ((sec != null) && !sym.isUndef()) {
                System.out
                    .println("Symbol '" + sym.getName()
                        + "' refers to unknown section '"
                        + sec.getName() + "'");
            }
        }

        // Add all relocation items
        Section rels = elf.getSectionByName(".rel.text");
        if (rels == null) {
            rels = elf.getSectionByName(".rela.text");
        }
        if (rels != null) {
            final int relocCnt = rels.getNoRelocs();
            for (int i = 0; i < relocCnt; i++) {
                try {
                    final Reloc r = rels.getReloc(i);
                    final String symName = r.getSymbol().getName();
                    final boolean hasSymName = (symName.length() > 0);
                    final boolean hasAddEnd = r.hasAddEnd();
                    final long addr = r.getAddress() + start;
                    final long addend = r.getAddEnd();

                    final Reloc.Type relocType = r.getRelocType();
                    if ((relocType == Reloc.R_386_32) && !hasAddEnd) {
                        resolve_R386_32(addr, symName, hasSymName);
                    } else if ((relocType == Reloc.R_386_PC32) && !hasAddEnd) {
                        resolve_R386_PC32(addr, symName, hasSymName);
                    } else if ((relocType == Reloc.R_X86_64_32) && hasAddEnd) {
                        resolve_R_X86_64_32(addr, addend, symName, hasSymName);
                    } else if ((relocType == Reloc.R_X86_64_64) && hasAddEnd) {
                        resolve_R_X86_64_64(addr, addend, symName, hasSymName);
                    } else {
                        throw new BuildException("Unknown relocation type "
                            + relocType);
                    }
                } catch (UnresolvedObjectRefException ex) {
                    throw new BuildException(ex);
                }
            }
        }
    }

    /**
     * Resolve an absolute 32-bit address.
     *
     * @param addr
     * @param symName
     * @param hasSymName
     * @throws UnresolvedObjectRefException
     */
    private final void resolve_R386_32(long addr, String symName,
                                       boolean hasSymName) throws UnresolvedObjectRefException {
        if (!hasSymName) {
            os.set32((int) addr, os.get32((int) addr) + start + baseAddr);
        } else {
            final NativeStream.ObjectRef ref = os.getObjectRef(new Label(
                symName));
            if (ref.isResolved()) {
                os.set32((int) addr, ref.getOffset() + baseAddr);
            } else {
                os.set32((int) addr, -baseAddr);
                ref.addUnresolvedLink((int) addr, 4);
            }
        }
    }

    /**
     * Resolve an pc-relative 32-bit address.
     *
     * @param addr
     * @param symName
     * @param hasSymName
     * @throws UnresolvedObjectRefException
     */
    private final void resolve_R386_PC32(long addr, String symName,
                                         boolean hasSymName) throws UnresolvedObjectRefException {
        final NativeStream.ObjectRef ref = os.getObjectRef(new Label(symName));
        if (ref.isResolved()) {
            os.set32((int) addr, (int) (ref.getOffset() - (addr + 4)));
        } else {
            ref.addUnresolvedLink((int) addr, 4);
        }
    }

    /**
     * Resolve a direct 32 bit zero extended address.
     *
     * @param addr
     * @param symName
     * @param hasSymName
     * @throws UnresolvedObjectRefException
     */
    private final void resolve_R_X86_64_32(long addr, long addend,
                                           String symName, boolean hasSymName)
        throws UnresolvedObjectRefException {
        if (!hasSymName) {
            os.set32((int) addr, (int) (addend + start + baseAddr));
        } else {
            final NativeStream.ObjectRef ref = os.getObjectRef(new Label(
                symName));
            if (ref.isResolved()) {
                os.set32((int) addr,
                    (int) (ref.getOffset() + baseAddr + addend));
            } else {
                os.set32((int) addr, (int) -(baseAddr + addend));
                ref.addUnresolvedLink((int) addr, 4);
            }
        }
    }

    /**
     * Resolve a direct 64 bit address.
     *
     * @param addr
     * @param symName
     * @param hasSymName
     * @throws UnresolvedObjectRefException
     */
    private final void resolve_R_X86_64_64(long addr, long addend,
                                           String symName, boolean hasSymName)
        throws UnresolvedObjectRefException {
        if (!hasSymName) {
            os.set64((int) addr, addend + start + baseAddr);
        } else {
            final NativeStream.ObjectRef ref = os.getObjectRef(new Label(
                symName));
            if (ref.isResolved()) {
                os.set64((int) addr, ref.getOffset() + baseAddr + addend);
            } else {
                os.set64((int) addr, -(baseAddr + addend));
                ref.addUnresolvedLink((int) addr, 8);
            }
        }
    }

}
