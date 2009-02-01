/*
 * $Id$
 *
 * JNode.org
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

public class Symbol {
    /**
     * symbol table - page 4-25, figure 4-15 typedef struct { Elf32_Word
     * st_name; Elf32_Addr st_value; Elf32_Word st_size; unsigned char st_info;
     * unsigned char st_other; Elf32_Half st_shndx; } Elf32_Sym;
     */
    public static final int SHN_ABS = 0xFFFFFFF1;

    public static final int SHN_COMMON = 0xFFFFFFF2;

    public static final int SHN_UNDEF = 0;

    // Symbol binding
    public static final int STB_LOCAL = 0;

    public static final int STB_GLOBAL = 1;

    public static final int STB_WEAK = 2;

    public static final int STB_LOPROC = 13;

    public static final int STB_HIPROC = 15;

    // Symbol type
    public static final int STT_NOTYPE = 0;

    public static final int STT_OBJECT = 1;

    public static final int STT_FUNC = 2;

    public static final int STT_SECTION = 3;

    public static final int STT_FILE = 4;

    public static final int STT_LOPROC = 13;

    public static final int STT_HIPROC = 15;

    private int st_name;

    private long st_value;

    private long st_size;

    private int st_info;

    private int st_other;

    private int st_shndx;

    private Elf elf;

    public Symbol(Elf elf, InputStream in) throws IOException {
        this.elf = elf;
        if (elf.isClass32()) {
            st_name = LoadUtil.little32(in);
            st_value = LoadUtil.loadAddr(in, elf.e_ident);
            st_size = LoadUtil.loadXword(in, elf.e_ident);
            st_info = LoadUtil.little8(in);
            st_other = LoadUtil.little8(in);
            st_shndx = LoadUtil.little16(in);
        } else {
            st_name = LoadUtil.little32(in);
            st_info = LoadUtil.little8(in);
            st_other = LoadUtil.little8(in);
            st_shndx = LoadUtil.little16(in);
            st_value = LoadUtil.loadAddr(in, elf.e_ident);
            st_size = LoadUtil.loadXword(in, elf.e_ident);
        }
    }

    public Symbol(Elf elf, String name, int value, Section section) {
        this.elf = elf;
        if (name != null) {
            this.st_name = elf.addString(name);
        } else {
            this.st_name = 0;
        }
        this.st_value = value;
        if (section != null) {
            this.st_shndx = elf.getSectionIndex(section);
        } else {
            this.st_shndx = SHN_UNDEF;
        }
        this.st_info = (STB_GLOBAL << 4) | (STT_FUNC & 0xF);
    }

    public int store(OutputStream out) throws IOException {
        int cnt = 0;
        if (elf.isClass32()) {
            cnt += StoreUtil.little32(out, st_name);
            cnt += StoreUtil.storeAddr(out, elf.e_ident, st_value);
            cnt += StoreUtil.storeXword(out, elf.e_ident, st_size);
            cnt += StoreUtil.little8(out, st_info);
            cnt += StoreUtil.little8(out, st_other);
            cnt += StoreUtil.little16(out, st_shndx);
        } else {
            cnt += StoreUtil.little32(out, st_name);
            cnt += StoreUtil.little8(out, st_info);
            cnt += StoreUtil.little8(out, st_other);
            cnt += StoreUtil.little16(out, st_shndx);
            cnt += StoreUtil.storeAddr(out, elf.e_ident, st_value);
            cnt += StoreUtil.storeXword(out, elf.e_ident, st_size);
        }
        return cnt;
    }

    public String toString() {
        return getName() + ", " + getValue();
    }

    public String getName() {
        return elf.getString(st_name);
    }

    public long getValue() {
        return st_value;
    }

    public void setValue(long v) {
        st_value = v;
    }

    public long getSize() {
        return st_size;
    }

    public void setSize(long v) {
        st_size = v;
    }

    public int getBind() {
        return (st_info >> 4);
    }

    public void setBind(int v) {
        setInfo(v, getType());
    }

    public int getType() {
        return (st_info & 0xF);
    }

    public void setType(int v) {
        setInfo(getBind(), v);
    }

    public void setInfo(int bind, int type) {
        st_info = (bind << 4) | (type & 0xf);
    }

    public int getOther() {
        return st_other;
    }

    public void setOther(int v) {
        st_other = v;
    }

    public int getSectionIndex() {
        return st_shndx;
    }

    public Section getSection() {
        return elf.getSection(st_shndx);
    }

    public boolean isAbs() {
        return (st_shndx == SHN_ABS);
    }

    public void setAbs() {
        st_shndx = SHN_ABS;
    }

    public boolean isUndef() {
        return (st_shndx == SHN_UNDEF);
    }

    public void setUndef() {
        st_shndx = SHN_UNDEF;
    }

    public void print() {
        System.out.println("  ----- Symbol -----");
        System.out.println("  st_name       : " + getName());
        System.out.println("  st_value      : "
            + Long.toString(st_value, 16));
        System.out
            .println("  st_size       : " + Long.toString(st_size, 16));
        System.out
            .println("  st_info       : " + Integer.toString(st_info, 16));
        System.out.println("  st_other      : "
            + Integer.toString(st_other, 16));
        System.out.println("  st_section    : " + st_shndx /* + getSection().getName() */);
    }
}
