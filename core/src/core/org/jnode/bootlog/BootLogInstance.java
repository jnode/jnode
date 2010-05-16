package org.jnode.bootlog;


/**
 * Class holding the {@link BootLog} instance used by the system.
 * <br/><h1>Implementation note :</h1> The reference to the actual instance of 
 * the BootLog can't be stored in the InitialNaming that use VmType, which is 
 * not fully initialized at build time (but BootLog is used). So, we are always 
 * holding the reference in that class.  
 * 
 * @author Fabien DUMINY
 *
 */
public final class BootLogInstance {
	private static BootLog BOOT_LOG_INSTANCE;
	
	private BootLogInstance () {		
	}
	
	/**
	 * Get the system's {@link BootLog}.
	 * @return the system's {@link BootLog}.
	 */
	public static BootLog get() {
		return BOOT_LOG_INSTANCE;
	}

	/**
	 * Set the system's {@link BootLog}.
	 * @param bootLog the system's {@link BootLog}.
	 */
	public static void set(BootLog bootLog) {
		BOOT_LOG_INSTANCE = bootLog;
	}
}
