/*
 * Created on Feb 20, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.vm.compiler;

import java.io.PrintStream;

import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class CompiledExceptionHandler {
	
	private NativeStream.ObjectRef startPc;
	private NativeStream.ObjectRef endPc;
	private NativeStream.ObjectRef handler;
	
	/**
	 * @return NativeStream.ObjectRef
	 */
	public NativeStream.ObjectRef getEndPc() {
		return endPc;
	}

	/**
	 * @return NativeStream.ObjectRef
	 */
	public NativeStream.ObjectRef getHandler() {
		return handler;
	}

	/**
	 * @return NativeStream.ObjectRef
	 */
	public NativeStream.ObjectRef getStartPc() {
		return startPc;
	}

	/**
	 * Sets the endPc.
	 * @param endPc The endPcO to set
	 */
	public void setEndPc(NativeStream.ObjectRef endPc) {
		this.endPc = endPc;
	}

	/**
	 * Sets the handler.
	 * @param handler The handler to set
	 */
	public void setHandler(NativeStream.ObjectRef handler) {
		this.handler = handler;
	}

	/**
	 * Sets the startPc.
	 * @param startPc The startPc to set
	 */
	public void setStartPc(NativeStream.ObjectRef startPc) {
		this.startPc = startPc;
	}

	public void writeTo(PrintStream out, int startOffset) 
	throws UnresolvedObjectRefException {
		out.println("start:   0x" + NumberUtils.hex(startPc.getOffset() - startOffset));
		out.println("end:     0x" + NumberUtils.hex(endPc.getOffset() - startOffset));
		out.println("handler: 0x" + NumberUtils.hex(handler.getOffset() - startOffset));
	}
}
