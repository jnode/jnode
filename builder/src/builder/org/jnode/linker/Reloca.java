/*
 * $Id$
 */
package org.jnode.linker;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Reloca extends Reloc {

	private final long r_addend;

	/**
	 * @param elf
	 * @param in
	 * @throws IOException
	 */
	public Reloca(Elf elf, InputStream in) throws IOException {
		super(elf, in);
		this.r_addend = LoadUtil.loadXword(in, elf.e_ident);
	}

	public final boolean hasAddEnd() {
		return true;
	}
	
	public final long getAddEnd() {
		return r_addend;
	}
}
