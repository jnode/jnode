package org.jnode.driver.textscreen.fb;

import java.util.Collection;

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
import org.jnode.naming.NameSpaceListener;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellManager;

/**
 * @author Levente S\u00e1ntha
 */
class FBConsole {
    private static final Logger log = Logger.getLogger(FBConsole.class);

    public static void start() throws Exception {
        InitialNaming.addNameSpaceListener(ShellManager.NAME, new NameSpaceListener<ShellManager>() {

            @Override
            public void serviceBound(ShellManager service) {
                try {
                    shellManagerAvailable();
                } catch (Exception e) {
                    log.error(e);
                }
            }

            @Override
            public void serviceUnbound(ShellManager service) {
                // nothing
            }            
        });
    }
    
    private static void shellManagerAvailable() throws Exception {
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
                            
            //// FIXME we shouldn't need that code since command shell is already created elsewhere
            // but without that code :
            // - it's impossible to scroll nor to type anything that is displayed in the console
            // - it's also impossible to switch between consoles
            // NB : I have checked and the scrollUp/ScrollDown method are called properly 
            ConsoleManager mgr = InitialNaming.lookup(ConsoleManager.NAME);
            final TextConsole first = (TextConsole) mgr.getFocus();                
            new CommandShell(first).run();
//            CommandShell newShell = new CommandShell(first);
//            CommandShell oldShell = (CommandShell) ShellUtils.getShellManager().registerMainShell(newShell);
//            if (oldShell != null) {
//                oldShell.consoleClosed(new ConsoleEvent(first));
//            }
//            
//            newShell.run();
            
            Thread.sleep(60 * 1000);
            //// end of FIXME
            
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
