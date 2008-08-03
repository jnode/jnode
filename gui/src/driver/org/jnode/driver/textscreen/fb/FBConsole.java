package org.jnode.driver.textscreen.fb;

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
                            
            //// FIXME we shouldn't be forced to call that : a better (and more generic) solution 
            //// would be to use a listener on InitialNaming binding changes 
            ConsoleManager mgr = InitialNaming.lookup(ConsoleManager.NAME);
            mgr.textScreenManagerChanged();
            ////
            
            //// FIXME we shouldn't need that code since command shell is already created elsewhere
            // but without that code :
            // - it's impossible to scroll nor to type anything that is displayed in the console
            // - it's also impossible to switch between consoles
            // NB : I have checked and the scrollUp/ScrollDown method are called properly 
            final TextConsole first = (TextConsole) mgr.getFocus();                
            new CommandShell(first).run();
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
