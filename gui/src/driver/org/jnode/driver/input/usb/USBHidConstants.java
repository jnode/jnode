package org.jnode.driver.input.usb;

/**
 * Constants for HID usb devices.
 * 
 * @author Fabien Lesire
 * 
 */
public interface USBHidConstants {
    public static final byte HID_SUBCLASS_BOOT_INTERFACE = 0x01;
    public static final byte HID_PROTOCOL_KEYBOARD = 0x01;
    public static final byte HID_PROTOCOL_MOUSE = 0x02;
}
