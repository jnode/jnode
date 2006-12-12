package org.jnode.test.gui;

import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.NameSpace;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.AbstractDeviceManager;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.ShellManager;
import org.jnode.shell.def.DefaultShellManager;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Levente S\u00e1ntha
 */
public class Emu {
    protected static void initEnv() throws NamingException {
        if(true){
            InitialNaming.setNameSpace(new NameSpace() {
                private Map<Class<?>, Object> space = new HashMap<Class<?>, Object>();

                public <T> void bind(Class<T> name, T service) throws NamingException, NameAlreadyBoundException {
                    if (space.get(name) != null) throw new NameAlreadyBoundException();
                    space.put(name, service);
                }

                public void unbind(Class<?> name) {
                    space.remove(name);
                }

                public <T> T lookup(Class<T> name) throws NameNotFoundException {
                    T obj = (T) space.get(name);
                    if (obj == null) throw new NameNotFoundException(name.getName());
                    return obj;
                }

                public Set<Class<?>> nameSet() {
                    return space.keySet();
                }
            });
            InitialNaming.bind(DeviceManager.NAME, DeviceManager.INSTANCE);
            final AliasManager aliasMgr = new DefaultAliasManager(new DummyExtensionPoint());
            final ShellManager shellMgr = new DefaultShellManager();
            InitialNaming.bind(AliasManager.NAME, aliasMgr);
            InitialNaming.bind(ShellManager.NAME, shellMgr);
        }
    }

    private static class DummyExtensionPoint implements ExtensionPoint {
        public String getSimpleIdentifier() {
            return "A";
        }

        public String getUniqueIdentifier() {
            return "aaa";
        }

        public String getName() {
            return "B";
        }

        public Extension[] getExtensions() {
            return new Extension[0];
        }

        public void addListener(ExtensionPointListener listener) {
        }

        public void addPriorityListener(ExtensionPointListener listener) {
        }

        public void removeListener(ExtensionPointListener listener) {
        }

        public PluginDescriptor getDeclaringPluginDescriptor() {
            return null;
        }
    }

    public static class DeviceManager extends AbstractDeviceManager {
        public static final Logger log = Logger.getLogger(DeviceManager.class);

        public static final DeviceManager INSTANCE = new DeviceManager();

        private List<DeviceFinder> finders = new ArrayList<DeviceFinder>();

        private List<DeviceToDriverMapper> mappers = new ArrayList<DeviceToDriverMapper>();

        private DeviceManager() {
        }

        public void removeAll() {
            finders.clear();
            mappers.clear();

            for (Device device : getDevices()) {
                try {
                    unregister(device);
                } catch (DriverException e) {
                    log.error("can't unregister " + device);
                }
            }
        }

        public void add(DeviceFinder finder, DeviceToDriverMapper mapper) {
            boolean doStart = false;

            if (!finders.contains(finder)) {
                finders.add(finder);
                doStart = true;
            }

            if (!mappers.contains(mapper)) {
                mappers.add(mapper);
                doStart = true;
            }

            if (doStart) {
                start();
            }
        }

        /**
         * Start this manager
         */
        final public void start() {
            // Thread thread = new Thread()
            // {
            // public void run()
            // {
            log.debug("Loading extensions ...");
            loadExtensions();
            log.debug("Extensions loaded !");
            // }
            // };
            // thread.start();

            try {
                // must be called before findDeviceDrivers
                log.debug("findDevices ...");
                findDevices();

                log.debug("findDeviceDrivers ...");
                findDeviceDrivers();

                log.debug("StubDeviceManager initialized !");
            } catch (InterruptedException e) {
                log.fatal("can't find devices", e);
            }
        }

        /**
         * Refresh the list of finders, based on the mappers extension-point.
         *
         * @param finders
         */
        protected final void refreshFinders(List<DeviceFinder> finders) {
            log.info("refreshFinders");
            finders.clear();
            finders.addAll(this.finders);
        }

        /**
         * Refresh the list of mappers, based on the mappers extension-point.
         *
         * @param mappers
         */
        protected final void refreshMappers(List<DeviceToDriverMapper> mappers) {
            log.info("refreshMappers");
            mappers.clear();
            mappers.addAll(this.mappers);

            // Now sort them
            Collections.sort(mappers, MapperComparator.INSTANCE);
        }
    }

}
