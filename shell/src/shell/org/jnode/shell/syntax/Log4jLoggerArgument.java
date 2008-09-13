package org.jnode.shell.syntax;

import java.util.Enumeration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine.Token;

/**
 * This Argument class captures log4j logger names, and completes them against 
 * the set of logger names that are already defined.
 * 
 * @author crawley@jnode.org
 */
public class Log4jLoggerArgument extends Argument<Logger> {

    public Log4jLoggerArgument(String label, int flags, String description) {
        super(label, flags, new Logger[0], description);
    }

    @Override
    protected String argumentKind() {
        return "logger";
    }

    /**
     * Any token is an acceptable Logger name.
     */
    @Override
    protected Logger doAccept(Token value) throws CommandSyntaxException {
        return Logger.getLogger(value.token);
    }

    /**
     * Complete against existing logger names.
     */
    @Override
    public void complete(CompletionInfo completion, String partial) {
        Enumeration<?> en = LogManager.getCurrentLoggers();
        while (en.hasMoreElements()) {
            String loggerName = ((Logger) en.nextElement()).getName();
            if (loggerName.startsWith(partial)) {
                completion.addCompletion(loggerName);
            }
        }
    }
}
