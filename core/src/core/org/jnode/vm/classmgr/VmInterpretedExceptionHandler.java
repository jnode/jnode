/**
 * $Id$
 */
package org.jnode.vm.classmgr;


/**
 * <description>
 * 
 * @author epr
 */
public final class VmInterpretedExceptionHandler extends AbstractExceptionHandler {
	
	private final int startPC;
	private final int endPC;
	private final int handlerPC;
	
	/**
	 * Create a new instance
	 * @param cp
	 * @param startPC
	 * @param endPC
	 * @param handlerPC
	 * @param classIndex
	 */
	public VmInterpretedExceptionHandler(VmCP cp, int startPC, int endPC, int handlerPC, int classIndex) {
		this(cp.getConstClass(classIndex), startPC, endPC, handlerPC); 
	}

	/**
	 * Create a new instance
	 * @param catchType
	 * @param startPC
	 * @param endPC
	 * @param handlerPC
	 */
	public VmInterpretedExceptionHandler(VmConstClass catchType, int startPC, int endPC, int handlerPC) {
		super(catchType); 
		this.startPC = startPC;
		this.endPC = endPC;
		this.handlerPC = handlerPC;
	}

	/**
	 * Returns the endPC.
	 * @return int
	 */
	public int getEndPC() {
		return endPC;
	}

	/**
	 * Returns the handlerPC.
	 * @return int
	 */
	public int getHandlerPC() {
		return handlerPC;
	}

	/**
	 * Returns the startPC.
	 * @return int
	 */
	public int getStartPC() {
		return startPC;
	}
	
	/**
	 * Is the given PC between start and end.
	 * @param pc
	 * @return True if the given pc is between start (inclusive) and end (inclusive), false otherwise
	 */
	public boolean isInScope(int pc) {
		return (pc >= startPC) && (pc <= endPC);
	}
}
