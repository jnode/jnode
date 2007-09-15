/*
 * $Id$
 */
package org.jnode.vm;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * This interface provides the hooks for implementing special semantics for System.in, System.out
 * and System.err.  Specifically, it is used to implement 'proclet' mode where the stream objects
 * are proxies for context specific streams.
 * 
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public interface IOContext {
	
	/**
     * This hook is used to set the 'global' version of System.in; e.g. the one used
     * when there is no active proclet context.
     * 
     * @param in the new global System.in
     */
    void setGlobalInStream(InputStream in);
    
    /**
     * This hook is used to get the 'global' version of System.in.
     */
    InputStream getGlobalInStream();
    
    /**
     * This hook is used to set the 'global' version of System.out; e.g. the one used
     * when there is no active proclet context.
     * 
     * @param out the new global System.out
     */
    void setGlobalOutStream(PrintStream out);
    
    /**
     * This hook is used to get the 'global' version of System.out.
     */
    PrintStream getGlobalOutStream();
    
    /**
     * This hook is used to set the 'global' version of System.err; e.g. the one used
     * when there is no active proclet context.
     * 
     * @param err the new global System.err
     */
    void setGlobalErrStream(PrintStream err);
    
    /**
     * This hook is used to get the 'global' version of System.err.
     */
    PrintStream getGlobalErrStream();
    
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
    
    void enterContext();
    
    void exitContext();
}
