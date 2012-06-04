/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package java.lang;

import org.jnode.vm.VmSystem;
import org.jnode.vm.VmIOContext;

import java.util.Map;

/**
 * This class implements the native methods defined in ProcessEnvironment.
 * 
 * @see java.lang.ProcessEnvironment
 */
public class NativeProcessEnvironment {
    // This is the initial environment encoded in a format that matches
    // the expectations of the ProcessEnvironment static initializer.
    private static byte[][] isolateInitialEnv;
    
    /**
     * Set the 'binary' environment that will be returned by {@link #environ()}.
     * This can only be done once, and only 
     * 
     * @param env
     */
    public static void setIsolateInitialEnv(byte[][] env) {
        if (isolateInitialEnv != null) {
            throw new IllegalStateException("isolate initial env already set");
        }
        isolateInitialEnv = env;
    }
    
    /**
     * This method gets called just once (per isolate) by the static initializer 
     * for ProcessEnvironment.  It returns the bootstrap environment to be for 
     * the current isolate, or an empty bootstrap environment if none has been 
     * provided.
     * 
     * @see java.lang.ProcessEnvironment#environ()
     */
    @SuppressWarnings("unused")
    private static byte[][] environ() {
        if (isolateInitialEnv == null) {
            isolateInitialEnv = new byte[0][];
        } 
        return isolateInitialEnv;
    }

    /**
     * Fetch a named environment variable from the 'current' context
     * via the IOContext switch
     * 
     * @param name
     * @return
     */
    @SuppressWarnings("unused")
    private static String getenv(String name) {
        return VmSystem.getIOContext().getEnv().get(name);
    }

    /**
     * Fetch the environment map from the 'current' context 
     * via the IOContext switch
     * 
     * @param name
     * @return
     */
    @SuppressWarnings("unused")
    private static Map<String,String> getenv() {
        return VmSystem.getIOContext().getEnv();
    }

    /**
     * Set the global (to the isolate) environment map.
     * 
     * @param name
     * @return
     */
    @SuppressWarnings("unused")
    private static void setGlobalEnv0(Map<String,String> env) {
        VmIOContext.setGlobalEnv(env);
    }
}
