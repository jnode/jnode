package org.jnode.emu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.NameSpace;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;
import org.jnode.shell.def.DefaultShellManager;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.def.DefaultHelp;

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
            AliasManager alias_mgr = new DefaultAliasManager(new DummyExtensionPoint()).createAliasManager();
            alias_mgr.add("console", "org.jnode.shell.command.driver.console.ConsoleCommand");
            alias_mgr.add("help", "org.jnode.shell.command.HelpCommand");
            alias_mgr.add("alias", "org.jnode.shell.command.AliasCommand");
            alias_mgr.add("exit", "org.jnode.shell.command.ExitCommand");
            alias_mgr.add("edit", "org.jnode.apps.edit.EditCommand");
            alias_mgr.add("leed", "org.jnode.apps.editor.LeedCommand");
            alias_mgr.add("sconsole", "org.jnode.apps.console.SwingConsole");
            InitialNaming.bind(AliasManager.NAME, alias_mgr);
            InitialNaming.bind(ShellManager.NAME, new DefaultShellManager());
            InitialNaming.bind(Help.NAME, new DefaultHelp());
        }
    }
}
