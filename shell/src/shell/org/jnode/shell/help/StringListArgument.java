package org.jnode.shell.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringListArgument extends Argument {

	private final List<String> choices;

	public StringListArgument(String name, String description, boolean multi, List<String> choices) {
		super(name, description, multi);
		this.choices = choices;
	}

	public StringListArgument(String name, String description, List<String> choices) {
		this(name, description, false, choices);
	}

	public StringListArgument(String name, String description, boolean multi, String[] choices) {
		this(name, description, multi, Arrays.asList(choices));
	}

	public StringListArgument(String name, String description, String[] choices) {
		this(name, description, false, choices);
	}

	public String complete(String partial) {
		final List<String> result = new ArrayList<String>();
		for (String choice : choices) {
			if (choice.startsWith(partial)) {
				result.add(choice);
			}
		}

		Collections.sort(result);
		return complete(partial, result);
	}

	protected boolean isValidValue(String choice) {
		if ((choice == null) || "".equals(choice))
			return true;

		return choices.contains(choice);
	}

}
