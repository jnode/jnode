/*
 * $Id$
 */
package gnu.java.lang;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VMClassHelper {

    /**
     * Strip the last portion of the name (after the last dot).
     * 
     * @param name
     *            the name to get package of
     * @return the package name, or "" if no package
     */
    public static String getPackagePortion(String name) {
        int lastInd = name.lastIndexOf('.');
        if (lastInd == -1)
            return "";
        return name.substring(0, lastInd);
    }
    
    /**
     * Strip the package portion of the classname (before the last dot).
     * @param name
     * @return
     */
    public static String getClassNamePortion(String name) {
        int lastInd = name.lastIndexOf('.');
        if (lastInd == -1)
            return name;
        return name.substring(lastInd+1);        
    }

}