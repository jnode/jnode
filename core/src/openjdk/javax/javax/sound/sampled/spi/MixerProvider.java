/*
 * Copyright 1999-2003 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.sound.sampled.spi;		  	 

import javax.sound.sampled.Mixer;

/**
 * A provider or factory for a particular mixer type.
 * This mechanism allows the implementation to determine
 * how resources are managed in creation / management of
 * a mixer.
 *
 * @author Kara Kytle
 * @since 1.3
 */
public abstract class MixerProvider {


    /**
     * Indicates whether the mixer provider supports the mixer represented by
     * the specified mixer info object.
     * @param info an info object that describes the mixer for which support is queried
     * @return <code>true</code> if the specified mixer is supported, 
     * otherwise <code>false</code>
     */
    public boolean isMixerSupported(Mixer.Info info) {

	Mixer.Info infos[] = getMixerInfo();
		
	for(int i=0; i<infos.length; i++){
	    if( info.equals( infos[i] ) ) {
		return true;
	    }
	}
	return false;
    }


    /**
     * Obtains the set of info objects representing the mixer
     * or mixers provided by this MixerProvider.
     * @return set of mixer info objects
     */
    public abstract Mixer.Info[] getMixerInfo();


    /**
     * Obtains an instance of the mixer represented by the info object.
     * @param info an info object that describes the desired mixer
     * @return mixer instance
     * @throws IllegalArgumentException if the info object specified does not
     * match the info object for a mixer supported by this MixerProvider.
     */
    public abstract Mixer getMixer(Mixer.Info info);
}
