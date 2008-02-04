package org.jnode.apps.jpartition;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.apps.jpartition.consoleview.ConsoleViewFactory;
import org.jnode.apps.jpartition.swingview.SwingViewFactory;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.OptionArgument;
import org.jnode.shell.help.argument.StringArgument;

public class JPartitionCommand extends AbstractCommand {
	static public final String SWINGUI = "swing";
	static public final String CONSOLEUI = "console";

	static private final OptionArgument ARG_UI = new OptionArgument("ui",
            "The type of GUI you want to use",
         new OptionArgument.Option(SWINGUI,   "use swing for UI"),
         new OptionArgument.Option(CONSOLEUI, "use console for UI"));

	static private final StringArgument ARG_INSTALL = new StringArgument("install",
            "select a partition (optionally being created/formatted)");

	public static Help.Info HELP_INFO = new Help.Info("jpartition", "partition disks",
			new Parameter[] {
					new Parameter(ARG_UI, Parameter.MANDATORY),
					new Parameter(ARG_INSTALL, Parameter.OPTIONAL)
			});

	public static void main(String[] args) throws Exception
	{
//		testCharva(args);

		new JPartitionCommand().execute(args);
	}

	public void execute(CommandLine commandLine, InputStream in,
			PrintStream out, PrintStream err) throws Exception
	{
        ParsedArguments cmdLine = HELP_INFO.parse(commandLine);
        String ui = ARG_UI.getValue(cmdLine);
        boolean install = (ARG_INSTALL.getValue(cmdLine) != null);

        ViewFactory viewFactory = createViewFactory(ui, in, out, err);

        JPartition jpartition = new JPartition(viewFactory, install);
        jpartition.launch();
	}

	public static ViewFactory createViewFactory(String ui, InputStream in,
			PrintStream out, PrintStream err)
	{
        ViewFactory viewFactory = null;
		if(CONSOLEUI.equals(ui))
		{
			viewFactory = new ConsoleViewFactory(in, out, err);
		}
		else if(SWINGUI.equals(ui))
		{
			viewFactory = new SwingViewFactory();
		}

		return viewFactory;
	}

	private static void testCharva(String[] args) throws Exception
	{
/*
        //initEnv();
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        TextConsole tc = (TextConsole) cm.createConsole("Console 1",
                ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.SCROLLABLE);
        cm.focus(tc);

        DefaultShellManager sm = new DefaultShellManager();
        InitialNaming.bind(ShellManager.NAME, sm);

        ExtensionPoint aliasesEP = new DummyExtensionPoint();
        DefaultAliasManager am = new DefaultAliasManager(aliasesEP);
        InitialNaming.bind(AliasManager.NAME, am);

        //final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        CommandShell cs = new CommandShell(tc);
        sm.registerShell(cs);
        new Thread(cs).start();
*/
/*
		charvax.swing.JFrame frm = new charvax.swing.JFrame("test");
		JLabel label = new JLabel("test");
		frm.add(label);
		frm.setFocus(label);
		frm.setSize(20, 20);
		frm.setVisible(true);
		frm.setDefaultCloseOperation(charvax.swing.JFrame.EXIT_ON_CLOSE);
*/
	}
}
