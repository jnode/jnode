package org.jnode.apps.jpartition;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.controller.MainController;
import org.jnode.apps.jpartition.swingview.SwingViewFactory;
import org.jnode.apps.jpartition.utils.BasicNameSpace;
import org.jnode.driver.DeviceManager;
import org.jnode.emu.Emu;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.NameSpace;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.util.OsUtils;

public class JPartition extends Emu {
	private static final Logger log = Logger.getLogger(JPartition.class);
	
	public static void main(String[] args) throws Throwable 
	{
//		testCharva();
		
		if(!OsUtils.isJNode())
		{
			initJNodeCore();
		}
		
		ViewFactory viewFactory = new SwingViewFactory();
		MainController controller = new MainController(viewFactory);
	}
	
	private static void initJNodeCore() throws Exception {
        NameSpace namespace = new BasicNameSpace();
        InitialNaming.setNameSpace(namespace);
        namespace.bind(DeviceManager.NAME, StubDeviceManager.INSTANCE);
	}
	
/*	
	private static void testCharva() throws Throwable
	{
        initEnv();
        SwingTextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        TextConsole tc = (TextConsole) cm.createConsole("Console 1",
                ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.SCROLLABLE);
        cm.focus(tc);
        CommandShell cs = new CommandShell(tc);
        final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        sm.registerShell(cs);
        new Thread(cs).start();
		
		charvax.swing.JFrame frm = new charvax.swing.JFrame("test");
		JLabel label = new JLabel("test");
		frm.add(label);
		frm.setFocus(label);
		frm.setSize(300, 300);
		frm.setVisible(true);
		frm.setDefaultCloseOperation(charvax.swing.JFrame.EXIT_ON_CLOSE);		
	}
*/	
}
