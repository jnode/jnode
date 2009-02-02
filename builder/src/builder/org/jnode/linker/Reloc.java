/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.io.InputStream;
import java.io.OutputStream;
import org.jnode.util.NumberUtils;

public class Reloc {
    public static final Type R_386_32 = new Type("R_386_32", 1); /* ordinary absolute relocation */

    public static final Type R_386_PC32 = new Type("R_386_PC32", 2); /* PC-relative relocation */

    public static final Type R_386_GOT32 = new Type("R_386_GOT32", 3); /* an offset into GOT */

    public static final Type R_386_PLT32 = new Type("R_386_PLT32", 4);

    /* a PC-relative offset into PLT */
    public static final Type R_386_GOTOFF = new Type("R_386_GOTOFF", 9); /* an offset from GOT base */

    public static final Type R_386_GOTPC = new Type("R_386_GOTPC", 10);

    public static final Type[] i386_RelocNumbers = {
        R_386_32, R_386_PC32, R_386_GOT32, R_386_PLT32, R_386_GOTOFF, R_386_GOTPC
    };

    public static final Type R_X86_64_NONE = new Type(
        "R_X86_64_NONE", 0); /* No reloc */

    public static final Type R_X86_64_64 = new Type(
        "R_X86_64_64", 1); /* Direct 64 bit */

    public static final Type R_X86_64_PC32 = new Type(
        "R_X86_64_PC32", 2); /* PC relative 32 bit signed */

    public static final Type R_X86_64_GOT32 = new Type(
        "R_X86_64_GOT32", 3); /* 32 bit GOT entry */

    public static final Type R_X86_64_PLT32 = new Type(
        "R_X86_64_PLT32", 4); /* 32 bit PLT address */

    public static final Type R_X86_64_COPY = new Type(
        "R_X86_64_COPY", 5); /* Copy symbol at runtime */

    public static final Type R_X86_64_GLOB_DAT = new Type(
        "R_X86_64_GLOB_DAT", 6); /* Create GOT entry */

    public static final Type R_X86_64_JUMP_SLOT = new Type(
        "R_X86_64_JUMP_SLOT", 7); /* Create PLT entry */

    public static final Type R_X86_64_RELATIVE = new Type(
        "R_X86_64_RELATIVE", 8); /* Adjust by program base */

    public static final Type R_X86_64_GOTPCREL = new Type(
        "R_X86_64_GOTPCREL", 9); /*
                                    * 32 bit signed pc relative offset
                                    * to GOT
                                    */

    public static final Type R_X86_64_32 = new Type(
        "R_X86_64_32", 10); /* Direct 32 bit zero extended */

    public static final Type R_X86_64_32S = new Type(
        "R_X86_64_32S", 11); /* Direct 32 bit sign extended */

    public static final Type R_X86_64_16 = new Type(
        "R_X86_64_16", 12); /* Direct 16 bit zero extended */

    public static final Type R_X86_64_PC16 = new Type(
        "R_X86_64_PC16", 13); /* 16 bit sign extended pc relative */

    public static final Type R_X86_64_8 = new Type(
        "R_X86_64_8", 14); /* Direct 8 bit sign extended */

    public static final Type R_X86_64_PC8 = new Type(
        "R_X86_64_PC8", 15); /* 8 bit sign extended pc relative */

    public static final Type R_X86_64_DTPMOD64 = new Type(
        "R_X86_64_DTPMOD64", 16); /* ID of module containing symbol */

    public static final Type R_X86_64_DTPOFF64 = new Type(
        "R_X86_64_DTPOFF64", 17); /* Offset in TLS block */

    public static final Type R_X86_64_TPOFF64 = new Type(
        "R_X86_64_TPOFF64", 18); /* Offset in initial TLS block */

    public static final Type R_X86_64_TLSGD = new Type(
        "R_X86_64_TLSGD", 19); /* PC relative offset to GD GOT block */

    public static final Type R_X86_64_TLSLD = new Type(
        "R_X86_64_TLSLD", 20); /* PC relative offset to LD GOT block */

    public static final Type R_X86_64_DTPOFF32 = new Type(
        "R_X86_64_DTPOFF32", 21); /* Offset in TLS block */

    public static final Type R_X86_64_GOTTPOFF = new Type(
        "R_X86_64_GOTTPOFF", 22); /*
                                    * PC relative offset to IE GOT
                                    * entry
                                    */

    public static final Type R_X86_64_TPOFF32 = new Type(
        "R_X86_64_TPOFF32", 23); /* Offset in initial TLS block */

    public static final Type[] x86_64_RelocNumbers = {
        R_X86_64_NONE,           /* No reloc */
        R_X86_64_64,             /* Direct 64 bit  */
        R_X86_64_PC32,           /* PC relative 32 bit signed */
        R_X86_64_GOT32,          /* 32 bit GOT entry */
        R_X86_64_PLT32,          /* 32 bit PLT address */
        R_X86_64_COPY,           /* Copy symbol at runtime */
        R_X86_64_GLOB_DAT,       /* Create GOT entry */
        R_X86_64_JUMP_SLOT,      /* Create PLT entry */
        R_X86_64_RELATIVE,       /* Adjust by program base */
        R_X86_64_GOTPCREL,       /* 32 bit signed pc relative
                                    offset to GOT */
        R_X86_64_32,            /* Direct 32 bit zero extended */
        R_X86_64_32S,           /* Direct 32 bit sign extended */
        R_X86_64_16,            /* Direct 16 bit zero extended */
        R_X86_64_PC16,          /* 16 bit sign extended pc relative*/
        R_X86_64_8,             /* Direct 8 bit sign extended */
        R_X86_64_PC8,           /* 8 bit sign extended pc relative*/
        R_X86_64_DTPMOD64,      /* ID of module containing symbol */
        R_X86_64_DTPOFF64,      /* Offset in TLS block */
        R_X86_64_TPOFF64,       /* Offset in initial TLS block */
        R_X86_64_TLSGD,         /* PC relative offset to GD GOT block */
        R_X86_64_TLSLD,         /* PC relative offset to LD GOT block */
        R_X86_64_DTPOFF32,      /* Offset in TLS block */
        R_X86_64_GOTTPOFF,      /* PC relative offset to IE GOT entry */
        R_X86_64_TPOFF32,       /* Offset in initial TLS block */

    };

    private final long r_address;

    private int r_symndx;

    private final int r_type;

    private final Elf elf;

    private Symbol symbol;

    private long r_info;

    public Reloc(Elf elf, InputStream in) throws IOException {
        this.elf = elf;
        this.r_address = LoadUtil.loadAddr(in, elf.e_ident);
        final long info = LoadUtil.loadXword(in, elf.e_ident);
        this.r_info = info;
        if (elf.isClass32()) {
            this.r_symndx = (int) (info >> 8);
            this.r_type = (int) (info & 0xFF);
        } else {
            this.r_symndx = (int) (info >>> 32);
            this.r_type = (int) (info & 0xFFFFFFFF);
        }
    }

    private Reloc(Elf elf, Symbol symbol, long address, Type type) {
        this.elf = elf;
        this.r_address = address;
        this.r_type = type.getNr();
        this.symbol = symbol;
    }

    public static Reloc newAbsInstance(Elf elf, Symbol symbol, long address) {
        return new Reloc(elf, symbol, address, R_386_32);
    }

    public static Reloc newPcRelInstance(Elf elf, Symbol symbol, long address) {
        return new Reloc(elf, symbol, address, R_386_PC32);
    }

    public int store(OutputStream out) throws IOException {

        if (symbol != null) {
            r_symndx = elf.getIndexOfSymbol(symbol);
        }

        final long info;
        if (elf.isClass32()) {
            info = (r_type & 0xFF) | (r_symndx << 8);
        } else {
            info = ((long) r_type) | (((long) r_symndx) << 32);
        }
        int cnt = 0;
        cnt += StoreUtil.storeAddr(out, elf.e_ident, r_address);
        cnt += StoreUtil.storeXword(out, elf.e_ident, info);
        return cnt;
    }

    public long getAddress() {
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

    public Type getRelocType() {
        final Type[] types = (elf.isClass32() ? i386_RelocNumbers : x86_64_RelocNumbers);
        final int length = types.length;
        for (int i = 0; i < length; i++) {
            if (types[i].getNr() == r_type) {
                return types[i];
            }
        }
        return null;
    }

    public String getTypeName() {
        final Type type = getRelocType();
        if (type != null) {
            return type.toString();
        } else {
            return "type-" + r_type;
        }
    }

    public Symbol getSymbol() {
        if (symbol != null) {
            return symbol;
        } else {
            return elf.getSymbol(r_symndx);
        }
    }

    public boolean hasAddEnd() {
        return false;
    }

    public long getAddEnd() {
        return 0;
    }

    public void print() {
        System.out.println("  ----- Reloc -----");
        System.out.println("  r_address     : " + Long.toHexString(r_address));
        System.out.println("  r_symbol      : " + getSymbol());
        System.out.println("  r_type        : " + getTypeName());
        System.out.println("  r_info        : " + NumberUtils.hex(r_info));
        if (hasAddEnd()) {
            System.out.println("  r_addend      : " + NumberUtils.hex(getAddEnd()));
        }
    }

    public static final class Type {
        private final int nr;

        private final String name;


        private Type(String name, int nr) {
            this.name = name;
            this.nr = nr;
        }

        public int getNr() {
            return nr;
        }

        public String toString() {
            return name;
        }
    }
}
