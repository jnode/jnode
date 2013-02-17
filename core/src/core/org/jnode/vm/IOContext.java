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
 * This interface provides the hooks for implementing special semantics for 
 * System.in, System.out and System.err.  Specifically, it is used to implement 
 * 'proclet' mode where the stream objects are proxies for context specific 
 * streams.  It also supports 'proclet' mode contextualization of the System 
 * properties ({@link System#getProperties()}) and environment ({@link System#getenv()}).
 *
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public interface IOContext {
    
    // FIXME ... the name of this interface is misleading.

    /**
     * This hook is used when "setting" System.in; i.e. via System.setIn(in).
     *
     * @param in the application supplied value for System.in
     */
    void setSystemIn(InputStream in);

    /**
     * This hook is used when "setting" System.out; i.e. via System.setOut(out).
     *
     * @param out the application supplied value for System.out
     */
    void setSystemOut(PrintStream out);

    /**
     * This hook is used when "setting" System.err; i.e. via System.setErr(err).
     *
     * @param err the application supplied value for System.err
     */
    void setSystemErr(PrintStream err);

    /**
     * This hook is used to get the 'real' stream underlying System.in in the current context.
     */
    InputStream getRealSystemIn();

    /**
     * This hook is used to get the 'real' stream underlying System.out in the current context.
     */
    PrintStream getRealSystemOut();

    /**
     * This hook is used to get the 'real' stream underlying System.err in the current context.
     */
    PrintStream getRealSystemErr();
    
    /**
     * The hook is used to get the current context's 'system properties'
     */
    Properties getProperties();
    
    /**
     * The hook is used to set the current context's 'system properties'
     */
    void setProperties(Properties props);
    
    /**
     * The hook is used to get the current context's environment; e.g. containing exported
     * shell variables.
     */
    Map<String, String> getEnv();
    
    /**
     * The hook is used to get the current context's environment.
     */
    void setEnv(Map<String, String> env);

    void enterContext();

    void exitContext();
    
    
}
