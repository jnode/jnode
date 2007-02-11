/*
 * $Id: LongArgument.java 2945 2006-12-20 08:51:17Z qades $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.help.argument;

import org.jnode.shell.help.ParsedArguments;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 * TODO should be factorized with IntegerArgument
 */
public class SizeArgument extends LongArgument {
	public SizeArgument(String name, String description, boolean multi,
			String min, String max) 
	{
		this(name, description, multi, NumberUtils.getSize(min), 
			NumberUtils.getSize(max));
	}
	
	public SizeArgument(String name, String description, boolean multi,
			long min, long max) 
	{
		super(name, description, multi, min, max);
		checkSize("min", min);
		checkSize("max", max);
	}

	public SizeArgument(String name, String description, boolean multi) {
		this(name, description, multi, 0L, Long.MAX_VALUE);
	}

	public SizeArgument(String name, String description) {
		this(name, description, SINGLE);
	}
	
	public boolean hasSizeUnit(ParsedArguments args) {
		return (NumberUtils.getSizeUnit(this.getValue(args)) != null);
	}
	
	@Override
	protected long getLongValue(String value) {
		return NumberUtils.getSize(value);
	}
	
	protected void checkSize(String name, long size)
	{
		if(size < 0)
		{
			throw new IllegalArgumentException(name+" can't be negative");
		}		
	}
}
