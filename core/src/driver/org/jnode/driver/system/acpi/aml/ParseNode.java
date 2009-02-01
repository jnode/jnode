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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * ParseNode.
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

public class ParseNode {

    private static final Logger log = Logger.getLogger(ParseNode.class);
    protected int opcode;

    protected NameString name = null;

    protected List<ParseNode> args = new ArrayList<ParseNode>();

    protected Object value;

    protected ParseNode parent;
    protected ParseNode next;

    ByteBuffer data = null;

    public ParseNode() {
    }

    public ParseNode(int opcode) {
        this.opcode = opcode;
    }

    public ParseNode getNext() {
        return next;
    }

    public int getType() {
        return opcode;
    }

    public NameString getName() {
        return name;
    }

    public boolean isScope() {
        return opcode == Aml.AML_SCOPE;
    }

    public boolean isMethod() {
        return opcode == Aml.AML_METHOD;
    }

    public boolean isName() {
        return opcode == Aml.AML_NAME;
    }

    public String getNameToString() {
        if (name != null)
            return name.toString();
        return null;
    }

    /**
     * use append arg for the other classes
     *
     * @param arg
     *
    private void addArg(ParseNode arg) {
    args.add(arg);
    }*/

    /**
     * Gets the number of arguments
     *
     * @return int
     */
    public int argCount() {
        return args.size();
    }

    public boolean isNamedOpcode() {
        return Aml.isNamedOpcode(opcode);
    }

    /*
      * Append an argument to an op's argument list (a NULL arg is OK)
      */

    /**
     * arg mayt be the head of a linked list args, not just one arg.
     *
     * @param arg
     */
    public void appendArg(ParseNode arg) {
        if (arg == null) {
            return;
        }
        switch (opcode) {
            case Aml.AML_ZEROOP:
            case Aml.AML_ONEOP:
            case Aml.AML_ONESOP:
            case Aml.AML_BYTECONST:
            case Aml.AML_WORDCONST:
            case Aml.AML_DWORDCONST:
            case Aml.AML_STRING:
            case Aml.AML_STATICSTRING:

            case Aml.AML_NAMEPATH:
            case Aml.AML_NAMEDFIELD:
            case Aml.AML_RESERVEDFIELD:
            case Aml.AML_ACCESSFIELD:
            case Aml.AML_BYTELIST:

            case Aml.AML_LOCAL0:
            case Aml.AML_LOCAL1:
            case Aml.AML_LOCAL2:
            case Aml.AML_LOCAL3:
            case Aml.AML_LOCAL4:
            case Aml.AML_LOCAL5:
            case Aml.AML_LOCAL6:
            case Aml.AML_LOCAL7:

            case Aml.AML_ARG0:
            case Aml.AML_ARG1:
            case Aml.AML_ARG2:
            case Aml.AML_ARG3:
            case Aml.AML_ARG4:
            case Aml.AML_ARG5:
            case Aml.AML_ARG6:

            case Aml.AML_NOOP:
            case Aml.AML_BREAK:
            case Aml.AML_BREAKPOINT:
            case Aml.AML_EVENT:
            case Aml.AML_REVISION:
            case Aml.AML_DEBUG:
                // no arguments for these ops

                break;

            default:
                if (args.size() >= 1) {
                    final ParseNode tmp = args.get(args.size() - 1);
                    tmp.next = arg;
                    args.add(arg);
                } else {
                    // first argument
                    args.add(arg);
                }
                while (arg != null) {
                    arg.parent = this;
                    arg = arg.next;
                }
                break;
        }
    }

    /*
      * Get op's children or NULL if none
      */
    public ParseNode geChild() {
        ParseNode child = null;
        switch (opcode) {
            case Aml.AML_SCOPE:
            case Aml.AML_ELSE:
            case Aml.AML_DEVICE:
            case Aml.AML_THERMALZONE:
            case Aml.AML_METHODCALL:
                child = getArg(0);
                break;

            case Aml.AML_BUFFER:
            case Aml.AML_PACKAGE:
            case Aml.AML_METHOD:
            case Aml.AML_IF:
            case Aml.AML_WHILE:
            case Aml.AML_FIELD:
                child = getArg(1);
                break;

            case Aml.AML_POWERRES:
            case Aml.AML_INDEXFIELD:
                child = getArg(2);
                break;

            case Aml.AML_PROCESSOR:
            case Aml.AML_BANKFIELD:
                child = getArg(3);
                break;
        }
        return child;
    }

    /*
      * Get op's parent
      */
    public ParseNode getParent() {
        ParseNode parent = this;
        while (parent != null) {
            switch (parent.opcode) {
                case Aml.AML_SCOPE:
                case Aml.AML_PACKAGE:
                case Aml.AML_METHOD:
                case Aml.AML_DEVICE:
                case Aml.AML_POWERRES:
                case Aml.AML_THERMALZONE:
                    return parent.parent;
            }
            parent = parent.parent;
        }
        return parent;
    }

    /*
      * Get next op in tree (walking the tree in depth-first order) Return NULL when reaching
      * "origin" or when walking up from root
      */
    public ParseNode getDepthNext(ParseNode current) {
        ParseNode nextTarget = null;
        // look for an argument or child
        if (current != null) {
            // look for a sibling
            nextTarget = current.getArg(0);
            if (nextTarget == null) {
                // look for a sibling of parent
                nextTarget = current.next;
                if (nextTarget == null) {
                    ParseNode parentTarget = current.parent;
                    while (parentTarget != null) {
                        ParseNode arg = parentTarget.getArg(0);
                        while (arg != null && !arg.equals(this) && !arg.equals(current)) {
                            arg = arg.next;
                        }

                        if (arg.equals(this)) {
                            // reached parent of origin
                            // end search
                            return null;
                        }
                        if (parentTarget.next != null) {
                            // found sibling of parent
                            return parentTarget.next;
                        }

                        current = parentTarget;
                        parentTarget = parentTarget.parent;
                    }
                }
            }
        }
        return nextTarget;
    }

    /*
      * Get specified op's argument or NULL if none
      */
    public ParseNode getArg(int argn) {
        ParseNode arg = null;
        switch (opcode) {
            case Aml.AML_ZEROOP:
            case Aml.AML_ONEOP:
            case Aml.AML_ONESOP:
            case Aml.AML_BYTECONST:
            case Aml.AML_WORDCONST:
            case Aml.AML_DWORDCONST:
            case Aml.AML_STRING:
            case Aml.AML_STATICSTRING:
            case Aml.AML_FIELDFLAGS:
            case Aml.AML_REGIONSPACE:
            case Aml.AML_METHODFLAGS:
            case Aml.AML_SYNCFLAGS:

            case Aml.AML_NAMEPATH:
            case Aml.AML_NAMEDFIELD:
            case Aml.AML_RESERVEDFIELD:
            case Aml.AML_ACCESSFIELD:
            case Aml.AML_BYTELIST:

            case Aml.AML_LOCAL0:
            case Aml.AML_LOCAL1:
            case Aml.AML_LOCAL2:
            case Aml.AML_LOCAL3:
            case Aml.AML_LOCAL4:
            case Aml.AML_LOCAL5:
            case Aml.AML_LOCAL6:
            case Aml.AML_LOCAL7:

            case Aml.AML_ARG0:
            case Aml.AML_ARG1:
            case Aml.AML_ARG2:
            case Aml.AML_ARG3:
            case Aml.AML_ARG4:
            case Aml.AML_ARG5:
            case Aml.AML_ARG6:

            case Aml.AML_NOOP:
            case Aml.AML_BREAK:
            case Aml.AML_BREAKPOINT:
            case Aml.AML_EVENT:
            case Aml.AML_REVISION:
            case Aml.AML_DEBUG:

                // no arguments for these ops
                break;

            default:
                if (args != null) {
                    if (argn < args.size()) {
                        try {
                            arg = (ParseNode) args.get(argn);
                        } catch (Exception ex) {
                            ParseNode tmp = this;
                            while (tmp.parent != null)
                                tmp = tmp.parent;
                            log.debug(tmp, ex);
                        }
                    }
                }
                break;
        }
        return arg;
    }

    /*
      * Is opcode for a Field, IndexField, or BankField
      */
    boolean isFieldOpcode() {
        return Aml.isFieldOpcode(opcode);
    }

    public void setName(String name) {
        NameString ns = new NameString();
        ns.setNamePath(name);
        this.name = ns;
    }

    public void setName(NameString name) {
        this.name = name;
    }

    public ParseNode findName(NameString name, int opcode) {
        ParseNode child = this.geChild();
        while (child != null) {
            if (child.isFieldOpcode()) {
                // field, search named fields
                ParseNode field = child.geChild();
                while (field != null) {
                    if (field.isNamedOpcode() && field.getName().equals(name) &&
                        (opcode != 0 || field.opcode == opcode)) {
                        return field;
                    }
                    field = field.next;
                }
            } else if (child.isNamedOpcode() && child.getName().equals(name) &&
                (opcode != 0 || child.opcode == opcode)) {
                break;
            }
            child = child.next;
        }
        return child;
    }

    public Object findNameValue(String name) {
        Object result = null;
        NameString ns = new NameString();
        ns.setNamePath(name);
        ParseNode node = findName(ns, Aml.AML_NAME);
        if (node != null) {
            result = (node.args.get(0)).value;
        }
        return result;
    }

    public ParseNode find(NameString name, int opcode, boolean create) {
        boolean unprefixed = false;
        int seg_count;
        int name_op;
        String prefix = null;

        ParseNode searchedNode = this;

        ParseNode originNode = this;

        if (name == null)
            return null;

        // get prefix and starting scope
        prefix = name.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            for (int pos = 0; pos < prefix.length() && Aml.isPrefixChar((byte) prefix.charAt(pos)); pos++) {
                switch (prefix.charAt(pos)) {
                    case '\\':
                        while (originNode.parent != null) {
                            originNode = originNode.parent;
                        }
                        break;
                    case '^':
                        if (originNode.getParent() != null) {
                            originNode = originNode.getParent();
                        }
                        break;
                }
            }
        } else
            unprefixed = true;

        // at this stage originNode contains the appropriate search origin

        // get name segment count
        seg_count = name.getSegCount();

        // match each name segment
        int index = 0;
        while (originNode != null && seg_count != 0) {

            String nameseg = name.getNameseg(index);
            seg_count--;
            index++;
            name_op = seg_count != 0 ? 0 : opcode;

            searchedNode = originNode.findName(name, name_op);

            if (searchedNode == null) {
                if (create) {
                    // create new scope level
                    searchedNode = new ParseNode((seg_count != 0 ? Aml.AML_SCOPE : opcode));
                    if (searchedNode != null) {
                        searchedNode.setName(nameseg);
                        originNode.appendArg(searchedNode);
                    }
                } else if (unprefixed) {
                    // search scopes for unprefixed name
                    while (searchedNode != null && searchedNode.parent != null) {
                        originNode = originNode.parent;
                        searchedNode = originNode.findName(name, opcode);
                    }
                }
            }
            unprefixed = false;
            originNode = searchedNode;
        } // for

        return searchedNode;
    }

    public String toString() {
        return toString("");
    }

    public String toString(String prefix) {
        StringBuffer result = new StringBuffer();
        String opcodeName = null;
        AmlOpcode opc = AmlOpcode.getAmlOpcode(opcode);

        if (opc == null) {
            switch (opcode) {
                case Aml.AML_BYTELIST:
                    opcodeName = "Bytelist";
                    break;
                case Aml.AML_NAMEPATH:
                    opcodeName = "Namepath";
                    break;
                case Aml.AML_SYNCFLAGS:
                    opcodeName = "SyncLevel";
                    break;
                case Aml.AML_METHODFLAGS:
                    opcodeName = "ArgCount";
                    break;
                default:
                    opcodeName = "Opcode-" + opcode;
                    break;
            }
        } else
            opcodeName = opc.name;
        result.append(prefix + opcodeName);

        String nameString = "";
        if (this.name == null) {
            if (opcode == Aml.AML_SCOPE) {
                nameString = "\\";
            }
        } else
            nameString = name.toString();
        result.append(" " + nameString);

        if (value != null) {
            result.append(" = ");
            if (value instanceof Integer)
                result.append(Integer.toHexString(((Integer) value).intValue()));
            else
                result.append(value);
        }
        result.append('\n');

        for (int i = 0; i < args.size(); i++) {
            if (getArg(i) != null)
                result.append(getArg(i).toString(prefix + " | "));
        }
        return result.toString();
    }

    public String toString3(String prefix) {
        StringBuffer result = new StringBuffer();
        //String opcodeName = null;
        AmlOpcode opc = AmlOpcode.getAmlOpcode(opcode);

        if (opc != null) {
            switch (opcode) {
                case Aml.AML_NAME:
                    if (this.parent.opcode == Aml.AML_PACKAGE) {
                        return prefix + "currentPackage.add(\"" + value + "\");\n";
                    } else
                        return prefix + "currentName=Name(\"" + name.toString() + "\");\n";
                case Aml.AML_PACKAGE:
                    result.setLength(0);
                    result.append(prefix + "currentPackage=Package(" + this.args.get(0).value + ");\n");
                    result.append(prefix + "currentName.add(currentPackage);\n");
                    return result.toString();
                case Aml.AML_BUFFER:
                    result.setLength(0);
                    result.append(prefix + "currentBuffer=Buffer(" + this.args.get(0).value + ");\n");
                    result.append(prefix + "currentName.add(currentBuffer);\n");
                    return result.toString();
                case Aml.AML_STRING:
                    result.setLength(0);
                    result.append(prefix + "currentPackage.add(\"" + value + "\");\n");
                    return result.toString();
            }
        }
        return "@";
    }

}
