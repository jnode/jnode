/*
 * Copyright 1997-2003 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * An extension of BinaryCode that allows code to be printed.
 * Includes printing of disassembled byte codes, exception info,
 * local variable and line number info.
 *
 */

package sun.tools.javap.oldjavap;

import sun.tools.java.BinaryAttribute;
import sun.tools.java.BinaryCode;
import sun.tools.java.BinaryConstantPool;
import sun.tools.java.BinaryExceptionHandler;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.Constants;
import sun.tools.java.Identifier;
import sun.tools.java.MemberDefinition;
import sun.tools.java.MethodType;

import sun.tools.util.*;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;

class JavaPBinaryCode extends BinaryCode implements Constants {

    private JavaPEnvironment env;
    private PrintWriter output;
    private byte codeBytes[];
    private BinaryConstantPool cpool;
    private ConstantPrinter constPrint;
    private MemberDefinition fieldDef;	
    private String methodName;
    
    private int offset;

    /**
     * Constructor
     */  
    JavaPBinaryCode(byte data[], 
		BinaryConstantPool cpool, 
		JavaPEnvironment env,
		MemberDefinition fieldDef) {
	super(data, cpool, env);
	this.output = env.output;
	this.codeBytes = getCode();
	this.cpool = cpool;
	this.constPrint = new ConstantPrinter(cpool, env, output, !env.showBackwardCompatible);
	this.fieldDef = fieldDef;
	this.methodName = fieldDef.toString();
    }

    /**
     * Return the field definition for this code
     */  
    MemberDefinition fieldDefinition() {
    	return fieldDef;
    }

    /**
     * Return the byte stored at a given index from the offset
     * within code bytes
     */  
    private final int at(int index) {
    	return  codeBytes[offset+index] & 0xFF;
    }

    /**
     * Return the short stored at a given index from the offset
     * within code bytes
     */  
    private final int shortAt(int index) {
        int base = offset + index;
    	return ((codeBytes[base] & 0xFF) << 8) 
    	    | (codeBytes[base+1] & 0xFF);
    }

    /**
     * Given the table at the specified index, return the specified entry
     */  
    private final long intAt(int tbl, int entry) {
        int base = tbl + (entry << 2);
    	return (codeBytes[base] << 24) 
    	    | ((codeBytes[base+1] & 0xFF) << 16)
    	    | ((codeBytes[base+2] & 0xFF) << 8)
    	    | ((codeBytes[base+3] & 0xFF));
    }

    /**
     * Print an integer so that it takes 'length' characters in
     * the output.  Temporary until formatting code is stable.
     */  
    private void printFixedWidthInt(long x, int length) {
    	CharArrayWriter baStream = new CharArrayWriter();
    	PrintWriter pStream = new PrintWriter(baStream);
    	pStream.print(x);
    	String str = baStream.toString();
    	for (int cnt = length - str.length(); cnt > 0; --cnt)
    	    output.print(' ');
    	output.print(str);
    }
    
    /**
     * Print the code as Java assembly language instructions
     */  
    void printCodeSequence() throws IOException {
	output.println();
	output.println("Method " + methodName);
	for (offset = 0; offset < codeBytes.length; ) {
	    int opcode = at(0);
	    printFixedWidthInt(offset, 4); 
	    output.print(" ");
	    if (opcode == opc_wide) {
		opcode = at(1);
		output.print(opcNames[opcode] + "_w ");
		int arg = shortAt(2);
		switch (opcode) {
		    case opc_aload: case opc_astore:
		    case opc_fload: case opc_fstore:
		    case opc_iload: case opc_istore:
		    case opc_lload: case opc_lstore:
		    case opc_dload: case opc_dstore:
		    case opc_ret:
			output.print(arg);
			offset += 4;
			break;

		    case opc_iinc:
			output.print(arg + " " + (short) shortAt(4));
			offset += 6;
			break;
		    default:
			output.print("Invalid opcode");
			offset++;
			break;
		}
	    } else {
		output.print(opcNames[opcode]);
		switch (opcode) {
		    case opc_aload: case opc_astore:
		    case opc_fload: case opc_fstore:
		    case opc_iload: case opc_istore:
		    case opc_lload: case opc_lstore:
		    case opc_dload: case opc_dstore:
		    case opc_ret:
			output.print(" " + at(1));
			offset += 2;
			break;
		    
		    case opc_iinc:
			output.print(" " + at(1)  + " " + (byte) at(2));
			offset += 3;
			break;

		    case opc_tableswitch:{
			int tbl = (offset+1+3) & (~3);	// four byte boundry
			long default_skip = intAt(tbl, 0);
			long low = intAt(tbl, 1);
			long high = intAt(tbl, 2);
			output.print(" " + low + " to " + high + ": default=");
			output.print(offset + default_skip);
			tbl += 3 << 2; 			// three int header
			for (long i = low; i <= high; ++i) {
			    output.println("");
			    output.print('\t');
			    printFixedWidthInt(i, 5);
			    output.print(": " + (offset + intAt(tbl, (int)(i-low))));
			}
			offset = tbl + (int)((high - low + 1) << 2);
			break;
		    }

		    case opc_lookupswitch:{
			int tbl = (offset+1+3) & (~3);	// four byte boundry
			long default_skip = intAt(tbl, 0);
			int npairs = (int)intAt(tbl, 1);
			int nints = npairs * 2;
			output.print(" " + npairs);
			output.print(": default=" + (offset + default_skip));
			tbl += 2 << 2; 			// two int header
			for (int i = 0; i< nints; i += 2) {
			    output.println("");
			    output.print('\t');
			    printFixedWidthInt(intAt(tbl, i), 5);
			    output.print(": " + (offset + intAt(tbl, i+1)));
			}
			offset = tbl + (nints << 2);
			break;
		    }

		    case opc_newarray:
			switch (at(1)) {
			    case T_INT:    output.print(" int");    break;
			    case T_LONG:   output.print(" long");   break;
			    case T_FLOAT:  output.print(" float");  break;
			    case T_DOUBLE: output.print(" double"); break;
			    case T_CHAR:   output.print(" char");   break;
			    case T_SHORT:  output.print(" short");  break;
			    case T_BYTE:   output.print(" byte");   break;
			    case T_BOOLEAN:output.print(" boolean");   break;
			    default:       output.print(" BOGUS TYPE"); break;
			}
			offset += 2;
			break;

		    case opc_anewarray: {
			int index =  shortAt(1);
			output.print(" class #" + index + " ");
			constPrint.printConstant(index);
			offset += 3;
			break;
		    }
		      
		    case opc_sipush:
			output.print(" " + (short) shortAt(1));
			offset += 3;
			break;

		    case opc_bipush:
			output.print(" " + (byte) at(1));
			offset += 2;
			break;

		    case opc_ldc: {
			int index = at(1);
			output.print(" #" + index + " ");
			constPrint.printConstant(index);
			offset += 2;
			break;
		    }

		    case opc_ldc_w: case opc_ldc2_w:
		    case opc_instanceof: case opc_checkcast:
		    case opc_new:
		    case opc_putstatic: case opc_getstatic:
		    case opc_putfield: case opc_getfield:
		    case opc_invokevirtual:
		    case opc_invokespecial:
		    case opc_invokestatic: {
			int index = shortAt(1);
			output.print(" #" + index + " ");
			constPrint.printConstant(index);
			offset += 3;
			break;
		    }

		    case opc_invokeinterface: {
			int index = shortAt(1);
			output.print(" (args " + at(3) + ") #" + index + " ");
			constPrint.printConstant(index);
			offset += 5;
			break;
		    }

		    case opc_multianewarray: {
			int index = shortAt(1);
			output.print(" #" + index + " dim #" + at(3) + " ");
			constPrint.printConstant(index);
			offset += 4;
			break;
		    }

		    case opc_jsr: case opc_goto:
		    case opc_ifeq: case opc_ifge: case opc_ifgt:
		    case opc_ifle: case opc_iflt: case opc_ifne:
		    case opc_if_icmpeq: case opc_if_icmpne: case opc_if_icmpge:
		    case opc_if_icmpgt: case opc_if_icmple: case opc_if_icmplt:
		    case opc_if_acmpeq: case opc_if_acmpne:
		    case opc_ifnull: case opc_ifnonnull: {
			int target = offset + (short) shortAt(1);
			output.print(" " + target);
			offset += 3;
			break;
		    }

		    case opc_jsr_w:
		    case opc_goto_w: {
			long jumpPoint = offset + intAt(offset+1, 0);
			output.print(" " + jumpPoint);
			offset += 5;
			break;
		    }
			
		    default:
			offset++;
			break;
		}
	    }
            output.println();
	}
    }

    /**
     * Print the exception table for this method code
     */  
    void printExceptionTable() throws IOException {
	BinaryExceptionHandler handlers[] = getExceptionHandlers();
	if (handlers.length > 0) { 
	    output.print("Exception table:\n   from   to  target type\n");
	    for (int idx = 0; idx < handlers.length; ++idx) {
		BinaryExceptionHandler handler = handlers[idx];
		printFixedWidthInt(handler.startPC, 6);
		printFixedWidthInt(handler.endPC, 6);
		printFixedWidthInt(handler.handlerPC, 6);
		output.print("   ");
		ClassDeclaration exceptionClass = handler.exceptionClass;
		if (exceptionClass == null) 
		    output.println("any");
		else {
		    constPrint.printClassDeclaration(exceptionClass);
		    output.println("");
		}
	    }
	}
    }

    /**
     * Find a code attribute by name
     */  
    private DataInputStream findAttribute(Identifier name) {
	BinaryAttribute attr = getAttributes();
	while (attr != null) {
	    if (attr.getName().equals(name)) {
		ByteArrayInputStream inStream = new ByteArrayInputStream(attr.getData());
		return new DataInputStream(inStream);
	    }
	    attr = attr.getNextAttribute();
	}
	return null;
    }

    /**
     * Print the line number table for this method code
     */  
    void printLineNumberTable() throws IOException {
	DataInputStream dataStream = findAttribute(idLineNumberTable);
	int tableLength;
	if (dataStream != null && ((tableLength = dataStream.readShort()) > 0)) {
	    output.println("");
	    output.println("Line numbers for method " + methodName);
	    for (int tcnt = tableLength; tcnt > 0; --tcnt) {
		int startPC = dataStream.readShort();
		int lineNumber = dataStream.readShort();
		output.println("   line " + lineNumber + ": " + startPC);
	    }
	}
    }

    /**
     * Print the local variable table for this method code
     */  
    void printLocalVariableTable() throws IOException {
// Fix this in java/Constants.java
//	Identifier idLocalVariableTable = Identifier.lookup("LocalVariableTable");
	DataInputStream dataStream = findAttribute(idLocalVariableTable);
	int tableLength;
	if (dataStream != null && ((tableLength = dataStream.readShort()) > 0)) {
	    output.println("");
	    output.println("Local variables for method " + methodName);
	    for (int tcnt = tableLength; tcnt > 0; --tcnt) {
		int startPC = dataStream.readShort();
		int length = dataStream.readShort();
		int nameIndex = dataStream.readShort();
		int descriptorIndex = dataStream.readShort();
		int index = dataStream.readShort();
		output.print("   ");
            	output.print(cpool.getType(descriptorIndex));
		output.print(" ");
            	output.print(cpool.getString(nameIndex));
		output.println("  pc=" + startPC + ", length=" + length + ", slot=" + index);
	    }
	}
    }

    /**
     * Print stack, argument and locals counts for this method 
     */  
    void printVerboseHeader() {
	MethodType mtype = (MethodType)fieldDef.getType();
	int argCount = mtype.getArgumentTypes().length;
	if (!fieldDef.isStatic())  
	    ++argCount;  // for 'this'
	
	output.println("\t/* Stack=" + getMaxStack() 
		+ ", Locals=" + getMaxLocals() 
		+ ", Args_size=" + argCount
		+ " */");
    }
}
