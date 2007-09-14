/*
 * $Id$
 */
package org.jnode.install;

/**
 * @author Levente S\u00e1ntha
 */
public interface InstallerAction {
    ActionInput getInput(InputContext inContext);
    void execute() throws Exception;
    ActionOutput getOutput(OutputContext outContext);
}
