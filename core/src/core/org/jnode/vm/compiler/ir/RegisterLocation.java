/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class RegisterLocation extends Location {
	private Object register;
	
	/**
	 * @param name
	 */
	public RegisterLocation(Object register) {
		super(register.toString());
		this.register = register;
	}
	
	public Object getRegister() {
		return register;
	}
}
