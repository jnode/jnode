/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import java.io.OutputStream;
import java.io.IOException;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public abstract class HardwareSupport {
    public abstract void assemble();
    public abstract void writeTo(OutputStream out) throws IOException;
    public abstract void setPass(int pass);
    public abstract boolean isRegister(String str);
}
