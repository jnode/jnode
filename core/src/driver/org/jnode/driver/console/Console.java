/**
 * $Id$
 */
package org.jnode.driver.console;

import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.PointerListener;
import org.jnode.system.event.FocusListener;

/**
 * @author epr
 */
public interface Console extends FocusListener, KeyboardListener,
        PointerListener {

    /**
     * Has this control the focus.
     * @return
     */
    public boolean isFocused();
    
    /**
     * Close this console
     */
    public void close();

    /**
     * returns the registered name for this console
     * 
     * @return registered name for this console
     */
    public String getConsoleName();

    /**
     * Sets the keycode of the accelerator that will focus this console.
     * 
     * @param keyCode
     */
    public void setAcceleratorKeyCode(int keyCode);

    /**
     * Gets the keycode of the accelerator that will focus this console.
     */
    public int getAcceleratorKeyCode();
    /**
     * Add a keyboard listener
     * 
     * @param l
     */
    public void addKeyboardListener(KeyboardListener l);

    /**
     * Remove a keyboard listener
     * 
     * @param l
     */
    public void removeKeyboardListener(KeyboardListener l);

    /**
     * Add a pointer listener
     * 
     * @param l
     */
    public void addPointerListener(PointerListener l);

    /**
     * Remove a pointer listener
     * 
     * @param l
     */
    public void removePointerListener(PointerListener l);
    
    /**
     * Gets the manager this console is registered with.
     * @return
     */
    public ConsoleManager getManager();
}