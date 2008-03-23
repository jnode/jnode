package org.jnode.test.shell;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.log4j.BasicConfigurator;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.NameSpace;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.def.DefaultShellManager;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.def.DefaultHelp;

/**
 * A Cassowary is another large Australian bird ... 
 * 
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class Cassowary {
    protected static void initEnv() throws NamingException {
        InitialNaming.setNameSpace(new NameSpace() {
            private Map<Class<?>, Object> space = new HashMap<Class<?>, Object>();

            public <T> void bind(Class<T> name, T service) 
            throws NamingException, NameAlreadyBoundException {
                if (space.get(name) != null) {
                    throw new NameAlreadyBoundException();
                }
                space.put(name, service);
            }

            public void unbind(Class<?> name) {
                space.remove(name);
            }

            public <T> T lookup(Class<T> name) throws NameNotFoundException {
                T obj = (T) space.get(name);
                if (obj == null) {
                    throw new NameNotFoundException(name.getName());
                }
                return obj;
            }

            public Set<Class<?>> nameSet() {
                return space.keySet();
            }
        });
        InitialNaming.bind(DeviceManager.NAME, DeviceManager.INSTANCE);
        AliasManager alias_mgr = 
            new DefaultAliasManager(new DummyExtensionPoint()).createAliasManager();
        InitialNaming.bind(AliasManager.NAME, alias_mgr);
        InitialNaming.bind(ShellManager.NAME, new DefaultShellManager());
        InitialNaming.bind(Help.NAME, new DefaultHelp());
        
        BasicConfigurator.configure();
    }
}
