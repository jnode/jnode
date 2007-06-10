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
 * Provides printing related access to the BinaryConstantPool.
 * Prints constant and provides other utilities.
 *
 */

package sun.tools.javap.oldjavap;

import java.io.PrintWriter;

import sun.tools.java.BinaryConstantPool;
import sun.tools.java.ClassDeclaration;
import sun.tools.java.ClassDefinition;
import sun.tools.java.Constants;

/**
 * This class is used to print constants in the constant pool
 */
public final
class ConstantPrinter implements Constants {

    BinaryConstantPool cpool;
    private JavaPEnvironment env;
    private PrintWriter output;
    private boolean decodeTypeSignatures;


    /**
     * Constructor
     */
    public
    ConstantPrinter(BinaryConstantPool cpool, JavaPEnvironment env, PrintWriter output, boolean decodeTypeSignatures) {
	this.cpool = cpool;
	this.env = env;
	this.output = output;
	this.decodeTypeSignatures = decodeTypeSignatures;
    }

    /**
     * get a string
     */
    public String getString(int n) {
	return cpool.getString(n);
    }

    /**
     * Print a constant name and its type
     */
    private void printNameAndType(int key, String separator, boolean decodeType) {
	int fieldnameindex = key >> 16;
	int fieldtypeindex = key & 0xFFFF;
	output.print(cpool.getString(fieldnameindex) + separator);
    	if (decodeType) 
	    output.print(cpool.getType(fieldtypeindex));
    	else 
	    output.print(cpool.getString(fieldtypeindex));
    }

    /**
     * Print a constant name and its type from an index
     */
    private void printNameAndTypeFromIndex(int index, String separator) {
	int key = cpool.getInteger(index); 
	printNameAndType(key, separator, decodeTypeSignatures);
    }
    
    /**
     * Print a reference
     */
    private void printRef(int index, String separator) {
    	if (decodeTypeSignatures) {
            output.print(cpool.getConstant(index, env));
    	} else {
            int key = cpool.getInteger(index);
	    int classkey = key >> 16; 
	    int nametypekey = key & 0xFFFF;
	    output.print(cpool.getDeclaration(env, classkey).getName());
	    output.print(".");
	    printNameAndTypeFromIndex(nametypekey, separator);
	}
    }
    	    
    /**
     * Print a constant in the constant pool
     */
    public void printClassDeclaration(ClassDeclaration cdcl) {
    	ClassDefinition cdef = cdcl.getClassDefinition();
    	if (cdef != null && cdef.isInterface())
	    output.print("<Interface ");
	else
	    output.print("<Class ");
    	output.print(cdcl.getName());
	output.print(">");
    }
    	    
    /**
     * Print a constant in the constant pool
     */
    public void printConstant(int index) {
    	int typeId = cpool.getConstantType(index);
        switch(typeId) {
        case CONSTANT_STRING: 
	    output.print("<String \"" + cpool.getConstant(index, env) + "\"");
	    break;
	   
        case CONSTANT_UTF8:
            output.print("<\"" + cpool.getString(index) + "\"");
	    break;
	
        case CONSTANT_INTEGER:
	    output.print("<Integer " + cpool.getInteger(index));
	    break;

        case CONSTANT_FLOAT:
	    output.print("<Real " + cpool.getValue(index));
	    break;

        case CONSTANT_LONG: 
	    output.print("<Long " + cpool.getValue(index));
	    break;

        case CONSTANT_DOUBLE:
	    output.print("<Double " + cpool.getValue(index));
	    break;

        case CONSTANT_CLASS:  
	    printClassDeclaration(cpool.getDeclaration(env, index));
	    return;

	case CONSTANT_METHOD: 
	    output.print("<Method ");
	    printRef(index, "");
	    break;
	
        case CONSTANT_FIELD:  
	    output.print("<Field ");
	    printRef(index, " ");
	    break;
	    
	case CONSTANT_INTERFACEMETHOD: 
	    output.print("<InterfaceMethod ");
	    printRef(index, "");
	    break;
	    	    
        case CONSTANT_NAMEANDTYPE:
	    output.print("<NameAndType");
	    printNameAndTypeFromIndex(index, " ");
	    break;
	    
	default:
	    output.print("<Unknown " + typeId);
	    break;
	
        }
        output.print('>');
    }

}
