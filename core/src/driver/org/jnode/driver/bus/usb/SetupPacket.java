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
 * Java wrapper of an USB Setup packet.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SetupPacket extends USBPacket implements USBConstants {

    /**
     * Create a new instance
     *
     * @param requestType
     * @param request
     * @param value
     * @param index
     * @param length
     */
    public SetupPacket(int requestType, int request, int value, int index, int length) {
        super(8);
        setByte(0, requestType);
        setByte(1, request);
        setShort(2, value);
        setShort(4, index);
        setShort(6, length);
    }

    /**
     * Gets the request type.
     */
    public final int getRequestType() {
        return getByte(0);
    }

    /**
     * Gets the request.
     */
    public final int getRequest() {
        return getByte(1);
    }

    /**
     * Gets the value.
     */
    public final int getValue() {
        return getShort(2);
    }

    /**
     * Gets the index.
     */
    public final int getIndex() {
        return getShort(4);
    }

    /**
     * Gets the length.
     */
    public final int getLength() {
        return getShort(6);
    }

    /**
     * Is this a Host to Device transfer.
     */
    public boolean isDirOut() {
        return ((getRequestType() & USB_DIR_MASK) == USB_DIR_OUT);
    }

    /**
     * Is this a Device to Host transfer.
     */
    public boolean isDirIn() {
        return ((getRequestType() & USB_DIR_MASK) == USB_DIR_IN);
    }

    /**
     * Create a device SET_ADDRESS setup packet
     */
    public static SetupPacket createDeviceSetAddressPacket(int deviceAddress) {
        return new SetupPacket(USB_DIR_OUT | USB_RECIP_DEVICE, USB_REQ_SET_ADDRESS, deviceAddress, 0, 0);
    }

    /**
     * Create a device GET_CONFIGURATION setup packet
     */
    public static SetupPacket createDeviceGetConfigurationPacket() {
        return new SetupPacket(USB_DIR_IN | USB_RECIP_DEVICE, USB_REQ_GET_CONFIGURATION, 0, 0, 1);
    }

    /**
     * Create a device SET_CONFIGURATION setup packet
     */
    public static SetupPacket createDeviceSetConfigurationPacket(int configValue) {
        return new SetupPacket(USB_DIR_OUT | USB_RECIP_DEVICE, USB_REQ_SET_CONFIGURATION, configValue, 0, 0);
    }

    /**
     * Create an interface GET_INTERFACE setup packet
     */
    public static SetupPacket createInterfaceGetInterfacePacket(int intface) {
        return new SetupPacket(USB_DIR_IN | USB_RECIP_INTERFACE, USB_REQ_GET_INTERFACE, 0, intface, 1);
    }

    /**
     * Create an interface SET_INTERFACE setup packet
     */
    public static SetupPacket createInterfaceSetInterfacePacket(int intface, int configValue) {
        return new SetupPacket(USB_DIR_OUT | USB_RECIP_INTERFACE, USB_REQ_SET_INTERFACE, configValue, intface, 0);
    }

    /**
     * Create an endpoint SYNC_FRAME setup packet
     */
    public static SetupPacket createEndPointSyncFramePacket(int endPoint) {
        return new SetupPacket(USB_DIR_IN | USB_RECIP_ENDPOINT, USB_REQ_SYNCH_FRAME, 0, endPoint, 2);
    }

    /**
     * Create an interface SET_INTERFACE setup packet
     */
    public static SetupPacket createEndPointSetInterfacePacket(int configValue) {
        return new SetupPacket(USB_DIR_OUT | USB_RECIP_INTERFACE, USB_REQ_SET_INTERFACE, configValue, 0, 0);
    }

    /**
     * Create a generic GET_DESCRIPTOR setup packet
     */
    public static SetupPacket createGetDescriptorPacket(int reqType, int descrType, int index, int langId,
                                                        int descrLength) {
        return new SetupPacket(USB_DIR_IN | reqType, USB_REQ_GET_DESCRIPTOR, (descrType << 8) | index, langId,
            descrLength);
    }

    /**
     * Create an generic GET_STATUS setup packet
     */
    public static SetupPacket createGetStatusPacket(int reqType, int index) {
        return new SetupPacket(USB_DIR_IN | reqType, USB_REQ_GET_STATUS, 0, index, 2);
    }

    /**
     * Create an generic CLEAR_FEATURE setup packet
     */
    public static SetupPacket createClearFeaturePacket(int reqType, int index, int featureSelector) {
        return new SetupPacket(USB_DIR_OUT | reqType, USB_REQ_CLEAR_FEATURE, featureSelector, index, 0);
    }

    /**
     * Create an generic SET_FEATURE setup packet
     */
    public static SetupPacket createSetFeaturePacket(int reqType, int index, int featureSelector) {
        return new SetupPacket(USB_DIR_OUT | reqType, USB_REQ_SET_FEATURE, featureSelector, index, 0);
    }
}
