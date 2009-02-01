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
 
package org.jnode.driver.system.acpi.aml;

/**
 * AmlOpcode.
 * <p/>
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class AmlOpcode {

    // private field names
    int opcode;
    String name;
    String argsFormat;


    public static AmlOpcode[] acpi_aml_ops = {
        new AmlOpcode(Aml.AML_ZEROOP, "ZeroOp", null),
        new AmlOpcode(Aml.AML_ONEOP, "OneOp", null),
        new AmlOpcode(Aml.AML_ALIAS, "Alias", "nN"),
        new AmlOpcode(Aml.AML_NAME, "Name", "No"),
        new AmlOpcode(Aml.AML_BYTECONST, "ByteConst", "b"),
        new AmlOpcode(Aml.AML_WORDCONST, "WordConst", "w"),
        new AmlOpcode(Aml.AML_DWORDCONST, "DwordConst", "d"),
        new AmlOpcode(Aml.AML_STRING, "String", "A"),
        new AmlOpcode(Aml.AML_SCOPE, "Scope", "pNT"),
        new AmlOpcode(Aml.AML_BUFFER, "Buffer", "ptB"),
        new AmlOpcode(Aml.AML_PACKAGE, "Package", "pbO"),
        new AmlOpcode(Aml.AML_METHOD, "Method", "pNMT"),
        new AmlOpcode(Aml.AML_LOCAL0, "Local0", null),
        new AmlOpcode(Aml.AML_LOCAL1, "Local1", null),
        new AmlOpcode(Aml.AML_LOCAL2, "Local2", null),
        new AmlOpcode(Aml.AML_LOCAL3, "Local3", null),
        new AmlOpcode(Aml.AML_LOCAL4, "Local4", null),
        new AmlOpcode(Aml.AML_LOCAL5, "Local5", null),
        new AmlOpcode(Aml.AML_LOCAL6, "Local6", null),
        new AmlOpcode(Aml.AML_LOCAL7, "Local7", null),
        new AmlOpcode(Aml.AML_ARG0, "Arg0", null),
        new AmlOpcode(Aml.AML_ARG1, "Arg1", null),
        new AmlOpcode(Aml.AML_ARG2, "Arg2", null),
        new AmlOpcode(Aml.AML_ARG3, "Arg3", null),
        new AmlOpcode(Aml.AML_ARG4, "Arg4", null),
        new AmlOpcode(Aml.AML_ARG5, "Arg5", null),
        new AmlOpcode(Aml.AML_ARG6, "Arg6", null),
        new AmlOpcode(Aml.AML_STORE, "Store", "ts"),
        new AmlOpcode(Aml.AML_REFOF, "RefOf", "s"),
        new AmlOpcode(Aml.AML_ADD, "Add", "ttl"),
        new AmlOpcode(Aml.AML_CONCAT, "Concat", "ttl"),
        new AmlOpcode(Aml.AML_SUBTRACT, "Subtract", "ttl"),
        new AmlOpcode(Aml.AML_INCREMENT, "Increment", "s"),
        new AmlOpcode(Aml.AML_DECREMENT, "Decrement", "s"),
        new AmlOpcode(Aml.AML_MULTIPLY, "Multiply", "ttl"),
        new AmlOpcode(Aml.AML_DIVIDE, "Divide", "ttll"),
        new AmlOpcode(Aml.AML_SHIFTLEFT, "ShiftLeft", "ttl"),
        new AmlOpcode(Aml.AML_SHIFTRIGHT, "ShiftRight", "ttl"),
        new AmlOpcode(Aml.AML_AND, "And", "ttl"),
        new AmlOpcode(Aml.AML_NAND, "NAnd", "ttl"),
        new AmlOpcode(Aml.AML_OR, "Or", "ttl"),
        new AmlOpcode(Aml.AML_NOR, "NOr", "ttl"),
        new AmlOpcode(Aml.AML_XOR, "XOr", "ttl"),
        new AmlOpcode(Aml.AML_NOT, "Not", "tl"),
        new AmlOpcode(Aml.AML_FINDSETLEFTBIT, "FindSetLeftBit", "tl"),
        new AmlOpcode(Aml.AML_FINDSETRIGHTBIT, "FindSetRightBit", "tl"),
        new AmlOpcode(Aml.AML_DEREFOF, "DerefOf", "t"),
        new AmlOpcode(Aml.AML_NOTIFY, "Notify", "st"),
        new AmlOpcode(Aml.AML_SIZEOF, "SizeOf", "s"),
        new AmlOpcode(Aml.AML_INDEX, "Index", "ttl"),
        new AmlOpcode(Aml.AML_MATCH, "Match", "tbtbtt"),
        new AmlOpcode(Aml.AML_CREATEDWORDFIELD, "CreateDWordField", "ttN"),
        new AmlOpcode(Aml.AML_CREATEWORDFIELD, "CreateWordField", "ttN"),
        new AmlOpcode(Aml.AML_CREATEBYTEFIELD, "CreateByteField", "ttN"),
        new AmlOpcode(Aml.AML_CREATEBITFIELD, "CreateBitField", "ttN"),
        new AmlOpcode(Aml.AML_OBJECTTYPE, "ObjectType", "s"),
        new AmlOpcode(Aml.AML_LAND, "LAnd", "tt"),
        new AmlOpcode(Aml.AML_LOR, "LOr", "tt"),
        new AmlOpcode(Aml.AML_LNOT, "LNot", "t"),
        new AmlOpcode(Aml.AML_LEQUAL, "LEqual", "tt"),
        new AmlOpcode(Aml.AML_LGREATER, "LGreater", "tt"),
        new AmlOpcode(Aml.AML_LLESS, "LLess", "tt"),
        new AmlOpcode(Aml.AML_IF, "If", "ptT"),
        new AmlOpcode(Aml.AML_ELSE, "Else", "pT"),
        new AmlOpcode(Aml.AML_WHILE, "While", "ptT"),
        new AmlOpcode(Aml.AML_NOOP, "Noop", null),
        new AmlOpcode(Aml.AML_RETURN, "Return", "t"),
        new AmlOpcode(Aml.AML_BREAK, "Break", null),
        new AmlOpcode(Aml.AML_BREAKPOINT, "BreakPoint", null),
        new AmlOpcode(Aml.AML_ONESOP, "OnesOp", null),
        new AmlOpcode(Aml.AML_MUTEX, "Mutex", "NS"),
        new AmlOpcode(Aml.AML_EVENT, "Event", "N"),
        new AmlOpcode(Aml.AML_CONDREFOF, "CondRefOf", "ss"),
        new AmlOpcode(Aml.AML_CREATEFIELD, "CreateField", "tttN"),
        new AmlOpcode(Aml.AML_LOAD, "Load", "ns"),
        new AmlOpcode(Aml.AML_STALL, "Stall", "t"),
        new AmlOpcode(Aml.AML_SLEEP, "Sleep", "t"),
        new AmlOpcode(Aml.AML_ACQUIRE, "Acquire", "sw"),
        new AmlOpcode(Aml.AML_SIGNAL, "Signal", "s"),
        new AmlOpcode(Aml.AML_WAIT, "Wait", "st"),
        new AmlOpcode(Aml.AML_RESET, "Reset", "s"),
        new AmlOpcode(Aml.AML_RELEASE, "Release", "s"),
        new AmlOpcode(Aml.AML_FROMBCD, "FromBCD", "tl"),
        new AmlOpcode(Aml.AML_TOBCD, "ToBCD", "tl"),
        new AmlOpcode(Aml.AML_UNLOAD, "Unload", "s"),
        new AmlOpcode(Aml.AML_REVISION, "Revision", null),
        new AmlOpcode(Aml.AML_DEBUG, "Debug", null),
        new AmlOpcode(Aml.AML_FATAL, "Fatal", "bdt"),
        new AmlOpcode(Aml.AML_OPREGION, "OpRegion", "NRtt"),
        new AmlOpcode(Aml.AML_FIELD, "Field", "pnLF"),
        new AmlOpcode(Aml.AML_DEVICE, "Device", "pNP"),
        new AmlOpcode(Aml.AML_PROCESSOR, "Processor", "pNbdbP"),
        new AmlOpcode(Aml.AML_POWERRES, "PowerRes", "pNbwP"),
        new AmlOpcode(Aml.AML_THERMALZONE, "ThermalZone", "pNP"),
        new AmlOpcode(Aml.AML_INDEXFIELD, "IndexField", "pnnbF"),
        new AmlOpcode(Aml.AML_BANKFIELD, "BankField", "pnntbF"),
        new AmlOpcode(Aml.AML_LNOTEQUAL, "LNotEqual", "tt"),
        new AmlOpcode(Aml.AML_LLESSEQUAL, "LLessEqual", "tt"),
        new AmlOpcode(Aml.AML_LGREATEREQUAL, "LGreaterEqual", "tt"),
        new AmlOpcode(0, null, null)

    };

    public static byte[] acpi_aml_op_index = {
/*00*/  0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00,
        0x03, 0x00, 0x04, 0x05, 0x06, 0x07, 0x00, 0x00,

/*10*/  0x08, 0x09, 0x0a, 0x00, 0x0b, 0x00, 0x00, 0x46,
        0x47, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

/*20*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x48, 0x49, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

/*30*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x4a, 0x4b,
        0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53,

/*40*/  0x54, 0x00, 0x00, 0x00, 0x00, 0x00, 0x55, 0x56,
        0x57, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

/*50*/  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

/*60*/  0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13,
        0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x00,

/*70*/  0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20, 0x21, 0x22,
        0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a,

        0x2b, 0x2c, 0x2d, 0x2e, 0x00, 0x00, 0x2f, 0x30,
        0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x00,

        0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x58, 0x59,
        0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x00, 0x00,

        0x3e, 0x3f, 0x40, 0x41, 0x42, 0x43, 0x60, 0x61,
        0x62, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x44, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,

        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x45,
    };

    public AmlOpcode(int opcode, String name, String argsFormat) {
        this.opcode = opcode;
        this.name = name;
        this.argsFormat = argsFormat;
    }

    public boolean acpi_is_named_op() {
        return Aml.isNamedOpcode(opcode);
    }

    /*
    * Find AML opcode description based on opcode
    */
    public static AmlOpcode getAmlOpcode(int opcode) {
        AmlOpcode op;
        int hash;

        // compute hash
        switch (opcode >> 8) {
            case 0:
                hash = opcode;
                break;
            case Aml.AML_EXTOP:
                hash = (opcode + Aml.AML_EXTOP_HASH_OFFSET) & 0xff;
                break;
            case Aml.AML_LNOT:
                hash = (opcode + Aml.AML_LNOT_HASH_OFFSET) & 0xff;
                break;
            default:
                hash = opcode & 0xff;
                break;
        }

        op = acpi_aml_ops[acpi_aml_op_index[hash]];
        return ((op.opcode == opcode) ? op : null);
    }

}
