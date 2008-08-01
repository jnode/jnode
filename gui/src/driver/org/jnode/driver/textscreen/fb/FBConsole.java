package org.jnode.driver.textscreen.fb;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManagerListener;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.textscreen.TextScreenManager;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellManager;
import org.jnode.shell.ShellUtils;
import org.jnode.vm.VmSystem;

/**
 * @author Levente S\u00e1ntha
 */
class FBConsole {
    private static final Logger log = Logger.getLogger(FBConsole.class);

    /**
     * TODO use a listener mechanism instead 
     */
    private static void waitShellManagerAvailable() {
        while (true) {
            try {
                ShellManager mgr = ShellUtils.getShellManager();
                if (mgr != null) {
                    break;
                }
            } catch (NameNotFoundException e) {
                // not yet available                
            }
            
            // not yet available
            Thread.yield();
        }
    }
    
    public static void start() throws Exception {

        waitShellManagerAvailable();

        Device dev = null;
        final Collection<Device> devs = DeviceUtils.getDevicesByAPI(FrameBufferAPI.class);
        int dev_count = devs.size();
        if (dev_count > 0) {
            Device[] dev_a = devs.toArray(new Device[dev_count]);
            dev = dev_a[0];
        }

        if (dev == null) {
            DeviceUtils.getDeviceManager().addListener(new DeviceManagerListener() {

                public void deviceRegistered(Device device) {
                    if (device.implementsAPI(FrameBufferAPI.class)) {                        
                        startFBConsole(device);
                    }
                }

                public void deviceUnregister(Device device) {
                    // TODO stop the FBConsole
                }
            });
        } else {
            startFBConsole(dev);
        }
    }
    
    private static void startFBConsole(Device dev) {
        FbTextScreenManager fbTsMgr = null;
        try {
            final FrameBufferAPI api = dev.getAPI(FrameBufferAPI.class);
            final FrameBufferConfiguration conf = api.getConfigurations()[0];

            fbTsMgr = new FbTextScreenManager(api, conf);
            InitialNaming.unbind(TextScreenManager.NAME);
            InitialNaming.bind(TextScreenManager.NAME, fbTsMgr);
                            
            ////
            ConsoleManager mgr = InitialNaming.lookup(ConsoleManager.NAME);
            
            //
            final int options = ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.SCROLLABLE;

            final TextConsole first = (TextConsole) mgr.createConsole(
                    null, options);
            
            mgr.registerConsole(first);            
            mgr.focus(first);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    System.setOut(new PrintStream(first.getOut()));
                    System.setErr(new PrintStream(first.getErr()));
                    return null;
                }
            });
            System.out.println(VmSystem.getBootLog());

            if (first.getIn() == null) {
                throw new Exception("console input is null");
            }

            new CommandShell(first).run();
            Thread.sleep(60 * 1000);

        } catch (Throwable ex) {
            log.error("Error in FBConsole", ex);
        } finally {
            if (fbTsMgr != null) {
                log.info("Close graphics");
                fbTsMgr.ownershipLost();
            }
        }        
    }
}
