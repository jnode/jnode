
// (C) 1996 Glynn Clements <glynn@sensei.co.uk> - Freely Redistributable

package java.lang;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.jnode.vm.VmSystem;

public final class System {

	public static InputStream in;
	public static PrintStream out;
	public static PrintStream err;

	private static SecurityManager sm;
	private static Properties properties;

	/**
	 * Initialize the default properties of the system
	 */
	private static void initProperties() {
		int index;

		properties = new Properties();

		/* Load basic, VM-specific properties. */
		final Properties VMprops = VmSystem.getInitProperties();
		if (VMprops != null) {
			properties.putAll(VMprops);
		}

		/* Now load any properties from the user's property file. */
		/*try {
			String fname = NativeLang.defaultPropertiesFile();
			if (fname != null) {
				FileInputStream propIn = new FileInputStream(fname);
				properties.load(propIn);
				propIn.close();
			}
		} catch (IOException e) {
			// Not having a properties file is okay.
		}*/
	}

	/**
	 * A private method used only by the VM as a bootstrap point into
	 * the Java libraries.
	 * <p>
	 * This function will be called after the initial thread group and
	 * thread are created, but before they go off and do their own thing.
	 * It can do whatever is necessary to initialize the system.
	 */
	private static void initializeSystemClass() {
		if (properties == null) {/* should always be null for this func. */
			initProperties();
		}
	}

	public static void setProperties(Properties props) {
		if (sm != null) {
			sm.checkPropertiesAccess();
		}
		properties = props;
	}

	public static Properties getProperties() {
		if (sm != null) {
			sm.checkPropertiesAccess();
		}
		if (properties == null) {
			initProperties();
		}
		return properties;
	}

	public static String getProperty(String key) {
		if (sm != null) {
			sm.checkPropertyAccess(key);
		}
		if (properties == null) {
			initProperties();
		}
		return properties.getProperty(key);
	}

	public static String getProperty(String key, String def) {
		if (sm != null) {
			sm.checkPropertyAccess(key);
		}
		if (properties == null) {
			initProperties();
		}
		return properties.getProperty(key, def);
	}

	public static SecurityManager getSecurityManager() {
		return sm;
	}

	public static void setSecurityManager(SecurityManager s) {
		if (sm != null) {
			throw new SecurityException("SecurityManager already installed");
		}
		sm = s;
	}

	public static long currentTimeMillis() {
		return VmSystem.currentTimeMillis();
	}

	public static void exit(int status) {
		Runtime.getRuntime().exit(status);
	}

	public static void gc() {
		Runtime.getRuntime().gc();
	}

	public static void load(String filename) {
		Runtime.getRuntime().load(filename);
	}

	public static void loadLibrary(String libname) {
		Runtime.getRuntime().loadLibrary(libname);
	}

	public static void arraycopy(Object src, int srcOffset, Object dst, int dstOffset, int length) {
		VmSystem.arrayCopy(src, srcOffset, dst, dstOffset, length);
	}

	/**
	* Get a hash code computed by the VM for the Object. This hash code will
	* be the same as Object's hashCode() method.  It is usually some
	* convolution of the pointer to the Object internal to the VM.  It
	* follows standard hash code rules, in that it will remain the same for a
	* given Object for the lifetime of that Object.
	*
	* @param o the Object to get the hash code for
	* @return the VM-dependent hash code for this Object
	* @since 1.1
	*/
	public static int identityHashCode(Object o) {
		return VmSystem.getHashCode(o);
	}

	/**
	 * Sets the err.
	 * @param err The err to set
	 */
	public static void setErr(PrintStream err) {
		System.err = err;
	}

	/**
	 * Sets the in.
	 * @param in The in to set
	 */
	public static void setIn(InputStream in) {
		System.in = in;
	}

	/**
	 * Sets the out.
	 * @param out The out to set
	 */
	public static void setOut(PrintStream out) {
		System.out = out;
	}
}