package java.lang;

import gnu.classpath.VMStackWalker;

/**
 * @see SecurityManager
 */
public class NativeSecurityManager {
    private static Class[] getClassContext(SecurityManager instance) {
        Class[] stack1 = VMStackWalker.getClassContext();
        Class[] stack2 = new Class[stack1.length - 1];
        System.arraycopy(stack1, 1, stack2, 0, stack1.length - 1);
        return stack2;
    }
}
