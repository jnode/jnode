/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import java.util.List;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class Instruction {
    public static final int LOCK_PREFIX = 1;
    public static final int REP_PREFIX = 2;
    private int prefix;
    private int lineNumber;
    private String sizeInfo;
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

    public void setOperands(List operands) {
        this.operands = operands;
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

    public String getSizeInfo() {
        return sizeInfo;
    }

    public void setSizeInfo(String sizeInfo) {
        this.sizeInfo = sizeInfo;
    }

    public int getPrefix() {
        return prefix;
    }

    public void addPrefix(int prefix) {
        if(prefix == LOCK_PREFIX || prefix == REP_PREFIX){
            this.prefix |= prefix;
        } else {
            throw new RuntimeException("Invalid prefix: " + prefix);
        }
    }

    public void setPrefix(int prefix) {
        this.prefix = prefix;
    }
}
