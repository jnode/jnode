package javax.rmi;

import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: lsantha
 * Date: Nov 24, 2008
 * Time: 2:13:20 PM
 * To change this template use File | Settings | File Templates.
 */
class GetORBPropertiesFileAction implements PrivilegedAction {
    private boolean debug = false ;

    public GetORBPropertiesFileAction () {
    }

    private String getSystemProperty(final String name) {
	// This will not throw a SecurityException because this
	// class was loaded from rt.jar using the bootstrap classloader.
        String propValue = (String) AccessController.doPrivileged(
	    new PrivilegedAction() {
		public Object run() {
	            return System.getProperty(name);
	        }
            }
	);

	return propValue;
    }

    private void getPropertiesFromFile( Properties props, String fileName )
    {
        try {
	    File file = new File( fileName ) ;
	    if (!file.exists())
		return ;

            FileInputStream in = new FileInputStream( file ) ;

	    try {
		props.load( in ) ;
	    } finally {
		in.close() ;
	    }
        } catch (Exception exc) {
            if (debug)
                System.out.println( "ORB properties file " + fileName +
		    " not found: " + exc) ;
        }
    }

    public Object run()
    {
        Properties defaults = new Properties() ;

	String javaHome = getSystemProperty( "java.home" ) ;
	String fileName = javaHome + File.separator + "lib" + File.separator +
	    "orb.properties" ;

	getPropertiesFromFile( defaults, fileName ) ;

	Properties results = new Properties( defaults ) ;

        String userHome = getSystemProperty( "user.home" ) ;
        fileName = userHome + File.separator + "orb.properties" ;

	getPropertiesFromFile( results, fileName ) ;
	return results ;
    }
}
