/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.bus.usb;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface USBConstants {

    /* USB constants */

    /*
      * Device and/or Interface Class codes
      */
    public static final int USB_CLASS_PER_INTERFACE = 0; /* for DeviceClass */
    public static final int USB_CLASS_AUDIO = 1;
    public static final int USB_CLASS_COMM = 2;
    public static final int USB_CLASS_HID = 3;
    public static final int USB_CLASS_PHYSICAL = 5;
    public static final int USB_CLASS_STILL_IMAGE = 6;
    public static final int USB_CLASS_PRINTER = 7;
    public static final int USB_CLASS_MASS_STORAGE = 8;
    public static final int USB_CLASS_HUB = 9;
    public static final int USB_CLASS_CDC_DATA = 0x0a;
    public static final int USB_CLASS_CSCID = 0x0b; /* chip+ smart card */
    public static final int USB_CLASS_CONTENT_SEC = 0x0d; /* content security */
    public static final int USB_CLASS_WIRELESS = 0xe0;
    public static final int USB_CLASS_APP_SPEC = 0xfe;
    public static final int USB_CLASS_VENDOR_SPEC = 0xff;

    /*
      * USB types
      */
    public static final int USB_TYPE_MASK = (0x03 << 5);
    public static final int USB_TYPE_STANDARD = (0x00 << 5);
    public static final int USB_TYPE_CLASS = (0x01 << 5);
    public static final int USB_TYPE_VENDOR = (0x02 << 5);
    public static final int USB_TYPE_RESERVED = (0x03 << 5);

    /*
      * USB recipients
      */
    public static final int USB_RECIP_MASK = 0x1f;
    public static final int USB_RECIP_DEVICE = 0x00;
    public static final int USB_RECIP_INTERFACE = 0x01;
    public static final int USB_RECIP_ENDPOINT = 0x02;
    public static final int USB_RECIP_OTHER = 0x03;

    /*
      * USB directions
      */
    public static final int USB_DIR_MASK = 0x80;
    public static final int USB_DIR_OUT = 0; /* to device */
    public static final int USB_DIR_IN = 0x80; /* to host */

    /*
      * Descriptor types
      */
    public static final int USB_DT_DEVICE = 0x01;
    public static final int USB_DT_CONFIG = 0x02;
    public static final int USB_DT_STRING = 0x03;
    public static final int USB_DT_INTERFACE = 0x04;
    public static final int USB_DT_ENDPOINT = 0x05;

    public static final int USB_DT_HID = (USB_TYPE_CLASS | 0x01);
    public static final int USB_DT_REPORT = (USB_TYPE_CLASS | 0x02);
    public static final int USB_DT_PHYSICAL = (USB_TYPE_CLASS | 0x03);

    /*
      * Descriptor sizes per descriptor type
      */
    public static final int USB_DT_DEVICE_SIZE = 18;
    public static final int USB_DT_CONFIG_SIZE = 9;
    public static final int USB_DT_INTERFACE_SIZE = 9;
    public static final int USB_DT_ENDPOINT_SIZE = 7;
    public static final int USB_DT_ENDPOINT_AUDIO_SIZE = 9; /* Audio extension */
    public static final int USB_DT_HID_SIZE = 9;

    /*
      * Endpoints
      */
    public static final int USB_ENDPOINT_NUMBER_MASK = 0x0f; /* in bEndpointAddress */
    public static final int USB_ENDPOINT_DIR_MASK = 0x80;
    public static final int USB_ENDPOINT_MAX = 16;

    public static final int USB_ENDPOINT_XFERTYPE_MASK = 0x03; /* in bmAttributes */
    public static final int USB_ENDPOINT_XFER_CONTROL = 0;
    public static final int USB_ENDPOINT_XFER_ISOC = 1;
    public static final int USB_ENDPOINT_XFER_BULK = 2;
    public static final int USB_ENDPOINT_XFER_INT = 3;
    public static final String USB_ENDPOINT_XFER_NAMES[] = {"control", "isochronous", "bulk", "interrupt"};

    public static final int USB_ENDPOINT_MAXPS_MASK = 0x7ff; /* in wMaxPacketSize */

    /*
      * USB Packet IDs (PIDs)
      */
    public static final int USB_PID_UNDEF_0 = 0xf0;
    public static final int USB_PID_OUT = 0xe1;
    public static final int USB_PID_ACK = 0xd2;
    public static final int USB_PID_DATA0 = 0xc3;
    public static final int USB_PID_PING = 0xb4; /* USB 2.0 */
    public static final int USB_PID_SOF = 0xa5;
    public static final int USB_PID_NYET = 0x96; /* USB 2.0 */
    public static final int USB_PID_DATA2 = 0x87; /* USB 2.0 */
    public static final int USB_PID_SPLIT = 0x78; /* USB 2.0 */
    public static final int USB_PID_IN = 0x69;
    public static final int USB_PID_NAK = 0x5a;
    public static final int USB_PID_DATA1 = 0x4b;
    public static final int USB_PID_PREAMBLE = 0x3c; /* Token mode */
    public static final int USB_PID_ERR = 0x3c; /* USB 2.0: handshake mode */
    public static final int USB_PID_SETUP = 0x2d;
    public static final int USB_PID_STALL = 0x1e;
    public static final int USB_PID_MDATA = 0x0f; /* USB 2.0 */

    /*
      * Standard requests
      */
    public static final int USB_REQ_GET_STATUS = 0x00;
    public static final int USB_REQ_CLEAR_FEATURE = 0x01;
    public static final int USB_REQ_SET_FEATURE = 0x03;
    public static final int USB_REQ_SET_ADDRESS = 0x05;
    public static final int USB_REQ_GET_DESCRIPTOR = 0x06;
    public static final int USB_REQ_SET_DESCRIPTOR = 0x07;
    public static final int USB_REQ_GET_CONFIGURATION = 0x08;
    public static final int USB_REQ_SET_CONFIGURATION = 0x09;
    public static final int USB_REQ_GET_INTERFACE = 0x0A;
    public static final int USB_REQ_SET_INTERFACE = 0x0B;
    public static final int USB_REQ_SYNCH_FRAME = 0x0C;

    /*
      * HID requests
      */
    public static final int USB_REQ_GET_REPORT = 0x01;
    public static final int USB_REQ_GET_IDLE = 0x02;
    public static final int USB_REQ_GET_PROTOCOL = 0x03;
    public static final int USB_REQ_SET_REPORT = 0x09;
    public static final int USB_REQ_SET_IDLE = 0x0A;
    public static final int USB_REQ_SET_PROTOCOL = 0x0B;

    /*
      * Speeds
      */
    public static final int USB_SPEED_LOW = 1; /* usb 1.1 */
    public static final int USB_SPEED_FULL = 2; /* usb 1.1 */
    public static final int USB_SPEED_HIGH = 3; /* usb 2.0 */

    /*
      * Timeouts and retries
      */
    public static final long GET_TIMEOUT = 5000;
    public static final long SET_TIMEOUT = 5000;

    public static final int GET_DESCRIPTOR_ATTEMPTS = 3;
    public static final int GET_STATUS_ATTEMPTS = 1;
    public static final int SET_ADDRESS_ATTEMPTS = 3;
    public static final int SET_CONFIGURATION_ATTEMPTS = 3;
    public static final int SET_FEATURE_ATTEMPTS = 1;
    public static final int SYNC_FRAME_ATTEMPTS = 1;

    /*
      * USBRequest status codes.
      */
    public static final int USBREQ_ST_STALLED = 0x01;
    public static final int USBREQ_ST_NAK = 0x02;
    public static final int USBREQ_ST_DATABUFFER = 0x04;
    public static final int USBREQ_ST_TIMEOUT = 0x08;
    public static final int USBREQ_ST_BITSTUFF = 0x10;
    public static final int USBREQ_ST_BABBLE = 0x20;
    public static final int USBREQ_ST_ERROR_MASK = USBREQ_ST_STALLED | USBREQ_ST_NAK | USBREQ_ST_DATABUFFER |
        USBREQ_ST_TIMEOUT | USBREQ_ST_BITSTUFF | USBREQ_ST_BABBLE;
    public static final int USBREQ_ST_COMPLETED = 0x8000;

}
