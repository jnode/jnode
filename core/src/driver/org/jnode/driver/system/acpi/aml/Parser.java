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

package org.jnode.driver.system.acpi.aml;

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

/**
 * Parser.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class Parser {

    private static final Logger log = Logger.getLogger(Parser.class);

    private class ReferencedInteger {

        int value;

        public ReferencedInteger() {
        }

        public ReferencedInteger(int value) {
            this.value = value;
        }

        public int increment() {
            value++;
            return value;
        }

        public int decrement() {
            value--;
            return value;
        }

        public void set(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }

    private class Scope {

        ParseNode originNode; // current op being parsed
        ByteBuffer buffer;
        String argsFormat;
        int next_arg; // next argument to parse in argsFormat String
        int arg_count = 0; // # fixed arguments
        int arg_end; // current argument end
        int pkg_end; // current package end

        Scope parent = null; // parent scope

        public Scope() {
        }

        public Scope(acpi_parse_state state, ParseNode scopeRoot, String argsFormat, int next_arg, int arg_count) {
            this.originNode = scopeRoot;
            this.buffer = state.buffer.duplicate();
            this.arg_count = arg_count;
            this.argsFormat = argsFormat;
            this.next_arg = next_arg;
            this.arg_end = (arg_count == Aml.ACPI_VAR_ARGS) ? state.pkg_end : Aml.ACPI_MAX_AML;
            this.pkg_end = state.pkg_end;
        }

        public ParseNode getOp() {
            return originNode;
        }
    }

    private class acpi_parse_state {
        ByteBuffer buffer;
        Scope scope; // current scope
        Scope scope_avail; // unused (extra) scope structs
        int pkg_end; // current package end

        public acpi_parse_state(ByteBuffer buf) {
            this.buffer = buf;
            this.pkg_end = buf.limit();
        }

        public void acpi_cleanup_scope() {
            // destroy available list
            while (scope_avail != null) {
                Scope scopeTmp = scope_avail;
                scope_avail = scopeTmp.parent;
                scopeTmp = null;
            }

            // destroy scope stack
            while (scope != null) {
                //Scope scopeTmp = scope;
                scope = scope.parent;
                //scopeTmp = null;
            }

        }

        public ParseNode acpi_pop_scope(Scope newScope) {
            Scope prevScope = scope;
            ParseNode node = null;
            if (prevScope.parent != null) {
                // return to parsing previous op
                node = scope.getOp();
                newScope.argsFormat = scope.argsFormat;
                newScope.next_arg = scope.next_arg;
                pkg_end = prevScope.pkg_end;
                this.scope = prevScope.parent;

                // add scope to available list
                prevScope.parent = this.scope_avail;
                this.scope_avail = prevScope;
            } else {
                // empty parse stack, prepare to fetch next opcode
                newScope.next_arg = 0;
            }
            return node;
        }

        public void acpi_push_scope(ParseNode pushedNode, String argsFormat, int next_arg, int arg_count) {
            Scope newScope = this.scope_avail;
            if (newScope != null) {
                // grabbed scope from available list
                scope_avail = newScope.parent;
            } else {
                // allocate scope from the heap
                newScope = new Scope();
            }
            newScope.originNode = pushedNode;
            newScope.buffer = buffer.duplicate();
            newScope.argsFormat = argsFormat;
            newScope.next_arg = next_arg;
            newScope.arg_count = arg_count;
            newScope.arg_end = (arg_count == Aml.ACPI_VAR_ARGS) ? pkg_end : Aml.ACPI_MAX_AML;
            newScope.pkg_end = pkg_end;
            newScope.parent = this.scope;
            this.scope = newScope;

        }

        public void acpi_next_namepath(ParseNode arg, ReferencedInteger arg_count, boolean method_calls) {
            NameString path = acpi_next_namestring();
            ParseNode method = null;
            if (method_calls) {
                method = acpi_get_parent_scope().find(path, Aml.AML_METHOD, false);
            }

            if (method != null) {
                // method call
                ParseNode count = method.getArg(0);
                if (count != null && count.opcode == Aml.AML_BYTECONST) {
                    ParseNode name = new ParseNode(Aml.AML_NAMEPATH);
                    if (name != null) {
                        arg.opcode = Aml.AML_METHODCALL;
                        name.opcode = Aml.AML_NAMEPATH;
                        name.setName(path);
                        arg.appendArg(name);
                        arg_count.set(((Integer) count.value).intValue() & Aml.ACPI_METHOD_ARG_MASK);
                    }
                }
            } else {
                // variable/name access
                arg.opcode = Aml.AML_NAMEPATH;
                arg.setName(path);
            }

        }

        public boolean acpi_has_completed_scope() {
            return buffer.position() >= scope.arg_end || scope.arg_count == 0;
        }

        public ParseNode acpi_get_parent_scope() {
            return scope.getOp();
        }

        public byte peekByte() {
            return buffer.get(buffer.position());
        }

        public int peekByteInt() {
            return (buffer.get(buffer.position()) & 0xff);
        }

        public byte getByte() {
            return buffer.get();
        }

        public int getByteInt() {
            return (buffer.get()) & 0xff;
        }

        public int getWord() {
            // should use getShort but not implemented in JNode...
            int value = peekWord();
            move(2);
            return value;
        }

        public int getDWord() {
            int value = peekDWord();
            move(4);
            return value;
        }

        public String getNameseg() {
            StringBuilder buffer = new StringBuilder(4);
            for (int i = 0; i < 4; i++)
                buffer.append((char) (getByteInt()));
            return buffer.toString();
        }

        public int peekWord() {
            int tmp;
            tmp = (buffer.get(buffer.position() + 1) & 0xff) << 8;
            tmp |= buffer.get(buffer.position()) & 0xff;
            return tmp;
        }

        public int peekDWord() {
            int tmp;
            tmp = (buffer.get(buffer.position() + 3) & 0xff) << 24;
            tmp |= (buffer.get(buffer.position() + 2) & 0xff) << 16;
            tmp |= (buffer.get(buffer.position() + 1) & 0xff) << 8;
            tmp |= buffer.get(buffer.position()) & 0xff;
            return tmp;
        }

        public int acpi_next_pkg_end() {
            int start = buffer.position();
            int byte1, byte2, byte3, byte4, len = 0;

            byte1 = getByteInt();

            // bits 6-7 contain encoding scheme
            switch (byte1 >> 6) {
                case 0: // 1-byte encoding (bits 0-5)
                    len = (byte1 & 0x3f);
                    break;
                case 1: // 2-byte encoding (next byte + bits 0-3)
                    byte2 = getByteInt();
                    len = (byte2 << 4) | (byte1 & 0xf);
                    break;
                case 2: // 3-byte encoding (next 2 bytes + bits 0-3)
                    byte2 = getByteInt();
                    byte3 = getByteInt();
                    len = (byte3 << 12) | (byte2 << 4) | (byte1 & 0xf);

                    break;
                case 3: // 4-byte encoding (next 3 bytes + bits 0-3)
                    byte2 = getByteInt();
                    byte3 = getByteInt();
                    byte4 = getByteInt();
                    len = (byte4 << 20) | (byte3 << 12) | (byte2 << 4) | (byte1 & 0xf);
                    break;
            }

            return (start + len); // end of package
        }

        ParseNode acpi_next_field() {

            ParseNode field;
            int opcode;

            // determine field type
            switch (this.peekByte()) {
                default:
                    // assert(acpi_is_lead(readb(state->aml)));
                    opcode = Aml.AML_NAMEDFIELD;
                    break;
                case 0x00:
                    opcode = Aml.AML_RESERVEDFIELD;
                    getByte();
                    break;
                case 0x01:
                    opcode = Aml.AML_ACCESSFIELD;
                    getByte();
                    break;
            }

            field = new ParseNode(opcode);
            if (field != null) {
                int start = buffer.position();

                switch (opcode) {
                    case Aml.AML_NAMEDFIELD: {
                        field.setName(getNameseg());
                        field.value = new Integer(acpi_next_pkg_end() - start);
                        break;
                    }
                    case Aml.AML_RESERVEDFIELD:
                        field.value = new Integer(acpi_next_pkg_end() - start);
                        break;

                    case Aml.AML_ACCESSFIELD:
                        field.value = new Integer(getWord());
                        break;
                }
            }

            return field;
        }

        public int acpi_peek_opcode() {
            int opcode = peekByteInt();
            int nextOpcode = 0;
            if (buffer.position() < buffer.limit() - 1) {
                int wordOpcode = peekWord();
                nextOpcode = wordOpcode >> 8;
            }
            if (opcode == Aml.AML_EXTOP || (opcode == Aml.AML_LNOT &&
                (nextOpcode == Aml.AML_LEQUAL || nextOpcode == Aml.AML_LGREATER || nextOpcode == Aml.AML_LLESS))) {
                // extended opcode, !=, <=, or >=
                opcode = opcode << 8 | nextOpcode;
            }

            // don't convert bare name to a namepath

            return opcode;
        }

        public ParseNode acpi_next_arg(char argType, ReferencedInteger arg_count) {

            ParseNode arg = null;

            switch (argType) {
                case Aml.AML_BYTEDATA_ARG:
                case Aml.AML_WORDDATA_ARG:
                case Aml.AML_DWORDDATA_ARG:
                case Aml.AML_ASCIICHARLIST_ARG:
                case Aml.AML_NAMESTRING_ARG:
                case Aml.AML_NAME_ARG:
                case Aml.AML_REGIONSPACE_ARG:
                case Aml.AML_FIELDFLAGS_ARG:
                case Aml.AML_METHODFLAGS_ARG:
                case Aml.AML_SYNCFLAGS_ARG:
                    // constants, strings, and namestrings are all the same size
                    arg = new ParseNode(Aml.AML_BYTECONST);
                    acpi_next_simple(arg, argType);
                    break;

                case Aml.AML_PKGLENGTH_ARG:
                    // package length, nothing returned
                    pkg_end = acpi_next_pkg_end();
                    break;

                case Aml.AML_FIELDLIST_ARG:
                    if (buffer.position() < pkg_end) {
                        // non-empty list
                        ParseNode prev = null;
                        while (buffer.position() < pkg_end) {
                            ParseNode field = acpi_next_field();
                            if (field == null) {
                                break;
                            }
                            if (prev != null) {
                                prev.next = field;
                            } else {
                                arg = field;
                            }
                            prev = field;
                        }

                        // skip to end of byte data
                        buffer.position(pkg_end);
                    }
                    break;

                case Aml.AML_BYTELIST_ARG:
                    if (buffer.position() < pkg_end) {
                        // non-empty list
                        arg = new ParseNode(Aml.AML_BYTELIST);
                        if (arg != null) {
                            // fill in bytelist data
                            int oldlimit = buffer.limit();
                            buffer.limit(pkg_end);
                            arg.data = buffer.slice();
                            buffer.limit(oldlimit);
                        }
                        // skip to end of byte data
                        buffer.position(pkg_end);
                    }
                    break;

                case Aml.AML_TARGET_ARG:
                case Aml.AML_SUPERNAME_ARG: {
                    int subop = acpi_peek_opcode();
                    if (subop == 0 || Aml.isLeadChar((byte) subop) || Aml.isPrefixChar((byte) subop)) {
                        // NullName or NameString
                        arg = new ParseNode(Aml.AML_NAMEPATH);
                        acpi_next_namepath(arg, arg_count, false);
                    } else {
                        // single complex argument, nothing returned
                        arg_count.set(1);
                    }
                    break;
                }


                case Aml.AML_DATAOBJECT_ARG:
                case Aml.AML_TERMARG_ARG:
                    // single complex argument, nothing returned
                    arg_count.set(1);
                    break;

                case Aml.AML_DATAOBJECTLIST_ARG:
                case Aml.AML_TERMLIST_ARG:
                case Aml.AML_OBJECTLIST_ARG:
                    if (buffer.position() < pkg_end) {
                        // non-empty list of variable args, nothing returned
                        arg_count.set(Aml.ACPI_VAR_ARGS);
                    }
                    break;
                default:
                    // Missing a case above after modifying amlop.txt and mkaml
                    // THROW EXCEPTION *((int *)0) = 0;
            }

            return arg;
        }

        /*
           * Get next namestring
           */
        NameString acpi_next_namestring() {

            NameString ns = new NameString();

            StringBuffer prefix = new StringBuffer();
            if (Aml.isPrefixChar(peekByte())) {
                while (Aml.isPrefixChar(peekByte())) {
                    prefix.append((char) getByteInt());
                }
                ns.setPrefix(prefix.toString());
            }

            switch (peekByte()) {
                case 0:

                    // NullName
                    if (prefix.length() == 0) {
                        ns = null;
                    }
                    move(1);
                    break;
                case Aml.AML_DUALNAMEPATH:
                    // two name segments
                    move(1); // skips the dual name path indicator
                    ns.addDualNamePath(this.buffer); // buffer is advanced accordingly
                    break;
                case Aml.AML_MULTINAMEPATH: {
                    // multiple name segments
                    move(1);
                    ns.addMultiNamePath(this.buffer); // buffer is advanced accordingly
                    break;
                }

                default:

                    // single name segment
                    // assert(acpi_is_lead(readb(end)));
                    ns.addNamePath(buffer);
                    break;
            }

            return ns;
        }

        /*
           * Get next simple argument (constant, string, or namestring)
           */
        ParseNode acpi_next_simple(ParseNode arg, char arg_type) {

            switch (arg_type) {

                case Aml.AML_BYTEDATA_ARG:
                    arg.opcode = Aml.AML_BYTECONST;
                    arg.value = new Integer(getByteInt());
                    break;

                case Aml.AML_WORDDATA_ARG:
                    arg.opcode = Aml.AML_WORDCONST;
                    arg.value = new Integer(getWord());
                    break;

                case Aml.AML_DWORDDATA_ARG:
                    arg.opcode = Aml.AML_DWORDCONST;
                    arg.value = new Integer(getDWord());
                    break;

                case Aml.AML_ASCIICHARLIST_ARG:
                    arg.opcode = Aml.AML_STRING;
                    String tmp = new String();
                    char c = 0;
                    while ((c = (char) getByte()) != '\0') {
                        tmp += c;
                    }
                    arg.value = tmp;
                    break;

                case Aml.AML_NAMESTRING_ARG:
                case Aml.AML_NAME_ARG:
                    arg.opcode = Aml.AML_NAMEPATH;
                    arg.setName(acpi_next_namestring());
                    break;

                case Aml.AML_FIELDFLAGS_ARG:
                    arg.opcode = Aml.AML_FIELDFLAGS;
                    arg.value = new Integer(getByteInt());
                    break;

                case Aml.AML_REGIONSPACE_ARG:
                    arg.opcode = Aml.AML_REGIONSPACE;
                    arg.value = new Integer(getByteInt());
                    break;

                case Aml.AML_METHODFLAGS_ARG:
                    arg.opcode = Aml.AML_METHODFLAGS;
                    arg.value = new Integer(getByteInt());
                    break;

                case Aml.AML_SYNCFLAGS_ARG:
                    arg.opcode = Aml.AML_SYNCFLAGS;
                    arg.value = new Integer(getByteInt());
                    break;
            }

            return arg;
        }

        public void move(int bytes) {
            buffer.position(buffer.position() + bytes);
        }

    } // END OF PRIVATE INNER CLASS STATE

    public Parser() {
    }

    public void acpi_parse_aml(ParseNode root, ByteBuffer buffer) {
        acpi_parse_state state = null; // parser state
        ParseNode currentNode = null; // current op
        String argsFormat = null; // current op next argument
        int argsIndex = 0;
        ReferencedInteger arg_count = new ReferencedInteger(0);

        // initialize parser state
        state = new acpi_parse_state(buffer);

        // adds the root scope
        state.scope = new Scope(state, root, null, 0, Aml.ACPI_VAR_ARGS);

        while (state.buffer.position() < state.buffer.limit()) {

            if (currentNode == null) {

                int opcode = state.acpi_peek_opcode();
                AmlOpcode opc = AmlOpcode.getAmlOpcode(opcode);
                if (opc != null) {
                    // normal opcode
                    state.move(((opcode & 0xff00) > 0) ? 2 : 1);
                    argsFormat = opc.argsFormat;
                    argsIndex = 0;
                } else if (Aml.isPrefixChar((byte) opcode) || Aml.isLeadChar((byte) opcode)) {
                    // convert bare name string to namepath
                    opcode = Aml.AML_NAMEPATH;
                    opc = AmlOpcode.getAmlOpcode(opcode);
                    argsFormat = "n";
                    argsIndex = 0;
                } else {
                    // skip unknown opcodes
                    state.move(((opcode & 0xff00) > 0) ? 2 : 1);
                    continue;
                }

                // create and append to parent's argument list
                if (Aml.isNamedOpcode(opcode)) {
                    ParseNode preop = null;
                    while (argsIndex < argsFormat.length() && argsFormat.charAt(argsIndex) != Aml.AML_NAME_ARG) {
                        ParseNode arg = state.acpi_next_arg(argsFormat.charAt(argsIndex), arg_count);
                        if (preop == null)
                            preop = arg;
                        else
                            preop.appendArg(arg);
                        argsIndex++;
                    }
                    currentNode = (state.acpi_get_parent_scope()).find(state.acpi_next_namestring(), opcode, true);
                    argsIndex++;
                    if (currentNode != null)
                        currentNode.appendArg(preop);
                    else
                        log.debug("Trying to add to an empty node");
                } else {
                    currentNode = new ParseNode(opcode);
                    state.acpi_get_parent_scope().appendArg(currentNode);
                }

                if (currentNode == null) {
                    break;
                }

            } // if currentNode ==null

            arg_count.set(0);
            if (argsFormat != null) {
                // get arguments
                switch (currentNode.opcode) {
                    case Aml.AML_BYTECONST: // AML_BYTEDATA_ARG
                    case Aml.AML_WORDCONST: // AML_WORDDATA_ARG
                    case Aml.AML_DWORDCONST: // AML_DWORDATA_ARG
                    case Aml.AML_STRING: // AML_ASCIICHARLIST_ARG

                        // fill in constant or string argument directly
                        state.acpi_next_simple(currentNode, argsFormat.charAt(argsIndex));
                        break;

                    case Aml.AML_NAMEPATH:
                        state.acpi_next_namepath(currentNode, arg_count, true);
                        argsFormat = null;
                        break;

                    default:

                        // op is not a constant or string
                        // append each argument
                        while (argsIndex < argsFormat.length() && arg_count.get() == 00) {
                            ParseNode arg = state.acpi_next_arg(argsFormat.charAt(argsIndex), arg_count);
                            currentNode.appendArg(arg);
                            argsIndex++;
                        }
                        if (currentNode.opcode == Aml.AML_METHOD) {
                            // skip parsing of method body
                            currentNode.data = state.buffer.duplicate();
                            currentNode.data.limit(state.pkg_end);
                            currentNode.data = currentNode.data.slice();
                            currentNode.data.rewind();
                            try {
                                state.buffer.position(state.pkg_end);
                            } catch (Exception ex) {
                                System.err.println(
                                    state.buffer.position() + "," + state.buffer.limit() + ":" + state.pkg_end);
                            }
                            arg_count.set(0);
                        }
                        break;
                }
            }

            if (arg_count.get() == 0) {
                // completed op, prepare for next
                state.scope.arg_count--;
                if (state.acpi_has_completed_scope()) {
                    Scope newScope = new Scope();
                    currentNode = state.acpi_pop_scope(newScope);
                    argsFormat = newScope.argsFormat;
                    argsIndex = newScope.next_arg;
                } else {
                    currentNode = null;
                }
            } else {
                // complex argument, push op and prepare for argument
                state.acpi_push_scope(currentNode, argsFormat, argsIndex, arg_count.get());

                currentNode = null;
            }
        }

        // complete any remaining ops
        state.acpi_cleanup_scope();

    }

    public ParseNode parse(ByteBuffer buffer) {

        ParseNode root = new ParseNode(Aml.AML_SCOPE);

        if (root == null) {
            return root;
        }

        //ParseNode op;

        // parse excluding method bodies
        acpi_parse_aml(root, buffer);

        return root;
    }

}
