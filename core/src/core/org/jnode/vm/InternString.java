package org.jnode.vm;

import org.jnode.util.BootableHashMap;

/**
 *
 */
public class InternString extends VmSystemObject {
    private static BootableHashMap<String, String> bootInternTable;
    private static boolean booted = false;

    public static String internString(String instance) {
        if (booted) {
            return instance.intern();
        } else {
            if (Vm.getVm().isBootstrap()) {
                if (bootInternTable == null) {
                    bootInternTable = new BootableHashMap<String, String>();
                }

                instance = instance.intern();
                //todo the string interned after emiting bootInternTable will be lost 
                if (!bootInternTable.isLocked()) {
                    synchronized (bootInternTable) {
                        final String str = bootInternTable.get(instance);
                        if (str != null) {
                            return str;
                        }
                        bootInternTable.put(instance, instance);
                    }
                }
                return instance;
            } else {
                return instance.intern();
            }
        }
    }

    //todo protect it
    public static void boot() {
        booted = true;
    }

    //todo protect it
    public static BootableHashMap<String, String> getBootInternTable() {
        return bootInternTable;
    }
}
