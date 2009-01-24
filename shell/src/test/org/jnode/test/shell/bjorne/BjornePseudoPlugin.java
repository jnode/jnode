package org.jnode.test.shell.bjorne;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandInterpreter;
import org.jnode.shell.ShellManager;
import org.jnode.shell.bjorne.BjorneInterpreter;

public class BjornePseudoPlugin {
    private static final CommandInterpreter.Factory FACTORY = new CommandInterpreter.Factory() {
        public CommandInterpreter create() {
            return new BjorneInterpreter();
        }

        public String getName() {
            return "bjorne";
        }
    };

    /**
     * Initialize a new instance
     * 
     * @param descriptor
     */
    public BjornePseudoPlugin() throws NamingException {
        ShellManager mgr = InitialNaming.lookup(ShellManager.NAME);
        mgr.registerInterpreterFactory(FACTORY);
    }

}
