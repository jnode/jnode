package org.jnode.apps.jpartition.swingview;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;

import charva.awt.Component;
import charvax.swing.JOptionPane;

public class SwingErrorReporter implements ErrorReporter  
{	
	public void reportError(Logger log, Object source, Throwable t) 
	{
		reportError(log, source, (Object) t);
	}
	
	public void reportError(Logger log, Object source, String message) 
	{
		reportError(log, source, (Object) message);
	}
	
	private void reportError(Logger log, Object source, Object message) 
	{
		Component parent = (source instanceof Component) ? (Component) source : null;
		Throwable t = (message instanceof Throwable) ? (Throwable) message : null;
		
		String msg = (t == null) ? String.valueOf(message) : t.getMessage();
		JOptionPane.showMessageDialog(parent, 
				"an error happened : "+msg+"\nSee logs for details",
				"error",
				JOptionPane.ERROR_MESSAGE);
		
		if(t != null)
		{
			log.error(msg, t);
		}
		else
		{
			log.error(msg);
		}
	}
}
