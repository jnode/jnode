package org.jnode.shell.bjorne;

import static org.jnode.shell.bjorne.BjorneToken.TOK_CLOBBER;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DGREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_DLESSDASH;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREAT;
import static org.jnode.shell.bjorne.BjorneToken.TOK_GREATAND;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESS;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSAND;
import static org.jnode.shell.bjorne.BjorneToken.TOK_LESSGREAT;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.jnode.shell.CommandInfo;
import org.jnode.shell.CommandInterpreter;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.CommandThread;
import org.jnode.shell.Completable;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellFailureException;
import org.jnode.shell.ShellInvocationException;
import org.jnode.shell.ShellSyntaxException;
import org.jnode.shell.syntax.CommandSyntaxException;

/**
 * This is a Java implementation of the Bourne Shell language.
 * 
 * @author crawley@jnode.org
 * 
 */
public class BjorneInterpreter implements CommandInterpreter {

    public static final int CMD_EMPTY = 0;

    public static final int CMD_COMMAND = 1;

    public static final int CMD_LIST = 2;

    public static final int CMD_FOR = 3;

    public static final int CMD_WHILE = 4;

    public static final int CMD_UNTIL = 5;

    public static final int CMD_IF = 6;

    public static final int CMD_ELIF = 7;

    public static final int CMD_ELSE = 8;

    public static final int CMD_CASE = 9;

    public static final int CMD_SUBSHELL = 10;

    public static final int CMD_BRACE_GROUP = 11;

    public static final int CMD_FUNCTION_DEF = 12;

    public static final int BRANCH_BREAK = 1;

    public static final int BRANCH_CONTINUE = 2;

    public static final int BRANCH_EXIT = 3;

    public static final int BRANCH_RETURN = 4;

    public static final int REDIR_LESS = TOK_LESS;

    public static final int REDIR_GREAT = TOK_GREAT;

    public static final int REDIR_DLESS = TOK_DLESS;

    public static final int REDIR_DLESSDASH = TOK_DLESSDASH;

    public static final int REDIR_DGREAT = TOK_DGREAT;

    public static final int REDIR_LESSAND = TOK_LESSAND;

    public static final int REDIR_GREATAND = TOK_GREATAND;

    public static final int REDIR_LESSGREAT = TOK_LESSGREAT;

    public static final int REDIR_CLOBBER = TOK_CLOBBER;

    public static final int FLAG_ASYNC = 0x0001;

    public static final int FLAG_AND_IF = 0x0002;

    public static final int FLAG_OR_IF = 0x0004;

    public static final int FLAG_BANG = 0x0008;

    public static final int FLAG_PIPE = 0x0010;

    public static final CommandNode EMPTY = 
        new SimpleCommandNode(CMD_EMPTY, new BjorneToken[0]);

    private static HashMap<String, BjorneBuiltin> BUILTINS = 
        new HashMap<String, BjorneBuiltin>();
    
    private static boolean DEBUG = false;

    static {
        BUILTINS.put("break", new BreakBuiltin());
        BUILTINS.put("continue", new ContinueBuiltin());
        BUILTINS.put("exit", new ExitBuiltin());
        BUILTINS.put("return", new ReturnBuiltin());
        BUILTINS.put("source", new SourceBuiltin());
    }

    private CommandShell shell;

    private BjorneContext context;

    public BjorneInterpreter() {
    }

    public String getName() {
        return "bjorne";
    }

    public int interpret(CommandShell shell, String command) throws ShellException {
        return interpret(shell, command, null, false);
    }

    public Completable parsePartial(CommandShell shell, String partial) throws ShellSyntaxException {
        // TODO Auto-generated method stub
        return null;
    }

    int interpret(CommandShell shell, String command, OutputStream capture, boolean source) 
        throws ShellException {
        BjorneContext myContext;
        if (capture == null) {
            if (this.shell != shell) {
                if (this.shell != null) {
                    throw new ShellFailureException("my shell changed");
                }
                this.shell = shell;
                this.context = new BjorneContext(this);
            }
            myContext = this.context;
        } else {
            myContext = new BjorneContext(this);
        }
        BjorneTokenizer tokens = new BjorneTokenizer(command);
        CommandNode tree = new BjorneParser(tokens).parse();
        if (DEBUG) {
            System.err.println(tree);
        }
        try {
            return tree.execute((BjorneContext) myContext);
        } catch (BjorneControlException ex) {
            switch (ex.getControl()) {
                case BRANCH_EXIT:
                    return ex.getCount();
                case BRANCH_BREAK:
                case BRANCH_CONTINUE:
                    return 0;
                case BRANCH_RETURN:
                    return (source) ? ex.getCount() : 1;
                default:
                    throw new ShellFailureException("unknown control " + ex.getControl());
            }
        }
    }

    int executeCommand(CommandLine cmdLine, BjorneContext context, Closeable[] streams) throws ShellException {
        BjorneBuiltin builtin = BUILTINS.get(cmdLine.getCommandName());
        if (builtin != null) {
            // FIXME ... built-in commands should use the Syntax mechanisms so
            // that completion, help, etc work as expected.
            return builtin.invoke(cmdLine, this, context);
        } else {
            cmdLine.setStreams(streams);
            try {
                CommandInfo cmdInfo = cmdLine.parseCommandLine(shell);
                return shell.invoke(cmdLine, cmdInfo);
            } catch (CommandSyntaxException ex) {
                throw new ShellException("Command arguments don't match syntax", ex);
            }
        }
    }

    public BjorneContext createContext() throws ShellFailureException {
        return new BjorneContext(this);
    }

    public CommandShell getShell() {
        return shell;
    }

    public PrintStream resolvePrintStream(Closeable stream) {
        return shell.resolvePrintStream(stream);
    }

    public InputStream resolveInputStream(Closeable stream) {
        return shell.resolveInputStream(stream);
    }

    public CommandThread fork(CommandLine command, Closeable[] streams) 
        throws ShellException {
        command.setStreams(streams);
        CommandInfo cmdInfo = command.parseCommandLine(shell);
        return shell.invokeAsynchronous(command, cmdInfo);
    }
}
