/*
 * Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
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
package sun.net;

import java.util.EventObject;
import java.net.URL;

/**
 * ProgressEvent represents an progress event in monitering network input stream.
 *
 * @author Stanley Man-Kit Ho 
 */
public class ProgressEvent extends EventObject	{
    // URL of the stream
    private URL url;
    // content type of the stream
    private String contentType;
    // method associated with URL
    private String method;
    // bytes read 
    private int progress;
    // bytes expected 
    private int expected;
    // the last thing to happen
    private ProgressSource.State state;
 
    /**
     * Construct a ProgressEvent object.
     */
    public ProgressEvent(ProgressSource source, URL url, String method, String contentType, ProgressSource.State state, int progress, int expected) {
	super(source);
	this.url = url;
	this.method = method;
	this.contentType = contentType;
	this.progress = progress;
	this.expected = expected;
	this.state = state;
    }

    /**
     * Return URL related to the progress.
     */
    public URL getURL()	
    {
	return url;
    }

    /**
     * Return method associated with URL.
     */
    public String getMethod()	
    {
	return method;
    }

    /** 
     * Return content type of the URL.
     */
    public String getContentType()  
    {
	return contentType;
    }    

    /**
     * Return current progress value.
     */
    public int getProgress()	
    {
	return progress;
    }
    
    /** 
     * Return expected maximum progress value; -1 if expected is unknown.
     */
    public int getExpected() {
	return expected;
    }
    
    /**
     * Return state.
     */
    public ProgressSource.State getState() {
	return state;
    }
    
    public String toString()	{
	return getClass().getName() + "[url=" + url + ", method=" + method + ", state=" + state 
	     + ", content-type=" + contentType + ", progress=" + progress + ", expected=" + expected + "]";
    }
}
