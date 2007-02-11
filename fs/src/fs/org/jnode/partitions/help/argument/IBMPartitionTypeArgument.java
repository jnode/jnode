package org.jnode.partitions.help.argument;

import java.util.Arrays;
import java.util.Collection;

import org.jnode.partitions.ibm.IBMPartitionTypes;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.ListArgument;


public class IBMPartitionTypeArgument extends ListArgument<IBMPartitionTypes>
{
	public IBMPartitionTypeArgument(String name, String description) 
	{
		super(name, description, false);
	}
	
	public IBMPartitionTypes getArgValue(String value) 
	{
		int fs = Integer.parseInt(value, 16);
		return IBMPartitionTypes.valueOf(fs);
	}

	@Override
	protected String toStringArgument(IBMPartitionTypes arg) {
		return Integer.toHexString(arg.getCode());
	}
	
	@Override
	protected Collection<IBMPartitionTypes> getValues() {
		return Arrays.asList(IBMPartitionTypes.values());
	}
	
	@Override
	public int compare(IBMPartitionTypes choice1, IBMPartitionTypes choice2) {
		return choice1.getCode() - choice2.getCode();
	}

	@Override
	protected boolean isPartOfArgument(IBMPartitionTypes argument, String part) {
		return toStringArgument(argument).startsWith(part);
	}
}
