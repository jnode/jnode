/*
 * $Id$
 */
package org.jnode.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class represents the command line as an iterator.
 * A trailing space leads to an empty token to be appended.
 * @author epr
 * @author qades
 */
public class CommandLine {

	public static final int LITERAL = 0;
	public static final int STRING = 1;
	public static final int CLOSED = 2;

	public static final char ESCAPE_CHAR = '\\';
	public static final char FULL_ESCAPE_CHAR = '\'';
	public static final char QUOTE_CHAR = '"';
	public static final char SPACE_CHAR = ' ';
	public static final char COMMENT_CHAR = '#';

	private final String s;
	private int pos = 0;
	private int type = LITERAL;

	//private boolean inEscape = false;
	private boolean inFullEscape = false;
	private boolean inQuote = false;

	/**
	* Creates a new instance.
	*/
	public CommandLine(String s) {
		this.s = s;
	}

	/**
	 * Create a new instance.
	 * @param args
	 */
	public CommandLine(String[] args) {
		this(escape(args));
	}

	/**
	* Reset, so a call to nextToken will retreive the first token.
	*/
	public void reset() {
		pos = 0;
	}

	/**
	 * Returns if there is another token on the command list.
	 * @return <code>true</code> if there is another token; <code>false</code> otherwise
	 */
	public boolean hasNext() {
		return pos < s.length();
	}

	/**
	 * Go to the next token and return it.
	 * @return the next token
	 */
	public Token nextToken() throws NoSuchElementException {
		int start = pos;
		String token = next();
		int end = pos;
		return new Token(token, getTokenType(), start, end);
	}

	/**
	 * Go to the next token string and return it.
	 * @return the next token
	 */
	public String next() throws NoSuchElementException {
		if (!hasNext())
			throw new NoSuchElementException();

		type = LITERAL;
		String t = "";
		boolean finished = false;
		while (!finished && pos < s.length()) {
			char c = s.charAt(pos++);
			switch (c) {
				case ESCAPE_CHAR :
					t += CommandLine.unescape(s.charAt(pos++));
					break;
				case FULL_ESCAPE_CHAR :
					if (inQuote) {
						t += c;
					} else {
						inFullEscape = !inFullEscape; // just a toggle
						type = STRING;
						if (!inFullEscape)
							type |= CLOSED;
					}
					break;
				case QUOTE_CHAR :
					if (inFullEscape) {
						t += c;
					} else {
						inQuote = !inQuote;
						type = STRING;
						if (!inQuote)
							type |= CLOSED;
					}
					break;
				case SPACE_CHAR :
					if (inFullEscape || inQuote) {
						t += c;
					} else {
						if (t.length() != 0) { // don't return an empty token
							finished = true;
							pos--; // to return trailing space as empty last token
						}
					}
					break;
				case COMMENT_CHAR :
					if (inFullEscape || inQuote) {
						t += c;
					} else {
						finished = true;
						pos = s.length(); // ignore EVERYTHING
					}
					break;
				default :
					t += c;
			}
		}
		return t;
	}

	/**
	 * Gets the type of this token.
	 * @return the type, <code>LITERAL</code> if it cannot be determined
	 */
	public int getTokenType() {
		return type;
	}

	/**
	 * Get the remaining CommandLine.
	 * @return the remainder
	 */
	public CommandLine getRemainder() {
		return new CommandLine(s.substring(pos));
	}

	/**
	 * Get the entire command line as String[].
	 * @return the command line as String[]
	 */
	public String[] toStringArray() {
		List res = new ArrayList();
		CommandLine line = new CommandLine(s);

		while (line.hasNext()) {
			res.add(line.next());
		}

		String[] result = (String[]) res.toArray(new String[res.size()]);
		return result;
	}

	/** Returns the entire command line as a string.
	 *  @return the entire command line
	 **/
	public String toString() {
		return escape(toStringArray()); // perform all possible conversions
	}

	/**
	 * Gets the next token without stepping through the list.
	 * @return the next token, or an empty string if there are no tokens left
	 */
	public String peek() {
		if (!hasNext()) {
			return "";
		}
		return getRemainder().next();
	}

	/**
	 * Gets the remaining number of parts
	 * @return the remaining number of parts
	 */
	public int getLength() {
		if (!hasNext())
			return 0;

		CommandLine remainder = getRemainder();
		int result = 0;
		while (remainder.hasNext()) {
			result++;
			remainder.next();
		}
		return result;
	}

	public static class Token {
		public final String token;
		public final int tokenType;
		public final int start;
		public final int end;
		Token(String token, int type, int start, int end) {
			this.token = token;
			this.tokenType = type;
			this.start = start;
			this.end = end;
		}
	}

	// escape and unescape methods

	private static final Escape[] escapes = {
		//	plain	escaped
		new Escape('\\', '\\'), new Escape('\b', 'b'), new Escape('\n', 'n'), new Escape('\r', 'r'), new Escape('\t', 't'), new Escape('\'', '\'')};

	/**
	* Escape a single command line argument for the Shell.
	* Same as calling escape(arg, <code>false</code>)
	* @param arg the unescaped argument
	* @return the escaped argument
	*/
	public static String escape(String arg) {
		return escape(arg, false); // don't force quotation
	}

	/**
	* Escape a single command line argument for the Shell.
	* @param arg the unescaped argument
	* @param forceQuote if <code>true</code>, forces the argument to be returned in quotes even if not necessary
	* @return the escaped argument
	*/
	public static String escape(String arg, boolean forceQuote) {
		String s = "";

		// one-character escapes
		for (int i = 0; i < arg.length(); i++) {
			char c = arg.charAt(i);
			for (int j = 0; j < escapes.length; j++)
				if (escapes[j].plain == c) {
					s += ESCAPE_CHAR;
					c = escapes[j].escaped;
					break;
				}
			s += c;
		}

		if (s.indexOf(QUOTE_CHAR) != -1) { // full escape needed
			s = FULL_ESCAPE_CHAR + s + FULL_ESCAPE_CHAR;
		} else if (forceQuote || (s.indexOf(SPACE_CHAR) != -1)) { // normal quote if needed or forced
			s = QUOTE_CHAR + s + QUOTE_CHAR;
		}
		// debug output do show how escapes are translated
		//System.out.println();
		//System.out.println("escaped \"" + arg + "\" as \"" + s + "\"");
		return s;
	}

	/**
	* Escape a command line for the Shell.
	* @param args the unescaped command line
	* @return the escaped argument
	*/
	public static String escape(String[] args) {
		String s = "";
		for (int i = 0; i < args.length; i++) {
			s += escape(args[i]); // escape the argument
			if (i != args.length - 1)
				s += " "; // white space if needed
		}
		return s;
	}

	/**
	* Unescape a single character
	*/
	private static char unescape(char arg) {
		// one-character escapes
		for (int i = 0; i < escapes.length; i++) {
			Escape e = escapes[i];
			if (e.escaped == arg)
				return e.plain;
		}
		return arg;
	}

	private static class Escape {
		final char plain;
		final char escaped;
		Escape(char plain, char escaped) {
			this.plain = plain;
			this.escaped = escaped;
		}
	}

}
