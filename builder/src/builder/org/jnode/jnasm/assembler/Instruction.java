/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import java.util.List;

/**
 * @author Levente S\u00e1ntha
 */
public class Instruction {
    private int lineNumber;
    private String label;
    private String mnemonic;
    private List operands;

    public Instruction(String label) {
        this(label, null, null);
    }

    public Instruction(String mnemonic, List operands) {
        this(null, mnemonic, operands);
    }

    public Instruction(String label, String mnemonic, List operands) {
        this.label = label;
        this.mnemonic = mnemonic;
        this.operands = operands;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public List getOperands() {
        return operands;
    }

    public String getLabel(){
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
