/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm;

import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.objects.BootableHashMap;
import org.jnode.vm.objects.VmSystemObject;

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
            if (VmUtils.getVm().isBootstrap()) {
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
