package org.jnode.shell.help.def;

import java.io.PrintWriter;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;

/**
 * This Help implementation provides for 'old' syntax commands in plain text format.
 * 
 * @author crawley@jnode.org
 */
public class OldSyntaxHelp extends TextHelpBase implements Help {
    private final Info info;
    private final String command;
    
    public OldSyntaxHelp(Info info, String command) {
        this.info = info;
        this.command = command;
    }

    @Override
    public void help(PrintWriter pw) {
        final Syntax[] syntaxes = info.getSyntaxes();
        for (int i = 0; i < syntaxes.length; i++) {
            help(syntaxes[i], pw);
            if (i < syntaxes.length) {
                pw.println();
            }
        }
    }
    
    @Override
    public void usage(PrintWriter pw) {
        final Syntax[] syntaxes = info.getSyntaxes();
        for (int i = 0; i < syntaxes.length; i++) {
            usage(syntaxes[i], pw);
            if (i < syntaxes.length) {
                pw.println();
            }
        }
    }
    
    private String commandName() {
        return command == null ? info.getName() : command;
    }
    
    /**
     * Shows the help for a command syntax.
     */
    public void help(Syntax syntax, PrintWriter pw) {
        usage(syntax, pw);
        if (syntax.getDescription() != null) {
            pw.println("\n" + HelpFactory.getLocalizedHelp("help.description") + ":");
            format(pw, new TextCell[]{new TextCell(4, NOMINAL_WIDTH - 4)},
                new String[]{syntax.getDescription()});
        }
        final Parameter[] params = syntax.getParams();
        if (params.length != 0) {
            pw.println("\n" + HelpFactory.getLocalizedHelp("help.parameters") + ":");
            for (int i = 0; i < params.length; i++) {
                describeParameter(params[i], pw);
            }
        }
    }
    
    private void usage(Syntax syntax, PrintWriter pw) {
        StringBuilder line = new StringBuilder(commandName());
        final Parameter[] params = syntax.getParams();
        for (int i = 0; i < params.length; i++) {
            line.append(' ').append(params[i].format());
        }
        pw.println(HelpFactory.getLocalizedHelp("help.usage") + ": " + line);
    }

    public void describeParameter(Parameter param, PrintWriter out) {
        format(out, new TextCell[]{new TextCell(2, 18), new TextCell(2, NOMINAL_WIDTH - 22)},
            new String[]{param.getName(), param.getDescription()});
    }

    public void describeArgument(Argument arg, PrintWriter out) {
        format(out, new TextCell[]{new TextCell(4, 16), new TextCell(2, NOMINAL_WIDTH - 22)},
            new String[]{arg.getName(), arg.getDescription()});
    }
}
