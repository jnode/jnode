/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class Address {
    public String reg;
    public int disp;
    public String sreg;
    public int scale;

    public String getImg() {
        return reg;
    }
}
