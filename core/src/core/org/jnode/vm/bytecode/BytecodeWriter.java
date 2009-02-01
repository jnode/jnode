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
 
package org.jnode.vm.bytecode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BytecodeWriter {

    private final ByteBuffer code;
    private HashSet<Label> labels;

    public BytecodeWriter(int capacity) {
        this.code = ByteBuffer.allocate(capacity);
    }

    public ByteBuffer toByteBuffer() {
        // Test all labels, they must have been resolved
        if (labels != null) {
            for (Label l : labels) {
                if (!l.isResolved()) {
                    throw new RuntimeException("Unresolved label " + l);
                }
            }
        }
        if (code.limit() > 0) {
            return (ByteBuffer) code.duplicate().rewind();
        } else {
            return null;
        }
    }

    public Label newLabel(String text) {
        final Label l = new Label(text);
        if (labels == null) {
            labels = new HashSet<Label>();
        }
        labels.add(l);
        return l;
    }

    public void clear() {
        code.rewind().limit(0);
        if (labels != null) {
            labels.clear();
        }
    }

    public void aaload() {
        write8(0x32);
    }

    public void aastore() {
        write8(0x53);
    }

    public void aconst_null() {
        write8(0x01);
    }

    public void aload(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x2A + index);
        } else {
            write8(0x19);
            write8(index);
        }
    }

    public void anewarray(int index) {
        write8(0xbd);
        write16(index);
    }

    public void areturn() {
        write8(0xb0);
    }

    public void arraylength() {
        write8(0xbe);
    }

    public void astore(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x4b + index);
        } else {
            write8(0x3a);
            write8(index);
        }
    }

    public void athrow() {
        write8(0xbf);
    }

    public void baload() {
        write8(0x33);
    }

    public void bastore() {
        write8(0x54);
    }

    public void bipush() {
        write8(0x10);
    }

    public void caload() {
        write8(0x34);
    }

    public void castore() {
        write8(0x55);
    }

    public void checkcast() {
        write8(0xc0);
    }

    public void d2f() {
        write8(0x90);
    }

    public void d2i() {
        write8(0x8e);
    }

    public void d2l() {
        write8(0x8f);
    }

    public void dadd() {
        write8(0x63);
    }

    public void daload() {
        write8(0x31);
    }

    public void dastore() {
        write8(0x52);
    }

    public void dcmpg() {
        write8(0x98);
    }

    public void dcmpl() {
        write8(0x97);
    }

    public void dconst_0() {
        write8(0x0e);
    }

    public void dconst_1() {
        write8(0x0f);
    }

    public void ddiv() {
        write8(0x6f);
    }

    public void dload(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x26 + index);
        } else {
            write8(0x18);
            write8(index);
        }
    }

    public void dmul() {
        write8(0x6b);
    }

    public void dneg() {
        write8(0x77);
    }

    public void drem() {
        write8(0x73);
    }

    public void dreturn() {
        write8(0xaf);
    }

    public void dstore(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x39 + index);
        } else {
            write8(0x47);
            write8(index);
        }
    }

    public void dsub() {
        write8(0x67);
    }

    public void dup() {
        write8(0x59);
    }

    public void dup_x1() {
        write8(0x5a);
    }

    public void dup_x2() {
        write8(0x5b);
    }

    public void dup2() {
        write8(0x5c);
    }

    public void dup2_x1() {
        write8(0x5d);
    }

    public void dup2_x2() {
        write8(0x5e);
    }

    public void f2d() {
        write8(0x8d);
    }

    public void f2i() {
        write8(0x8b);
    }

    public void f2l() {
        write8(0x8c);
    }

    public void fadd() {
        write8(0x62);
    }

    public void faload() {
        write8(0x30);
    }

    public void fastore() {
        write8(0x51);
    }

    public void fcmpg() {
        write8(0x96);
    }

    public void fcmpl() {
        write8(0x95);
    }

    public void fconst_0() {
        write8(0x0b);
    }

    public void fconst_1() {
        write8(0x0c);
    }

    public void fconst_2() {
        write8(0x0d);
    }

    public void fdiv() {
        write8(0x6e);
    }

    public void fload(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x22 + index);
        } else {
            write8(0x17);
            write8(index);
        }
    }

    public void fmul() {
        write8(0x6a);
    }

    public void fneg() {
        write8(0x76);
    }

    public void frem() {
        write8(0x72);
    }

    public void freturn() {
        write8(0xae);
    }

    public void fstore(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x43 + index);
        } else {
            write8(0x38);
            write8(index);
        }
    }

    public void fsub() {
        write8(0x66);
    }

    public void getfield(int index) {
        write8(0xb4);
        write16(index);
    }

    public void getstatic(int index) {
        write8(0xb2);
        write16(index);
    }

    public void goto_(Label label) {
        final int start = code.position();
        write8(0xa7);
        write16(start, label);
    }

    /*public void goto_w(Label label) {
         write8(0xc8);
         write32(label);
     }*/

    public void i2b() {
        write8(0x91);
    }

    public void i2c() {
        write8(0x92);
    }

    public void i2d() {
        write8(0x87);
    }

    public void i2f() {
        write8(0x86);
    }

    public void i2l() {
        write8(0x85);
    }

    public void i2s() {
        write8(0x93);
    }

    public void iadd() {
        write8(0x60);
    }

    public void iaload() {
        write8(0x2e);
    }

    public void iand() {
        write8(0x7e);
    }

    public void iastore() {
        write8(0x4f);
    }

    public void iconst_m1() {
        write8(0x02);
    }

    public void iconst_0() {
        write8(0x03);
    }

    public void iconst_1() {
        write8(0x04);
    }

    public void iconst_2() {
        write8(0x05);
    }

    public void iconst_3() {
        write8(0x06);
    }

    public void iconst_4() {
        write8(0x07);
    }

    public void iconst_5() {
        write8(0x08);
    }

    public void idiv() {
        write8(0x6c);
    }

    public void if_acmpeq(Label label) {
        final int start = code.position();
        write8(0xa5);
        write16(start, label);
    }

    public void if_acmpne(Label label) {
        final int start = code.position();
        write8(0xa6);
        write16(start, label);
    }

    public void if_icmpeq(Label label) {
        final int start = code.position();
        write8(0x97);
        write16(start, label);
    }

    public void if_icmpne(Label label) {
        final int start = code.position();
        write8(0xa0);
        write16(start, label);
    }

    public void if_icmplt(Label label) {
        final int start = code.position();
        write8(0xa1);
        write16(start, label);
    }

    public void if_icmpge(Label label) {
        final int start = code.position();
        write8(0xa2);
        write16(start, label);
    }

    public void if_icmpgt(Label label) {
        final int start = code.position();
        write8(0xa3);
        write16(start, label);
    }

    public void if_icmple(Label label) {
        final int start = code.position();
        write8(0xa4);
        write16(start, label);
    }

    public void ifeq(Label label) {
        final int start = code.position();
        write8(0x99);
        write16(start, label);
    }

    public void ifne(Label label) {
        final int start = code.position();
        write8(0x9a);
        write16(start, label);
    }

    public void iflt(Label label) {
        final int start = code.position();
        write8(0x9b);
        write16(start, label);
    }

    public void ifge(Label label) {
        final int start = code.position();
        write8(0x9c);
        write16(start, label);
    }

    public void ifgt(Label label) {
        final int start = code.position();
        write8(0x9d);
        write16(start, label);
    }

    public void ifle(Label label) {
        final int start = code.position();
        write8(0x9e);
        write16(start, label);
    }

    public void ifnonnull(Label label) {
        final int start = code.position();
        write8(0xc7);
        write16(start, label);
    }

    public void ifnull(Label label) {
        final int start = code.position();
        write8(0xc6);
        write16(start, label);
    }

    public void iinc(int index, int constValue) {
        write8(0x84);
        write8(index);
        write8(constValue);
    }

    public void iload(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x1a + index);
        } else {
            write8(0x15);
            write8(index);
        }
    }

    public void imul() {
        write8(0x68);
    }

    public void ineg() {
        write8(0x74);
    }

    public void instanceof_(int index) {
        write8(0xc1);
        write16(index);
    }

    public void invokeinterface(int index, int count) {
        write8(0xb9);
        write16(index);
        write8(count);
        write8(0);
    }

    public void invokespecial(int index) {
        write8(0xb7);
        write16(index);
    }

    public void invokestatic(int index) {
        write8(0xb8);
        write16(index);
    }

    public void invokevirtual(int index) {
        write8(0xb6);
        write16(index);
    }

    public void ior() {
        write8(0x80);
    }

    public void irem() {
        write8(0x70);
    }

    public void ireturn() {
        write8(0xac);
    }

    public void ishl() {
        write8(0x78);
    }

    public void ishr() {
        write8(0x7a);
    }

    public void istore(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x3b + index);
        } else {
            write8(0x54);
            write8(index);
        }
    }

    public void isub() {
        write8(0x64);
    }

    public void iushr() {
        write8(0x7c);
    }

    public void ixor() {
        write8(0x82);
    }

    public void jsr(Label label) {
        final int start = code.position();
        write8(0xa8);
        write16(start, label);
    }

    /*public void jsr_w(Label label) {
         write8(0xc9);
         write32(label);
     }*/

    public void l2d() {
        write8(0x8a);
    }

    public void l2f() {
        write8(0x89);
    }

    public void l2i() {
        write8(0x88);
    }

    public void ladd() {
        write8(0x61);
    }

    public void laload() {
        write8(0x2f);
    }

    public void land() {
        write8(0x7f);
    }

    public void lastore() {
        write8(0x50);
    }

    public void lcmp() {
        write8(0x94);
    }

    public void lconst_0() {
        write8(0x09);
    }

    public void lconst_1() {
        write8(0x0a);
    }

    public void ldc(int index) {
        write8(0x12);
        write8(index);
    }

    public void ldc_w(int index) {
        write8(0x13);
        write16(index);
    }

    public void ldc2_w(int index) {
        write8(0x14);
        write16(index);
    }

    public void ldiv() {
        write8(0x6d);
    }

    public void lload(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x1e + index);
        } else {
            write8(0x16);
            write8(index);
        }
    }

    public void lmul() {
        write8(0x69);
    }

    public void lneg() {
        write8(0x75);
    }

    /*public void lookupswitch(int defaultBranch, int[] branches) {
         // Not implemented yet
     }*/

    public void lor() {
        write8(0x81);
    }

    public void lrem() {
        write8(0x71);
    }

    public void lreturn() {
        write8(0xad);
    }

    public void lshl() {
        write8(0x79);
    }

    public void lshr() {
        write8(0x7b);
    }

    public void lstore(int index) {
        if ((index >= 0) && (index <= 3)) {
            write8(0x3f + index);
        } else {
            write8(0x37);
            write8(index);
        }
    }

    public void lsub() {
        write8(0x65);
    }

    public void lxor() {
        write8(0x83);
    }

    public void monitorenter() {
        write8(0xc2);
    }

    public void monitorexit() {
        write8(0xc3);
    }

    public void multianewarray(int index, int dims) {
        write8(0xc5);
        write16(index);
        write8(dims);
    }

    public void new_(int index) {
        write8(0xbb);
        write16(index);
    }

    public void newarray(int atype) {
        write8(0xbc);
        write8(atype);
    }

    public void nop() {
        write8(0x00);
    }

    public void pop() {
        write8(0x57);
    }

    public void pop2() {
        write8(0x58);
    }

    public void putfield(int index) {
        write8(0xb5);
        write16(index);
    }

    public void putstatic(int index) {
        write8(0xb3);
        write16(index);
    }

    public void ret(int index) {
        write8(0xa9);
        write8(index);
    }

    public void return_() {
        write8(0xb1);
    }

    public void saload() {
        write8(0x53);
    }

    public void sastore() {
        write8(0x56);
    }

    public void sipush(int index) {
        write8(0x11);
        write16((short) index);
    }

    public void swap() {
        write8(0x5f);
    }

    /*public void tableswitch(int defaultBranch, int low, int high, int[] branches) {
         write8(0xaa);
     }*/

    private final void ensureSpace(int extra) {
        final int size = code.position() + extra;
        if (code.limit() < size) {
            code.limit(size);
        }
    }

    /**
     * Write an 8-bit int
     *
     * @param v
     */
    private final void write8(int v) {
        ensureSpace(1);
        code.put((byte) (v & 0xFF));
    }

    /**
     * Write an 16-bit int
     *
     * @param v
     */
    private final void write16(int v) {
        ensureSpace(2);
        code.put((byte) ((v >> 8) & 0xFF));
        code.put((byte) (v & 0xFF));
    }

    /**
     * Write an 16-bit branch
     *
     * @param insStart
     * @param label
     */
    private final void write16(int insStart, Label label) {
        if (label.isResolved()) {
            write16(label.getAddress() - insStart);
        } else {
            label.addUnresolvedLocation(code.position());
            write16(code.position() - insStart);
        }
    }

    /**
     * Write an 32-bit int
     * @param v
     */
    /*private final void write32(int v) {
         ensureSpace(4);
         set32(code, used, v);
         used += 4;
     }*/

    /**
     * Set an 8-bit int
     *
     * @param code
     * @param ofs
     * @param v
     */
    public static final void set8(ByteBuffer code, int ofs, int v) {
        code.put(ofs, (byte) (v & 0xFF));
    }

    /**
     * Set an 16-bit int
     *
     * @param code
     * @param ofs
     * @param v
     */
    public static final void set16(ByteBuffer code, int ofs, int v) {
        code.put(ofs++, (byte) ((v >> 8) & 0xFF));
        code.put(ofs++, (byte) (v & 0xFF));
    }

    /**
     * Set an 32-bit int
     *
     * @param code
     * @param ofs
     * @param v
     */
    public static final void set32(ByteBuffer code, int ofs, int v) {
        code.put(ofs++, (byte) ((v >> 24) & 0xFF));
        code.put(ofs++, (byte) ((v >> 16) & 0xFF));
        code.put(ofs++, (byte) ((v >> 8) & 0xFF));
        code.put(ofs++, (byte) (v & 0xFF));
    }

    /**
     * Get an 8-bit int
     *
     * @param code
     * @param ofs
     * @return int
     */
    public static final int get8(ByteBuffer code, int ofs) {
        return code.get(ofs) & 0xFF;
    }

    /**
     * Set an 16-bit int
     *
     * @param code
     * @param ofs
     * @return int
     */
    public static final int get16(ByteBuffer code, int ofs) {
        final int v1 = code.get(ofs++) & 0xFF;
        final int v2 = code.get(ofs) & 0xFF;
        return (v1 << 8) | v2;
    }

    /**
     * Get an 32-bit int
     *
     * @param code
     * @param ofs
     * @return int
     */
    public static final int get32(ByteBuffer code, int ofs) {
        final int v1 = code.get(ofs++) & 0xFF;
        final int v2 = code.get(ofs++) & 0xFF;
        final int v3 = code.get(ofs++) & 0xFF;
        final int v4 = code.get(ofs) & 0xFF;
        return (v1 << 24) | (v2 << 16) | (v3 << 8) | v4;
    }

    public final int getLength() {
        return code.position();
    }

    final ByteBuffer getCode() {
        return (ByteBuffer) code.duplicate().rewind();
    }

    class Label {
        private final String text;
        private List<Integer> unresolvedLocations;
        private int address = -1;

        Label(String text) {
            this.text = text;
        }

        public String toString() {
            return text;
        }

        public boolean isResolved() {
            return (address >= 0);
        }

        public int getAddress() {
            if (address < 0) {
                throw new RuntimeException("Not resolved yet");
            }
            return address;
        }

        void addUnresolvedLocation(int loc) {
            if (isResolved()) {
                throw new RuntimeException("Already resolved");
            }
            if (unresolvedLocations == null) {
                unresolvedLocations = new ArrayList<Integer>();
            }
            unresolvedLocations.add(loc);
        }

        public void resolve() {
            if (isResolved()) {
                throw new RuntimeException("Cannot resolve twice");
            }
            this.address = getLength();
            if (unresolvedLocations != null) {
                for (int loc : unresolvedLocations) {
                    final int distance = address - get16(getCode(), loc);
                    set16(getCode(), loc, distance);
                }
                unresolvedLocations = null;
            }
        }

    }
}
