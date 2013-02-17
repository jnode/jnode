/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

/**
 * This is the implementation of the IOContext API that is be used when
 * 'proclet' mode is not enabled.  It also provides static methods for
 * getting and setting the 'global' versions of the Stream state,
 * the System properties and the System environment.  The 'global' state 
 * is used and (in the case of the properties and 'env' map, updated) in 
 * 'proclet' mode when the current thread is not part of a 'proclet'.
 * 
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class VmIOContext implements IOContext {
    // FIXME ... restrict visibility (if possible) and add Java security
    // access controls.
    
    private static InputStream globalInStream;
    private static PrintStream globalOutStream;
    private static PrintStream globalErrStream;
    private static Properties globalSysProps;
    private static Map<String, String> globalEnv;

    public void setSystemIn(InputStream in) {
        globalInStream = in;
        VmSystem.setStaticField(System.class, "in", in);
    }

    public void setSystemOut(PrintStream out) {
        globalOutStream = out;
        VmSystem.setStaticField(System.class, "out", out);
    }

    public void setSystemErr(PrintStream err) {
        globalErrStream = err;
        VmSystem.setStaticField(System.class, "err", err);
    }

    public void enterContext() {
        // No-op
    }

    public void exitContext() {
        // No-op
    }

    public PrintStream getRealSystemErr() {
        return System.err;
    }

    public InputStream getRealSystemIn() {
        return System.in;
    }

    public PrintStream getRealSystemOut() {
        return System.out;
    }

    public Map<String, String> getEnv() {
        return globalEnv;
    }
    
    public Properties getProperties() {
        return globalSysProps;
    }

    public void setEnv(Map<String, String> env) {
        globalEnv = env;
    }

    public void setProperties(Properties props) {
        globalSysProps = props;
    }
    
    /**
     * Set the 'global' view of {@link System#in}. 
     * @param in the new input stream.
     */
    public static void setGlobalInStream(InputStream in) {
        globalInStream = in;
    }

    /**
     * Set the 'global' view of {@link System#out}. 
     * @param out the new output stream.
     */
    public static void setGlobalOutStream(PrintStream out) {
        globalOutStream = out;
    }

    /**
     * Get the 'global' view of {@link System#out}. 
     * @return the 'global' output stream.
     */
    public static PrintStream getGlobalOutStream() {
        return globalOutStream;
    }

    /**
     * Set the 'global' view of {@link System#err}. 
     * @param err the new error stream.
     */
    public static void setGlobalErrStream(PrintStream err) {
        globalErrStream = err;
    }

    /**
     * Get the 'global' view of {@link System#err}. 
     * @return the 'global' error stream.
     */
    public static PrintStream getGlobalErrStream() {
        return globalErrStream;
    }

    /**
     * Get the 'global' view of {@link System#in}. 
     * @return the 'global' input stream.
     */
    public static InputStream getGlobalInStream() {
        return globalInStream;
    }

    /**
     * Set the 'global' view of the environment returned by {@link System#getenv()}. 
     * @param env the new 'global' environment.
     */
    public static void setGlobalEnv(Map<String, String> env) {
        globalEnv = env;
    }

    /**
     * Set the 'global' view of the Properties returned by {@link System#getProperties()}. 
     * @param props the new 'global' properties.
     */
    public static void setGlobalProperties(Properties props) {
        globalSysProps = props;
    }

    /**
     * Get the 'global' view of the environment returned by {@link System#getenv()}. 
     * @return the current 'global' environment.
     */
    public static Map<String, String> getGlobalEnv() {
        return globalEnv;
    }
    
    /**
     * Get the 'global' view of the Properties returned by {@link System#getProperties()}. 
     * @return the current 'global' Properties.
     */
    public static Properties getGlobalProperties() {
        return globalSysProps;
    }
}
