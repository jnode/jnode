package org.jnode.driver.console;

import org.jnode.system.event.SystemEvent;

/**
 * @author Levente S\u00e1ntha
 */
public class ConsoleEvent extends SystemEvent {
    public static final int CONSOLE_CLOSED = 201;
    private Console console;

    /**
     * Create a new system event
     */
    public ConsoleEvent(Console console) {
        super(CONSOLE_CLOSED);
        this.console = console;
    }

    public Console getConsole() {
        return console;
    }
}
