/*
 * $Id$
 */
package org.jnode.debug;

import java.io.OutputStreamWriter;

import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class UDPAppender extends WriterAppender {

	public static final String LAYOUT = "%-5p [%c{1}]: %m%n";
    
    /**
	 * Create an appender for a given outputstream
	 */
	public UDPAppender(UDPOutputStream out, Layout layout) {
	    if (layout != null) {
	        setLayout(layout);
	    } else {
	        setLayout(new PatternLayout(LAYOUT));
	    }
		setWriter(new OutputStreamWriter(out));
	}

}
