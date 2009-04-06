package gnu.classpath;

import org.jnode.vm.VmSystem;

/**
 *
 */
class NativeVMStackWalker {
    private static Class[] getClassContext() {
		return VmSystem.getClassContext();
	}
}
