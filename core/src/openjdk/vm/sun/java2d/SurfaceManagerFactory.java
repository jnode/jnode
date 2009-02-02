/*
 * $Id$
 *
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
 
package sun.java2d;

import sun.awt.image.SunVolatileImage;
import sun.awt.image.SurfaceManager;
import sun.awt.image.VolatileSurfaceManager;
import sun.awt.image.BufImgVolatileSurfaceManager;

/**
 * This is a factory class with static methods for creating a
 * platform-specific instance of a particular SurfaceManager.  Each platform
 * (Windows, Unix, etc.) has its own specialized SurfaceManagerFactory.
 */
public class SurfaceManagerFactory {
    /**
     * Creates a new instance of a VolatileSurfaceManager given any
     * arbitrary SunVolatileImage.  An optional context Object can be supplied
     * as a way for the caller to pass pipeline-specific context data to
     * the VolatileSurfaceManager (such as a backbuffer handle, for example).
     *
     * For Unix platforms, this method returns either an X11- or a GLX-
     * specific VolatileSurfaceManager based on the GraphicsConfiguration
     * under which the SunVolatileImage was created.
     */
    /*
    public static VolatileSurfaceManager
        createVolatileManager(SunVolatileImage vImg,
                              Object context)
    {
        GraphicsConfiguration gc = vImg.getGraphicsConfig();
        if (gc instanceof GLXGraphicsConfig) {
            return new GLXVolatileSurfaceManager(vImg, context);
        } else {
            return new X11VolatileSurfaceManager(vImg, context);
        }
    }
    */

    public static VolatileSurfaceManager
        createVolatileManager(SunVolatileImage vImg,
                              Object context)
    {

        return new BufImgVolatileSurfaceManager(vImg, context);
        /*
        GraphicsConfiguration gc = vImg.getGraphicsConfig();
        if (gc instanceof GLXGraphicsConfig) {
            return new GLXVolatileSurfaceManager(vImg, context);
        } else {
            return new X11VolatileSurfaceManager(vImg, context);
        }
        */
    }
}
