/**
 * $Id$  
 */
package org.jnode.awt;

import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.Device;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.awt.peer.RawJNodeToolkit;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.awt.event.KeyEvent;
import java.awt.Component;

/**
 * @author Levente S?ntha
 */
public class KeyboardHandler implements KeyboardListener {
    private final Logger log = Logger.getLogger(KeyboardHandler.class);
    private KeyboardAPI keyboardAPI;

    public KeyboardHandler() {
        try {
            final Collection keyboards = DeviceUtils.getDevicesByAPI(KeyboardAPI.class);
            if (!keyboards.isEmpty()) {
                Device keyboardDevice = (Device) keyboards.iterator().next();
                keyboardAPI = (KeyboardAPI) keyboardDevice.getAPI(KeyboardAPI.class);
                keyboardAPI.addKeyboardListener(this);
            }
        } catch (ApiNotFoundException ex) {
            log.error("Strange...", ex);
        }
    }

    /**
     * @param event
     */
    public void keyPressed(KeyboardEvent event) {
        postEvent(KeyEvent.KEY_PRESSED, event.getTime(), event.getModifiers(), event.getKeyCode(), event.getKeyChar());
    }

    /**
     * @param event
     */
    public void keyReleased(KeyboardEvent event) {
        postEvent(KeyEvent.KEY_RELEASED, event.getTime(), event.getModifiers(), event.getKeyCode(), event.getKeyChar());
        char ch = event.getKeyChar();
        if(ch != KeyEvent.CHAR_UNDEFINED){
            postEvent(KeyEvent.KEY_TYPED, event.getTime(), event.getModifiers(), KeyEvent.VK_UNDEFINED, ch);
        }
    }

    /**
     * @param id
     * @param modifiers
     * @param keyCode
     * @param keyChar
     */
    private void postEvent(int id, long time, int modifiers, int keyCode, char keyChar) {
        RawJNodeToolkit tk = (RawJNodeToolkit) RawJNodeToolkit.getDefaultToolkit();
        Component source = tk.getFocusHandler().getFocusedComponent();
        if (source == null) source = tk.getTop();
        KeyEvent me = new KeyEvent(source, id, time, modifiers, keyCode, keyChar);
        JNodeGenericPeer.q.postEvent(me);
    }

    /**
     * Close this handler
     */
    public void close() {
        if (keyboardAPI != null) {
            keyboardAPI.removeKeyboardListener(this);
        }
    }
}
