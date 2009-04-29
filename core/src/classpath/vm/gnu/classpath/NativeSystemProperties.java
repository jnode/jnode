package gnu.classpath;

import java.util.Properties;

import org.jnode.vm.VmSystem;

/**
 * @see gnu.classpath.SystemProperties
 */
class NativeSystemProperties {
    
    /**
     * @see gnu.classpath.SystemProperties#doGetProperties()
     */
    private static Properties doGetProperties() {
        return VmSystem.getIOContext().getProperties();
    }
    
    /**
     * @see gnu.classpath.SystemProperties#doSetProperties(java.util.Properties)
     */
    private static void doSetProperties(Properties props) {
        VmSystem.getIOContext().setProperties(props);
    }
}
