/*
 * $Id$
 */
package org.jnode.shell.help.def;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Argument;

/**
 * @author qades
 */
public class DefaultHelp extends Help {

	/**
	 * Create a new instance
	 */
	public DefaultHelp() {
	}

	/**
	* Shows the complete help for a command.
	*/
	public void help(Info info) {
		final Syntax[] syntaxes = info.getSyntaxes();
		for (int i = 0; i < syntaxes.length; i++) {
			help(info.getName(), syntaxes[i]);
			if (i < syntaxes.length)
				System.out.println();
		}
	}

	/**
	* Shows the help for a command syntax.
	*/
	public void help(String name, Syntax syntax) {
		usage(name, syntax);

		final Parameter[] params = syntax.getParams();
		if (params.length != 0)
			System.out.println("\nParameters:");
		for (int i = 0; i < params.length; i++)
			params[i].describe(this);
	}

	/**
	* Shows the usage information of a command.
	*/
	public void usage(Info info) {
		final Syntax[] syntaxes = info.getSyntaxes();
		for (int i = 0; i < syntaxes.length; i++)
			usage(info.getName(), syntaxes[i]);
	}

	/**
	 * Shows the usage information of a command.
	 */
	public void usage(String name, Syntax syntax) {
		String line = name;
		final Parameter[] params = syntax.getParams();
		for (int i = 0; i < params.length; i++)
			line += " " + params[i].format();
		System.out.println("Usage: " + line);
		format(new Cell[] { new Cell(4, 54)}, new String[] { syntax.getDescription()});
	}

	public void describeParameter(Parameter param) {
		format(new Cell[] { new Cell(2, 18), new Cell(2, 53)}, new String[] { param.getName(), param.getDescription()});
	}

	public void describeArgument(Argument arg) {
		format(new Cell[] { new Cell(4, 16), new Cell(2, 53)}, new String[] { arg.getName(), arg.getDescription()});
	}

	protected void format(Cell[] cells, String[] texts) {
		if (cells.length != texts.length)
			throw new IllegalArgumentException("Number of cells and texts must match");

		StringBuffer result = new StringBuffer();
		String[] remains = new String[texts.length];
		for (int i = 0; i < texts.length; i++)
			remains[i] = texts[i];

		while (true) {
			String line = "";
			for (int i = 0; i < cells.length; i++) {
				String field = cells[i].fit(remains[i]);
				remains[i] = remains[i].substring(field.length());
				line += cells[i].stamp(field.trim());
			}
			if (line.trim().length() == 0)
				break;
			result.append(line + "\n");
		}
		System.out.print(result.toString());
	}

	protected class Cell {
		final String field;
		final int margin;
		final int width;
		Cell(int margin, int width) {
			this.margin = margin;
			this.width = width;

			// for performance, we pre-build the field mask
			String field = "";
			for (int i = 0; i < margin + width; i++)
				field = " " + field;
			this.field = field;
		}
		String fit(String text) {
			String hardFit = text.substring(0, Math.min(width, text.length()));
			int lastSpace = hardFit.lastIndexOf(" ");
			return hardFit.substring(0, (hardFit.endsWith(" ") && (lastSpace > width / 4) ? lastSpace : hardFit.length()));
		}
		String stamp(String text) {
			if (text.length() > field.length())
				throw new IllegalArgumentException("Text length exceeds field width");
			return field.substring(0, margin) + text + field.substring(0, width - text.length());
		}
	}

}
