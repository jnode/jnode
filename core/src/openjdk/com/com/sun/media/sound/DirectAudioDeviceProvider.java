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
 * DirectAudioDevice provider.
 *
 * @author Florian Bomers
 */
public class DirectAudioDeviceProvider extends MixerProvider {

    // STATIC VARIABLES

    /**
     * Set of info objects for all port input devices on the system.
     */
    private static DirectAudioDeviceInfo[] infos;

    /**
     * Set of all port input devices on the system.
     */
    private static DirectAudioDevice[] devices;


    // STATIC

    static {
        // initialize
        Platform.initialize();
    }


    // CONSTRUCTOR


    /**
     * Required public no-arg constructor.
     */
    public DirectAudioDeviceProvider() {
        //if (Printer.trace) Printer.trace("DirectAudioDeviceProvider: constructor");
        if (Platform.isDirectAudioEnabled()) {
            init();
        } else {
            infos = new DirectAudioDeviceInfo[0];
            devices = new DirectAudioDevice[0];
        }
    }

    private synchronized static void init() {
        // get the number of input devices
        int numDevices = nGetNumDevices();

        if (infos == null || infos.length != numDevices) {
            if (Printer.trace) Printer.trace("DirectAudioDeviceProvider: init()");
            // initialize the arrays
            infos = new DirectAudioDeviceInfo[numDevices];
            devices = new DirectAudioDevice[numDevices];

            // fill in the info objects now.
            for (int i = 0; i < infos.length; i++) {
                infos[i] = nNewDirectAudioDeviceInfo(i);
            }
            if (Printer.trace) Printer.trace("DirectAudioDeviceProvider: init(): found numDevices: " + numDevices);
        }
    }

    public Mixer.Info[] getMixerInfo() {
        Mixer.Info[] localArray = new Mixer.Info[infos.length];
        System.arraycopy(infos, 0, localArray, 0, infos.length);
        return localArray;
    }


    public Mixer getMixer(Mixer.Info info) {
        // if the default device is asked, we provide the mixer
        // with SourceDataLine's
        if (info == null) {
            for (int i = 0; i < infos.length; i++) {
                Mixer mixer = getDevice(infos[i]);
                if (mixer.getSourceLineInfo().length > 0) {
                    return mixer;
                }
            }
        }
        // otherwise get the first mixer that matches
        // the requested info object
        for (int i = 0; i < infos.length; i++) {
            if (infos[i].equals(info)) {
                return getDevice(infos[i]);
            }
        }

        throw new IllegalArgumentException("Mixer " + info.toString() + " not supported by this provider.");
    }


    private Mixer getDevice(DirectAudioDeviceInfo info) {
        int index = info.getIndex();
        if (devices[index] == null) {
            devices[index] = new DirectAudioDevice(info);
        }
        return devices[index];
    }

    // INNER CLASSES


    /**
     * Info class for DirectAudioDevices.  Adds an index value and a string for
     * making native references to a particular device.
     * This constructor is called from native.
     */
    static class DirectAudioDeviceInfo extends Mixer.Info {
        private int index;
        private int maxSimulLines;

        // For ALSA, the deviceID contains the encoded card index, device index, and sub-device-index
        private int deviceID;

        private DirectAudioDeviceInfo(int index, int deviceID, int maxSimulLines,
                                      String name, String vendor,
                                      String description, String version) {
            super(name, vendor, "Direct Audio Device: "+description, version);
            this.index = index;
            this.maxSimulLines = maxSimulLines;
            this.deviceID = deviceID;
        }

        int getIndex() {
            return index;
        }

        int getMaxSimulLines() {
            return maxSimulLines;
        }

        int getDeviceID() {
            return deviceID;
        }
    } // class DirectAudioDeviceInfo

    // NATIVE METHODS
    private static native int nGetNumDevices();
    // index: [0..nGetNumDevices()-1]
    private static native DirectAudioDeviceInfo nNewDirectAudioDeviceInfo(int deviceIndex);
}
