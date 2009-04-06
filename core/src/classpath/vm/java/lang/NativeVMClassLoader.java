package java.lang;

import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmType;

/**
 *
 */
class NativeVMClassLoader {
    private static ClassLoader getSystemClassLoader() {
		return VmSystem.getSystemClassLoader().asClassLoader();
	}
    private static Class getPrimitiveClass(char type) {
        return VmType.getPrimitiveClass(type).asClass();
    }
}
