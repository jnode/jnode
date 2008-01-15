package org.jnode.apps.jpartition;

import org.jnode.apps.jpartition.model.UserFacade;
import org.jnode.apps.jpartition.swingview.SwingViewFactory;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.emu.ShellEmu;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.def.DefaultShellManager;

import charvax.swing.JLabel;

public class JPartition {
	public static void main(String[] args) throws Exception
	{
		testCharva(args);
/*
		ViewFactory viewFactory = createViewFactory(args);
		launch(viewFactory);
*/
	}

	public static ViewFactory createViewFactory(String[] args)
	{
		return new SwingViewFactory();
	}

	private static final void launch(ViewFactory viewFactory) throws Exception
	{
		ErrorReporter errorReporter = viewFactory.createErrorReporter();
		UserFacade.getInstance().setErrorReporter(errorReporter);

        // CommandProcessor
        Object cmdProcessorView = viewFactory.createCommandProcessorView();

        // Device
        viewFactory.createDeviceView(errorReporter, cmdProcessorView);
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
		charvax.swing.JFrame frm = new charvax.swing.JFrame("test");
		JLabel label = new JLabel("test");
		frm.add(label);
		frm.setFocus(label);
		frm.setSize(20, 20);
		frm.setVisible(true);
		frm.setDefaultCloseOperation(charvax.swing.JFrame.EXIT_ON_CLOSE);
	}
}
