package org.jnode.driver.textscreen.fb;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManagerListener;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.textscreen.TextScreenManager;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.naming.InitialNaming;

/**
 * @author Levente S\u00e1ntha
 */
class FBConsole {
    private static final Logger log = Logger.getLogger(FBConsole.class);

    public static void start() throws Exception {
        final Collection<Device> devs = DeviceUtils.getDevicesByAPI(FrameBufferAPI.class);
        int dev_count = devs.size();
        if (dev_count > 0) {
            startFBConsole(devs.iterator().next());
        } else {
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
        } catch (Throwable ex) {
            log.error("Error in FBConsole", ex);
            if (fbTsMgr != null) {
                log.info("Close graphics");
                fbTsMgr.ownershipLost();
            }
        }
    }
}
