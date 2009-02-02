/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.test.shell.harness;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellUtils;

/**
 * This class performs Emu and CommandShell initialization without exposing 
 * the Emu APIs at the class loader level.
 * 
 * @author crawley@jnode.org
 */
public class TestEmu {

    private static boolean emuInitialized;
    private static boolean emuAvailable;
    private static CommandShell shell;
    private static Class<?> emuClass;
    
    @SuppressWarnings("unused")
    private static Object emuObject;
    
    private static Set<String> loadedPseudoPlugins = new HashSet<String>();

    public static synchronized boolean initEmu(File root) {
        if (!emuInitialized) {
            // This is a bit of a hack.  We don't want class loader dependencies
            // on the Emu code because that won't work when we run on JNode.  But
            // we need to use Emu if we are running tests on the dev't platform.
            // The following infers that we are running on the dev't platform if 
            // the 'Emu' class is not loadable.
            try {
                emuClass = Class.forName("org.jnode.emu.Emu");
                Constructor<?> constructor = emuClass.getConstructor(File.class);
                emuObject = constructor.newInstance(root);
                emuAvailable = true;
            } catch (Throwable ex) {
                // debug ...
                ex.printStackTrace(System.err);
                emuAvailable = false;
            }
            try {
                if (emuAvailable) {
                    shell = null;
                } else {
                    shell = (CommandShell) ShellUtils.getCurrentShell();
                }
            } catch (Exception ex) {
                // debug ...
                ex.printStackTrace(System.err);
                throw new RuntimeException(ex);
            }
            emuInitialized = true;
        }
        return emuAvailable;
    }

    public static synchronized CommandShell getShell() {
        if (!emuInitialized) {
            throw new IllegalStateException("Emu not initialized");
        }
        return shell;
    }

    public static synchronized void loadPseudoPlugin(String pluginId, String className) {
        if (!emuInitialized) {
            throw new IllegalStateException("Emu not initialized");
        }
        if (!loadedPseudoPlugins.contains(className)) {
            try {
                Class<?> clazz = Class.forName(className);
                clazz.newInstance();
                Method method = emuClass.getMethod("configurePluginCommands", String.class);
                method.invoke(emuObject, pluginId);
            } catch (Exception ex) {
                throw new RuntimeException("Cannot configure plugin '" + pluginId + "'", ex);
            }
            loadedPseudoPlugins.add(className);
        }
    }

}
