package java.lang;

import java.nio.ByteBuffer;
import java.security.ProtectionDomain;
import java.io.InputStream;
import java.io.IOException;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.VmJavaClassLoader;
import org.jnode.vm.VmSystem;
import org.jnode.security.JNodePermission;

/**
 *
 */
class NativeClassLoader {

    private static void checkArg0(Object vmClassLoader) {
        if (!((VmClassLoader)vmClassLoader).isSystemClassLoader()) {
            throw new IllegalArgumentException("vmClassLoader must be system classloader");
        }
    }
    private static void checkArgs0(Object vmClassLoader) {
        VmClassLoader vmcl = (VmClassLoader) vmClassLoader;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkCreateClassLoader();
            sm.checkPermission(new JNodePermission("wrapVmClassLoader"));
        }
        if (vmClassLoader == null) {
            throw new IllegalArgumentException("vmClassLoader cannot be null");
        }
        if (vmcl.isSystemClassLoader()) {
            throw new IllegalArgumentException("vmClassLoader must not be system classloader");
        }
    }

    private static Object createVmJavaClassLoader0(ClassLoader instance) {
        return new VmJavaClassLoader(instance);
    }

    private static Class defineClass0(ClassLoader instance, String name, byte[] data, int offset, int length,
                               ProtectionDomain protDomain) {
        return ((VmClassLoader)instance.vmClassLoader).defineClass(name, data, offset, length, protDomain).asClass();
    }
    private static Class defineClass0(ClassLoader instance, String name, ByteBuffer data, ProtectionDomain protDomain) {
        return ((VmClassLoader)instance.vmClassLoader).defineClass(name, data, protDomain).asClass();
    }

    protected static Class findLoadedClass(ClassLoader instance, String name) {
        VmType< ? > vmClass = ((VmClassLoader)instance.vmClassLoader).findLoadedClass(name);
        if (vmClass != null) {
            return vmClass.asClass();
        } else {
            return null;
        }
    }

    private static boolean resourceExists0(ClassLoader instance, String name) {
        return ((VmClassLoader)instance.vmClassLoader).resourceExists(name);
    }

    private static Object getVmClassLoader0(ClassLoader instance) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new JNodePermission("getVmClassLoader"));
        }
        return instance.vmClassLoader;
    }

    private static Class loadClass0(String name, boolean resolve) throws ClassNotFoundException {
        return VmSystem.getSystemClassLoader().loadClass(name, resolve).asClass();
    }

    private static InputStream getSystemResourceAsStream0(String name) throws IOException {
        return VmSystem.getSystemClassLoader().getResourceAsStream(name);
    }

    private static ClassLoader getSystemClassLoader() {
        return VmSystem.getSystemClassLoader().asClassLoader();
    }
}
