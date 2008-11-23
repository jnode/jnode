/*
 * Copyright 2002-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.media.sound;

import java.util.Vector;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.spi.MixerProvider;


/**
 * Port provider.
 *
 * @author Florian Bomers
 */
public class PortMixerProvider extends MixerProvider {

    // STATIC VARIABLES

    /**
     * Set of info objects for all port input devices on the system.
     */
    private static PortMixerInfo[] infos;

    /**
     * Set of all port input devices on the system.
     */
    private static PortMixer[] devices;


    // STATIC

    static {
        // initialize
        Platform.initialize();
    }


    // CONSTRUCTOR


    /**
     * Required public no-arg constructor.
     */
    public PortMixerProvider() {
        //if (Printer.trace) Printer.trace("PortMixerProvider: constructor");
        if (Platform.isPortsEnabled()) {
            init();
        } else {
            infos = new PortMixerInfo[0];
            devices = new PortMixer[0];
        }
    }

    private static synchronized void init() {
        // get the number of input devices
        int numDevices = nGetNumDevices();

        if (infos == null || infos.length != numDevices) {
            if (Printer.trace) Printer.trace("PortMixerProvider: init()");
            // initialize the arrays
            infos = new PortMixerInfo[numDevices];
            devices = new PortMixer[numDevices];

            // fill in the info objects now.
            // we'll fill in the device objects as they're requested.
            for (int i = 0; i < infos.length; i++) {
                infos[i] = nNewPortMixerInfo(i);
            }
            if (Printer.trace) Printer.trace("PortMixerProvider: init(): found numDevices: " + numDevices);
        }
    }

    public Mixer.Info[] getMixerInfo() {
        Mixer.Info[] localArray = new Mixer.Info[infos.length];
        System.arraycopy(infos, 0, localArray, 0, infos.length);
        return localArray;
    }


    public Mixer getMixer(Mixer.Info info) {
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].equals(info)) {
                return getDevice(infos[i]);
            }
        }
        throw new IllegalArgumentException("Mixer " + info.toString() + " not supported by this provider.");
    }


    private Mixer getDevice(PortMixerInfo info) {
        int index = info.getIndex();
        if (devices[index] == null) {
            devices[index] = new PortMixer(info);
        }
        return devices[index];
    }

    // INNER CLASSES


    /**
     * Info class for PortMixers.  Adds an index value for
     * making native references to a particular device.
     * This constructor is called from native.
     */
    static class PortMixerInfo extends Mixer.Info {
        private int index;

        private PortMixerInfo(int index, String name, String vendor, String description, String version) {
            super("Port " + name, vendor, description, version);
            this.index = index;
        }

        int getIndex() {
            return index;
        }

    } // class PortMixerInfo

    // NATIVE METHODS
    private static native int nGetNumDevices();
    private static native PortMixerInfo nNewPortMixerInfo(int mixerIndex);
}
