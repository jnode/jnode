package org.jnode.shell;

import gnu.java.io.NullOutputStream;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.CompletionException;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.FileArgument;


/**
 * This command interpretter supports simple input and output redirection and pipelines.
 * 
 * @author crawley@jnode.org
 */
public class RedirectingInterpreter extends DefaultInterpreter implements ThreadExitListener {
	
	private static final int COMPLETE_ALIAS = 1;
	private static final int COMPLETE_ARG = 2;
	private static final int COMPLETE_INPUT = 3;
	private static final int COMPLETE_OUTPUT = 4;
	private static final int COMPLETE_PIPE = 5;
	
	static final Factory FACTORY = new Factory() {
		public CommandInterpreter create() {
			return new RedirectingInterpreter();
		}
		public String getName() {
			return "redirecting";
		}
    };

    private int completionContext;
	
	public String getName() {
		return "redirecting";
	}
	
	public int interpret(CommandShell shell, String line) throws ShellException {
		Tokenizer tokenizer = new Tokenizer(line, REDIRECTS_FLAG);
    	List<CommandDescriptor> commands = parse(tokenizer, line, false);
		int len = commands.size();
		if (len == 0) {
			return 0;	// empty command line.
		}
		shell.addCommandToHistory(line);
		if (len == 1) {
			return runCommand(shell, commands.get(0));
		}
		else {
			return runPipeline(shell, commands);
		}
	}
	
	public Completable parsePartial(CommandShell shell, String line) throws ShellSyntaxException {
		Tokenizer tokenizer = new Tokenizer(line, REDIRECTS_FLAG); 
    	List<CommandDescriptor> commands = parse(tokenizer, line, true);
    	int nosCommands = commands.size();
    	if (nosCommands == 0) {
    		return new CommandLine("", null);
    	}
    	else {
    		CommandDescriptor lastDesc = commands.get(nosCommands - 1);
    		CommandLine lastCommand = lastDesc.commandLine;
    		lastCommand.setArgumentAnticipated(tokenizer.whitespaceAfter());
    		int startPos = tokenizer.getTokenStartPos();
			switch (completionContext) {
    		case COMPLETE_ALIAS:
    		case COMPLETE_ARG:
    		case COMPLETE_PIPE:
    			break;
    		case COMPLETE_INPUT:
    			if (lastDesc.fromFileName == null || !tokenizer.whitespaceAfter()) {
    				return new RedirectionCompleter(line, startPos);
    			}
    			break;
    		case COMPLETE_OUTPUT:
    			if (lastDesc.toFileName == null || !tokenizer.whitespaceAfter()) {
    				return new RedirectionCompleter(line, startPos);
    			}
    			break;
    		default:
    			throw new ShellFailureException("bad completion context (" + completionContext + ")");
    		}
    		return new SubcommandCompleter(lastCommand, line, 
					tokenizer.whitespaceAfter() ? tokenizer.getPos() : startPos);
    	}
    }

	private List<CommandDescriptor> parse(Tokenizer tokenizer, String line, boolean allowPartial) 
	throws ShellSyntaxException {
    	LinkedList<CommandDescriptor> commands = new LinkedList<CommandDescriptor>();
    	boolean pipeTo = false;
    	while(tokenizer.hasNext()) {
			completionContext = COMPLETE_ALIAS;
    		String commandName = tokenizer.next();
    		if (tokenizer.getType() == SPECIAL) {
				throw new ShellSyntaxException(
						"Misplaced '" + commandName + "': expected a command name");
    		}
    		String fromFileName = null;
    		String toFileName = null;
    		LinkedList<String> args = new LinkedList<String>();
    		pipeTo = false;
    		while (tokenizer.hasNext()) {
    			completionContext = COMPLETE_ARG;
    			String token = tokenizer.next();
    			if (tokenizer.getType() == SPECIAL) {
    				if (token.equals("<")) {
    					fromFileName = parseFileName(tokenizer, "<");
    					if (fromFileName == null && !allowPartial) {
    						throw new ShellSyntaxException("no filename after '<'");
    					}
    					completionContext = COMPLETE_INPUT;
    					continue;
    				}
    				else if (token.equals(">")) {
    					toFileName = parseFileName(tokenizer, ">");
    					if (toFileName == null && !allowPartial) {
    						throw new ShellSyntaxException("no filename after '>'");
    					}
    					completionContext = COMPLETE_OUTPUT;
    					continue;
    				}
    				else if (token.equals("|")) {
    					pipeTo = true;
    					completionContext = COMPLETE_PIPE;
    					break;
    				}
    				else {
    					throw new ShellSyntaxException(
							"unrecognized symbol: '" + token + "'");
    				}
    			}
    			else {
    				args.add(token);
    			}
    		}
    		CommandLine commandLine = 
    			new CommandLine(commandName, 
    						    args.toArray(new String[args.size()]));
    		commands.add(new CommandDescriptor(commandLine, fromFileName,
    										   toFileName, pipeTo));
    	}
    	if (pipeTo && !allowPartial) {
    		throw new ShellSyntaxException("no command after '<'");
    	}
    	return commands;
	}
	
	private String parseFileName(Tokenizer tokenizer, String special) 
	throws ShellSyntaxException {
		if (!tokenizer.hasNext()) {
			return null;
		}
		String token = tokenizer.next();
		if (tokenizer.getType() == SPECIAL) {
			throw new ShellSyntaxException("misplaced '" + token + "'");
		}
		if (token.isEmpty()) {
			throw new ShellSyntaxException("empty '" + special + "' file name");
		}
		return token;
	}

	private int runCommand(CommandShell shell, CommandDescriptor desc) 
	throws ShellException {
		InputStream in = null;
		PrintStream out = null;
		PrintStream err = null;
		try {
			try {
				if (desc.fromFileName != null) {
					in = new FileInputStream(desc.fromFileName);
				}
			}
			catch (IOException ex) {
				throw new ShellInvocationException(
						"cannot open '" + desc.fromFileName + "': " + 
						ex.getMessage());
			}
			try {
				if (desc.toFileName != null) {
					out = new PrintStream(new FileOutputStream(desc.toFileName));
				}
			}
			catch (IOException ex) {
				throw new ShellInvocationException(
						"cannot open '" + desc.toFileName + "': " + 
						ex.getMessage());
			}
			desc.commandLine.setStreams(new Closeable[] {in, out, err});
			int rc = shell.invoke(desc.commandLine);
			if (out != null && out.checkError()) {
				throw new ShellInvocationException("problem flushing output");
			}
			return rc;
		}
		finally {
			try {
				if (desc.fromFileName != null) {
					in.close();
				}
			}
			catch (IOException ex) {
				// squash
			}
			if (desc.toFileName != null && out != null) {
				out.close();
			}
		}
	}
	
	private synchronized int runPipeline(CommandShell shell, List<CommandDescriptor> descs) 
	throws ShellException {
		int nosStages = descs.size();
		try {
			// Create all the threads for the pipeline, wiring up their input
			// and output streams.
			int stageNo = 0;
			PipedOutputStream pipeOut = null;
			for (CommandDescriptor desc : descs) {
				InputStream in = null;
				PrintStream out = null;
				PrintStream err = null;
				desc.openedStreams = new ArrayList<Closeable>(2);
				try {
					// redirect from
					if (desc.fromFileName != null) {
						in = new FileInputStream(desc.fromFileName);
						desc.openedStreams.add(in);
					}
				}
				catch (IOException ex) {
					throw new ShellInvocationException(
							"cannot open '" + desc.fromFileName + "': " + 
							ex.getMessage());
				}
				try {
					// redirect to
					if (desc.toFileName != null) {
						out = new PrintStream(new FileOutputStream(desc.toFileName));
						desc.openedStreams.add(out);
					}
				}
				catch (IOException ex) {
					throw new ShellInvocationException(
							"cannot open '" + desc.toFileName + "': " + 
							ex.getMessage());
				}
				if (stageNo > 0) {
					// pipe from
					if (pipeOut != null) {
						// the previous stage is sending stdout to the pipe
						if (in == null) {
							// this stage is going to read from the pipe
							PipedInputStream pipeIn = new PipedInputStream();
							try {
								pipeIn.connect(pipeOut);
							}
							catch (IOException ex) {
								throw new ShellInvocationException("Problem connecting pipe", ex);
							}
							in = pipeIn;
							desc.openedStreams.add(pipeIn);
						}
						else {
							// this stage has redirected stdin from a file ... so go back and 
							// replace the previous stage's pipeOut with a NullOutputStream/PrintStream
							CommandDescriptor prev = descs.get(stageNo - 1);
							Closeable[] ps = prev.commandLine.getStreams();
							try {
								pipeOut.close();
							}
							catch (IOException ex) {
								// squash
							}
							prev.commandLine.setStreams(
									new Closeable[] {ps[0], new NullOutputStream(), ps[2]});
						}
					}
					else {
						//	the previous stage has explicitly redirected stdout
						if (in == null) {
							// this stage hasn't redirected stdin, so we need to give
							// it a NullInputStream to suck on.
							in = new NullInputStream();
							desc.openedStreams.add(in);
						}
					}
				}
				if (stageNo < nosStages - 1) {
					// this stage is not the last one, and it hasn't redirected
					// its stdout, so it will write to a pipe
					if (out == null) {	
						pipeOut = new PipedOutputStream();
						out = new PrintStream(pipeOut);
						desc.openedStreams.add(out);
					}
				}
				desc.commandLine.setStreams(new Closeable[] {in, out, err});
				desc.thread = shell.invokeAsynchronous(desc.commandLine);
				stageNo++;
			}
			
			currentDescriptors = descs;
			threadsLeft = descs.size();
			
			// Start all threads.
			for (CommandDescriptor desc : descs) {
				desc.thread.start(this);
			}
			
			// Wait until they have finished
			while (threadsLeft > 0) {
				try {
					wait();
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return -1;
				}
			}
			return descs.get(nosStages - 1).thread.getReturnCode();
		}
		finally {
			// Close any remaining streams.
			for (CommandDescriptor desc : descs) {
				if (desc.openedStreams != null) {
					for (Closeable stream : desc.openedStreams) {
						try {
							stream.close();
						}
						catch (IOException ex) {
							// squash
						}
					}
				}
			}
			// TODO deal with any left over threads 
		}
	}
	
	
	private List<CommandDescriptor> currentDescriptors;
	private int threadsLeft;
	
	/**
	 * Callback to deal with a thread that has exitted.
	 * 
	 * @param thread
	 */
	public synchronized void notifyThreadExitted(CommandThread thread) {
		// If the thread owned any input or output streams, they need to
		// be closed.  In particular, this will cause the next downstream
		// command in a pipeline to see an "end of file".
		for (CommandDescriptor desc : currentDescriptors) {
			if (thread == desc.thread) {
				if (desc.openedStreams == null) {
					throw new ShellFailureException("bad thread exit callback");
				}
				for (Closeable stream : desc.openedStreams) {
					try {
						stream.close();
					}
					catch (IOException ex) {
						// squash
					}
				}
				desc.openedStreams = null;
				threadsLeft--;
				notify();
				break;
			}
		}
	}

	
	private static class CommandDescriptor {
		public final CommandLine commandLine;
		public final String fromFileName;
		public final String toFileName;
		public final boolean pipeTo;
		public List<Closeable> openedStreams;
		public CommandThread thread;
		
		public CommandDescriptor(CommandLine commandLine, String fromFileName, 
				String toFileName, boolean pipeTo) {
			super();
			this.commandLine = commandLine;
			this.fromFileName = fromFileName;
			this.toFileName = toFileName;
			this.pipeTo = pipeTo;
		}
	}
	
	private abstract class EmbeddedCompleter implements Completable {
		private final String partial;
		private final int startPos;
		
		public EmbeddedCompleter(String partial, int startPos) {
			this.partial = partial;
			this.startPos = startPos;
		}
		
		public String getCompletableString() {
			return partial.substring(startPos);
		}
		
		public void setFullCompleted(CompletionInfo completion, String completed) {
			completion.setCompleted(partial.substring(0, startPos) + completed);
		}
	}
		
    private class SubcommandCompleter extends EmbeddedCompleter {
    	private final CommandLine subcommand;
    	
	    public SubcommandCompleter(CommandLine subcommand, String partial, int startPos) {
	    	super(partial, startPos);
	    	this.subcommand = subcommand;
		}

		public void complete(CompletionInfo completion, CommandShell shell)
				throws CompletionException {
			subcommand.complete(completion, shell);
			setFullCompleted(completion, completion.getCompleted());
		}
	}
	
	private class RedirectionCompleter extends EmbeddedCompleter {
		
	    private final Help.Info fileParameter = 
	    	new Help.Info("file",
	            "default parameter for file redirection completion",
				new Parameter(new FileArgument("file", "a file", Argument.SINGLE), 
						      Parameter.MANDATORY)
		);

	    public RedirectionCompleter(String partial, int startPos) {
	    	super(partial, startPos);
		}

		public void complete(CompletionInfo completion, CommandShell shell)
				throws CompletionException {
			CommandLine command = new CommandLine("?", new String[]{getCompletableString()});
			String result = fileParameter.complete(command);
			setFullCompleted(completion, result);
		}
	}
}
