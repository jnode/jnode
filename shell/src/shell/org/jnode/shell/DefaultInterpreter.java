package org.jnode.shell;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * This interpretter simply parses the command line into a command name
 * and arguments, with simple quoting and escaping.
 * 
 * @author crawley@jnode.org
 */
public class DefaultInterpreter implements CommandInterpreter {
	
	static final Factory FACTORY = new Factory() {
		public CommandInterpreter create() {
			return new DefaultInterpreter();
		}
		public String getName() {
			return "default";
		}
    };

    static final String[] NO_ARGS = new String[0];
    
    public static final int REDIRECTS_FLAG = 0x01;

    // Token types.
    public static final int LITERAL = 0;
    public static final int STRING = 1;
    public static final int CLOSED = 2;
    public static final int SPECIAL = 4;

    // Recognized meta-characters
    public static final char ESCAPE_CHAR = '\\';
    public static final char FULL_ESCAPE_CHAR = '\'';
    public static final char QUOTE_CHAR = '"';
    public static final char SPACE_CHAR = ' ';
    public static final char SEND_OUTPUT_TO_CHAR = '>';
    public static final char GET_INPUT_FROM_CHAR = '<';
    public static final char PIPE_CHAR = '|';
    public static final char COMMENT_CHAR = '#';

    // Recognized '\' escapes
    private static final char ESCAPE_B = '\b';
    private static final char B = 'b';
    private static final char ESCAPE_N = '\n';
    private static final char N = 'n';
    private static final char ESCAPE_R = '\r';
    private static final char R = 'r';
    private static final char ESCAPE_T = '\t';
    private static final char T = 't';
    

    public String getName() {
    	return "default";
    }
    
    public int interpret(CommandShell shell, String line) throws ShellException {
    	LinkedList<String> words = new LinkedList<String>();
		Tokenizer tokenizer = new Tokenizer(line);
		while (tokenizer.hasNext()) {
			words.add(tokenizer.next());
		}
		int nosTokens = words.size();
		if (nosTokens == 0) {
			return 0;
		}
		CommandLine commandLine;
		if (nosTokens == 1) {
			commandLine = new CommandLine(words.get(0), NO_ARGS);
		}
		else {
			String commandName = null;
			String[] args = new String[nosTokens - 1];
			int pos = 0;
			for (String token : words) {
				if (commandName == null) {
					commandName = token;
				}
				else {
					args[pos++] = token;
				}
			}
			commandLine = new CommandLine(commandName, args);
		}
		shell.addCommandToHistory(line);
		return shell.invoke(commandLine);
	}
    
    public Completable parsePartial(CommandShell shell, String line) throws ShellSyntaxException {
    	Tokenizer tokenizer = new Tokenizer(line);
		if (!tokenizer.hasNext()) {
			return new CommandLine("", null);
		}
		String commandName = tokenizer.next();
		LinkedList<String> args = new LinkedList<String>();
		while (tokenizer.hasNext()) {
			args.add(tokenizer.next());
		}
		CommandLine res = new CommandLine(commandName, args.toArray(new String[args.size()]));
		res.setArgumentAnticipated(tokenizer.whitespaceAfter());
		return res;
    }

    /**
     * A simple command line tokenizer for the 'built-in' interpretters.  It understands
     * quoting, some '\' escapes, and (depending on constructor flags) certain "special"
     * symbols.
     */
    static class Tokenizer implements Iterator<String> {
        private final String s;
        private final int flags;
        
        private int pos = 0;
        private int tokenStartPos = 0;
        private boolean inFullEscape = false;
        private boolean inQuote = false;
        private int type = -1; // undefined until next() is called.
        private boolean whitespaceAfter;

        /**
         * Instantiate a commandline tokenizer for a given input String.
         * @param line the input String.
         * @param flags flags controlling the tokenization.
         */
        public Tokenizer(String line, int flags) {
    		pos = 0;
    		s = line;
    		this.flags = flags;
    	}

        public Tokenizer(String line) {
    		this(line, 0);
    	}

        /**
    	 * Returns if there are no more tokens to return.
    	 * 
    	 * @return <code>true</code> if there is another token; <code>false</code>
    	 *         otherwise
    	 */
    	public boolean hasNext() {
    		while (pos < s.length() && s.charAt(pos) == SPACE_CHAR) {
    			pos++;
    		}
    		return pos < s.length() && s.charAt(pos) != COMMENT_CHAR;
    	}
    	
    	/**
    	 * Is there whitespace after the token returned by <code>next()</code>?
    	 * @return If there is whitespace after the token, then <code>true</code>, 
    	 * otherwise <code>false</code>
    	 */
    	public boolean whitespaceAfter() {
    		return whitespaceAfter;
    	}
    	
    	/**
    	 * Get the current position in the line string.
    	 * @return The current position.
    	 */
    	public int getPos() {
    		return pos;
    	}

    	/**
    	 * Get the start position in the line string of the token returned by <code>next()</code>.
    	 * @return The current position.
    	 */
    	public int getTokenStartPos() {
    		return tokenStartPos;
    	}

    	/**
    	 * Extract the next token string and return it.
    	 * 
    	 * @return the next token
    	 */
    	public String next() throws NoSuchElementException {
    		if (!hasNext()) {
    			throw new NoSuchElementException();
    		}

    		type = LITERAL;
    		tokenStartPos = pos;
    		
            StringBuilder token = new StringBuilder(5);
    		char currentChar;

    		boolean finished = false;

    		while (!finished && pos < s.length()) {
    			currentChar = s.charAt(pos++);

    			switch (currentChar) {
    			case ESCAPE_CHAR:
    				if (pos >= s.length()) {
    					throw new IllegalArgumentException("escape char ('\\') not followed by a character");
    				}
    				char ch;
    				switch (ch = s.charAt(pos++)) {
    				case N:
    					token.append(ESCAPE_N);
    					break;
    				case B:
    					token.append(ESCAPE_B);
    					break;
    				case R:
    					token.append(ESCAPE_R);
    					break;
    				case T:
    					token.append(ESCAPE_T);
    					break;
    				default:
    					token.append(ch);
    				}
    				break;

    			case FULL_ESCAPE_CHAR:
    				if (inQuote) {
    					token.append(currentChar);
    				} else {
    					inFullEscape = !inFullEscape; // just a toggle
    					type = STRING;
                        if (!inFullEscape) {
                            type |= CLOSED;
                        }
    				}
    				break;
    			case QUOTE_CHAR:
    				if (inFullEscape) {
    					token.append(currentChar);
    				} else {
    					inQuote = !inQuote;
                        type = STRING;
                        if (!inQuote) {
                            type |= CLOSED;
                        }
    				}
    				break;
    			case SPACE_CHAR:
    				if (inFullEscape || inQuote) {
    					token.append(currentChar);
    				} else {
    					if (token.length() != 0) { // don't return an empty token
    						finished = true;
    						pos--; // to return trailing space as empty last token
    					}
    				}
    				break;
    			case COMMENT_CHAR:
    				if (inFullEscape || inQuote) {
    					token.append(currentChar);
    				} else {
    					finished = true;
    					pos = s.length(); // ignore EVERYTHING
    				}
    				break;
    			case GET_INPUT_FROM_CHAR:
    			case SEND_OUTPUT_TO_CHAR:
    			case PIPE_CHAR:
    				if (inFullEscape || inQuote || (flags & REDIRECTS_FLAG) == 0) {
    					token.append(currentChar);
    				} else {
    					finished = true;
    					if (token.length() == 0) {
    						token.append(currentChar);
    						type = SPECIAL;
    					}
    					else {
    						pos--;  // the special character terminates the literal.
    					}
    				}
    				break;
    			default:
    				token.append(currentChar);
    			}
    		}

    		whitespaceAfter = (pos < s.length() && s.charAt(pos) == SPACE_CHAR);
    		return token.toString();
    	}

    	/**
    	 * This operation it not supported.
    	 */
		public void remove() {
			throw new UnsupportedOperationException("remove");
		}
		
		/**
		 * Get the token type for the token returned by the last call
		 * to next().
		 * 
		 * @return a token type.
		 */
		public int getType() {
			return type;
		}
    }
}
