/*
 * $Id$
 */

package org.jnode.driver.input;
import java.util.Locale;
import java.util.ResourceBundle;



/**
 * KeyboardInterpreterFactory.java
 *
 * @author Created by Marc DENTY
 * @since 0.15
 */
public class KeyboardInterpreterFactory {
	
	/**
	 * Method loadDefaultKeyboardInterpreter
	 *
	 * @return   a  valid KeyboardInterpreter
	 * @version  2/8/2004
	 */
	public static KeyboardInterpreter getDefaultKeyboardInterpreter() {
		try {
			ResourceBundle rb = null;
			String defaultCountry = null;
			String defaultRegion = null;
			
			try {
				rb = ResourceBundle.getBundle("org.jnode.driver.input.KeyboardLayout", Locale.getDefault(), Thread.currentThread().getContextClassLoader());
				defaultCountry = rb.getString("defaultCountry");
			} catch(Exception e) {
				System.err.println("Cannot load default keyboard layout");
				return getKeyboardInterpreter("US", null);
			}
			try {
				defaultRegion = rb.getString("defaultRegion");
			} catch(Exception e) {
			}
			
			return getKeyboardInterpreter(defaultCountry, defaultRegion);
		} catch (Exception e) {
			try {
				return getKeyboardInterpreter("US", null);
			} catch(Exception err) {
				return new KeyboardInterpreter();
			} catch(Error err) {
				return new KeyboardInterpreter();
			}
		}
	}
	
	/**
	 * Method getKeyboardInterpreter this method
	 *
	 * @param    country      a  String
	 * @param    region       a  String
	 *
	 * @return   a  KeyboardInterpreter
	 * @version  2/8/2004
	 */
	public static KeyboardInterpreter getKeyboardInterpreter(String country, String region) throws InstantiationException, IllegalAccessException {
		country = country.toUpperCase();
		if(region != null) {
			region = region.toLowerCase();
		}
		
		System.out.print("Searching for "+country+ (region==null ? "":"_"+region) +"...");
		KeyboardInterpreter interpreter = null;
		String classI10N = "org.jnode.driver.input.l10n.KeyboardInterpreter_";
		
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if(region == null) {
				interpreter = (KeyboardInterpreter)cl.loadClass(classI10N+country).newInstance();
			} else {
				interpreter = (KeyboardInterpreter)cl.loadClass(classI10N+country+"_"+region).newInstance();
			}
		} catch (ClassNotFoundException e) {
			System.err.println(" Failed, not found");
			e.printStackTrace(System.err);
		}
		
		return interpreter;
	}
}


