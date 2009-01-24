package org.jnode.test.shell.harness;

import java.io.File;
import java.lang.reflect.Constructor;
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
                Class<?> cls = Class.forName("org.jnode.emu.Emu");
                Constructor<?> constructor = cls.getConstructor(File.class);
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

    public static synchronized void loadPseudoPlugin(String pseudoPluginClassName) {
        if (!emuInitialized) {
            throw new IllegalStateException("Emu not initialized");
        }
        if (!loadedPseudoPlugins.contains(pseudoPluginClassName)) {
            try {
                Class<?> clazz = Class.forName(pseudoPluginClassName);
                clazz.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Cannot load '" + pseudoPluginClassName + "'", ex);
            }
            loadedPseudoPlugins.add(pseudoPluginClassName);
        }
    }

}
