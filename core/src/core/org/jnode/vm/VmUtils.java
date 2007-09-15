package org.jnode.vm;

import gnu.java.lang.VMClassHelper;

/**
 * Utility class to share some Vm features.
 * For now, it's especially used to know how native methods are implemented in JNode.
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class VmUtils {
	private static final String NATIVE_CLASSNAME_PREFIX = "Native"; 
	public static boolean couldImplementNativeMethods(String className)
	{
		String clsName = VMClassHelper.getClassNamePortion(className);
		return clsName.startsWith(NATIVE_CLASSNAME_PREFIX);
	}
	
	public static String getNativeClassName(String className)
	{
	    final String pkg = VMClassHelper.getPackagePortion(className);
	    final String nativeClassName = pkg + ((pkg.length() > 0) ? "." : "")
	            +  NATIVE_CLASSNAME_PREFIX + VMClassHelper.getClassNamePortion(className);
	    return nativeClassName;
	}
	
	public static boolean allowNatives(String className, String architectureName)
	{
        boolean allowNatives = false;
        allowNatives |= className.equals("org.jnode.vm.Unsafe");
        allowNatives |= className.equals("org.jnode.vm." + architectureName + ".Unsafe"
                + architectureName.toUpperCase());
        
        return allowNatives;
	}
}
